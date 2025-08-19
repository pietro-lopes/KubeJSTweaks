package dev.uncandango.kubejstweaks.kubejs.event;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.ListJS;
import dev.latvian.mods.kubejs.util.RegExpKJS;
import dev.latvian.mods.rhino.regexp.NativeRegExp;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.uncandango.kubejstweaks.kubejs.debug.DumpErroringRecipes;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class PreRecipeEventJS implements KubeEvent {
    private static final Pattern ARRAY_PATTERN = Pattern.compile("(.+)\\[(\\d+)]");
    private final Map<ResourceLocation, JsonElement> recipeJsons;
    private static final Set<ResourceLocation> IGNORE_WARNING = new HashSet<>();

    public PreRecipeEventJS(Map<ResourceLocation, JsonElement> recipeJsons) {
        this.recipeJsons = recipeJsons;
        DumpErroringRecipes.disable();
        IGNORE_WARNING.clear();
    }

    @HideFromJS
    public static boolean shouldIgnoreWarning(ResourceLocation rl){
        return IGNORE_WARNING.contains(rl);
    }

    static JsonElement fromString(@Nullable String string) {
        if (string == null || string.isEmpty() || string.equals("null")) {
            return JsonNull.INSTANCE;
        }

        try {
            JsonReader jsonReader = new JsonReader(new StringReader(string));
            JsonElement element;
            boolean lenient = jsonReader.isLenient();
            jsonReader.setLenient(true);
            element = Streams.parse(jsonReader);

            if (!element.isJsonNull() && jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonSyntaxException("Did not consume the entire document.");
            }

            return element;
        } catch (Exception ignore) {
        }

        return JsonNull.INSTANCE;
    }

    public void dumpErroringRecipes(){
        DumpErroringRecipes.enable();
    }

    public Stream<RecipeEntry> getEntry(Object obj) {
        if (obj instanceof CharSequence || obj instanceof NativeRegExp || obj instanceof Pattern) {
            String s = obj.toString();
            var r = RegExpKJS.wrap(s);
            if (r != null) {
                return filterRegex(r);
            }
        }

        if (obj instanceof JsonPrimitive jp) {
            var rl = ResourceLocation.tryParse(jp.getAsString());
            if (rl != null) {
                return getEntry(rl);
            }
        }

        if (obj instanceof CharSequence cs) {
            if (cs.charAt(0) == '@') {
                return filterByModId(cs.subSequence(1, cs.length()).toString());
            }
            var rl = ResourceLocation.tryParse(obj.toString());
            if (rl != null) {
                return getEntry(rl);
            }
        }

        var list = ListJS.of(obj);
        if (list != null) {
            List<Stream<RecipeEntry>> streams = new ArrayList<>();
            for (var element : list) {
                streams.add(getEntry(element));
            }
            return streams.stream().flatMap(Function.identity());
        }

        return Stream.empty();
    }

    public void fixCondition(Object obj) {
        getEntry(obj).forEach(RecipeEntry::fixCondition);
    }

    public void fixItemAtKey(Object obj, String key) {
        getEntry(obj).forEach(entry -> entry.fixItemAtKey(key));
    }

    public void disable(Object obj) {
        getEntry(obj).forEach(RecipeEntry::disable);
    }

    public void ignoreWarning(Object obj) {
        getEntry(obj).forEach(RecipeEntry::ignoreWarning);
    }

    private Stream<RecipeEntry> getEntry(ResourceLocation rl) {
        var json = recipeJsons.get(rl);
        if (json == null) {
            return Stream.empty();
        }
        return Stream.of(new RecipeEntry(rl, json.getAsJsonObject()));
    }

    private Stream<RecipeEntry> filter(Predicate<RecipeEntry> predicate) {
        return recipeJsons.entrySet().stream().map(entry -> new RecipeEntry(entry.getKey(), entry.getValue().getAsJsonObject())).filter(predicate);
    }

    private Stream<RecipeEntry> filterRegex(Pattern regex) {
        return filter((recipe) -> regex.asPredicate().test(recipe.id.toString()));
    }

    private Stream<RecipeEntry> filterByModId(String modId) {
        return filter(recipe -> recipe.id().getNamespace().equals(modId));
    }

    public record RecipeEntry(ResourceLocation id, JsonObject json) {
        private static void fixItem(JsonElement keyValue) {
            if (keyValue instanceof JsonObject obj) {
                if (obj.has("item")) {
                    obj.add("id", obj.remove("item"));
                }
            }
            if (keyValue instanceof JsonArray array) {
                for (var element : array) {
                    fixItem(element);
                }
            }
        }

        public Optional<Pair<JsonElement, JsonElement>> fromPath(String path) {
            return fromPath(path, null);
        }

        public void ignoreWarning() {
            IGNORE_WARNING.add(id);
        }

        public void disable() {
            NoOpEventJS.mergeJson(json.getAsJsonObject(), NoOpEventJS.NO_OP_CONDITION_OPS.get());
        }

        public void fixItemAtKey(String key) {
            if (json.has(key)) {
                var keyValue = json.get(key);
                fixItem(keyValue);
            }

        }

        public void replaceValueAtKey(String root, String key, String oldValue, String newValue) {
            if (!json.has(root)) {
                return;
            }
            if (key == null || key.isEmpty()) {
                replaceIfMatches(json, json.get(root), root, oldValue, newValue);
            }
            var rootObj = json.get(root);
            lookForKey(rootObj, key).forEach(pair -> {
                replaceIfMatches(pair.getFirst(), pair.getSecond(), key, oldValue, newValue);
            });
        }

        private static List<Pair<JsonObject, JsonElement>> lookForKey(JsonElement element, String key) {
            List<Pair<JsonObject, JsonElement>> results = new ArrayList<>();
            if (element instanceof JsonArray ja) {
                for (var value : ja) {
                    results.addAll(lookForKey(value, key));
                }
            }
            if (element instanceof JsonObject obj) {
                if (obj.has(key)) {
                    results.add(new Pair<>(obj, obj.get(key)));
                } else {
                    obj.asMap().values().forEach(jsons -> {
                        results.addAll(lookForKey(jsons, key));
                    });
                }
            }
            return results;
        }

        private static void replaceIfMatches(JsonObject parent, JsonElement current, String key, String oldValue, String newValue) {
            if (current instanceof JsonPrimitive jp) {
                if (jp.getAsString().equals(oldValue)) {
                    var newValueJson = fromString(newValue);
                    if (newValueJson.isJsonNull()) {
                        newValueJson = new JsonPrimitive(newValue);
                    }
                    parent.add(key, newValueJson);
                }
            } else {
                if (current.toString().equals(oldValue)) {
                    parent.add(key, fromString(newValue));
                }
            }
        }

        public void fixCondition() {
            if (!json.has("conditions")) {
                return;
            }
            var conditions = json.remove("conditions").getAsJsonArray();
            for (var condition : conditions) {
                if (condition instanceof JsonObject jo) {
                    fixConditionType(jo);
                }
            }
            json.add("neoforge:conditions", conditions);

        }

        private static void fixConditionType(JsonObject condition) {
            if (!condition.has("type")) {
                return;
            }
            var type = condition.get("type").getAsJsonPrimitive();
            if (type.getAsString().startsWith("forge:")) {
                condition.add("type", new JsonPrimitive(type.getAsString().replace("forge:", "neoforge:")));
            }
            if (condition.has("value")) {
                fixConditionType(condition.get("value").getAsJsonObject());
            }
            if (condition.has("values")) {
                for (var value : condition.get("values").getAsJsonArray()) {
                    fixConditionType(value.getAsJsonObject());
                }
            }
        }

        public void renameKey(String oldKey, String newKey, boolean toArray) {
            if (json.has(oldKey)) {
                JsonElement newValue = json.remove(oldKey);
                if (toArray && !newValue.isJsonArray()) {
                    var array = new JsonArray();
                    array.add(newValue);
                    newValue = array;
                }
                json.add(newKey, newValue);
            }
        }

        public void addItemTagCondition(String tag) {
            var notCond = new JsonObject();
            notCond.add("type", new JsonPrimitive("neoforge:not"));
            var tagCondition = new JsonObject();
            tagCondition.add("type", new JsonPrimitive("neoforge:tag_empty"));
            tagCondition.add("tag", new JsonPrimitive(tag));
            notCond.add("value", tagCondition);
            addCondition(notCond);
        }

        public void addConditionsFromKey(String key) {
            var targetObj = json.getAsJsonObject().get(key);
            findConditions(targetObj);
        }

        private void findConditions(JsonElement target) {
            if (target instanceof JsonObject obj) {
                var item = obj.get("item");
                if (item != null && item.isJsonPrimitive()) {
                    addItemCondition(item.getAsString());
                }
                var itemId = obj.get("id");
                if (itemId != null && itemId.isJsonPrimitive()) {
                    addItemCondition(itemId.getAsString());
                }
                var tag = obj.get("tag");
                if (tag != null && tag.isJsonPrimitive()) {
                    addItemTagCondition(tag.getAsString());
                }
                for (var value : obj.asMap().values()) {
                    findConditions(value);
                }
            }
            if (target instanceof JsonArray array) {
                for (var obj : array) {
                    findConditions(obj);
                }
            }
        }

        public void addModConditionFromType() {
            var type = json.get("type");
            if (type == null) {
                return;
            }
            var condition = new JsonObject();
            condition.add("type", new JsonPrimitive("neoforge:mod_loaded"));
            var rl = ResourceLocation.tryParse(type.getAsString());
            if (rl == null) {
                return;
            }
            condition.add("modid", new JsonPrimitive(rl.getNamespace()));
            addCondition(condition);
        }

        public void addItemCondition(String item) {
            var itemCond = new JsonObject();
            itemCond.add("type", new JsonPrimitive("neoforge:item_exists"));
            itemCond.add("item", new JsonPrimitive(item));
            addCondition(itemCond);
        }

        private void addCondition(JsonObject condition) {
            if (json.has("neoforge:conditions")) {
                var conditions = json.get("neoforge:conditions").getAsJsonArray();
                conditions.add(condition);
            } else {
                var conditions = new JsonArray();
                conditions.add(condition);
                json.add("neoforge:conditions", conditions);
            }
        }

        public Optional<Pair<JsonElement, JsonElement>> fromPath(String path, String expectedValue) {
            if (path == null || path.isEmpty()) {
                return Optional.empty();
            }

            String[] tokens = path.split("\\.");
            JsonElement current = json;
            JsonElement parent = null;

            for (String token : tokens) {
                if (current == null || current.isJsonNull()) {
                    return Optional.empty();
                }

                Matcher matcher = ARRAY_PATTERN.matcher(token);
                String key = token;
                Integer arrayIndex = null;

                if (matcher.matches()) {
                    key = matcher.group(1);
                    arrayIndex = Integer.parseInt(matcher.group(2));
                }

                if (!current.isJsonObject()) {
                    return Optional.empty();
                }
                JsonObject obj = current.getAsJsonObject();

                if (!obj.has(key)) {
                    return Optional.empty();
                }
                parent = obj; // track parent
                current = obj.get(key);

                if (arrayIndex != null) {
                    if (!current.isJsonArray()) {
                        return Optional.empty();
                    }
                    JsonArray arr = current.getAsJsonArray();
                    if (arrayIndex < 0 || arrayIndex >= arr.size()) {
                        return Optional.empty();
                    }
                    parent = arr;
                    current = arr.get(arrayIndex);
                }
            }

            // Expected value check
            if (expectedValue != null) {
                if (current.isJsonPrimitive()) {
                    JsonPrimitive prim = current.getAsJsonPrimitive();
                    if (!prim.getAsString().equals(expectedValue)) {
                        return Optional.empty();
                    }
                } else {
                    String normalized = current.toString();
                    if (!normalized.equals(expectedValue)) {
                        return Optional.empty();
                    }
                }
            }

            return Optional.of(Pair.of(parent, current));
        }


    }
}

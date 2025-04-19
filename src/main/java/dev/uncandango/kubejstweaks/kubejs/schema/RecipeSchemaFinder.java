package dev.uncandango.kubejstweaks.kubejs.schema;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.component.EnumComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaStorage;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.rhino.Context;
import dev.uncandango.kubejstweaks.KubeJSTweaks;
import dev.uncandango.kubejstweaks.kubejs.component.CodecComponent;
import dev.uncandango.kubejstweaks.kubejs.plugin.KJSTPluginUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.conditions.ConditionContext;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class RecipeSchemaFinder {
    public static final List<Pattern> OUTPUTS;
    public static final List<Pattern> INPUTS;
    public static Map<String, RecipeComponent<?>> DEFAULT_COMPONENTS;
    private static ConditionalOps<JsonElement> conditionalOps;
    private static ArrayListMultimap<ResourceKey<RecipeSerializer<?>>, RecipeHolder<?>> serializerToRecipeMap = ArrayListMultimap.create();
    public static Map<RecipeSerializer<?>, Map<ResourceLocation, Set<Event>>> serializerEventMap = new HashMap<>();
    public static RecipeSerializer<?> currentSerializer;
    public static ResourceLocation currentRecipe;

    static {
        OUTPUTS = new ArrayList<>();
        OUTPUTS.add(Pattern.compile(".*result.*"));
        OUTPUTS.add(Pattern.compile(".*output.*"));
        INPUTS = new ArrayList<>();
        INPUTS.add(Pattern.compile(".*input.*"));
        INPUTS.add(Pattern.compile(".*ingredient.*"));
    }

    private final List<? extends RecipeHolder<?>> recipeHolders;
    private final Map.Entry<ResourceKey<RecipeSerializer<?>>,RecipeSerializer<?>> recipeSerializer;
    private final Map<String, RecipeComponent<?>> customComponents = new HashMap<>();
    private final Map<String, RecipeKeyInfo> recipeKeyInfos = new HashMap<>();

    private RecipeSchemaFinder(List<? extends RecipeHolder<?>> recipeHolders, Map.Entry<ResourceKey<RecipeSerializer<?>>,RecipeSerializer<?>> recipeSerializer) {
        this.recipeHolders = recipeHolders;
        this.recipeSerializer = recipeSerializer;
    }

    public static List<RecipeSchemaFinder> of(Set<Map.Entry<ResourceKey<RecipeSerializer<?>>, RecipeSerializer<?>>> recipeSerializers) {
        var map = getSerializersMap();
        return recipeSerializers.stream().map(entry -> new RecipeSchemaFinder(map.get(entry.getKey()), entry)).toList();
    }

    private static ArrayListMultimap<ResourceKey<RecipeSerializer<?>>, RecipeHolder<?>> getSerializersMap() {
        if (serializerToRecipeMap.isEmpty()) {
            var server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                var serializerRegistry = server.registryAccess().registryOrThrow(Registries.RECIPE_SERIALIZER);
                server.getRecipeManager().getRecipes().forEach(recipeHolder -> {
                    serializerToRecipeMap.put(serializerRegistry.getResourceKey(recipeHolder.kjs$getRecipe().getSerializer()).get(), recipeHolder);
                });
            }
        }
        return serializerToRecipeMap;
    }

    private static void clearRecipeSerializerMap() {
        serializerToRecipeMap.clear();
    }

    public static void cleanUp() {
        clearRecipeSerializerMap();
        clearConditionalOps();
    }

    private Set<Map.Entry<RecipeHolder<?>, JsonElement>> loadJsons(){
        refreshComponents();
        Set<Map.Entry<RecipeHolder<?>, JsonElement>> set = new HashSet<>();
        Context cx = KubeJS.getStartupScriptManager().contextFactory.enter();
        List<RecipeHolder<?>> toRemove = new ArrayList<>();
        for (RecipeHolder<?> recipeHolder : recipeHolders) {
            JsonElement recipeJson = KJSTPluginUtils.readJsonFromMod(cx, recipeHolder.id().getNamespace(), ResourceLocation.parse(recipeHolder.id().getNamespace() + ":recipe/" + recipeHolder.id().getPath()) + ".json");
            if (recipeJson != null && !recipeJson.isJsonNull()) {
                set.add(Map.entry(recipeHolder, recipeJson));
            } else {
                toRemove.add(recipeHolder);
            }
        }
        recipeHolders.removeAll(toRemove);
        return set;
    }

    public void start() {
        if (recipeHolders.isEmpty()) {
            return;
        }
        Set<Map.Entry<RecipeHolder<?>, JsonElement>> jsons = loadJsons();
        CodecParsedListener.enabled.set(true);
        jsons.forEach(entry -> {
            JsonElement jsonElement = entry.getValue();
            if (jsonElement instanceof JsonObject json) {
//                KubeJSTweaks.LOGGER.info("Parsing recipe: {}", entry.getKey().id());
                currentSerializer = recipeSerializer.getValue();
                currentRecipe = entry.getKey().id();
                json.remove("neoforge:conditions");
                Recipe.CONDITIONAL_CODEC.parse(getConditionalOps(), json).getOrThrow(JsonParseException::new);
                currentSerializer = null;
                currentRecipe = null;
                // var recipe = recipeSerializer.getValue().codec().codec().parse(, json).getOrThrow();
                //                json.remove("type");

//                json.asMap().forEach((key, value) -> recipeKeyInfos.computeIfAbsent(key, RecipeKeyInfo::new).addJson(value));
            } else {
                KubeJSTweaks.LOGGER.warn("Invalid recipe json for {} with json {}", entry.getKey().id(), jsonElement);
            }
        });
        CodecParsedListener.enabled.set(false);

        serializerEventMap.forEach((serializer, recipeMap) -> {

            List<CodecNode> masterNodes = new ArrayList<>();

            for (Map.Entry<ResourceLocation, Set<Event>> entry : recipeMap.entrySet()) {
                KubeJSTweaks.LOGGER.info("Recipe: {}", entry.getKey());

                int prevDepth = -1;
                CodecNode prevNode = null;
                Map<Integer, List<CodecNode>> bufferChildsByDepth = new HashMap<>();

                List<CodecNode> rootNodes = new ArrayList<>();

                for (Event event : entry.getValue()) {
                    var currentNode = new CodecNode(event);
                    var depth = CodecParsedListener.getDepth(event);
                    if (prevNode != null && prevDepth - depth == 1){
                        // prevNode.setParent(currentNode);
                        var childs = bufferChildsByDepth.computeIfAbsent(prevDepth, (dp) -> new ArrayList<>());
                        currentNode.setChilds(childs);
                        bufferChildsByDepth.put(prevDepth, new ArrayList<>());
                    }
                    if (depth != 0) {
                        var childs = bufferChildsByDepth.computeIfAbsent(depth, (dp) -> new ArrayList<>());
                        childs.add(currentNode);
                    } else {
                        rootNodes.add(currentNode);
                    }
                    prevNode = currentNode;
                    prevDepth = depth;

//                    var input = CodecParsedListener.getInput(event);
//                    var decoder = CodecParsedListener.getDecoder(event);
//                    boolean isArray = input != null && input.isJsonArray() || decoder.toString().startsWith("Codec[ListCodec") || decoder.toString().startsWith("ListCodec");
                }

                if (masterNodes.isEmpty()) {
                    masterNodes = rootNodes;
                } else {
                    mergeNodes(rootNodes, masterNodes);
                }
                KubeJSTweaks.LOGGER.info("Root nodes: {}", rootNodes.size());
            }
            KubeJSTweaks.LOGGER.info("Master nodes: {}", masterNodes.size());
        });

        {
            KubeJSTweaks.LOGGER.info("Size of events is: {}", serializerEventMap.size());
        }

        var allComponents = new HashMap<String, RecipeComponent<?>>();
        allComponents.putAll(DEFAULT_COMPONENTS);
        allComponents.putAll(customComponents);
        allComponents.forEach( (key, component) -> {
            recipeKeyInfos.values().forEach(recipeKeyInfo -> recipeKeyInfo.tryParse(Map.entry(key, component)));
        });
        KubeJSTweaks.LOGGER.info("===========================");
        KubeJSTweaks.LOGGER.info("Recipe type {} has {} jsons", recipeSerializer.getKey().location(), jsons.size());
        recipeKeyInfos.values().forEach(RecipeKeyInfo::printSummary);
    }

    private void mergeNodes(List<CodecNode> rootNodes, List<CodecNode> masterNodes) {
        List<CodecNode> flatRootNodes = CodecNode.flattenNodes(rootNodes);
        List<CodecNode> flatMasterNodes = CodecNode.flattenNodes(masterNodes);

        if (flatMasterNodes.size() == flatRootNodes.size()) {
            boolean isEqual = true;
            for (int i = 0; i < flatRootNodes.size(); i++) {
                if (!flatMasterNodes.get(i).hasSameKeyAndDecoder(flatRootNodes.get(i))) {
                    isEqual = false;
                    break;
                }
            }
            if(isEqual) return;
        }

        for (CodecNode rootNode : flatRootNodes) {
            if (rootNode.parent == null) continue;
            for (CodecNode masterNode : flatMasterNodes) {
                if (masterNode.parent == null) continue;
                if (rootNode.isSibling(masterNode)) {
                    var alreadyContains = false;
                    for (CodecNode masterChild : masterNode.parent.childs) {
                        if (masterChild.hasSameKeyAndDecoder(rootNode)) {
                            alreadyContains = true;
                            var inputRoot = CodecParsedListener.getInput(rootNode.parent.value);
                            if (inputRoot != null && inputRoot.isJsonArray()){
                                var inputMaster = CodecParsedListener.getInput(masterChild.parent.value);
                                if (inputMaster != null) {
                                    inputMaster.getAsJsonArray().addAll(inputRoot.getAsJsonArray());
                                    if (CodecParsedListener.getResult(masterChild.parent.value).getOrThrow() instanceof Collection masterList && CodecParsedListener.getResult(rootNode.parent.value).getOrThrow() instanceof Collection rootList){
                                        CodecParsedListener.setResult(masterChild.parent.value, DataResult.success(Streams.concat(masterList.stream(), rootList.stream()).toList()));
                                    }
                                }
                            }
                            break;
                        }
                    }
                    if (alreadyContains) continue;
                    masterNode.parent.childs.add(rootNode);
                    rootNode.setParent(masterNode.parent);
                    break;
                }
            }
        }
    }

    private void refreshComponents() {
        DEFAULT_COMPONENTS = new HashMap<>();
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            var ssm = server.getServerResources().managers().kjs$getServerScriptManager();
            var storage = ssm.recipeSchemaStorage;
            storage.simpleComponents.forEach((key, value) -> {
                if (key.contains(":")) return;
                DEFAULT_COMPONENTS.put(key, value);
            });
            DEFAULT_COMPONENTS.remove("nested_recipe");
            DEFAULT_COMPONENTS.remove("unwrapped_ingredient_list");
            DEFAULT_COMPONENTS.remove("flat_sized_ingredient");
            DEFAULT_COMPONENTS.remove("non_empty_ingredient");
            DEFAULT_COMPONENTS.remove("flat_sized_ingredient");
            // DEFAULT_COMPONENTS.remove("flat_sized_fluid_ingredient");
            DEFAULT_COMPONENTS.remove("strict_item_stack");
            String[] defaultDynamicComponents = new String[]{
                "double",
                "registry_element<minecraft:item>",
                "float",
                "int",
                "long",
                "pattern<ingredient>",
                "either<item_stack,fluid_stack>",
                "either<ingredient,fluid_ingredient>",
                "either<ingredient,codec<com.simibubi.create.foundation.fluid.FluidIngredient#CODEC>>",
                "either<codec<blusunrize.immersiveengineering.api.crafting.IngredientWithSize#CODEC>,item_stack>",
                "codec<net.minecraft.world.level.storage.loot.providers.number.NumberProviders>",
                "codec<net.minecraft.world.level.storage.loot.predicates.LootItemCondition#CODEC>"
            };
            for (String s : defaultDynamicComponents) {
                try {
                    var reader = new StringReader(s);
                    String key = reader.readUnquotedString();
                    var factory = storage.dynamicComponents.get(key);
                    var comp = factory.readComponent(ssm.getRegistries(), storage, reader);
                    DEFAULT_COMPONENTS.put(s, comp);
                } catch (Exception e) {
                    KubeJSTweaks.LOGGER.error("Failed to parse dynamic component {}", s, e);
                }
            }
            Class<?> recipeClass = this.recipeHolders.getFirst().kjs$getRecipe().getClass();
            Class<?> serializerClass = this.recipeHolders.getFirst().kjs$getRecipe().getSerializer().getClass();

            KubeJSTweaks.LOGGER.info("----------------------");
            KubeJSTweaks.LOGGER.info("Found recipe class {} and serializer class {}", recipeClass.getName(), serializerClass.getName());

            findComponentsFromClasses(recipeClass, serializerClass, customComponents, ssm, storage);

            // CodecComponent.FACTORY.readComponent(ssm.getRegistries(), storage, reader)
        }

    }

    private static void findComponentsFromClasses(Class<?> recipeClass, Class<?> serializerClass, Map<String, RecipeComponent<?>> customComponents, ServerScriptManager ssm, RecipeSchemaStorage storage) {
        var currentRecipeClass = recipeClass;
        var recipeFields = new HashSet<Field>();
        var classesToScanForCodecs = new HashSet<Class<?>>();
        while (currentRecipeClass != Object.class) {
            for (var constructor : currentRecipeClass.getConstructors()) {
                classesToScanForCodecs.addAll(Arrays.asList(constructor.getParameterTypes()));
            }
            recipeFields.addAll(Arrays.asList(currentRecipeClass.getDeclaredFields()));
            currentRecipeClass = currentRecipeClass.getSuperclass();
        }
        recipeFields.forEach(field -> {
            var clazz = field.getType();
            if (field.getGenericType() instanceof ParameterizedType genericType) {
                for (var type : genericType.getActualTypeArguments()) {
                    if (type instanceof Class<?> clazz2) {
                        classesToScanForCodecs.add(clazz2);
                    }
                    if (type instanceof ParameterizedType genericType2) {
                        if (genericType2.getRawType() instanceof Class<?> clazz3) {
                            classesToScanForCodecs.add(clazz3);
                        }
                    }
                }
            }
            if (clazz.isEnum()) {
                try {
                    var component = EnumComponent.FACTORY.readComponent(ssm.getRegistries(), storage, new StringReader("<" + clazz.getName() + ">"));
                    customComponents.put("enum<" + clazz.getName() + ">", component);
                } catch (Exception e) {
                    KubeJSTweaks.LOGGER.error("Failed to parse enum component {}", clazz.getName(), e);
                }
            }
            classesToScanForCodecs.add(clazz.isArray() ? clazz.getComponentType() : clazz);
        });

        var currentSerializerClass = serializerClass;
        // var serializerFields = new ArrayList<Field>();
        while (currentSerializerClass != Object.class) {
            for (var constructor : currentSerializerClass.getConstructors()) {
                classesToScanForCodecs.addAll(Arrays.asList(constructor.getParameterTypes()));
            }
            classesToScanForCodecs.add(currentSerializerClass);
            currentSerializerClass = currentSerializerClass.getSuperclass();
        }

        List<Field> codecs = classesToScanForCodecs.stream()
            .map(Class::getDeclaredFields)
            .flatMap(Arrays::stream)
            .filter(field -> field.getType() != Object.class && Modifier.isStatic(field.getModifiers()) && (field.getType().isAssignableFrom(Codec.class) || field.getType().isAssignableFrom(MapCodec.class)))
            .toList();

        codecs.forEach(field -> {
            var module = field.getDeclaringClass().getModule().getName();
            if (module.equals("minecraft") || module.equals("neoforge")) return;
            try {
                customComponents.put("codec<" + field.getDeclaringClass().getName() + "#" + field.getName() + ">", CodecComponent.FACTORY.readComponent(ssm.getRegistries(), storage, new StringReader("<" + field.getDeclaringClass().getName() + "#" + field.getName() + ">")));
                KubeJSTweaks.LOGGER.info("Adding Codec {} for type {} found at class {}", field.getName(), field.getGenericType().getTypeName(), field.getDeclaringClass().getName());
            } catch (Exception e) {
                KubeJSTweaks.LOGGER.error("Failed to parse codec component {}", field.getGenericType().getTypeName(), e);
            }


//            if (field.getGenericType() instanceof ParameterizedType genericType) {
//                for (var type : genericType.getActualTypeArguments()) {
//                    if (type instanceof Class<?> clazz) {
//                        String moduleName = clazz.getModule().getName();
//                        if (moduleName.equals("minecraft") || moduleName.equals("neoforge")) {
//                            return;
//                        } else {
//                            try {
//                                customComponents.put("codec<" + field.getDeclaringClass().getName() + "#" + field.getName() + ">", CodecComponent.FACTORY.readComponent(ssm.getRegistries(), storage, new StringReader("<" + field.getDeclaringClass().getName() + "#" + field.getName() + ">")));
//                                KubeJSTweaks.LOGGER.info("Adding Codec {} for type {} found at class {}", field.getName(), clazz, field.getDeclaringClass().getName());
//                            } catch (Exception e) {
//                                KubeJSTweaks.LOGGER.error("Failed to parse codec component {}", clazz.getName(), e);
//                            }
//                        }
//                    }
//                }
//            }
        });
    }

    private static void clearConditionalOps(){
        conditionalOps = null;
    }

    public static ConditionalOps<JsonElement> getConditionalOps(){
        if (conditionalOps != null) return conditionalOps;
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            var conditionContext = new ConditionContext(server.getServerResources().managers().kjs$getTagManager());
            conditionalOps = new ConditionalOps<>(server.registryAccess().createSerializationContext(JsonOps.INSTANCE), conditionContext);
        }
        return conditionalOps;
    }

    public static class RecipeKeyInfo {
        private final String name;
        private final List<JsonElement> jsons = new ArrayList<>();
        private final Map<String, RecipeComponent<?>> compatibleComponents = new HashMap<>();
        private final List<Map.Entry<String, Object>> parsedValues = new ArrayList<>();

        public RecipeKeyInfo(String name) {
            this.name = name;
        }

        public void addJson(JsonElement json) {
            jsons.add(json);
        }

        public void printSummary() {
            KubeJSTweaks.LOGGER.info("===========================");
            KubeJSTweaks.LOGGER.info("Recipe key \"{}\" has {} jsons", name, jsons.size());
            Map<String,Integer> occurrences = new HashMap<>();
            parsedValues.forEach(entry -> occurrences.merge(entry.getKey(), 1, Integer::sum));
            var partial = occurrences.entrySet().stream().filter(entry -> entry.getKey().contains("(Partial)")).sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).toList();
            var sortedFull = occurrences.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).filter(entry -> !entry.getKey().contains("(Partial)") && !(entry.getKey().startsWith("either<") && entry.getValue() < jsons.size())).toList();
            boolean parsedFully = sortedFull.stream().anyMatch(entry -> entry.getValue() == jsons.size());
            if (!parsedFully) KubeJSTweaks.LOGGER.warn("Recipe key \"{}\" was NOT fully parsed", name);
            if (!sortedFull.isEmpty()) KubeJSTweaks.LOGGER.info("---- Fully parsed components ----");
            sortedFull.stream().filter(elem -> !parsedFully || elem.getValue() == jsons.size()).forEach((entry) -> KubeJSTweaks.LOGGER.info("Component \"{}\" was parsed {} times.", entry.getKey(), entry.getValue()));
            if (!partial.isEmpty()) KubeJSTweaks.LOGGER.info("---- Partially parsed components ----");
            partial.forEach((entry) -> KubeJSTweaks.LOGGER.info("Component \"{}\" was parsed {} times.", entry.getKey(), entry.getValue()));
        }

        public <T> void tryParse(Map.Entry<String, RecipeComponent<T>> entry) {
            jsons.forEach(json -> {
                try {
                    DataResult<T> dr = entry.getValue().codec().parse(RecipeSchemaFinder.getConditionalOps(), json);
                    switch (dr) {
                        case DataResult.Success(var value, var lifecycle) -> {
                            if (!entry.getValue().isEmpty(value)){
                                compatibleComponents.put(entry.getKey(), entry.getValue());
                                parsedValues.add(Map.entry(entry.getKey(), value));
                            }
                        }
                        case DataResult.Error<T> error -> {}
                    }
                } catch (KubeRuntimeException ignore) {
                }
                try {
                    if (json.isJsonArray()) {
                        DataResult<List<T>> drList = entry.getValue().asList().codec().parse(RecipeSchemaFinder.getConditionalOps(), json);
                        switch (drList) {
                            case DataResult.Success(var value, var lifecycle) -> {
                                if (json.getAsJsonArray().isEmpty() || !entry.getValue().asList().isEmpty(value)){
                                    compatibleComponents.put(entry.getKey() + "[]", entry.getValue().asList());
                                    parsedValues.add(Map.entry(entry.getKey() + "[]", value));
                                }
                            }
                            case DataResult.Error<List<T>>(var messageSupplier, var partialValue, var lifecycle) -> {
                                if (partialValue.isPresent()){
                                    var list = partialValue.get();
                                    if (list.isEmpty()) return;
                                    if (list.getFirst() instanceof List<?> innerList) {
                                        if (innerList.isEmpty()) return;
                                    }
                                    if (entry.getKey().equals("pattern<ingredient>")) return;
                                    if (entry.getKey().contains("either<")) return;
                                    compatibleComponents.put(entry.getKey() + "[](Partial)", entry.getValue().asList());
                                    parsedValues.add(Map.entry(entry.getKey() + "[](Partial)", list));
                                }
                            }
                        }

                    }
                } catch (KubeRuntimeException ignore) {
                }
            });
        }
    }
}

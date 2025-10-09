package dev.uncandango.kubejstweaks.kubejs.kjs71.event;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.script.data.GeneratedData;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.uncandango.kubejstweaks.kubejs.kjs71.plugin.KJSTPluginUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.Supplier;

@Info(value = """
    Disables an entry at datapack level
    """)
public class NoOpEventJS implements KubeEvent {

    @HideFromJS
    public static final Supplier<JsonObject> NO_OP_CONDITION_OPS = () -> {
        var json = new JsonObject();
        var conditions = new JsonArray();
        var condition = new JsonObject();
        condition.add("type", new JsonPrimitive("neoforge:false"));
        conditions.add(condition);
        json.add("neoforge:conditions", conditions);
        return json;
    };

    private static final Supplier<JsonObject> NO_OP_NEOFORGE_NONE = () -> {
        var json = new JsonObject();
        json.add("type", new JsonPrimitive("neoforge:none"));
        return json;
    };

    private final KubeDataGenerator generator;

    public NoOpEventJS(KubeDataGenerator generator) {
        this.generator = generator;
    }

    @Info(value = """
    Adds a condition that is always false, which effectively disables it
    """)
    public void recipes(ResourceLocation id) {
        id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "recipe/" + id.getPath());
        JsonObject merged = queryAndMergeJson(id, KJSTPluginUtils.KJSTPackType.DATA, NO_OP_CONDITION_OPS.get());

        setNoOpForId(id, () -> merged);
    }

    private JsonObject queryAndMergeJson(ResourceLocation id, KJSTPluginUtils.KJSTPackType kjstPackType, JsonObject noOpJson) {
        JsonElement originalJson = KJSTPluginUtils.readJsonFromMod(KubeJS.getStartupScriptManager().contextFactory.enter(), id.getNamespace(), id.toString(), kjstPackType);
        if (originalJson instanceof JsonObject obj){
            mergeJson(obj, noOpJson);
            return obj;
        }
        return noOpJson;
    }

    @Info(value = """
    Adds a condition that is always false, which effectively disables it
    """)
    public void lootTables(ResourceLocation id) {
        id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "loot_table/" + id.getPath());
        JsonObject merged = queryAndMergeJson(id, KJSTPluginUtils.KJSTPackType.DATA, NO_OP_CONDITION_OPS.get());

        setNoOpForId(id, () -> merged);
    }

    @Info(value = """
    Adds a condition that is always false, which effectively disables it
    """)
    public void lootTablesBlock(ResourceLocation id) {
        id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "loot_table/blocks/" + id.getPath());
        JsonObject merged = queryAndMergeJson(id, KJSTPluginUtils.KJSTPackType.DATA, NO_OP_CONDITION_OPS.get());

        setNoOpForId(id, () -> merged);
    }

    @Info(value = """
    Adds a no-op type, disabling the biome modifier
    """)
    public void biomeModifiers(ResourceLocation id) {
        id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "neoforge/biome_modifier/" + id.getPath());
        JsonObject merged = queryAndMergeJson(id, KJSTPluginUtils.KJSTPackType.DATA, NO_OP_NEOFORGE_NONE.get());
        setNoOpForId(id, () -> merged);
    }

    @Info(value = """
    This is effectively the same event as generateData with "last" argument.
    Use it to add any kind of json you wish
    """)
    public void json(ResourceLocation id, JsonElement json) {
        generator.add(GeneratedData.json(id, () -> json));
    }

    @HideFromJS
    public static void mergeJson(JsonObject first, JsonObject second) {
        Map<String, JsonElement> map = second.asMap();
        map.forEach(first::add);
    }

    private void setNoOpForId(ResourceLocation id, Supplier<JsonElement> json) {
        generator.add(GeneratedData.json(id, json));
    }
}

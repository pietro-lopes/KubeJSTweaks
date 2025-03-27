package dev.uncandango.kubejstweaks.kubejs.plugin;

import dev.latvian.mods.kubejs.bindings.event.ServerEvents;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistry;
import dev.latvian.mods.kubejs.registry.RegistryType;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.TypeDescriptionRegistry;
import dev.latvian.mods.kubejs.script.data.GeneratedDataStage;
import dev.latvian.mods.rhino.type.JSObjectTypeInfo;
import dev.latvian.mods.rhino.type.JSOptionalParam;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.uncandango.kubejstweaks.kubejs.component.CodecComponent;
import dev.uncandango.kubejstweaks.kubejs.event.KJSTEvents;
import dev.uncandango.kubejstweaks.kubejs.event.NoOpEventJS;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.neoforged.neoforge.common.util.Lazy;

public class KJSTPlugin implements KubeJSPlugin {
    private static final Lazy<TypeInfo> NUMBER_PROVIDER_TYPE =
        Lazy.of(() -> JSObjectTypeInfo.NUMBER
            .or(JSObjectTypeInfo.NUMBER.asArray())
            .or(JSObjectTypeInfo.of(
                new JSOptionalParam("min", JSObjectTypeInfo.NUMBER),
                new JSOptionalParam("max", JSObjectTypeInfo.NUMBER)))
            .or(JSObjectTypeInfo.of(
                new JSOptionalParam("type", RegistryType.ofClass(LootNumberProviderType.class).type()),
                new JSOptionalParam("n", JSObjectTypeInfo.NUMBER),
                new JSOptionalParam("p", JSObjectTypeInfo.NUMBER)))
            .or(JSObjectTypeInfo.of(
                new JSOptionalParam("value", JSObjectTypeInfo.NUMBER))));


    @Override
    public void registerRecipeComponents(RecipeComponentFactoryRegistry registry) {
        registry.register("codec", CodecComponent.FACTORY);
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(KJSTEvents.GROUP);
    }

    @Override
    public void registerTypeDescriptions(TypeDescriptionRegistry registry) {
        registry.register(NumberProvider.class, NUMBER_PROVIDER_TYPE.get());
    }

    @Override
    public void afterScriptsLoaded(ScriptManager manager) {
        if (manager.scriptType == ScriptType.SERVER) {
            ServerEvents.GENERATE_DATA.listenJava(ScriptType.SERVER, GeneratedDataStage.LAST, handler -> {
                if (handler instanceof KubeDataGenerator generator) {
                    if (KJSTEvents.noOp.hasListeners()) {
                        KJSTEvents.noOp.post(new NoOpEventJS(generator));
                    }
                }
                return EventResult.PASS;
            });
        }
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("KJSTweaks", KJSTPluginUtils.class);
    }
}

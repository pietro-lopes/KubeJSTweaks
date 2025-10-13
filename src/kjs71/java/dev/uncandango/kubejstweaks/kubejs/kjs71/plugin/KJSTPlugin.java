package dev.uncandango.kubejstweaks.kubejs.kjs71.plugin;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistry;
import dev.latvian.mods.kubejs.registry.RegistryType;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.TypeDescriptionRegistry;
import dev.latvian.mods.rhino.type.JSObjectTypeInfo;
import dev.latvian.mods.rhino.type.JSOptionalParam;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.uncandango.kubejstweaks.kubejs.event.CompatibilityEventJS;
import dev.uncandango.kubejstweaks.kubejs.kjs71.component.CodecComponent;
import dev.uncandango.kubejstweaks.kubejs.kjs71.event.CommonEvents;
import dev.uncandango.kubejstweaks.kubejs.kjs71.event.KJSTEvents;
import dev.uncandango.kubejstweaks.kubejs.kjs71.event.RegisterCodecEventJS;
import dev.uncandango.kubejstweaks.kubejs.plugin.KJSTPluginUtils;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.common.util.Lazy;

public class KJSTPlugin implements KubeJSPlugin {
    private static final boolean KJS_LOADED = LoadingModList.get().getModFileById("kubejs").versionString().startsWith("2101.7.1-");

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
        if (!KJS_LOADED) return;
        registry.register("codec", CodecComponent.FACTORY);
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        if (!KJS_LOADED) return;
        registry.register(KJSTEvents.GROUP);
    }

    @Override
    public void registerTypeDescriptions(TypeDescriptionRegistry registry) {
        if (!KJS_LOADED) return;
        registry.register(NumberProvider.class, NUMBER_PROVIDER_TYPE.get());
    }

//    @Override
//    public void beforeRecipeLoading(RecipesKubeEvent event, RecipeManagerKJS manager, Map<ResourceLocation, JsonElement> recipeJsons) {
//        var preRecipeEvent = new PreRecipeEventJS(recipeJsons);
//        KJSTEvents.preRecipes.post(preRecipeEvent);
//    }


    @Override
    public void afterInit() {
        if (!KJS_LOADED) return;
        if (KJSTEvents.compatibility.hasListeners()) {
            var event = new CompatibilityEventJS();
            KJSTEvents.compatibility.post(event);
            var messages = event.getMessages();
            if (!messages.isEmpty()) {
                String s = String.join("\n",messages);
                CrashReport crashreport = new CrashReport("\n" + s, new Throwable("\n" + s));
                CrashReportCategory crashreportcategory = crashreport.addCategory("Mod Incompatibility details");
                NativeModuleLister.addCrashSection(crashreportcategory);
                throw new ReportedException(crashreport);
            }
        }
    }

    @Override
    public void afterScriptsLoaded(ScriptManager manager) {
        if (!KJS_LOADED) return;
        if (manager.scriptType == ScriptType.SERVER) {
            CommonEvents.listenKubeEvent();
//            RecipeSchemaFinder.cleanUp();
        }
        if (manager.scriptType == ScriptType.STARTUP) {
            if (KJSTEvents.schema.hasListeners()) {
                KJSTEvents.schema.post(new RegisterCodecEventJS());
            }
        }

    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        if (!KJS_LOADED) return;
        bindings.add("KJSTweaks", KJSTPluginUtils.class);
    }
}

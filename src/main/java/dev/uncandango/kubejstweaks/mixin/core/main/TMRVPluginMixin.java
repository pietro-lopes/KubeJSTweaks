//package dev.uncandango.kubejstweaks.mixin.core.main;
//
//import dev.emi.emi.api.EmiRegistry;
//import dev.emi.emi.jemi.JemiPlugin;
//import dev.latvian.mods.kubejs.BuiltinKubeJSPlugin;
//import dev.latvian.mods.kubejs.integration.emi.KubeJSEMIPlugin;
//import dev.latvian.mods.kubejs.integration.jei.KubeJSJEIPlugin;
//import dev.nolij.toomanyrecipeviewers.JEIPlugins;
//import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
//import mezz.jei.api.runtime.IJeiRuntime;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@ConditionalMixin(modId = "toomanyrecipeviewers", versionRange = "[0.1,)")
//@Mixin(KubeJSEMIPlugin.class)
//public class TMRVPluginMixin {
//    @Inject(method = "register", at = @At("HEAD"))
//    private void populateEmiRegistryOnGlobal(EmiRegistry registry, CallbackInfo ci){
//        BuiltinKubeJSPlugin.GLOBAL.put("emiRegistry", registry);
//    }
//}

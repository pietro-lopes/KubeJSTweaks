package dev.uncandango.kubejstweaks.kubejs.kjs72.mixin.core.main;


import dev.latvian.mods.kubejs.integration.emi.KubeJSEMIPlugin;
import dev.latvian.mods.kubejs.plugin.builtin.BuiltinKubeJSPlugin;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.2,2101.7.3)", extraModDep = {"emi"}, extraModDepVersions = {"[1.1,)"})
@Mixin(KubeJSEMIPlugin.class)
public class KubeJSEMIPluginMixin {
    @Inject(method = "register", at = @At("HEAD"))
    private void populateEmiRegistryOnGlobal(@Coerce Object registry, CallbackInfo ci){
        BuiltinKubeJSPlugin.GLOBAL.put("emiRegistry", registry);
    }

    @Inject(method = "register", at = @At("TAIL"))
    private void removeEmiRegistryFromGlobal(@Coerce Object registry, CallbackInfo ci){
        BuiltinKubeJSPlugin.GLOBAL.remove("emiRegistry");
    }
}

package dev.uncandango.kubejstweaks.mixin.core.main;

import dev.emi.emi.api.EmiRegistry;
import dev.latvian.mods.kubejs.BuiltinKubeJSPlugin;
import dev.latvian.mods.kubejs.integration.emi.KubeJSEMIPlugin;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ConditionalMixin(modId = "emi", versionRange = "[1.1,)")
@Mixin(KubeJSEMIPlugin.class)
public class KubeJSEMIPluginMixin {
    @Inject(method = "register", at = @At("HEAD"))
    private void populateEmiRegistryOnGlobal(EmiRegistry registry, CallbackInfo ci){
        BuiltinKubeJSPlugin.GLOBAL.put("emiRegistry", registry);
    }

    @Inject(method = "register", at = @At("TAIL"))
    private void removeEmiRegistryFromGlobal(EmiRegistry registry, CallbackInfo ci){
        BuiltinKubeJSPlugin.GLOBAL.remove("emiRegistry");
    }
}

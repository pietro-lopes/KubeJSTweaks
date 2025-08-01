package dev.uncandango.kubejstweaks.mixin.core.main;

import dev.latvian.mods.kubejs.BuiltinKubeJSPlugin;
import dev.latvian.mods.kubejs.integration.jei.KubeJSJEIPlugin;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ConditionalMixin(modId = "jei", versionRange = "[19.8,20)")
@Mixin(KubeJSJEIPlugin.class)
public abstract class KubeJEIPluginMixin implements IModPlugin {

    @Inject(method = "onRuntimeAvailable", at = @At("HEAD"))
    private void populateJeiRuntimeOnGlobal(IJeiRuntime runtime, CallbackInfo ci){
        BuiltinKubeJSPlugin.GLOBAL.put("jeiRuntime", runtime);
    }

    @Override
    public void onRuntimeUnavailable() {
        BuiltinKubeJSPlugin.GLOBAL.remove("jeiRuntime");
    }
}

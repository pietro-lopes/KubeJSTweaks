package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;

// https://github.com/KubeJS-Mods/KubeJS/commit/82a224df4626c85290e5c4ee29613373b0ddc1f6
@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(KubeJSPaths.class)
public interface KubeJSPathsMixin {
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/nio/file/Path;resolve(Ljava/lang/String;)Ljava/nio/file/Path;", ordinal = 18))
    private static Path fixServerScript(Path instance, String other){
        return instance.resolve("local_startup_scripts");
    }
}

package dev.uncandango.kubejstweaks.mixin.core.main;

import dev.latvian.mods.kubejs.server.DataExport;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ExecutorService;

// https://github.com/KubeJS-Mods/KubeJS/pull/963
@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(DataExport.class)
public class DataExportMixin {
    @Redirect(method = "exportData0", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;ioPool()Ljava/util/concurrent/ExecutorService;"))
    private ExecutorService redirectToBackground(){
        return Util.backgroundExecutor();
    }
}

package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import dev.latvian.mods.kubejs.bindings.JavaWrapper;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import dev.uncandango.kubejstweaks.mixin.extension.JavaWrapperExtension;
import org.spongepowered.asm.mixin.Mixin;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(JavaWrapper.class)
public interface JavaWrapperMixin extends JavaWrapperExtension {
}

package dev.uncandango.kubejstweaks.mixin.core.main;

import dev.latvian.mods.kubejs.bindings.JavaWrapper;
import dev.uncandango.kubejstweaks.mixin.extension.JavaWrapperExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(JavaWrapper.class)
public interface JavaWrapperMixin extends JavaWrapperExtension {
}

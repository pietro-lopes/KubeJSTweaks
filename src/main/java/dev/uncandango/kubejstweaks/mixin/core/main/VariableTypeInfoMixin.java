package dev.uncandango.kubejstweaks.mixin.core.main;

import dev.latvian.mods.rhino.type.TypeInfoBase;
import dev.latvian.mods.rhino.type.VariableTypeInfo;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;

// https://github.com/KubeJS-Mods/Rhino/issues/61
@ConditionalMixin(modId = "rhino", versionRange = "[2101.2.7-build.74]")
@Mixin(VariableTypeInfo.class)
public abstract class VariableTypeInfoMixin extends TypeInfoBase {

    @Override
    public boolean shouldConvert() {
        return this.asClass() != Object.class;
    }
}

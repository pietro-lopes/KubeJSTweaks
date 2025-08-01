package dev.uncandango.kubejstweaks.mixin.core.main;

import dev.latvian.mods.rhino.type.TypeInfoBase;
import dev.latvian.mods.rhino.type.VariableTypeInfo;
import org.spongepowered.asm.mixin.Mixin;

// https://github.com/KubeJS-Mods/Rhino/issues/61
@Mixin(VariableTypeInfo.class)
public abstract class VariableTypeInfoMixin extends TypeInfoBase {

    @Override
    public boolean shouldConvert() {
        return this.asClass() != Object.class;
    }
}

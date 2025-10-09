package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.type.TypeInfoBase;
import dev.latvian.mods.rhino.type.VariableTypeInfo;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// https://github.com/KubeJS-Mods/Rhino/issues/61
@ConditionalMixin(modId = "rhino", versionRange = "[2101.2.7-build.77]")
@Mixin(VariableTypeInfo.class)
public abstract class VariableTypeInfo2Mixin extends TypeInfoBase {

    @Shadow
    public abstract TypeInfo[] getBounds();

    @Override
    public boolean shouldConvert() {
        return getBounds() != TypeInfo.EMPTY_ARRAY;
    }
}

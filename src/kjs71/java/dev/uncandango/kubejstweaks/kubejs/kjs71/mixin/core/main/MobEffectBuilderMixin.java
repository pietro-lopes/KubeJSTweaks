package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.kubejs.misc.BasicMobEffect;
import dev.latvian.mods.kubejs.misc.MobEffectBuilder;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.Util;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(MobEffectBuilder.class)
public class MobEffectBuilderMixin {
    @ModifyReturnValue(method = "createObject()Lnet/minecraft/world/effect/MobEffect;", at = @At("RETURN"))
    private MobEffect initiateModifiers(MobEffect original){
        return Util.make(original, effect -> ((BasicMobEffectAccessor)effect).kjstweaks$applyAttributeModifications());
    }

    @Mixin(BasicMobEffect.class)
    public interface BasicMobEffectAccessor {
        @Invoker("applyAttributeModifications")
        void kjstweaks$applyAttributeModifications();
    }
}

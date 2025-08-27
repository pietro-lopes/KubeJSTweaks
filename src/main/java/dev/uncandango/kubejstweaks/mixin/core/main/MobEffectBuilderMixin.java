package dev.uncandango.kubejstweaks.mixin.core.main;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.kubejs.misc.BasicMobEffect;
import dev.latvian.mods.kubejs.misc.MobEffectBuilder;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEffectBuilder.class)
public class MobEffectBuilderMixin {
    @ModifyReturnValue(method = "createObject()Lnet/minecraft/world/effect/MobEffect;", at = @At("RETURN"))
    private MobEffect initiateModifiers(MobEffect original){
        ((BasicMobEffectAccessor)original).kjstweaks$applyAttributeModifications();
        return original;
    }

    @Mixin(BasicMobEffect.class)
    public interface BasicMobEffectAccessor {
        @Invoker("applyAttributeModifications")
        void kjstweaks$applyAttributeModifications();
    }
}

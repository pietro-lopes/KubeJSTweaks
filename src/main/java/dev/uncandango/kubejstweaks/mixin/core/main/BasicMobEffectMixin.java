package dev.uncandango.kubejstweaks.mixin.core.main;

import dev.latvian.mods.kubejs.misc.BasicMobEffect;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.BiConsumer;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(BasicMobEffect.class)
public abstract class BasicMobEffectMixin extends MobEffect {

    @Shadow
    protected abstract void applyAttributeModifications();

    protected BasicMobEffectMixin(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void createModifiers(int amplifier, BiConsumer<Holder<Attribute>, AttributeModifier> output) {
        applyAttributeModifications();
        super.createModifiers(amplifier, output);
    }
}

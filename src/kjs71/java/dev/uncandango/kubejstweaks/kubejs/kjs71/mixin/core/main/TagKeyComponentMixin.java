package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.TagKeyComponent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TagKeyComponent.class)
public abstract class TagKeyComponentMixin<T> implements RecipeComponent<T> {
//    @Redirect(method = "codec", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagKey;codec(Lnet/minecraft/resources/ResourceKey;)Lcom/mojang/serialization/Codec;"))
//    private Codec<?> useHasedCodec(ResourceKey<? extends Registry<Object>> registry){
//        return Codec.withAlternative(TagKey.codec(registry),TagKey.hashedCodec(registry));
//    }

    @Override
    public boolean isEmpty(T value) {
        return value == null;
    }
}

package dev.uncandango.kubejstweaks.mixin.core.main;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.MapLike;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipeTypeFunction;
import dev.latvian.mods.kubejs.recipe.component.EitherRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.ListRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentBuilder;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.rhino.Context;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
// This is needed to wrap values for the RecipeComponentBuilder or ListRecipeComponent
// Or it breaks at the encode/decode
@Mixin(RecipeTypeFunction.class)
public class RecipeTypeFunctionMixin {
    @WrapOperation(method = "createRecipe", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/rhino/Wrapper;unwrapped(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1))
    private Object wrapValue(Object w, Operation<Object> original , @Local RecipeKey<?> key, @Local(argsOnly = true) Context cx){
        if (key.component instanceof RecipeComponentBuilder rcb) {
            var mapLike = MapLike.forMap((Map<Object,Object>)original.call(w), JavaOps.INSTANCE);
            var result = rcb.mapCodec().decode(JavaOps.INSTANCE, mapLike);

            return result.getOrThrow();

//            return rcb.wrap(cx, null, original.call(w));
        }
        if (key.component instanceof ListRecipeComponent<?> lrc) {
            return lrc.wrap(cx, null, original.call(w));
        }
        return original.call(w);
    }

//    private RecipeComponent<?> unwrapComponent(RecipeComponent<?> rc){
//        if (rc instanceof ListRecipeComponent<?> lrc) {
//            return unwrapComponent(lrc.component());
//        if (rc instanceof EitherRecipeComponent<?,?> erc) {
//            unwrapComponent(erc.low());
//            unwrapComponent(erc.high());
//            return
//        }
//        return rc;
//    }
}

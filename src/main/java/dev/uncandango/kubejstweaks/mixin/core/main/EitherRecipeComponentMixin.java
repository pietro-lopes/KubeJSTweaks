package dev.uncandango.kubejstweaks.mixin.core.main;

import com.mojang.datafixers.util.Either;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.EitherRecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

// https://github.com/KubeJS-Mods/KubeJS/commit/a98b71b481472e825c4edec49b145daea1750493
// https://github.com/KubeJS-Mods/KubeJS/issues/967
@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(EitherRecipeComponent.class)
public class EitherRecipeComponentMixin {
    @Redirect(method = "validate(Lcom/mojang/datafixers/util/Either;)V", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isEmpty()Z"))
    private boolean fixEmptyCheck(Optional instance){
        return instance.isPresent();
    }
    @Inject(method = "wrap(Ldev/latvian/mods/rhino/Context;Ldev/latvian/mods/kubejs/recipe/KubeRecipe;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Either;", at = @At("HEAD"), cancellable = true)
    private void returnifWrapped(Context cx, KubeRecipe recipe, Object from, CallbackInfoReturnable<Either<?, ?>> cir){
        if (from instanceof Either<?, ?> either) {
            cir.setReturnValue(either);
        }
    }
}

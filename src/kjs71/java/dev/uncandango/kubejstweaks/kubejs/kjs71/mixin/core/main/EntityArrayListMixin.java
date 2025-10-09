package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.latvian.mods.kubejs.player.EntityArrayList;
import net.minecraft.Util;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

// https://github.com/KubeJS-Mods/KubeJS/issues/1039
@Mixin(EntityArrayList.class)
public class EntityArrayListMixin {
    @Inject(method = "filter", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;I)Ldev/latvian/mods/kubejs/player/EntityArrayList;"))
    private void createPredicate(List<Predicate<Entity>> filterList, CallbackInfoReturnable<EntityArrayList> cir, @Share("predicate") LocalRef<Predicate<Entity>> predicate){
        predicate.set(Util.allOf(filterList));
    }

    @ModifyExpressionValue(method = "filter", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 1))
    private boolean cancelLoop(boolean original){
        return false;
    }

    @ModifyExpressionValue(method = "filter", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;", ordinal = 0))
    private Object testNewPredicate(Object original, @Share("predicate") LocalRef<Predicate<Entity>> predicate, @Local EntityArrayList list){
        if (predicate.get().test((Entity)original)) {
            list.add((Entity)original);
        }
        return original;
    }
}

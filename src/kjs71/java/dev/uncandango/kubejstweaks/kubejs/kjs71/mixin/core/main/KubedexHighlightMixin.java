package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.kubejs.kubedex.KubedexHighlight;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;

// https://github.com/KubeJS-Mods/KubeJS/issues/1024
@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(KubedexHighlight.class)
public class KubedexHighlightMixin {
    @WrapOperation(method = "requestInventory", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z", ordinal = 1))
    private boolean checkIfEmpty(ArrayList<ItemStack> instance, Object e, Operation<Boolean> original){
        if (((ItemStack) e).isEmpty()) return false;
        return original.call(instance, e);
    }
}

package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.api.EmiRegistry;
import dev.latvian.mods.kubejs.integration.emi.EMIIntegration;
import dev.latvian.mods.kubejs.integration.emi.EMIRemoveEntriesKubeEvent;
import dev.latvian.mods.kubejs.item.ItemPredicate;
import dev.latvian.mods.rhino.Context;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// https://github.com/KubeJS-Mods/KubeJS/issues/1052
@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]", extraModDep = "emi", extraModDepVersions = "*")
@Mixin(EMIRemoveEntriesKubeEvent.class)
public class EMIRemoveEntriesKubeEventMixin {
    @Shadow
    @Final
    private EmiRegistry registry;

    @Inject(method = "remove", at = @At(value = "JUMP", opcode = Opcodes.IF_ACMPNE, ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void fixClassCast(Context cx, Object filter, CallbackInfo ci, @Local(ordinal = 1) Object predicate){
        registry.removeEmiStacks(EMIIntegration.predicate((ItemPredicate) predicate));
        ci.cancel();
    }
}

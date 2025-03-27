package dev.uncandango.kubejstweaks.mixin.core.main;

import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.client.ClientAssetPacks;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.WeakReference;
import java.util.List;

import static dev.uncandango.kubejstweaks.kubejs.plugin.KJSTPluginUtils.CLIENT_PACK_RESOURCES;

@Mixin(ClientAssetPacks.class)
public class ClientAssetPacksMixin {
    @Inject(method = "inject0", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/script/data/VirtualAssetPack;reset()V", ordinal = 0))
    private void grabPackResources(List<PackResources> original, CallbackInfoReturnable<List<PackResources>> cir, @Local List<PackResources> packs){
        CLIENT_PACK_RESOURCES = new WeakReference<>(new MultiPackResourceManager(PackType.CLIENT_RESOURCES, packs));
    }
}

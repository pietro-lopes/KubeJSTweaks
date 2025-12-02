package dev.uncandango.kubejstweaks.kubejs.kjs71.mixin.core.main;

import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.client.ClientAssetPacks;
import dev.uncandango.kubejstweaks.impl.TempResourceManager;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.uncandango.kubejstweaks.kubejs.plugin.KJSTPluginUtils.CLIENT_PACK_RESOURCES;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(value = ClientAssetPacks.class)
public class ClientAssetPacksMixin {
    @Inject(method = "inject0", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/script/data/VirtualAssetPack;reset()V", ordinal = 0))
    private void grabPackResources(List<PackResources> original, CallbackInfoReturnable<List<PackResources>> cir, @Local(ordinal = 0) ArrayList<PackResources> packs){
        CLIENT_PACK_RESOURCES = new TempResourceManager(PackType.CLIENT_RESOURCES, packs);
    }
}

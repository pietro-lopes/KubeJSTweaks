package dev.uncandango.kubejstweaks.mixin.core.main;

import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static dev.uncandango.kubejstweaks.kubejs.plugin.KJSTPluginUtils.SERVER_PACK_RESOURCES;
import static dev.uncandango.kubejstweaks.kubejs.plugin.KJSTPluginUtils.TEMPORARY_SERVER_PACK_RESOURCES;

@Mixin(ServerScriptManager.class)
public class ServerPackMixin {
    @Inject(method = "createPackResources", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/server/ServerScriptManager;reload()V"))
    private static void grabPackList(List<PackResources> original, CallbackInfoReturnable<List<PackResources>> cir, @Local(ordinal = 0) ArrayList<PackResources> packs){
        if (TEMPORARY_SERVER_PACK_RESOURCES != null){
            TEMPORARY_SERVER_PACK_RESOURCES.close();
            TEMPORARY_SERVER_PACK_RESOURCES = null;
        }
        SERVER_PACK_RESOURCES = new WeakReference<>(new MultiPackResourceManager(PackType.SERVER_DATA,packs));
    }
}

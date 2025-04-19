package dev.uncandango.kubejstweaks.mixin.core.main;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Iterator;

// https://github.com/KubeJS-Mods/KubeJS/issues/972
@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(ServerScriptManager.class)
public class ServerScriptManagerMixin {

    @WrapOperation(method = "loadAdditional", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 2))
    private boolean interceptLoop(Iterator instance, Operation<Boolean> original, @Local(name = "furnaceFuelsJson") JsonObject furnaceFuelsJson) {
        while (instance.hasNext()) {
            var base = (BuilderBase) instance.next();
            if (base instanceof ItemBuilder item) {
                long b = item.burnTime;

                if (b > 0L) {
                    var json = new JsonObject();
                    json.addProperty("burn_time", b);
                    furnaceFuelsJson.add(item.id.toString(), json);
                }
            }
        }
        return false;
    }
}

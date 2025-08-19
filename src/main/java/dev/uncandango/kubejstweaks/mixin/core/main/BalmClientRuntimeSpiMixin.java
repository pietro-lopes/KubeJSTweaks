package dev.uncandango.kubejstweaks.mixin.core.main;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.blay09.mods.balm.api.client.BalmClientRuntimeFactory;
import net.blay09.mods.balm.api.client.BalmClientRuntimeSpi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ServiceLoader;

@ConditionalMixin(modId = "balm", versionRange = "*")
@Mixin(BalmClientRuntimeSpi.class)
public class BalmClientRuntimeSpiMixin {
    @ModifyExpressionValue(method = "create", at = @At(value = "INVOKE", target = "Ljava/util/ServiceLoader;load(Ljava/lang/Class;)Ljava/util/ServiceLoader;"), require = 0)
    private static ServiceLoader<?> kjstweaks$saferLoad(ServiceLoader<?> original){
        return ServiceLoader.load(BalmClientRuntimeFactory.class, BalmClientRuntimeFactory.class.getClassLoader());
    }
}

package dev.uncandango.kubejstweaks.mixin.core.main;

import appeng.api.integrations.igtooltip.TooltipProvider;
import appeng.integration.modules.igtooltip.TooltipProviders;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ServiceLoader;

@Pseudo
@ConditionalMixin(modId = "ae2", versionRange = "*")
@Mixin(TooltipProviders.class)
public class TooltipProvidersMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/ServiceLoader;load(Ljava/lang/Class;)Ljava/util/ServiceLoader;"), require = 0)
    private static ServiceLoader<?> kjstweaks$saferLoad(ServiceLoader<?> original){
        return ServiceLoader.load(TooltipProvider.class, TooltipProvider.class.getClassLoader());
    }
}

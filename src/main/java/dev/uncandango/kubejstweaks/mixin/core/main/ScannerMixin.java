package dev.uncandango.kubejstweaks.mixin.core.main;

import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.kubejs.registry.RegistryType;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

// https://github.com/KubeJS-Mods/KubeJS/commit/c88bae170f686120d091e4b67c545b11cb2469b7
// https://github.com/KubeJS-Mods/KubeJS/commit/f1befc44784d9a64986f8275b6a80b7ce8b33966
@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.1-build.181]")
@Mixin(RegistryType.Scanner.class)
public class ScannerMixin {
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/Set;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/Set;"))
    private static <T> Set<T> redirectOf(T t1, T t2, T t3, T t4) {
        return Sets.newHashSet(t1,t2,t3,t4, null);
    }

    @WrapOperation(method = "processClass", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;map(Ljava/util/function/Function;)Ljava/util/stream/Stream;"))
    private static <T,R> Stream<R> checkNull(Stream instance, Function<? super T, ? extends R> function, Operation<Stream<R>> original) {
        return original.call(instance.filter(Objects::nonNull), function);
    }
}

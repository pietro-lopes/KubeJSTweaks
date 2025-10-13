package dev.uncandango.kubejstweaks.kubejs.kjs72.mixin.core.main;

import dev.latvian.mods.kubejs.holder.HolderWrapper;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RegistryComponent;
import dev.latvian.mods.kubejs.registry.RegistryType;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.2,2101.7.3)")
@Mixin(RegistryComponent.class)
public abstract class RegistryComponentMixin<T> implements RecipeComponent<Holder<T>> {
    @Shadow
    @Final
    private Registry<T> registry;

    @Shadow
    @Final
    private @Nullable RegistryType<T> regType;

//    @ModifyArg(method = "<init>(Ldev/latvian/mods/kubejs/util/RegistryAccessContainer;Ldev/latvian/mods/kubejs/registry/RegistryType;Lnet/minecraft/resources/ResourceKey;)V", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/recipe/component/RegistryComponent;<init>(Lnet/minecraft/core/Registry;Ldev/latvian/mods/kubejs/registry/RegistryType;Lcom/mojang/serialization/Codec;Ldev/latvian/mods/rhino/type/TypeInfo;)V"), index = 2)
//    private static <T> Codec<Holder<T>> fix(Codec<Holder<T>> codec, @Local(argsOnly = true) ResourceKey<Registry<T>> key, @Local(argsOnly = true) RegistryAccessContainer access){
//        return access.access().registry(key).orElseThrow().holderByNameCodec();
//    }

    @Inject(method = "wrap(Ldev/latvian/mods/kubejs/recipe/RecipeScriptContext;Ljava/lang/Object;)Lnet/minecraft/core/Holder;", at = @At(value = "JUMP", opcode = Opcodes.IFNULL, ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void fixContext(RecipeScriptContext cx, Object from, CallbackInfoReturnable<Holder<T>> cir){
        cir.setReturnValue((Holder<T>) HolderWrapper.wrap((KubeJSContext) cx.cx(), from, regType.type()));
    }

//    @Override
//    public boolean isEmpty(Holder<T> value) {
//        if (value == null) return true;
//        if (this.registry instanceof DefaultedRegistry<T> dr){
//            return dr.get(dr.getDefaultKey()).equals(value.value());
//        }
//        return !value.isBound();
//    }
}

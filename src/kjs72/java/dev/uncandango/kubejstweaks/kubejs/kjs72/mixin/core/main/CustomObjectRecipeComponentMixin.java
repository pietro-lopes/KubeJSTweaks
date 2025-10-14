package dev.uncandango.kubejstweaks.kubejs.kjs72.mixin.core.main;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.CustomObjectRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.ErrorStack;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import dev.latvian.mods.kubejs.util.RegistryOpsContainer;
import dev.latvian.mods.rhino.Context;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.2-build.270]")
@Mixin(CustomObjectRecipeComponent.class)
public abstract class CustomObjectRecipeComponentMixin implements RecipeComponent<List<CustomObjectRecipeComponent.Value>> {

    @Shadow
    public abstract MapCodec<List<CustomObjectRecipeComponent.Value>> mapCodec();

    @Redirect(method = {"matches*", "replace*", "buildUniqueId*", "validate*", "isEmpty*"}, at = @At(value = "FIELD", target = "Ldev/latvian/mods/kubejs/recipe/component/CustomObjectRecipeComponent$Value;value:Ljava/lang/Object;"))
    private Object callValue(CustomObjectRecipeComponent.Value instance) {
        return instance.getValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CustomObjectRecipeComponent.Value> wrap(RecipeScriptContext cx, Object from) {
        if (from instanceof Map<?, ?> mapLike) {
            return mapCodec().decode(cx.ops().java(), MapLike.forMap((Map<Object, Object>) mapLike, cx.ops().java())).getOrThrow();
        }
        if (from instanceof List<?> listLike) {
            boolean alreadyWrapped = listLike.stream().allMatch(element -> element instanceof CustomObjectRecipeComponent.Value);
            if (alreadyWrapped) {
                return (List<CustomObjectRecipeComponent.Value>) listLike;
            }
        }
        throw new IllegalStateException("Unexpected value: " + from);
    }

    @ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.2-build.270]")
    @Mixin(CustomObjectRecipeComponent.Value.class)
    public static class ValueMixin {
        @Shadow
        @Final
        private Object value;

        @Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
        private void unwrapDataResult(CallbackInfoReturnable<Object> cir) {
            if (value instanceof DataResult<?> dr) {
                if (dr.isSuccess()) {
                    if (dr.getOrThrow() instanceof Pair p) {
                        cir.setReturnValue(p.getFirst());
                    } else {
                        cir.setReturnValue(dr.getOrThrow());
                    }
                } else {
                    var result = dr.result();
                    if (result.isEmpty()) {
                        cir.setReturnValue(null);
                    } else {
                        cir.setReturnValue(result.get());
                    }
                }
            }
        }
    }

    @ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.2-build.270]")
    @Mixin(targets = "dev.latvian.mods.kubejs.recipe.component.CustomObjectRecipeComponent$1")
    public static abstract class MapCodecMixin {
        @Shadow
        @Final
        CustomObjectRecipeComponent this$0;

        @Redirect(method = "encode*", at = @At(value = "FIELD", target = "Ldev/latvian/mods/kubejs/recipe/component/CustomObjectRecipeComponent$Value;value:Ljava/lang/Object;"))
        private Object callValue(CustomObjectRecipeComponent.Value instance) {
            return instance.getValue();
        }

        @ModifyArg(method = "decode", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;decode(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"), index = 1)
        private Object wrapValue(Object par2, @Local CustomObjectRecipeComponent.Key key, @Local(argsOnly = true) DynamicOps<?> ops) {
            if (par2 instanceof JsonElement) {
                return par2;
            }
            var server = ServerLifecycleHooks.getCurrentServer();
            Context cx;
            if (server != null) {
                cx = server.getServerResources().managers().kjs$getServerScriptManager().contextFactory.enter();
            } else {
                cx = KubeJS.getStartupScriptManager().contextFactory.enter();
            }
            RecipeScriptContext recipeCtx = new RecipeScriptContext() {
                @Override
                public KubeRecipe recipe() {
                    return null;
                }

                @Override
                public ErrorStack errors() {
                    return null;
                }

                @Override
                public Context cx() {
                    return cx;
                }

                @Override
                public RegistryAccessContainer registries() {
                    return RegistryAccessContainer.of(cx());
                }

                @Override
                public RegistryOpsContainer ops() {
                    return registries();
                }
            };
            var newValue = key.component().wrap(recipeCtx, par2);

            RecipeComponent<Object> rc = (RecipeComponent<Object>) key.component();
            var regJson = ((RegistryOps<Object>) ops).withParent(JsonOps.INSTANCE);
            JsonElement newJson = rc.codec().encodeStart(regJson, newValue).getOrThrow();
            if (newJson.isJsonObject()) {
                return JsonUtils.toObject(newJson);
            }
            return regJson.convertTo(ops, newJson);
        }

        @WrapOperation(method = "decode", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/DataResult;success(Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"))
        private DataResult<Object> errorIfEmptyMap(Object result, Operation<DataResult<Object>> original, @Local(argsOnly = true) MapLike<?> input) {
            var dr = original.call(result);
            var list = (List<CustomObjectRecipeComponent.Value>) dr.getOrThrow();
            var missingKeys = Optional.empty();

            for (var key : this$0.keys()) {
                if (!list.stream().anyMatch(value -> value.getKey() == key && !key.optional())) {
                    missingKeys = Optional.of(key.name());
                    break;
                }
            }

            return missingKeys.map(s -> DataResult.error(() -> "Missing key: " + s + " in map " + input)).orElse(dr);
        }
    }
}


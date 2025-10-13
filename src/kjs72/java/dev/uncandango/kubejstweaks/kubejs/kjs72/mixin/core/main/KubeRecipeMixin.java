package dev.uncandango.kubejstweaks.kubejs.kjs72.mixin.core.main;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.kubejs.DevProperties;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.uncandango.kubejstweaks.kubejs.debug.DumpErroringRecipes;
import dev.uncandango.kubejstweaks.mixin.annotation.ConditionalMixin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Consumer;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.2,2101.7.3)")
@Mixin(KubeRecipe.class)
public class KubeRecipeMixin {
    @Shadow
    public ResourceLocation id;

    @Shadow
    public JsonObject json;

//    @Inject(method = "deserialize", at = @At(value = "JUMP", opcode = Opcodes.IF_ICMPGE))
//    private void storeCurrentException(boolean merge, CallbackInfo ci, @Share(value = "currentEx") LocalRef<Throwable> exRef){
//    }
//
//    @Inject(method = "deserialize", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/script/ConsoleJS;warn(Ljava/lang/String;Ldev/latvian/mods/kubejs/script/SourceLine;Ljava/lang/Throwable;Ljava/util/regex/Pattern;)Ldev/latvian/mods/kubejs/script/ConsoleLine;"))
//    private void setCurrentException(boolean merge, CallbackInfo ci, @Local Exception ex, @Share(value = "currentEx") LocalRef<Throwable> exRef){
//        exRef.set(ex);
//    }
//
//    @ModifyExpressionValue(method = "deserialize", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/error/MissingComponentException;source(Ldev/latvian/mods/kubejs/script/SourceLine;)Ldev/latvian/mods/kubejs/error/KubeRuntimeException;"))
//    private KubeRuntimeException checkCurrentException(KubeRuntimeException original, @Share(value = "currentEx") LocalRef<Throwable> exRef){
//        var ex = exRef.get();
//        if (ex == null) return original;
//        original.initCause(ex);
//        return original;
//    }

    @ModifyArg(method = "getOriginalRecipe", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/DataResult;ifError(Ljava/util/function/Consumer;)Lcom/mojang/serialization/DataResult;"), index = 0)
    private Consumer<? super DataResult.Error<Object>> dumpErroringRecipe(Consumer<? super DataResult.Error<Object>> ifError){
        return err -> {
            var origMessage = err.message();
            var targetMessage = origMessage
                .replaceAll("\\sin\\s.*?(;|$)", ";")
                .replaceAll("Not a JSON object\\:\\s.*?(;|$)","Not a JSON object;")
                .replaceAll("Not a json array\\:\\s.*?(;|$)","Not a json array;")
                .replaceAll("recipe \\S+:\\S+:", "recipe;")
                .replaceAll("with type\\:\\s.*?(;|$)", "with type;");
            DumpErroringRecipes.add(new KubeRuntimeException(targetMessage, new IllegalArgumentException(err.message())), this.id, this.json);
            if (DevProperties.get().logErroringParsedRecipes) {
                ConsoleJS.SERVER.error(err.message());
            } else {
                RecipeManager.LOGGER.error(err.message());
            }
        };
    }
}

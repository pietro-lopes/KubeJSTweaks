package dev.uncandango.kubejstweaks.kubejs.kjs72.mixin.core.main;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.CustomObjectRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.Cast;
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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ConditionalMixin(modId = "kubejs", versionRange = "[2101.7.2-build.277]")
@Mixin(CustomObjectRecipeComponent.class)
public abstract class CustomObjectRecipeComponentMixin2 implements RecipeComponent<List<CustomObjectRecipeComponent.Value>> {

    @Shadow
    @Final
    private List<CustomObjectRecipeComponent.Key> keys;

    /**
     * @author Uncandango
     * @reason too much codec stuff, lambdas, capturing variables
     */
    @Overwrite
    public MapCodec<List<CustomObjectRecipeComponent.Value>> mapCodec() {
        return new MapCodec<>() {
            // EDITED CODE AT LINE 80
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return keys.stream().map(CustomObjectRecipeComponent.Key::name).map(ops::createString);
            }

            @Override
            public <T> DataResult<List<CustomObjectRecipeComponent.Value>> decode(DynamicOps<T> ops, MapLike<T> input) {
                List<CustomObjectRecipeComponent.Value> list = new ArrayList<>(keys.size());
                Map<String, CustomObjectRecipeComponent.Key> keyMap = new HashMap<>();

                keys.forEach(key -> keyMap.put(key.name(), key));

                Stream.Builder<Pair<T, T>> failed = Stream.builder();

                var result = input.entries().reduce(
                    DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
                    (r, pair) -> {
                        var keyResult = ops.getStringValue(pair.getFirst()).flatMap(k -> {
                            if (keyMap.containsKey(k)) {
                                return DataResult.success(keyMap.get(k));
                            } else {
                                return DataResult.error(() -> "Unknown key in custom object: " + k);
                            }
                        });

                        // EDITED CODE HERE
                        var valueResult = keyResult.map(CustomObjectRecipeComponent.Key::component)
                            .flatMap(component -> {
                                var resultCodec = component.codec().decode(ops, pair.getSecond());
                                if (resultCodec.isError()) {
                                    try {
                                        // "12x minecraft:apple" -> ItemStack with count 12
                                        var resultJava = component.wrap(kjstweaks$getRecipeScriptContext(), pair.getSecond());
                                        var jsonOps = ((RegistryOps<Object>)ops).withParent(JsonOps.INSTANCE);
                                        // ItemStack with count 12 -> {id: "minecraft:apple", count: 12}
                                        var resultJson = ((RecipeComponent<Object>)component).codec().encodeStart(jsonOps, resultJava).getOrThrow();
                                        // {id: "minecraft:apple", count: 12} -> Java Map with those 2 entries
                                        resultJava = JsonUtils.toObject(resultJson);
                                        resultCodec = component.codec().decode(ops, Cast.to(resultJava));
                                    } catch (Exception e) {
                                        return DataResult.error(e::getMessage);
                                    }
                                }
                                return resultCodec;
                            }).map(Pair::getFirst);

                        // EDITED CODE ENDS HERE

                        var entryResult = keyResult.apply2stable((k, v) -> new CustomObjectRecipeComponent.Value(k, keys.indexOf(k), v), valueResult);

                        entryResult.resultOrPartial().ifPresent(list::add);

                        if (entryResult.isError()) {
                            failed.add(pair);
                        }

                        return r.apply2stable((u, p) -> u, entryResult);
                    },
                    (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2)
                );


                if (list.size() >= 2) {
                    list.sort(null);
                }

                var errors = ops.createMap(failed.build());

                return result.map(unit -> list)
                    .setPartial(list)
                    .mapError(e -> e + " missed input: " + errors);
            }

            @Override
            public <T> RecordBuilder<T> encode(List<CustomObjectRecipeComponent.Value> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                var builder = ops.mapBuilder();

                for (var entry : input) {
                    builder.add(ops.createString(entry.key().name()), entry.key().component().codec().encodeStart(ops, Cast.to(entry.value())));
                }

                return builder;
            }
        };

    }

    @Unique
    static private RecipeScriptContext kjstweaks$getRecipeScriptContext(){
        return new RecipeScriptContext() {
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
                // Cursed AF
                var server = ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    return server.getServerResources().managers().kjs$getServerScriptManager().contextFactory.enter();
                }
                return KubeJS.getStartupScriptManager().contextFactory.enter();
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
    }

}

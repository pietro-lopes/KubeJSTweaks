package dev.uncandango.kubejstweaks.command;

import blusunrize.immersiveengineering.api.crafting.ClocheRenderFunction;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.codecs.EitherCodec;
import com.mojang.serialization.codecs.FieldDecoder;
import com.mojang.serialization.codecs.FieldEncoder;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.OptionalFieldCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kubejs.util.Cast;
import dev.uncandango.kubejstweaks.KubeJSTweaks;
import dev.uncandango.kubejstweaks.kubejs.codec.CodecScanner;
import dev.uncandango.kubejstweaks.kubejs.schema.RecipeSchemaFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.conditions.ConditionContext;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.server.command.EnumArgument;
import net.neoforged.neoforge.server.command.ModIdArgument;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import thedarkcolour.exdeorum.recipe.BlockPredicate;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class KJSTCommands {

    public static void registerClientCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        if (!FMLEnvironment.production) dispatcher.register(
            Commands.literal("kjstweaks")
                .then(Commands.literal("generate_schemas")
                    .executes(cmd -> {
                        return generateRecipeSchemas(cmd.getSource(), "all mods", SchemaFilter.FULL);
                    })
                    .then(Commands.literal("by_mod")
                        .then(Commands.argument("mod_ids", ModIdArgument.modIdArgument())
                            .executes(cmd -> {
                                return generateRecipeSchemas(cmd.getSource(), cmd.getArgument("mod_ids", String.class), SchemaFilter.BY_MOD);
                            })
                        )
                    )
                    .then(Commands.literal("by_recipe")
                        .then(Commands.argument("recipe_serializers_ids", ResourceKeyArgument.key(Registries.RECIPE_SERIALIZER))
                            .executes(cmd -> {
                                return generateRecipeSchemas(cmd.getSource(), cmd.getArgument("recipe_serializers_ids", ResourceKey.class).location().toString(), SchemaFilter.BY_RECIPE);
                            })
                        )
                    )
                )
                .then(Commands.literal("scan_codec")
                    .executes(cmd -> {
                        return scanCodec(cmd.getSource());
                    })
                )
                .then(Commands.literal("test_codec_context")
                    .executes(cmd -> {
                        return deconstructCodec(cmd.getSource());
                    })
                )
        );
    }

    private static int deconstructCodec(CommandSourceStack source) {
        visitedDecoders.clear();
        visitedSupportedDecoders.clear();
        visitedSupportedUnsupportedDecoders.clear();
        var recipeLookup = source.registryAccess().lookup(Registries.RECIPE_SERIALIZER).orElseThrow();
        recipeLookup.listElements().forEach(recipeSerializerReference -> {
            KubeJSTweaks.LOGGER.info("Found recipe serializer: {}", recipeSerializerReference.getKey().location());
            var codec = recipeSerializerReference.value().codec();
            getMapCodecContext(codec, null);
        });
        return Command.SINGLE_SUCCESS;
    }

    private static CodecContext getCodecContext(Decoder<?> decoder, @Nullable CodecContext context) {
        if (context == null) {
            context = new CodecContext(decoder);
        }

        if (CodecScanner.codecFields.containsKey(decoder)) {
            Field field = CodecScanner.codecFields.get(decoder);
            try {
                field.setAccessible(true);
                field.get(null);
                KubeJSTweaks.LOGGER.info("Found codec: {}, at class: {}, with field: {}", field.getGenericType(), field.getDeclaringClass(), field.getName());
                var module = field.getDeclaringClass().getModule();
                if (module != null && (module.getName().equals("minecraft") || module.getName().equals("neoforge"))){
                    KubeJSTweaks.LOGGER.info("Found primitive codec from {} and class {}", field.getDeclaringClass().getModule(), field.getGenericType());
                    if (field.getGenericType().getTypeName().equals("com.mojang.serialization.Codec<net.minecraft.resources.ResourceLocation>")) {
                        try {
                            var decoder2 = ObfuscationReflectionHelper.getPrivateValue(Cast.to(decoder.getClass()), decoder, "val$decoder");
                            var function = ObfuscationReflectionHelper.getPrivateValue(Cast.to(decoder2.getClass()), decoder2, "val$function");
                            var arg1 = ObfuscationReflectionHelper.getPrivateValue(Cast.to(function.getClass()), function, "arg$1");
                            KubeJSTweaks.LOGGER.info("Found registry key at {}", arg1);
                        } catch (Throwable ignore) {
                        }
                    }
                    return context;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }

        if (decoder instanceof EitherCodec<?,?> either) {
            KubeJSTweaks.LOGGER.info("Reached an either first");
            getCodecContext(either.first(), context);
            KubeJSTweaks.LOGGER.info("Reached an either second");
            getCodecContext(either.second(), context);
            return context;
        }
        if (decoder instanceof MapCodec.MapCodecCodec<?> mapCodecCodec) {
            KubeJSTweaks.LOGGER.info("Reached a map");
            getMapCodecContext(mapCodecCodec.codec(), context);
            return context;
        }
        if (decoder instanceof ListCodec<?> listCodecCodec) {
            KubeJSTweaks.LOGGER.info("Reached a list");
            getCodecContext(listCodecCodec.elementCodec(), context);
            return context;
        }

        var newDecoder = unwrapDecoder(decoder);
        if (newDecoder != decoder) {
            getCodecContext(newDecoder, context);
        }
        return context;
    }

    private static final Set<Class<?>> visitedDecoders = new HashSet<>();
    private static final Set<Class<?>> visitedSupportedDecoders = new HashSet<>();
    private static final Set<Class<?>> visitedSupportedUnsupportedDecoders = new HashSet<>();
    private static final List<Class<?>> skipClasses = List.of(Encoder.class, MapEncoder.class, java.util.function.Supplier.class);
    private static CodecContext getMapCodecContext(MapDecoder<?> mapDecoder, @Nullable CodecContext context) {
        if (context == null) {
            context = new CodecContext(mapDecoder);
        }

        if (CodecScanner.codecFields.containsKey(mapDecoder)) {
            Field field = CodecScanner.codecFields.get(mapDecoder);
            try {
                field.setAccessible(true);
                field.get(null);
                KubeJSTweaks.LOGGER.info("Found codec: {}, at class: {}, with field: {}", field.getGenericType(), field.getDeclaringClass(), field.getName());
                var module = field.getDeclaringClass().getModule();
                if (module != null && (module.getName().equals("minecraft") || module.getName().equals("neoforge"))){
                    KubeJSTweaks.LOGGER.info("Found primitive codec from {} and class {}", field.getDeclaringClass().getModule(), field.getGenericType());
                    // if it is resourcelocation, try to find key at fieldDecoder.elementCodec.val$decoder.val$function.arg$1
                    try {
                        var decoder = ObfuscationReflectionHelper.getPrivateValue(Cast.to(mapDecoder.getClass()), mapDecoder, "val$decoder");
                        var function = ObfuscationReflectionHelper.getPrivateValue(Cast.to(decoder.getClass()), decoder, "val$function");
                        var arg1 = ObfuscationReflectionHelper.getPrivateValue(Cast.to(function.getClass()), function, "arg$1");
                        KubeJSTweaks.LOGGER.info("Found registry key at {}", arg1);
                    } catch (Throwable ignore) {
                    }
                    return context;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }

        if (mapDecoder instanceof KeyDispatchCodec<?,?> keyDispatchCodec) {
            KubeJSTweaks.LOGGER.info("Reached a key dispatch codec {}", keyDispatchCodec);
            return context;
        }

        if (mapDecoder instanceof OptionalFieldCodec<?> ofc) {
            KubeJSTweaks.LOGGER.info("Reached an optional field codec {}", ofc);
            getCodecContext(ObfuscationReflectionHelper.getPrivateValue(OptionalFieldCodec.class, ofc, "elementCodec"), context);
            return context;
        }

        if (mapDecoder instanceof FieldDecoder<?> fieldDecoder) {
            KubeJSTweaks.LOGGER.info("Reached a field decoder {}", fieldDecoder);
            getCodecContext(ObfuscationReflectionHelper.getPrivateValue(FieldDecoder.class, fieldDecoder, "elementCodec"), context);
            return context;
        }

        var newDecoders = unwrapMapDecoder(mapDecoder);
        for (var decoder : newDecoders) {
            if (decoder instanceof MapDecoder<?> mapDecoder2) {
                getMapCodecContext(mapDecoder2, context);
            }
            if (decoder instanceof Decoder<?> decoder2) {
                getCodecContext(decoder2, context);
            }
        }
        return context;
    }

    private static Decoder<?> unwrapDecoder(Decoder<?> decoder){
        var newDecoder = decoder;
        for (var field : decoder.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                var value = field.get(decoder);
                if (value instanceof Decoder<?> decoder2) {
                    return decoder2;
                }
                if (value instanceof MapDecoder<?> mapDecoder) {
                    return mapDecoder.decoder();
                }
                if (value instanceof Supplier<?> supplier) {
                    return (Decoder<?>) supplier.get();
                }
            } catch (Exception e) {
                KubeJSTweaks.LOGGER.error("Error unwrapping codec", e);
            }
        }
        return newDecoder;
    }

    private static List<Object> unwrapMapDecoder(MapDecoder<?> decoder){
        var newDecoders = new ArrayList<>();
        Field[] fields = Stream.concat(Arrays.stream(decoder.getClass().getSuperclass().getDeclaredFields()), Arrays.stream(decoder.getClass().getDeclaredFields())).toArray(size -> (Field[]) Array.newInstance(Field.class, size));
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                var value = field.get(decoder);
                if (value instanceof Decoder<?> decoder2) {
                    newDecoders.add(decoder2);
                }
                if (value instanceof MapDecoder<?> mapDecoder2) {
                    newDecoders.add(mapDecoder2);
                }
                if (value instanceof RecordCodecBuilder<?,?> recordCodecBuilder) {
                    newDecoders.add(ObfuscationReflectionHelper.getPrivateValue(RecordCodecBuilder.class, recordCodecBuilder, "decoder"));
                }
                if (value instanceof List<?> list) {
                    for (var element : list) {
                        for (var elementField : element.getClass().getDeclaredFields()){
                            elementField.setAccessible(true);
                            var elementValue = elementField.get(element);
                            if (elementValue instanceof Decoder<?> elementDecoder) {
                                newDecoders.add(elementDecoder);
                            }
                            if (elementValue instanceof MapDecoder<?> mapDecoder2) {
                                newDecoders.add(mapDecoder2);
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                KubeJSTweaks.LOGGER.error("Error unwrapping codec", e);
            }
        }
        return newDecoders;
    }


    private static class CodecContext {
        private final Object decoder;
        public CodecContext(Object decoder) {
            this.decoder = decoder;
        }

    }


    private static int generateRecipeSchemas(CommandSourceStack source, String search, SchemaFilter filter) {
        if (FMLEnvironment.production) {
            source.sendFailure(Component.literal("This is a WIP feature"));
            return 0;
        }
        var recipeTypeRegistry = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.RECIPE_SERIALIZER);
        Set<Map.Entry<ResourceKey<RecipeSerializer<?>>,RecipeSerializer<?>>> targetRecipeTypes = new HashSet<>();
        if (filter == SchemaFilter.BY_MOD) {
            Set<Map.Entry<ResourceKey<RecipeSerializer<?>>, RecipeSerializer<?>>> recipeTypes = recipeTypeRegistry.entrySet();
            recipeTypes.stream().filter(entry -> entry.getKey().location().getNamespace().equals(search)).forEach((targetRecipeTypes::add));
        }
        if (filter == SchemaFilter.BY_RECIPE) {
            var id = ResourceLocation.parse(search);
            recipeTypeRegistry.entrySet().stream().filter(entry -> entry.getKey().location().equals(id)).forEach(targetRecipeTypes::add);
        }
        if (filter == SchemaFilter.FULL) {
            targetRecipeTypes.addAll(recipeTypeRegistry.entrySet());
        }

        var finders = RecipeSchemaFinder.of(targetRecipeTypes);

        RecipeSchemaFinder.serializerEventMap.clear();
        finders.forEach(RecipeSchemaFinder::start);

        KubeJSTweaks.LOGGER.info("Found {} recipe serializers for {}", targetRecipeTypes.size(), search);

        return Command.SINGLE_SUCCESS;
    }

    private static int scanCodec(CommandSourceStack source) {
        CodecScanner.scanVanilla();
        return Command.SINGLE_SUCCESS;
    }

    public enum SchemaFilter {
        BY_MOD,
        BY_RECIPE,
        FULL
    }

}

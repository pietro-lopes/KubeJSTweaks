package dev.uncandango.kubejstweaks.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.JsonOps;
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
import net.neoforged.neoforge.common.conditions.ConditionContext;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.server.command.EnumArgument;
import net.neoforged.neoforge.server.command.ModIdArgument;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KJSTCommands {

    public static void registerClientCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
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
        );
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

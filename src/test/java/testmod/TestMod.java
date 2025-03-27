package testmod;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;
import net.neoforged.testframework.conf.ClientConfiguration;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.conf.MissingDescriptionAction;
import net.neoforged.testframework.impl.DefaultMarkdownFileSummaryDumper;
import net.neoforged.testframework.impl.MutableTestFramework;
import net.neoforged.testframework.summary.GitHubActionsStepSummaryDumper;
import net.neoforged.testframework.summary.JUnitSummaryDumper;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;

@Mod("testmod")
public class TestMod {

    public TestMod(IEventBus modEventBus, ModContainer modContainer) {
        if (!FMLEnvironment.production) {
            final MutableTestFramework framework = FrameworkConfiguration.builder(ResourceLocation.fromNamespaceAndPath("kubejstweaks", "tests"))
                .clientConfiguration(() -> ClientConfiguration.builder()
                    .toggleOverlayKey(GLFW.GLFW_KEY_J)
                    .openManagerKey(GLFW.GLFW_KEY_N)
                    .build())
                .enable(Feature.CLIENT_SYNC, Feature.CLIENT_MODIFICATIONS, Feature.TEST_STORE)
                .dumpers(new DefaultMarkdownFileSummaryDumper())
                .onMissingDescription(MissingDescriptionAction.ERROR)
                .build().create();

            framework.init(modEventBus, modContainer);
            NeoForge.EVENT_BUS.addListener((final RegisterCommandsEvent event) -> {
                final LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("tests");
                framework.registerCommands(node);
                event.getDispatcher().register(node);
            });
        }
    }
}

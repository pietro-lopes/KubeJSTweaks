package dev.uncandango.kubejstweaks;

import dev.uncandango.kubejstweaks.command.KJSTCommands;
import dev.uncandango.kubejstweaks.kubejs.schema.CodecParsedListener;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.nio.file.Path;

@Mod(KubeJSTweaks.MODID)
public class KubeJSTweaks {
    public static final String MODID = "kubejstweaks";
    public static final Logger LOGGER = LoggerFactory.getLogger("KubeJS Tweaks");
    private static IEventBus MOD_EVENT_BUS;
    private static final Path LOCAL = FMLPaths.GAMEDIR.get().resolve("local").resolve(MODID);

    public KubeJSTweaks(IEventBus modEventBus, ModContainer modContainer) {
        MOD_EVENT_BUS = modEventBus;
        //modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::loadCompleteSetup);

        // NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(this::registerClientCommands);
        }



//        modEventBus.

        // Load Class for transformers
//        try {
//            this.getClass().getClassLoader().loadClass("com.mojang.serialization.codecs.OptionalFieldCodec");
//        } catch (ClassNotFoundException e) {
//            KubeJSTweaks.LOGGER.error("Failed to load OptionalFieldCodec", e);
//        }

    }

    public static Path getLocal(){
        return LOCAL;
    }

    private void loadCompleteSetup(final FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            try {
                Class<? extends Event> clazz = (Class<Event>) Class.forName("com.mojang.serialization.Decoder$CodecParsedEvent");
                MOD_EVENT_BUS.addListener(clazz, CodecParsedListener::codecParsed);
            } catch (Exception e) {
                KubeJSTweaks.LOGGER.debug("Failed to listen to event of codec parsed", e);
            }
        });
        if (!FMLEnvironment.production) {
            event.enqueueWork(() -> MixinEnvironment.getCurrentEnvironment().audit());
        }
    }

//    private void commonSetup(final FMLCommonSetupEvent event) {
//        // Some common setup code
//    }

    public void registerClientCommands(RegisterClientCommandsEvent event){
        KJSTCommands.registerClientCommands(event.getDispatcher(), event.getBuildContext());
    }

//    @SubscribeEvent
//    public void onServerStarting(ServerStartingEvent event) {
//        // Do something when the server starts
//        LOGGER.info("HELLO from server starting");
//    }
//
//    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
//    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
//    public static class ClientModEvents {
//        @SubscribeEvent
//        public static void onClientSetup(FMLClientSetupEvent event) {
//            // Some client setup code
//            LOGGER.info("HELLO FROM CLIENT SETUP");
//            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
//        }
//    }
}

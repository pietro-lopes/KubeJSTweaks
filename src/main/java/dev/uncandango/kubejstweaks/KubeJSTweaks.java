package dev.uncandango.kubejstweaks;

import dev.uncandango.kubejstweaks.kubejs.schema.CodecParsedListener;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileParser;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.throwables.ClassAlreadyLoadedException;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.perf.Profiler;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

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
//        if (FMLEnvironment.dist.isClient()) {
//            NeoForge.EVENT_BUS.addListener(this::registerClientCommands);
//        }



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
            event.enqueueWork(KubeJSTweaks::auditMyMixinsOnly);
        }
    }

    private static void auditMyMixinsOnly(){
        try {
            Class<?> MIXIN_CONFIG_CLASS = ModFileParser.MixinConfig.class;
            MethodHandle unhandledMixinsMH = MethodHandles.lookup().unreflect(ObfuscationReflectionHelper.findMethod(MIXIN_CONFIG_CLASS, "getUnhandledTargets", Set.class));
            var configMain = org.spongepowered.asm.mixin.transformer.Config.create("kubejstweaks.mixins.json", MixinEnvironment.getCurrentEnvironment(), null);
            var kjs72Loaded = LoadingModList.get().getModFileById("kubejs").versionString().startsWith("2101.7.2-");
            var configSpecific = kjs72Loaded ? org.spongepowered.asm.mixin.transformer.Config.create("kubejstweaks-kjs72.mixins.json", MixinEnvironment.getCurrentEnvironment(), null) : org.spongepowered.asm.mixin.transformer.Config.create("kubejstweaks-kjs71.mixins.json", MixinEnvironment.getCurrentEnvironment(), null);
            Set<String> unhandled = (Set<String>) unhandledMixinsMH.invoke(configMain.getConfig());
            unhandled.addAll((Set<String>) unhandledMixinsMH.invoke(configSpecific.getConfig()));
            var unhandledCopy = new HashSet<>(unhandled);
            ILogger auditLogger = MixinService.getService().getLogger("mixin.audit");

            for (String target : unhandledCopy) {
                try {
                    auditLogger.info("Force-loading class {}", target);
                    MixinService.getService().getClassProvider().findClass(target, true);
                } catch (ClassNotFoundException ex) {
                    auditLogger.error("Could not force-load " + target, ex);
                }
            }

            for (String target : (Set<String>) unhandledMixinsMH.invoke(configMain.getConfig())) {
                ClassAlreadyLoadedException ex = new ClassAlreadyLoadedException(target + " was already classloaded");
                auditLogger.error("Could not force-load " + target, ex);
            }

            for (String target : (Set<String>) unhandledMixinsMH.invoke(configSpecific.getConfig())) {
                ClassAlreadyLoadedException ex = new ClassAlreadyLoadedException(target + " was already classloaded");
                auditLogger.error("Could not force-load " + target, ex);
            }

            if (MixinEnvironment.getCurrentEnvironment().getOption(MixinEnvironment.Option.DEBUG_PROFILER)) {
                Profiler.printAuditSummary();
            }

        } catch (Throwable e) {
            LOGGER.error("Error while auditing mixins", e);
        }
    }

//    private void commonSetup(final FMLCommonSetupEvent event) {
//        // Some common setup code
//    }

//    public void registerClientCommands(RegisterClientCommandsEvent event){
//        KJSTCommands.registerClientCommands(event.getDispatcher(), event.getBuildContext());
//    }

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

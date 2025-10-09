package dev.uncandango.kubejstweaks;

import net.minecraft.SharedConstants;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.nio.file.Path;

@Mod(KubeJSTweaksKJS71.MODID)
public class KubeJSTweaksKJS71 {
    public static final String MODID = "kubejstweaks";
    public static final Logger LOGGER = LoggerFactory.getLogger("KubeJS Tweaks");
    private static IEventBus MOD_EVENT_BUS;
    private static final Path LOCAL = FMLPaths.GAMEDIR.get().resolve("local").resolve(MODID);

    public KubeJSTweaksKJS71(IEventBus modEventBus, ModContainer modContainer) {
        MOD_EVENT_BUS = modEventBus;
        //modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::loadCompleteSetup);

        // NeoForge.EVENT_BUS.register(this);
    }

    public static Path getLocal(){
        return LOCAL;
    }

    private void loadCompleteSetup(final FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            try {
                Class<? extends Event> clazz = (Class<Event>) Class.forName("com.mojang.serialization.Decoder$CodecParsedEvent");
            } catch (Throwable e) {
                KubeJSTweaksKJS71.LOGGER.debug("Failed to listen to event of codec parsed", e);
            }
        });
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            event.enqueueWork(() -> MixinEnvironment.getCurrentEnvironment().audit());
        }
    }

}

package dev.uncandango.kubejstweaks.event;

import dev.uncandango.kubejstweaks.KubeJSTweaksKJS71;
import dev.uncandango.kubejstweaks.kubejs.kjs71.debug.DumpErroringRecipes;
import dev.uncandango.kubejstweaks.kubejs.kjs71.plugin.KJSTPluginUtils;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

@EventBusSubscriber(value = Dist.CLIENT, modid = KubeJSTweaksKJS71.MODID)
public class ClientEvents {
    public static final Deque<Component> MESSAGES = new ArrayDeque<>();

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event){
        KJSTPluginUtils.CLIENT_PACK_RESOURCES = null;
    }

    @SubscribeEvent
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event){
        while (!MESSAGES.isEmpty()) {
            var message = MESSAGES.poll();
            event.getPlayer().sendSystemMessage(message);
        }
    }

    @SubscribeEvent
    public static void onDataPackSync(OnDatapackSyncEvent event){
        if (DumpErroringRecipes.isEnabled()) {
            KubeJSTweaksKJS71.LOGGER.info("Dumping erroring recipes!");
            try {
                DumpErroringRecipes.dumpToJsonFiles();
            } catch (IOException e) {
                KubeJSTweaksKJS71.LOGGER.error("Error while dumping json files", e);
            }
        }
        DumpErroringRecipes.disable();
    }

}

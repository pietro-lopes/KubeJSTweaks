package dev.uncandango.kubejstweaks.kubejs.event;

import dev.uncandango.kubejstweaks.KubeJSTweaks;
import dev.uncandango.kubejstweaks.kubejs.plugin.KJSTPluginUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = KubeJSTweaks.MODID)
public class ClientEvents {
    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event){
        KJSTPluginUtils.CLIENT_PACK_RESOURCES = null;
    }
}

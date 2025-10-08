package dev.uncandango.kubejstweaks.mixin.core.main;

import net.mehvahdjukaar.moonlight.api.platform.neoforge.PlatHelperImpl;
import net.neoforged.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PlatHelperImpl.class)
public class PlatHelperImplMixin {
    /**
     * @author Uncandango
     * @reason Use FMLEnvironment
     */
    @Overwrite
    public static boolean isDev() {
        return !FMLEnvironment.production;
    }
}

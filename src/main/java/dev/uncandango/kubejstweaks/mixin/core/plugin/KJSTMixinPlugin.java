package dev.uncandango.kubejstweaks.mixin.core.plugin;

import com.bawnorton.mixinsquared.adjuster.MixinAnnotationAdjusterRegistrar;
import com.bawnorton.mixinsquared.canceller.MixinCancellerRegistrar;
import dev.uncandango.kubejstweaks.mixin.ConditionalMixinManager;
import dev.uncandango.kubejstweaks.mixin.asm.ListRecipeComponentTransformers;
import dev.uncandango.kubejstweaks.mixin_sq.adjuster.KJSTMixinAdjuster;
import dev.uncandango.kubejstweaks.mixin_sq.canceller.KJSTMixinCanceller;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class KJSTMixinPlugin implements IMixinConfigPlugin {

    private ConditionalMixinManager mixinManager;

    @Override
    public void onLoad(String mixinPackage) {
        mixinManager = new ConditionalMixinManager();
        // unused
        //MixinAnnotationAdjusterRegistrar.register(new KJSTMixinAdjuster());
        //MixinCancellerRegistrar.register(new KJSTMixinCanceller());
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return mixinManager.shouldLoad(mixinClassName);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (targetClassName.equals(ListRecipeComponentTransformers.TARGET_CLASS.replaceAll("/","."))) {
            ListRecipeComponentTransformers.transform(targetClass);
            // For Debug
            // Utils.saveClassToDisk(targetClass, FMLPaths.GAMEDIR.get().resolve("local/asm/" + targetClassName + ".class").toString());
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}

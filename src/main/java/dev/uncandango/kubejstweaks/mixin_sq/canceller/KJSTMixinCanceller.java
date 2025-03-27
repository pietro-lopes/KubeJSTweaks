package dev.uncandango.kubejstweaks.mixin_sq.canceller;

import com.bawnorton.mixinsquared.api.MixinCanceller;

import java.util.List;

public class KJSTMixinCanceller implements MixinCanceller {

    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        if (mixinClassName.equals("com.simibubi.create.foundation.mixin.SmithingTrimRecipeMixin")){
            return true;
        }
        return false;
    }
}

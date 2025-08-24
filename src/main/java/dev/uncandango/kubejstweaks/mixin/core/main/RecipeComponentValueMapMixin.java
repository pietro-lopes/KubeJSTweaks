package dev.uncandango.kubejstweaks.mixin.core.main;

import com.google.common.collect.ImmutableSortedSet;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentValueMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Set;

@Mixin(RecipeComponentValueMap.class)
public class RecipeComponentValueMapMixin {
    @Redirect(method = "entrySet", at = @At(value = "INVOKE", target = "Ljava/util/Set;of([Ljava/lang/Object;)Ljava/util/Set;"))
    private Set<Object> keepOrder(Object[] set){
        return ImmutableSortedSet.copyOf(List.of(set));
    }
}

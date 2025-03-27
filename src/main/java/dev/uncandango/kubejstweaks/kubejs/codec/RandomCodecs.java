package dev.uncandango.kubejstweaks.kubejs.codec;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.state.BlockState;
import thedarkcolour.exdeorum.recipe.RecipeUtil;
import thedarkcolour.exdeorum.recipe.WeightedList;

public class RandomCodecs {
    public static final Codec<WeightedList<BlockState>> WEIGHTED_LIST_BLOCKPREDICATE_CODEC = WeightedList.codec(Codec.STRING.xmap(RecipeUtil::parseBlockState, RecipeUtil::writeBlockState));
}

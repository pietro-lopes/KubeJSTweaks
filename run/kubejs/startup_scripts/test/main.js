// Visit the wiki for more info - https://kubejs.com/
console.info('Hello, World! (Loaded startup example script)')

let $Item$Properties = Java.loadClass("net.minecraft.world.item.Item$Properties");
let $FlintAndSteelItem = Java.loadClass("net.minecraft.world.item.FlintAndSteelItem");

StartupEvents.registry("item", event => {
    /*
    event.createCustom("kubejs:test_garget", () => new $FlintAndSteelItem(new $Item$Properties().durability(64).stacksTo(1)));
    event.create("kubejs:item_test").color("#225566").burnTime(60)
    */
});

let $WeightedList = Java.loadClass("thedarkcolour.exdeorum.recipe.WeightedList")
let $Codec = Java.loadClass("com.mojang.serialization.Codec")
let $RecipeUtil = Java.loadClass("thedarkcolour.exdeorum.recipe.RecipeUtil")

let $ClocheRenderFunction =  Java.loadClass("blusunrize.immersiveengineering.api.crafting.ClocheRenderFunction")

KubeJSTweaks.schema(event => {
    // Java for Reference: 
    // WeightedList.codec(Codec.STRING.xmap(RecipeUtil::parseBlockState, RecipeUtil::writeBlockState))
    let codec = $WeightedList.codec($Codec.STRING.xmap(str => $RecipeUtil.parseBlockState(str), state => $RecipeUtil.writeBlockState(state)))

    event.registerCodec("thedarkcolour.exdeorum.recipe.WeightedList,net.minecraft.world.level.block.state.BlockState", codec)

    let ieCodec = $ClocheRenderFunction.CODECS.codec()
    event.registerCodec("blusunrize.immersiveengineering.api.crafting.ClocheRenderFunction", codec)
})
// Visit the wiki for more info - https://kubejs.com/
console.info('Hello, World! (Loaded startup example script)')

let $Item$Properties = Java.loadClass("net.minecraft.world.item.Item$Properties");
let $BlockBehaviour$Properties = Java.loadClass('net.minecraft.world.level.block.state.BlockBehaviour$Properties');
let $AmethystClusterBlock = Java.loadClass('net.minecraft.world.level.block.AmethystClusterBlock');

let testBlock
StartupEvents.registry("block", event => {
    testBlock = event.createCustom("kubejs:testblock", () => new $AmethystClusterBlock(7, 3, $BlockBehaviour$Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER)));
});

let $BlockItem = Java.loadClass("net.minecraft.world.item.BlockItem")

StartupEvents.registry("item", event => {
    event.createCustom("kubejs:testblockitem", () => new $BlockItem(testBlock.get(), new $Item$Properties()));
});

let $FlintAndSteelItem = Java.loadClass("net.minecraft.world.item.FlintAndSteelItem");

StartupEvents.registry("item", event => {
    event.createCustom("kubejs:test_garget", () => new $FlintAndSteelItem(new $Item$Properties().durability(64).stacksTo(1)));
    event.create("kubejs:item_test").color("#225566").burnTime(60)
});

KubeJSTweaks.checkCompatibility(event => {
  event.checkModVersion("jei", "19.22.0.316", "This version is causing durability tools issues")
  event.checkModVersion("uranus", "[2.3.1-bugfix1,2.3.1-bugfix2]", "This version is causing TPS and memory leak issues")
  event.checkModVersion("octolib", "0.6.0.2", "This version is causing high CPU usage issues")
  event.checkModVersion("utilitarian", "1.21.1-0.15.0", "This version is crashing when bonemealing small flowers")
  event.checkModVersion("amendments", "1.21-2.0.4", "This version is crashing when opening lecterns")

  event.checkModLoaded("accessories_compat_layer", "This mod can cause Curio compatibility issues with other mods")
  event.checkModLoaded("letmedespawn", "This mod can cause same items to not stack due to NBT issues")

})

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

// at startup scripts
StartupEvents.registry("fluid", event => {
    let thickFluid = event.create("kubejs:jelly", "thick")
      .tint(0xFF0000)

    thickFluid.displayName("Test 123")
    thickFluid.block.displayName("Test 123")
    thickFluid.fluidType.displayName("Test 123")
    thickFluid.bucketItem.displayName("Test 123 Bucket")
    thickFluid.flowingFluid.displayName("Flowing Test 123")
})

global.flux =  [
    {
        clickedBlock: 'minecraft:obsidian',
        baseBlock:    'allthecompressed:obsidian_1x',
        inputItem:    'minecraft:redstone',
        outputItem:   'fluxnetworks:flux_dust',
        resultBlock:  'minecraft:cobblestone'
    }
];

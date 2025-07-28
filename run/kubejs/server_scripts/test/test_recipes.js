// priority 0

let $Stream = Java.loadClass("java.util.stream.Stream")

let $ItemStack = Java.loadClass("net.minecraft.world.item.ItemStack")

// Utils.getRandom() returns a RandomSource and I can't use on
// randomOf now, thanks Lat
let $Random = Java.loadClass("java.util.Random")
let $BuiltInRegistries = Java.loadClass("net.minecraft.core.registries.BuiltInRegistries")

// to use in `instanceof`
let $Ingredient = Java.loadClass("net.minecraft.world.item.crafting.Ingredient")
let $ItemStackKJS = Java.loadClass("dev.latvian.mods.kubejs.core.ItemStackKJS")
let $List = Java.loadClass("java.util.List")
let $Fluid = Java.loadClass("net.minecraft.world.level.material.Fluid")
let $FluidIngredient = Java.loadClass("net.neoforged.neoforge.fluids.crafting.FluidIngredient")
let $JsonInstance = Java.loadClass("com.mojang.serialization.JsonOps").INSTANCE
let $KubeJSTweaks = Java.loadClass("dev.uncandango.kubejstweaks.KubeJSTweaks")
let $SizedFluidIngredient = Java.loadClass("net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient")
let $WeightedList = Java.loadClass("thedarkcolour.exdeorum.recipe.WeightedList")
let $DataResult$Success = Java.loadClass("com.mojang.serialization.DataResult$Success")
let $Value = Java.loadClass("dev.latvian.mods.kubejs.recipe.component.RecipeComponentBuilder$Value")
let $Objects = Java.loadClass("java.util.Objects")
let $TinyMap = Java.loadClass("dev.latvian.mods.kubejs.util.TinyMap")
let $IntStream = Java.loadClass("java.util.stream.IntStream")

ServerEvents.recipes(event => {
  const random = new $Random()
  const itemList = Item.list.filter(item => !["minecraft:barrier", "minecraft:air", "ae2:facade"].includes(item.id))
  const fluidList = Registry.of("minecraft:fluid").registry().holders().map(holder => holder.key().location().toString()).toList().filter(fluid => !["minecraft:empty"].includes(fluid))
  const blockList = Registry.of("minecraft:block").registry().holders().map(holder => holder.key().location().toString()).toList().filter(block => !["minecraft:air","minecraft:cave_air","minecraft:barrier","ae2:facade"].includes(block))
  const rList = (list) => Utils.randomOf(random, list)
  const genRandomItem = () => rList(itemList).id
  let itemTagList = Registry.of("minecraft:item").registry().getTags()
    .filter(pair => pair.getSecond().size() > 0)
    .map(pair => pair.getFirst().location().toString())
    .toList()
  // Do /reload to populate those
  let fluidTagList = Registry.of("minecraft:fluid").registry().getTags()
    .filter(pair => pair.getSecond().size() > 0)
    .map(pair => pair.getFirst().location().toString())
    .toList()
  const genRandomNonEmptyItemTag = () => "#" + rList(itemTagList)
  const genRandomNonEmptyFluidTag = () => rList(fluidTagList)
  const ri = genRandomItem
  const rit = genRandomNonEmptyItemTag
  const genRandomItemOrTag = () => rList([ri(), rit()])
  const riot = genRandomItemOrTag
  const runFor = (number, func) => Array.from(Array(number).keys()).forEach((val, idx) => func(idx, val, number))
  const rRange = (min, max) => Utils.getRandom().nextInt(min, max + 1)
  const genRandomFluid = () => rList(fluidList)
  const rf = genRandomFluid
  const rft = genRandomNonEmptyFluidTag
  // Fix this, I think Lat changed fluistack parsing
  const rfs = () => `${rRange(1, 1000)}x ${rf()}`
  const genRandomBoolean = () => Utils.getRandom().nextBoolean()
  const rb = genRandomBoolean
  const genArray = (size, func, args) => new Array(size).fill().map(v => func.apply(null, args));
  const rChance = () => Utils.getRandom().nextFloat()
  const genRandomBlock = () => rList(blockList)
  const rBlock = genRandomBlock
  const rFluidState = () => $Stream.generate(() => rf()).map(rf => Fluid.of(rf).fluid.stateDefinition.possibleStates).filter(fs => fs.length > 0).map(fs2 => fs2[rRange(0,fs2.length - 1)]).map(fs3 => fs3.createLegacyBlock()).filter((/** @type {$BlockState_} */ bs) => !bs.isAir()).findFirst().get()

  /* Testing if random generators are working properly
  Array(10).fill(0).forEach(_ => console.log(rChance()))
  Array(10).fill(0).forEach(_ => console.log(rb()))
  Array(10).fill(0).forEach(_ => console.log(rft()))
  Array(10).fill(0).forEach(_ => console.log(rfs()))
  Array(10).fill(0).forEach(_ => console.log(rf()))
  Array(10).fill(0).forEach(_ => console.log(rRange(1, 1000)))
  Array(10).fill(0).forEach(_ => console.log(riot()))
  Array(10).fill(0).forEach(_ => console.log(rit()))
  Array(10).fill(0).forEach(_ => console.log(ri()))
  */

  // Oritech
  const recipeTypes = [
    ["assembler", 4, 1],
    ["atomic_forge", 3, 1],
    ["bio_generator", 1, 0],
    ["centrifuge_fluid", 1, 2],
    ["centrifuge", 1, 2],
    ["cooler", 0, 1],
    ["deep_drill", 1, 1],
    ["foundry", 2, 1],
    ["fuel_generator", 1, 1],
    ["grinder", 1, 3],
    ["laser", 1, 1],
    ["lava_generator", 1, 1],
    ["particle_collision", 2, 1],
    ["pulverizer", 1, 2],
    ["reactor", 1, 0],
    ["steam_engine", 0, 0]
  ]

  const oritech = event.recipes.oritech

  recipeTypes.forEach(type => {
    let counter = 0
    let exactValuesRecipe = ["particle_collision"]
    // if (type[1] == 0 || type[2] == 0) return
    let exactValue = false
    if (exactValuesRecipe.indexOf(type[0]) > -1) exactValue = true
    runFor(5, (idx) => {
      oritech[type[0]](type[2] == 0 && !exactValue ? []: genArray(rRange(exactValue ? type[2] : 1, type[2]), ri), type[1] == 0 && !exactValue ? []: genArray(rRange(exactValue ? type[1] : 1, type[1]), riot))
        .id("atmindev:" + type[0] + "/test/" + counter++)
      oritech[type[0]](type[2] == 0 && !exactValue ? []: genArray(rRange(exactValue ? type[2] : 1, type[2]), ri), type[1] == 0 && !exactValue ? []: genArray(rRange(exactValue ? type[1] : 1, type[1]), riot), rRange(20, 200))
        .id("atmindev:" + type[0] + "/test/" + counter++)
      oritech[type[0]](type[2] == 0 && !exactValue ? []: genArray(rRange(exactValue ? type[2] : 1, type[2]), ri), type[1] == 0 && !exactValue ? []: genArray(rRange(exactValue ? type[1] : 1, type[1]), riot), rRange(20, 200), rf(), rRange(1, 1000) * 81)
        .id("atmindev:" + type[0] + "/test/" + counter++)
      oritech[type[0]](type[2] == 0 && !exactValue ? []: genArray(rRange(exactValue ? type[2] : 1, type[2]), ri), type[1] == 0 && !exactValue ? []: genArray(rRange(exactValue ? type[1] : 1, type[1]), riot), rRange(20, 200), rf(), rRange(1, 1000) * 81, rf(), rRange(1, 1000) * 81)
        .id("atmindev:" + type[0] + "/test/" + counter++)
    })
  })

  // Needs to be on a second pass
  recipeTypes.forEach(type => {
    // if (type[1] == 0 || type[2] == 0) return
    testRecipes(event, new RegExp(`atmindev:${type[0]}/test/`))
  })

  const exdeorum = event.recipes.exdeorum

  exdeorum.hammer("minecraft:dirt", "minecraft:apple", { type: "binomial", p: 0.25, n: 20 })

  testRecipes(event, new RegExp(`exdeorum:.*/`))

  exdeorum.crucible_heat_source({block_tag: "c:stones"}, 1)
  exdeorum.crucible_heat_source({block: "acacia_log", state: {axis: "x"}}, 1)

  let counter = 0
  runFor(5, (idx) => {
    event.recipes.actuallyadditions.crushing(riot(),[{stack: ri(), chance: rChance()}, {stack: "air", chance: 0}])
      .id("atmindev:crushing/test/" + counter++)

    event.recipes.actuallyadditions.crushing(riot(),[{stack: ri(), chance: rChance()},{stack: ri(), chance: rChance()}])
      .id("atmindev:crushing/test/" + counter++)
  })

  event.recipes.ae2.inscriber("acacia_boat",{bottom: "#actuallyadditions:crystals", top: "#actuallyadditions:lamps", middle: "#ae2:all_fluix"},"press")

  event.recipes.ae2.inscriber("oak_trapdoor",{bottom: "minecraft:emerald", top: "minecraft:grass_block", middle: "minecraft:oak_log"}) // will be a inscribe

  event.recipes.ae2.inscriber("oak_door",{bottom: "minecraft:diamond", top: "minecraft:dirt", middle: "minecraft:stick"}).mode("inscribe")

  event.recipes.ae2.inscriber("oak_door",["minecraft:diamond", "minecraft:dirt",  "minecraft:stick"]).mode("inscribe")

  event.replaceInput({type: "ae2:inscriber"}, Ingredient.of("#c:silicon"), Ingredient.of("minecraft:gold_block"))

  event.forEachRecipe({type: "actuallyadditions:crushing"}, debugRecipe)
  
  testRecipes(event, new RegExp(`actuallyadditions:crushing/.*`))
  testRecipes(event, new RegExp(`atmindev:crushing/test/.*`))
  testRecipes(event, new RegExp(`ae2:inscriber/.*`))
  
  counter = 0
  runFor(5, (idx) => {
    event.recipes.ars_nouveau.enchanting_apparatus(riot(),ri(),genArray(rRange(1,8), riot),rRange(0,200), rb())
      .id("atmindev:ars_nouveau/enchanting_apparatus/test/" + counter++)
  })
  testRecipes(event, new RegExp(`atmindev:ars_nouveau/enchanting_apparatus/test/.*`))

  testRecipes(event, "*", "ars_nouveau:enchanting_apparatus")

  testRecipes(event, "*", "bigreactors:fluidizersolid")
  testRecipes(event, "*", "bigreactors:fluidizersolidmixing")

  testRecipes(event, "*", "create:mechanical_crafting")

  testRecipes(event, "*", "enderio:sag_milling")

  counter = 0
  runFor(15, (idx) => {
    event.recipes.enderio.sag_milling(riot(),[{item: ri()}], rRange(1000,10000))
      .id("atmindev:enderio/sag_milling/test/" + counter++)
    event.recipes.enderio.sag_milling(riot(),[{item: {tag: rit().replace("#", ""), count: rRange(1,10)}}], rRange(1000,10000))
      .id("atmindev:enderio/sag_milling/test/" + counter++)
    event.recipes.enderio.sag_milling(riot(),[{item: ri(), chance: rChance()},{item: {tag: rit().replace("#", ""), count: rRange(1,10)}}], rRange(1000,10000), rList(["chance_only","none"]))
      .id("atmindev:enderio/sag_milling/test/" + counter++)
    event.recipes.enderio.sag_milling(riot(),[{item: ri(), chance: rChance()},{item: {tag: rit().replace("#", ""), count: rRange(1,10)}}, {item: ri()}], rRange(1000,10000), rList(["chance_only","none"]))
      .id("atmindev:enderio/sag_milling/test/" + counter++)
    event.recipes.enderio.sag_milling(riot(),[{item: ri(), chance: rChance()},{item: {tag: rit().replace("#", ""), count: rRange(1,10)}}, {item: ri()}, {item: {tag: rit().replace("#", ""), count: rRange(1,10)}}], rRange(1000,10000), rList(["chance_only","none"]))
      .id("atmindev:enderio/sag_milling/test/" + counter++)
  })
  testRecipes(event, new RegExp(`atmindev:enderio/sag_milling/test/.*`))
  
  testRecipes(event, "*", "extendedae:circuit_cutter")

  testRecipes(event, "*", "extendedae:crystal_assembler")

  counter = 0
  runFor(15, (idx) => {
    event.recipes.extendedae.circuit_cutter(ri(),{ingredient: riot()})
      .id("atmindev:extendedae/circuit_cutter/test/" + counter++)
    event.recipes.extendedae.circuit_cutter(ri(),{ingredient: riot(), amount: rRange(2,10)})
      .id("atmindev:extendedae/circuit_cutter/test/" + counter++)
    event.recipes.extendedae.crystal_assembler(ri(),[{ingredient: riot(), amount: rRange(2,10)}])
      .id("atmindev:extendedae/crystal_assembler/test/" + counter++)
    event.recipes.extendedae.crystal_assembler(ri(),[{ingredient: riot(), amount: rRange(2,10)},{ingredient: riot(), amount: rRange(2,10)}],{ingredient: rf(), amount: rRange(1,5000)})
      .id("atmindev:extendedae/crystal_assembler/test/" + counter++)
    event.recipes.extendedae.crystal_assembler(ri(),[{ingredient: riot(), amount: rRange(2,10)},{ingredient: riot(), amount: rRange(2,10)},{ingredient: riot(), amount: rRange(2,10)},{ingredient: riot(), amount: rRange(2,10)},{ingredient: riot(), amount: rRange(2,10)},{ingredient: riot(), amount: rRange(2,10)},{ingredient: riot(), amount: rRange(2,10)},{ingredient: riot(), amount: rRange(2,10)},{ingredient: riot(), amount: rRange(2,10)}],{ingredient: "#"+rft(), amount: rRange(2,5000)})
      .id("atmindev:extendedae/crystal_assembler/test/" + counter++)
  })

  testRecipes(event, new RegExp(`atmindev:extendedae/circuit_cutter/test/.*`))
  testRecipes(event, new RegExp(`atmindev:extendedae/crystal_assembler/test/.*`))

  testRecipes(event, "*", "farmingforblockheads:market")

  counter = 0
  runFor(5, (idx) => {
    event.recipes.farmingforblockheads.market({item: ri(), count: 1}, "farmingforblockheads:test", "atmindev:test", {ingredient: riot(), count: rRange(2,10), tooltip: Text.red("Testing")})
      .id("atmindev:farmingforblockheads/market/test/" + counter++)
    let is = Item.of('minecraft:splash_potion[potion_contents={potion:"minecraft:strength"}]')
    event.recipes.farmingforblockheads.market({item: is.id, count: rRange(2,10), components: is.getComponentsPatch()}, "farmingforblockheads:test", "atmindev:test", {ingredient: riot(), count: rRange(2,10), tooltip: Text.yellow("Testing2")}, ri())
      .id("atmindev:farmingforblockheads/market/test/" + counter++)
  })

  testRecipes(event, new RegExp(`atmindev:farmingforblockheads/market/test/.*`))

  testRecipes(event, "*", "functionalstorage:custom_compacting")

  counter = 0
  runFor(10, (idx) => {
    event.recipes.functionalstorage.custom_compacting(ri(),ri())
      .id("atmindev:functionalstorage/custom_compacting/test/" + counter++)
  })

  testRecipes(event, new RegExp(`atmindev:functionalstorage/custom_compacting/test/.*`))

  testRecipes(event, "*", "immersiveengineering:arc_furnace")

  event.custom(
    {
      type: "immersiveengineering:arc_furnace",
      additives: [],
      energy: 51200,
      input: Ingredient.of({item: "immersiveengineering:sheetmetal_copper"}).toJson(),
      results: [
      {
        basePredicate: Ingredient.of("minecraft:copper_block").toJson(),
        count: 1
      }
      ],
      slag: Ingredient.of("#c:slag").toJson(),
      time: 100
  }).id("atmindev:immersiveengineering/arc_furnace/test/999")

  counter = 0
  runFor(10, (idx) => {
    event.recipes.immersiveengineering.arc_furnace([{basePredicate: riot(), count: rRange(2,10)}], rRange(20,400),rRange(10000,50000), {basePredicate: riot(), count: rRange(2,10)})
      .id("atmindev:immersiveengineering/arc_furnace/test/" + counter++)
    event.recipes.immersiveengineering.arc_furnace([Item.of(ri()).copyWithCount(rRange(2,10)),{basePredicate: riot(), count: rRange(2,10)}], rRange(20,400),rRange(10000,50000), riot(), [{basePredicate: riot(), count: rRange(2,10)}])
      .id("atmindev:immersiveengineering/arc_furnace/test/" + counter++)
    event.recipes.immersiveengineering.arc_furnace([Item.of(ri()).copyWithCount(rRange(2,10)),{basePredicate: riot(), count: rRange(2,10)}, riot()], rRange(20,400),rRange(10000,50000), riot(), riot(), [{basePredicate: riot(), count: rRange(2,10)}, riot()],[{output: riot(), chance: rChance()}])
      .id("atmindev:immersiveengineering/arc_furnace/test/" + counter++)
    event.recipes.immersiveengineering.arc_furnace([riot(),riot(),riot(),riot()], rRange(20,400), rRange(10000,50000), riot(), riot(), [riot(),riot(),riot(),riot()],[{output: riot(), chance: rChance()}], riot())
      .id("atmindev:immersiveengineering/arc_furnace/test/" + counter++)
  })

  testRecipes(event, new RegExp(`atmindev:immersiveengineering/arc_furnace/test/.*`))

  testRecipes(event, "*", "immersiveengineering:cloche")
  testRecipes(event, "*", "immersiveengineering:crusher")
  testRecipes(event, "*", "immersiveengineering:metal_press")

  counter = 0
  runFor(10, (idx) => {
    event.recipes.immersiveengineering.cloche([riot()], riot(), riot(), rRange(20, 200), {type: "immersiveengineering:generic", block: rBlock()})
      .id("atmindev:immersiveengineering/cloche/test/" + counter++)
    event.recipes.immersiveengineering.cloche([riot(),riot(),riot(),riot()], riot(), riot(), rRange(20, 200), {type: "immersiveengineering:generic", block: rBlock()})
      .id("atmindev:immersiveengineering/cloche/test/" + counter++)
  })

  testRecipes(event, new RegExp(`atmindev:immersiveengineering/cloche/test/.*`))

  counter = 0
  runFor(10, (idx) => {
    event.recipes.immersiveengineering.crusher(riot(), riot(),rRange(10000,50000),[{output: riot(), chance: rChance()}])
      .id("atmindev:immersiveengineering/crusher/test/" + counter++)
  })

  testRecipes(event, new RegExp(`atmindev:immersiveengineering/crusher/test/.*`))

  counter = 0
  runFor(10, (idx) => {
    event.recipes.immersiveengineering.metal_press(riot(),riot(),ri(), rRange(10000,50000))
      .id("atmindev:immersiveengineering/metal_press/test/" + counter++)
  })

  testRecipes(event, new RegExp(`atmindev:immersiveengineering/metal_press/test/.*`))

  testRecipes(event, "*", "industrialforegoing:dissolution_chamber")
  testRecipes(event, "*", "industrialforegoing:fluid_extractor")

  counter = 0
  runFor(10, (idx) => {
    event.recipes.industrialforegoing.dissolution_chamber([riot()], rfs(), rRange(20,200))
      .id("atmindev:industrialforegoing/dissolution_chamber/test/" + counter++)
    event.recipes.industrialforegoing.dissolution_chamber([riot(),riot(),riot(),riot(),riot(),riot(),riot(),riot()], rfs(), rRange(20,200), ri(), rfs())
      .id("atmindev:industrialforegoing/dissolution_chamber/test/" + counter++)
  })

  testRecipes(event, new RegExp(`atmindev:industrialforegoing/dissolution_chamber/test/.*`))

  counter = 0
  runFor(10, (idx) => {
    event.recipes.industrialforegoing.fluid_extractor(riot(), Block.getBlock(rBlock()).defaultBlockState(), rChance(), rfs(), rb())
      .id("atmindev:industrialforegoing/fluid_extractor/test/" + counter++)
  })

  testRecipes(event, new RegExp(`atmindev:industrialforegoing/fluid_extractor/test/.*`))

  testRecipes(event, "*", "integrateddynamics:squeezer")
  testRecipes(event, "*", "integrateddynamics:mechanical_squeezer")

  counter = 0
  runFor(10, (idx) => {
    event.recipes.integrateddynamics.squeezer(riot(),[{item: ri(), chance: rChance()}], rfs())
      .id("atmindev:integrateddynamics/squeezer/test/" + counter++)
    event.recipes.integrateddynamics.squeezer(riot(),[{item: ri(), chance: rChance()},{tag: {tag: rit().replace("#", ""), count: rRange(1,10)}, chance: rChance()}], rfs())
      .id("atmindev:integrateddynamics/squeezer/test/" + counter++)
  })

  testRecipes(event, new RegExp(`atmindev:integrateddynamics/squeezer/test/.*`))

  testRecipes(event, "*", "justdirethings:fluiddrop")

  counter = 0
  runFor(10, (idx) => {
    event.recipes.justdirethings.fluiddrop("testing_" + counter, rFluidState(), rFluidState(), ri())
      .id("atmindev:justdirethings/fluiddrop/test/" + counter++)
    // event.recipes.integrateddynamics.squeezer(riot(),[{item: ri(), chance: rChance()},{tag: {tag: rit().replace("#", ""), count: rRange(1,10)}, chance: rChance()}], rfs())
    //   .id("atmindev:integrateddynamics/squeezer/test/" + counter++)
  })

  testRecipes(event, new RegExp(`atmindev:justdirethings/fluiddrop/test/.*`))

})



function testRecipes(event, regex, type) {
  testExistingRecipes(event, regex, type)
  testAddedRecipes(event, regex, type)
}

const testExistingRecipes = (event, regex, type) => {
  console.log("Now running test on Existing Recipes using param: " + regex + " and " + type)
  event.forEachRecipe(type == null ? { id: regex} :  {type: type}, debugRecipe)
}

const testAddedRecipes = (event, regex, type) => {
  console.log("Now running test on Added Recipes using param: " + regex + " and " + type)
  event.addedRecipes.stream().filter(recipe => type == null ? regex.test(recipe.getId()) : recipe.type.id == type).forEach(debugRecipe)
}



let currentComponent = null;
const debugRecipe = (/** @type {$KubeRecipe_} */ recipe) => {
  console.log(` === ${recipe.getId()}  === `)
  let valuesMessage = ""
  // Requires AccessTransformer on field because Lat did @HideFromJS
  recipe.valueMap.entrySet().forEach((rcvKey) => {
    let currentClass
    let error = false
    if (recipe.kjs$getSchema().getKey(rcvKey.getKey().name) != null && (!(!rcvKey.getKey().optional() && rcvKey.getValue() == null))) {
      currentClass = rcvKey.getValue() == null ? "null" : KJSTweaks.getClass(rcvKey.getValue()) || rcvKey.getValue().toString()
      currentComponent = rcvKey.getKey().component
      // currentClass = rcvKey.getKey().component.typeInfo()
      let unwrapped = unwrapValue(rcvKey.getValue())
      valuesMessage += `✅ ${rcvKey.getKey()}: ${unwrapped} (${currentClass})` // ${unwrapped instanceof OutputItem ? unwrapped.item.toJson() : unwrapped}`
    } else {
      error = true
      valuesMessage += `❌ ${rcvKey.getKey()}: null`
    }
    console.log(valuesMessage)
    valuesMessage = ""
  })
}

function unwrapEither(/** @type { Internal.Either } */ value) {
  return value.left().orElseGet(() => value.right().get())
}

function unwrapMapBuilder(/** @type { Internal.RecipeComponentBuilderMap } */ mapBuilder) {
  let map = Utils.newMap()
  mapBuilder.entrySet().forEach((entry) => {
    let val = unwrapValue(entry.getValue())
    let key = entry.getKey().name
    map.put(key, val)
  })
  return map
}

function unwrapArray(/** @type { Internal.ArrayList } */ array) {
  let changed = false
  let newArray = Utils.newList()
  array.forEach((element) => {
    let newElement = unwrapValue(element)
    if (!$Objects.equals(newElement, element)) {
      changed = true
    }
    newArray.push(newElement)
  })
  return changed ? newArray : array
}

let $String = Java.loadClass("java.lang.String")

function unwrapValue(value) {
  if (value == null) return
  // if (value.getClass == null) return value
  let newValue = value
  if (KJSTweaks.getSuperclass(KJSTweaks.getClass(value)).simpleName == "Either") {
    newValue = unwrapEither(value)
  } else if (KJSTweaks.getClass(value).simpleName == "RecipeComponentBuilderMap") {
    newValue = currentComponent.codec().encodeStart($JsonInstance, value).getOrThrow()
    // newValue = unwrapMapBuilder(value)
  } else if (KJSTweaks.getClass(value).isArray() || value instanceof $List) {
    newValue = unwrapArray(value)
  } else if (KJSTweaks.getClass(value).isEnum()) {
    newValue = value.toString().toLowerCase()
  } else if (KJSTweaks.getClass(value).simpleName == "HashMap" || KJSTweaks.getClass(value).simpleName == "LinkedHashMap") {
    newValue = unwrapHashMap(value)
  } else if (value instanceof $Value) {
    newValue = value.getValue()
  }
  if (value == newValue && KJSTweaks.getClass(value) == KJSTweaks.getClass(newValue)) {
    if (value instanceof $Ingredient) return value.toJson()
    if (value instanceof $ItemStackKJS) return value.toJson()
    if (value instanceof $Fluid) return value
    if (value instanceof $FluidIngredient) {
      return $FluidIngredient.CODEC.encodeStart($JsonInstance, value).getOrThrow()
    }
    if (value instanceof $SizedFluidIngredient) {
      return $SizedFluidIngredient.FLAT_CODEC.encodeStart($JsonInstance, value).getOrThrow()
    }
    if (value instanceof $WeightedList) {
      return currentComponent.codec().encodeStart($JsonInstance, value).getOrThrow()
    }
    if (value instanceof $TinyMap) {
      return currentComponent.codec().encodeStart($JsonInstance, value).getOrThrow()
    }
    return value
  }
  return unwrapValue(newValue)
}

function unwrapHashMap(/** @type { $HashMap_<any,any> } */ mapBuilder) {
  let map = Utils.newMap()
  let replaced = false
  mapBuilder.entrySet().forEach((entry) => {
    let oldVal = entry.getValue()
    let val = unwrapValue(entry.getValue())
    if (oldVal != val) replaced = true
    let key = entry.getKey().name ? entry.getKey().name() : entry.getKey()
    map.put(key, val)
  })
  return replaced ? map : mapBuilder
}

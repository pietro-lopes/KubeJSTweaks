// Visit the wiki for more info - https://kubejs.com/
console.info('Hello, World! (Loaded server example script)')

ItemEvents.rightClicked("minecraft:lodestone", event => {
    /** @type {$ServerLevel_} */
    let level = event.level
    let map = level.structureManager().getAllStructuresAt(event.player.blockPosition())
    map.keySet().forEach(structure => {
        let key = event.level.registryAccess().registryOrThrow($Registries.STRUCTURE).getKey(structure)
        event.player.tell(key)
    })
})

KubeJSTweaks.noOp(event => {
    event.recipes("minecraft:anvil")
    event.lootTables("minecraft:gameplay/fishing/junk")
    event.lootTablesBlock("minecraft:birch_log")
    event.biomeModifiers("mekanism:lead")
    event.json("kubejs:some/random/path", {})
})

ServerEvents.generateData("last", event => {
    let jukeboxSongJson = KJSTweaks.readJsonFromMod("minecraft" , "jukebox_song/13")
    let recipeSchemaStoneCuttingJson = KJSTweaks.readJsonFromMod("kubejs" , "minecraft:kubejs/recipe_schema/stonecutting")

    console.log("Jukebox Song 5: " + jukeboxSongJson)
    console.log("Recipe Schema Stone Cutting: " + recipeSchemaStoneCuttingJson)

    // REMEMBER THAT GETTING ASSETS ON STARTUP/SERVER EVENT IS NOT SUPPORTED ON A DEDICATED SERVER
    let acaciaButtonModelJson = KJSTweaks.readJsonFromMod("minecraft" , "models/block/acacia_button", "assets")
    let generatedBucketJson = KJSTweaks.readJsonFromMod("kubejs" , "models/item/generated_bucket", "assets")

    console.log("Acacia Button Model: " + acaciaButtonModelJson)
    console.log("Generated Bucket: " + generatedBucketJson)
})
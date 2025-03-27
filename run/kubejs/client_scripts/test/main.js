// Visit the wiki for more info - https://kubejs.com/
console.info('Hello, World! (Loaded client example script)')

ClientEvents.generateAssets("last", event => {
    let acaciaButtonModel = KJSTweaks.readJsonFromMod("minecraft" , "models/block/acacia_button")
    let generatedBucket = KJSTweaks.readJsonFromMod("kubejs" , "models/item/generated_bucket")

    console.log("Acacia Model: " + acaciaButtonModel)
    console.log("Generated Bucket: " + generatedBucket)

    let jukeboxSong = KJSTweaks.readJsonFromMod("minecraft" , "jukebox_song/13", "data")
    let recipeSchemaStoneCutting = KJSTweaks.readJsonFromMod("kubejs" , "minecraft:kubejs/recipe_schema/stonecutting", "data")

    console.log("Jukebox Song 5: " + jukeboxSong)
    console.log("Recipe Schema Stone Cutting: " + recipeSchemaStoneCutting)
})
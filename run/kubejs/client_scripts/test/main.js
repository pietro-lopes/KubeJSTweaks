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

let newLang = {
  "fluid.kubejs.jelly": "Jam",
  "fluid_type.kubejs.jelly": "Jam",
  "block.kubejs.jelly": "Jam",
  "fluid.kubejs.flowing_jelly": "Flowing Jam",
  "item.kubejs.jelly_bucket": "Jam Bucket"
}

let newLangByCountry = {
  "en_gb": newLang,
  "en_nz": newLang,
  "en_au": newLang
}

for (let lang in newLangByCountry) {
  ClientEvents.lang(lang, event => {
    event.addAll(newLangByCountry[lang])
  })
}

const $CreatingFluxRecipe = Java.loadClass('sonar.fluxnetworks.client.jei.CreatingFluxRecipe')
const $CreatingFluxRecipeCategory = Java.loadClass('sonar.fluxnetworks.client.jei.CreatingFluxRecipeCategory')
let $JemiRecipe
let $JemiCategory
if (Platform.isLoaded("emi")) {
    $JemiRecipe = Java.loadClass("dev.emi.emi.jemi.JemiRecipe")
    $JemiCategory = Java.loadClass("dev.emi.emi.jemi.JemiCategory")
}

RecipeViewerEvents.addEntries('item', allthemods =>{
    let jeiRuntime = global.jeiRuntime
    let emiRegistry = global.emiRegistry
    if (emiRegistry) {
        let fluxRecipeCategory = global.jeiCategoryRegistration.getRecipeCategories().stream().filter(cat => cat.getRecipeType() == $CreatingFluxRecipeCategory.RECIPE_TYPE).findFist().get()
        console.log("Flux recipe category is:" + fluxRecipeCategory)
    }
    let jeiRecipeCategories = global.jeiCategoryRegistration.getRecipeCategories()
    global.flux.forEach(recipe => {
        let customFlux = new $CreatingFluxRecipe(recipe.baseBlock, recipe.clickedBlock, recipe.inputItem, recipe.outputItem)
        if (emiRegistry) {
            let recipeCategory = new $JemiCategory(fluxRecipeCategory)
            let recipe = new $JemiRecipe(recipeCategory, fluxRecipeCategory, customFlux)
            emiRegistry.addRecipe(recipe)
        }
        if (jeiRuntime) {
            let RecipeManager = jeiRuntime.getRecipeManager()
            RecipeManager.addRecipes($CreatingFluxRecipeCategory.RECIPE_TYPE, [customFlux])
        }
    })
})

let $LaserDrillRarity = Java.loadClass("com.buuz135.industrial.recipe.LaserDrillRarity")

ServerEvents.recipes(event => {
    /*
    event.forEachRecipe({type: "oritech:assembler"}, recipe => {
        console.log(recipe.getId())
        console.log(recipe.get("ingredients"))
        console.log(recipe.get("results"))
    })
    event.forEachRecipe({type: "oritech:centrifuge_fluid"}, recipe => {
        console.log(recipe.getId())
        console.log(recipe.get("fluidInputVariant"))
        console.log(recipe.get("fluidInputAmount"))
        console.log(recipe.get("fluidOutputVariant"))
        console.log(recipe.get("fluidOutputAmount"))
    })
    event.recipes.oritech.centrifuge_fluid(["acacia_boat"], ["#c:animal_foods","#c:bones"], 69,"minecraft:lava",81000,"minecraft:milk", 40500)
    event.recipes.oritech.centrifuge_fluid(["acacia_button"], ["#c:armors"], 42)
    event.recipes.oritech.centrifuge_fluid(["acacia_trapdoor"], ["#minecraft:stairs"])
    
    event.recipes.replication.matter_value("minecraft:apple",[{type: "replication:quantum", amount: 69.0}])
        .id("replication:matter_values/minecraft/items/apple")
    
    event.recipes.replication.matter_value("minecraft:carrot",[{type: "replication:precious", amount: 42.0}])
        .id("replication:matter_values/minecraft/items/carrot")
    
    event.recipes.replication.matter_value("minecraft:chicken",[{type: "replication:ender", amount: 13.0}])
        .id("replication:matter_values/minecraft/items/chicken")

    event.recipes.industrialforegoing.laser_drill_ore("#c:animal_foods","#c:armors",[{biomeRarity: {whitelist: ["c:is_cave"], blacklist: ["c:is_cold"]}, dimensionRarity: {whitelist: ["minecraft:overworld"], blacklist: ["the_end"]}, weight: 10, depth_min: 2, depth_max: 10}])
    // let test = new $LaserDrillRarity({blacklist: [""], whitelist: [""]}, {blacklist: [""], whitelist: [""]},20,20,20)
    */
})

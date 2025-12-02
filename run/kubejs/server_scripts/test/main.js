// Visit the wiki for more info - https://kubejs.com/
console.info('Hello, World! (Loaded server example script)')

//ItemEvents.rightClicked("minecraft:lodestone", event => {
//    /** @type {$ServerLevel_} */
//    let level = event.level
//    let map = level.structureManager().getAllStructuresAt(event.player.blockPosition())
//    map.keySet().forEach(structure => {
//        let key = event.level.registryAccess().registryOrThrow($Registries.STRUCTURE).getKey(structure)
//        event.player.tell(key)
//    })
//})

ItemEvents.rightClicked("minecraft:lodestone", event => {
    /** @type {$ServerLevel_} */
    if (event.hand != "main_hand") return
    let level = event.level
    let list = level.getEntities().filterSelector("@e[type=minecraft:text_display, distance=..64, limit=1, sort=nearest, tag=Player1]")
    for (let entity of list) {
      console.log(entity)
      console.log(entity.getNbt())
    }
})


KubeJSTweaks.noOp(event => {
    event.recipes("minecraft:anvil")
    event.lootTables("minecraft:gameplay/fishing/junk")
    event.lootTablesBlock("minecraft:birch_log")
    event.biomeModifiers("mekanism:lead")
    event.json("kubejs:some/random/path", {})
})

ServerEvents.generateData("last", event => {
    let jukeboxSongJson = KJSTweaks.readJsonFromMod("minecraft", "jukebox_song/13")
    let recipeSchemaStoneCuttingJson = KJSTweaks.readJsonFromMod("kubejs", "minecraft:kubejs/recipe_schema/stonecutting")

    console.log("Jukebox Song 5: " + jukeboxSongJson)
    console.log("Recipe Schema Stone Cutting: " + recipeSchemaStoneCuttingJson)

    // REMEMBER THAT GETTING ASSETS ON STARTUP/SERVER EVENT IS NOT SUPPORTED ON A DEDICATED SERVER
    let acaciaButtonModelJson = KJSTweaks.readJsonFromMod("minecraft", "models/block/acacia_button", "assets")
    let generatedBucketJson = KJSTweaks.readJsonFromMod("kubejs", "models/item/generated_bucket", "assets")

    console.log("Acacia Button Model: " + acaciaButtonModelJson)
    console.log("Generated Bucket: " + generatedBucketJson)
})

KubeJSTweaks.beforeRecipes(event => {
    event.dumpErroringRecipes()

    // Upgrade from forge to neoforge conditions
    event.fixCondition([
      "irons_spellbooks:patchouli_book",
      "apotheosis:book",
      /^silentgear:woodcutting\//,
      "silentgear:sapling/netherwood"
    ])

    // Fix "item" -> "id"
    event.fixItemAtKey([
      /^create:crushing\/gloomslate_/,
      /^create:crushing\/sculk_stone_/,
      /^create:cutting\/.*echo_/
    ], "results")

    // Fix farmer delight tool type that was renamed on 1.21.1
    // And sound also changed
    event.getEntry([/^farmersdelight:cutting\/echo_/,"farmersdelight:integration/silentgear/cutting/netherwood"]).forEach(entry => {
      entry.replaceValueAtKey("tool", "type", "farmersdelight:tool_action", "farmersdelight:item_ability")

      entry.fromPath("sound").ifPresent(result => {
        result.first.add("sound", {sound_id: result.second})
      })

      if (entry.id() == "farmersdelight:integration/silentgear/cutting/netherwood") {
        let resultArray = entry.json().get("result")
        if (resultArray == null) return
        for (let item of resultArray) {
          if (!item.has("item")) continue
          item.add("item", {
            "id": item.get("item")
          })
        }
      }
    })

    event.getEntry(/^createaddition:compat\/immersiveengineering\/rolling/).forEach(entry => {
      entry.renameKey("result", "results", true)
      entry.renameKey("input", "ingredients", true)
    })

    event.getEntry("createaddition:compat/ae2/charged_certus_quartz")
      .forEach(entry => {
        entry.renameKey("result", "results", true)
        entry.renameKey("ingredient", "ingredients", true)
      })

    event.getEntry("createaddition:compat/immersiveengineering/sphalerite")
      .forEach(entry => {
        entry.json().add("biome_predicates", [["minecraft:is_overworld"]])
      })

    event.getEntry(/^farmingforblockheads:market\//).forEach(entry => {
      entry.addConditionsFromKey("result")
    })

    event.getEntry(/^replication:matter_values\/c\/tags\//).forEach(entry => {
      entry.addConditionsFromKey("input")
    })

    event.getEntry("create:crafting/tree_fertilizer")
      .forEach(entry => {
        entry.addConditionsFromKey("ingredients")
      })

    event.getEntry("@silentgear")
      .forEach(entry => {
        let ings = entry.json().get("ingredients")
        let keys = entry.json().get("key")

        if (ings != null) {
          for (let ing of ings) {
            let type = ing.get("type")
            if (type != null) {
              if (type.getAsString() == "silentgear:material") {
                entry.ignoreWarning()
              }
            }
          }
        }

        if (keys != null) {
          for (let key of keys.asMap().values()) {
            let type = key.get("type")
            if (type != null) {
              if (type.getAsString() == "silentgear:material") {
                entry.ignoreWarning()
              }
            }
          }
        }
      })

    event.getEntry(/^deeperdarker:.*_smithing$/)
      .forEach(entry => {
        entry.fromPath("template", "[]").ifPresent(result => entry.ignoreWarning())
      })
})

ServerEvents.recipes(event => {
  event.forEachRecipe({output: "minecraft:acacia_boat"}, event => {})
//  event.remove({input: ["minecraft:iron_ingot"]})
//  event.remove({output: ["minecraft:iron_ingot"]})

  //event.shaped("minecraft:apple", ["XXX","XOX","XXX"], {"X": "minecraft:acacia_boat","O":"minecraft:empty_bucket"})
})

//
//ServerEvents.recipes(event => {
//    let $OutputData = Java.loadClass("dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe$OutputData")
//
//    let test1 = $OutputData["(net.minecraft.world.item.ItemStack,int,int)"](Item.of("dirt"), 1, 20)
//    let test2 = $OutputData["(net.minecraft.world.item.Item,int,int)"](Items.DIRT, 1, 20)
//    console.log(test1)
//    console.log(test2)
//})

// let $StatModifier = Java.loadClass("dev.shadowsoffire.apothic_spawners.modifiers.StatModifier")
// let $Integer = Java.loadClass("java.lang.Integer")

// ServerEvents.recipes(event => {
//     event.recipes.apothic_spawners.spawner_modifier(
//         [["apothic_spawners:spawn_count", $Integer.valueOf("2"), null, null, "add"]],
//         "minecraft:grass_block",
//     ).offhand("minecraft:dirt").consumeOffhand()
// })

// ServerEvents.recipes(event => {
//     event.recipes.apothic_spawners.spawner_modifier(
//         [new $StatModifier("apothic_spawners:spawn_count", $Integer.valueOf("2"), null, null, "add")],
//         "minecraft:grass_block",
//     ).offhand("minecraft:dirt").consumeOffhand()
// })

// ServerEvents.recipes(event => {
//     event.recipes.apothic_spawners.spawner_modifier(
//         [{
//             stat: "apothic_spawners:spawn_count",
//             value: $Integer.valueOf("2"),
//             mode: "add"
//         }],
//         "minecraft:grass_block",
//     ).offhand("minecraft:dirt").consumeOffhand()
// })

PlayerEvents.chat(event => {
  if (event.message == "nukelist reload") {
    event.server.runCommand(`tell @a Now reloading Nukelist scripts`)
    event.server.runCommand(`kubejs reload startup-scripts`)
    event.server.runCommand(`reload`)
    event.server.runCommand(`kubejs reload lang`)
  }
})

//BlockEvents.randomTick("actuallyadditions:black_quartz_ore", event => {
//    console.log(event.block)
//});

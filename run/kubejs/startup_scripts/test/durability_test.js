let $Lib = Java.loadClass("blusunrize.immersiveengineering.api.Lib")

StartupEvents.registry('item',event=> {
    event.create('ie_tiered_sword', 'sword')
      .tier($Lib.MATERIAL_Steel)
})

//StartupEvents.registry('potion',event => {
//    event.create('iron_skin')
//    .effect('kubejs:iron_skin',3600,0)
//})


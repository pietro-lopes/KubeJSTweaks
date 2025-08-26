
StartupEvents.registry('mob_effect',event=> {
    event.create('iron_skin')
    .beneficial()
    .color('#d1bcad')
    .modifyAttribute('minecraft:generic.armor','kubejs:iron_skin',8,'add_value')
})

StartupEvents.registry('potion',event => {
    event.create('iron_skin')
    .effect('kubejs:iron_skin',3600,0)
})


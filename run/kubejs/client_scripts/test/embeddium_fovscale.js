// This File has been authored by AllTheMods Staff, or a Community contributor for use in AllTheMods - AllTheMods 10.
// As all AllTheMods packs are licensed under All Rights Reserved, this file is not allowed to be used in any public packs not released by the AllTheMods Team, without explicit permission.

let $MinecraftOptionsStorage = Java.loadClass("org.embeddedt.embeddium.api.options.storage.MinecraftOptionsStorage")
let $OptionImpl = Java.loadClass("org.embeddedt.embeddium.api.options.structure.OptionImpl")
let $Integer = Java.loadClass("java.lang.Integer")
let $SliderControl = Java.loadClass("org.embeddedt.embeddium.api.options.control.SliderControl")
let $ControlValueFormatter = Java.loadClass("org.embeddedt.embeddium.api.options.control.ControlValueFormatter")

NativeEvents.onEvent("org.embeddedt.embeddium.api.OptionGroupConstructionEvent", event => {
  if (event.getId() == "minecraft:window") {
    let fovScaleOption = $OptionImpl.createBuilder($Integer, $MinecraftOptionsStorage.INSTANCE)
      .setId("minecraft:fov_effect_scale")
      .setName(Text.translatable("options.fovEffectScale"))
      .setTooltip(Text.translatable("options.fovEffectScale.tooltip"))
      .setControl(option => new $SliderControl(option, 0, 100, 1, $ControlValueFormatter.percentage()))
      .setBinding((opts, value) => {
        opts.fovEffectScale().set(value / 100);
      }, (opts) => Java.cast($Integer, JavaMath.floor(opts.fovEffectScale().get() * 100)))
      .build()
    event.getOptions().add(fovScaleOption)
  }
})

// This File has been authored by AllTheMods Staff, or a Community contributor for use in AllTheMods - AllTheMods 10.
// As all AllTheMods packs are licensed under All Rights Reserved, this file is not allowed to be used in any public packs not released by the AllTheMods Team, without explicit permission.

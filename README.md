# KubeJS Tweaks

## 📰 Introduction

This is an addon for KubeJS to abstract some common usage of KubeJS for heavly modded modpacks.

This also includes some fixes of KubeJS when needed (only applied at specific versions).

## 📦 Installation

Mod is available on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/alltheleaks).

## 🔧 Features

You can find examples [here](https://github.com/pietro-lopes/KubeJSTweaks/tree/master/run/kubejs).

New schema component:
- Codec

New Events:
- Register Custom Codec
- No Op (use to disable recipes, loot tables, features etc before they are parsed)

Others:
- `global.jeiRuntime` is back
- `KJSTweaks.readJsonFromMod(mod, path)` that reads original json (useful for manipulations at datagen event)

Hot Fix Kubejs issues for 2101.7.1-build.181:
- [Failed to encode packet 'clientbound/minecraft:update_recipes' Caused by: io.netty.handler.codec.EncoderException: Empty ItemStack not allowed](https://github.com/KubeJS-Mods/KubeJS/issues/878)
- [Dont freeze my pc anymore please thanks](https://github.com/KubeJS-Mods/KubeJS/pull/963)
- [EitherRecipeComponent unconditionally fails validation due to incorrect usage of Optional](https://github.com/KubeJS-Mods/KubeJS/issues/967)
- [Fixed enum component requiring two `>`]()
- [Fixed Registry Component](https://github.com/KubeJS-Mods/KubeJS/commit/4a5391bf2257f6b97d02ac2227089bd8b695775c)
- [Fixed Registry Scanner](https://github.com/KubeJS-Mods/KubeJS/commit/f1befc44784d9a64986f8275b6a80b7ce8b33966)
- [Fixed Registry Scanner again](https://github.com/KubeJS-Mods/KubeJS/commit/c88bae170f686120d091e4b67c545b11cb2469b7)
- [event.creatCustom seem broken](https://github.com/KubeJS-Mods/KubeJS/issues/972)


## 🤝 Contributing

If you find yourself using too much cursed stuff with Rhino or KubeJS itself that could be abstracted, feel free to suggest as a feature.

## 📝 License

This project is licensed under the MIT License.

## 📬 Contact

You can reach me on [All The Mods Discord](https://discord.gg/allthemods) with `@Uncandango`, you can also open your issue on [Github](https://github.com/pietro-lopes/KubeJSTweaks/issues) or comment on the mod comment's section on CurseForge.

## 👍 Sponsors

<div><a href="https://www.yourkit.com/" rel="nofollow"><img src="https://www.yourkit.com/images/yklogo.png" width="100"></a></div>

YourKit supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications. YourKit is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/), [YourKit .NET Profiler](https://www.yourkit.com/dotnet-profiler/), and [YourKit YouMonitor](https://www.yourkit.com/youmonitor/).

***

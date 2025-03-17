plugins {
    `java-library`
    `maven-publish`
    id("net.neoforged.moddev") version "2.0.78"
    idea
}

val mod_version: String by extra
val mod_group_id: String by extra

val neo_version: String by extra
val parchment_mappings_version: String by extra
val parchment_minecraft_version: String by extra
val minecraft_version: String by extra
val minecraft_version_range: String by extra
val neo_version_range: String by extra
val loader_version_range: String by extra
val mod_name: String by extra
val mod_license: String by extra
val mod_authors: String by extra
val mod_description: String by extra

repositories {
    mavenLocal()
    exclusiveContent {
        forRepository {
            maven("https://cursemaven.com")
        }
        filter {
            includeGroup("curse.maven")
        }
    }
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") {
        name = "GeckoLib"
        content {
            includeGroup("software.bernie.geckolib")
        }
    }
    maven {
        // saps.dev Maven (KubeJS and Rhino)
        url = uri("https://maven.saps.dev/releases")
        content {
            includeGroup("dev.latvian.mods")
            includeGroup("dev.latvian.apps")
        }
    }
    maven {
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github.rtyley")
        }
    }

    maven {
        name = "Iron's Maven - Release"
        url = uri("https://code.redspace.io/releases")
    }

    flatDir {
        dir("libs")
    }
}

val mod_id: String by extra

base {
    archivesName = mod_id
    group = mod_group_id
    version = mod_version
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

neoForge {
    version = neo_version

    parchment {
        mappingsVersion = parchment_mappings_version
        minecraftVersion = parchment_minecraft_version
    }

    runs {
        register("client") {
            client()

            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }

        register("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }

        register("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }

        register("data") {
            data()

            programArguments.addAll("--mod", mod_id, "--all", "--output", file("src/generated/resources/").absolutePath, "--existing", file("src/main/resources/").absolutePath)
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            jvmArgument("-Xmx3000m")
            jvmArgument("-XX:+IgnoreUnrecognizedVMOptions")
            jvmArgument("-XX:+AllowEnhancedClassRedefinition")

            logLevel = org.slf4j.event.Level.DEBUG

            if (type.get().startsWith("client")) {
                programArguments.addAll("--width", "1920", "--height", "1080")
                systemProperty("mixin.debug.export", "true")
                jvmArguments.addAll(
                    "-XX:+UnlockExperimentalVMOptions",
                    "-XX:+UseG1GC",
                    "-XX:G1NewSizePercent=20",
                    "-XX:G1ReservePercent=20",
                    "-XX:MaxGCPauseMillis=50",
                    "-XX:G1HeapRegionSize=32M"
                )
            }
        }
    }

    mods {
        register(mod_id) {
            sourceSet(sourceSets.main.get())
        }
    }
}

val localRuntime: Configuration by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(localRuntime)
    }
}

dependencies {
    implementation("dev.latvian.mods:kubejs-neoforge:2101.7.1-build.181")?.let {
        interfaceInjectionData(it)
    }
    // localRuntime("curse.maven:kubejs-238086:5810100")

    implementation("dev.latvian.mods:rhino:2101.2.6-build.66")
    // localRuntime("curse.maven:rhino-416294:6110233")

    // localRuntime("curse.maven:alltheleaks-1091339:6165880")
    localRuntime("blank:alltheleaks:0.1.14:beta+1.21.1-neoforge")

    localRuntime("curse.maven:probejs-585406:5820894")
    // localRuntime("blank:ProbeJS:7.7.2")

    // Mods
    localRuntime("curse.maven:oritech-1030830:6065115")
    localRuntime("curse.maven:replication-638351:6168308")
    localRuntime("curse.maven:industrial-foregoing-266515:6030556")
    localRuntime("curse.maven:selene-499980:6184655")
    localRuntime("curse.maven:mekanism-268560:6018306")
    implementation("curse.maven:ex-deorum-901420:6162365")
    localRuntime("curse.maven:ars-nouveau-401955:6228434")

    // Dependencies
    localRuntime("curse.maven:architectury-api-419699:5786327")
    localRuntime("curse.maven:forgified-fabric-api-889079:6136650")
    localRuntime("curse.maven:owo-lib-532610:5945693")
    localRuntime("curse.maven:geckolib-388172:6027599")
    localRuntime("curse.maven:athena-841890:5629395")
    localRuntime("curse.maven:titanium-287342:6155132")
    localRuntime("curse.maven:curios-309927:6296219")


    // Utils
    localRuntime("curse.maven:jei-238222:5846880")
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components.getByName("java"))
        }
    }
    repositories {
        maven("file://$projectDir/repo")
    }
}

tasks {
    register("generateModMetadata", ProcessResources::class) {
        val replaceProperties = mapOf(
            "minecraft_version" to minecraft_version,
            "minecraft_version_range" to minecraft_version_range,
            "neo_version" to neo_version,
            "neo_version_range" to neo_version_range,
            "loader_version_range" to loader_version_range,
            "mod_id" to mod_id,
            "mod_name" to mod_name,
            "mod_license" to mod_license,
            "mod_version" to mod_version,
            "mod_authors" to mod_authors,
            "mod_description" to mod_description
        )
        inputs.properties(replaceProperties)
        expand(replaceProperties)
        from("src/main/templates")
        into("build/generated/sources/modMetadata")
    }
    compileJava {
        options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
    }
    wrapper {
        distributionType = Wrapper.DistributionType.BIN
    }
}

sourceSets {
    main {
        resources.srcDir("src/generated/resources")
        resources.srcDir(tasks.named("generateModMetadata"))
    }
}

neoForge.ideSyncTask(tasks.named("generateModMetadata"))

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

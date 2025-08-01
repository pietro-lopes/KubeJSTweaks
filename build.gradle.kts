import net.neoforged.moddevgradle.internal.RunGameTask

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
val alltheleaks_version_range: String by extra

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
        url = uri("https://maven.latvian.dev/releases")
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

    maven { url = uri("https://maven.bawnorton.com/releases") }

    maven {
        url = uri("https://maven.blamejared.com/")
    }

    flatDir {
        dir("libs")
    }
}

val mod_id: String by extra
val testmod_id = "testmod"
base {
    archivesName = mod_id
    group = mod_group_id
    version = mod_version
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

evaluationDependsOn(project(":kjst-agent").path)

subprojects {
    version = project(":").version
}

neoForge {
    version = neo_version

    parchment {
        mappingsVersion = parchment_mappings_version
        minecraftVersion = parchment_minecraft_version
    }

    runs {
        register("client") {
            client()
            systemProperty("production", "")
            systemProperty("neoforge.enableGameTest", "false")
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }

        register("server") {
            server()
            systemProperty("production", "")
            systemProperty("neoforge.enableGameTest", "false")
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }

        register("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }

        val agentJarFile = project(":kjst-agent").tasks.jar.get().archiveFile.get().asFile.toString()
        register("testmod") {
            client()
            sourceSet = sourceSets.test.get()
            systemProperty("neoforge.gameTestServer", "true")
            systemProperty("neoforge.enabledGameTestNamespaces", "${mod_id},${testmod_id}")
            jvmArgument("-javaagent:$agentJarFile")
        }

        register("data") {
            data()

            programArguments.addAll("--mod", mod_id, "--all", "--output", file("src/generated/resources/").absolutePath, "--existing", file("src/main/resources/").absolutePath)
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            jvmArgument("-Xmx3000m")
            //jvmArgument("-XX:+IgnoreUnrecognizedVMOptions")
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
        register("testmod") {
            sourceSet(sourceSets.test.get())
        }
//        register("kjst-asm") {
//            sourceSet(project(":kjst-asm").sourceSets.main.get())
//        }
    }
    addModdingDependenciesTo(sourceSets.test.get())
}

val localRuntime: Configuration by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(localRuntime)
    }
    testRuntimeClasspath {
        extendsFrom(localRuntime)
    }
}

//afterEvaluate { // DO NOT ASK... it fixes the runClient and family tasks :harold:
//    tasks.withType(RunGameTask::class).configureEach {
//        classpathProvider.setFrom(classpathProvider.files.stream().filter {f -> !f.toString().contains("kjst-asm")}.toList())
//    }
//}

dependencies {
    // MixinExtras that supports @Expression
    jarJar("io.github.llamalad7:mixinextras-neoforge:0.5.0-rc.2")?.let {
        implementation(it)
    }

    // MixinSquared to Cancel and Adjust other mixins
    annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-common:0.2.0")?.let {
        compileOnly(it)
    }
    jarJar("com.github.bawnorton.mixinsquared:mixinsquared-neoforge:0.2.0")?.let {
        implementation(it)
    }

//    jarJar(project(":kjst-asm"))
//    implementation(project(":kjst-asm"))
//    localRuntime(project(":kjst-asm"))

    implementation("dev.latvian.mods:kubejs-neoforge:2101.7.1-build.181")?.let {
        interfaceInjectionData(it)
    }
    implementation("dev.latvian.mods:rhino:2101.2.7-build.74")

    testImplementation("net.neoforged:testframework:${neo_version}")

    compileOnly("curse.maven:probejs-585406:5820894")
    localRuntime("curse.maven:probejs-585406:5820894")

    // Mods
    implementation("curse.maven:ex-deorum-901420:6162365")
    compileOnly("curse.maven:jei-238222:6614392")
    compileOnly("curse.maven:emi-580555:6420931")
    compileOnly("curse.maven:tmrv-1194921:6269681")

    localRuntime("curse.maven:oritech-1030830:6300493")
    localRuntime("curse.maven:replication-638351:6763336")
    localRuntime("curse.maven:industrial-foregoing-266515:6283758")
    localRuntime("curse.maven:selene-499980:6818413")
    localRuntime("curse.maven:mekanism-268560:6486993")
    localRuntime("curse.maven:ars-nouveau-401955:6333245")
    // localRuntime("curse.maven:fastsuite-475117:6321099-sources")
    localRuntime("curse.maven:actually-additions-228404:6329770")
    localRuntime("curse.maven:applied-energistics-2-223794:6323510")
    localRuntime("curse.maven:extreme-reactors-250277:6158187")
    implementation("curse.maven:create-328085:6323264")
    localRuntime("curse.maven:ender-io-64578:6307311")
    localRuntime("curse.maven:ex-pattern-provider-892005:6283473")
    //localRuntime("curse.maven:farming-for-blockheads-261924:6409932")
    localRuntime("curse.maven:functional-storage-556861:6189752")
    implementation("curse.maven:immersive-engineering-231951:6235316")
    localRuntime("curse.maven:industrial-foregoing-266515:6283758")
    localRuntime("curse.maven:integrated-dynamics-236307:6331508")
    localRuntime("curse.maven:just-dire-things-1002348:6161633")
    localRuntime("curse.maven:the-twilight-forest-227639:6087105")
    localRuntime("curse.maven:apotheosis-313970:6545942")
    localRuntime("curse.maven:apothic-spawners-986583:6751589")
    localRuntime("curse.maven:apothic-attributes-898963:6751650")
    localRuntime("curse.maven:apothic-enchanting-1063926:6751658")
    localRuntime("curse.maven:irons-spells-n-spellbooks-855414:6826052")
    localRuntime("curse.maven:stone-zone-1154622:6710522")
    localRuntime("curse.maven:every-compat-628539:6762704")

    // Dependencies
    localRuntime("curse.maven:architectury-api-419699:5786327")
    localRuntime("curse.maven:forgified-fabric-api-889079:6136650")
    localRuntime("curse.maven:owo-lib-532610:5945693")
    //localRuntime("curse.maven:geckolib-388172:6027599")
    localRuntime("curse.maven:athena-841890:5629395")
    localRuntime("curse.maven:titanium-287342:6308421")
    localRuntime("curse.maven:curios-309927:6296219")
    localRuntime("curse.maven:placebo-283644:6274181-sources")
    localRuntime("curse.maven:oracle-index-1206582:6300145")
    localRuntime("curse.maven:guideme-1173950:6331224")
    localRuntime("curse.maven:zerocore-247921:6160798")
    localRuntime("curse.maven:glodium-957920:5821676")
    localRuntime("curse.maven:balm-531761:6338302")
    localRuntime("curse.maven:common-capabilities-247007:6332022")
    localRuntime("curse.maven:cyclops-core-232758:6340387")
    localRuntime("curse.maven:flux-networks-248020:6089446")
    localRuntime("curse.maven:allthecompressed-514045:6121584")
    localRuntime("curse.maven:placebo-283644:6751290")
    localRuntime("curse.maven:playeranimator-658587:6024462")
    localRuntime("curse.maven:geckolib-388172:6659026")

    implementation("malte0811:DualCodecs:0.1.2")
    implementation("blank:endec:0.1.8")

    // Utils
    // localRuntime("curse.maven:jei-238222:6614392")
    localRuntime("curse.maven:emi-580555:6420931")
    localRuntime("curse.maven:tmrv-1194921:6269681")
    localRuntime("curse.maven:jade-324717:6291517")
//    localRuntime(project(":kjst-agent"))
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
            "mod_description" to mod_description,
            "alltheleaks_version_range" to alltheleaks_version_range
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

    named("runTestmod") {
        dependsOn(":kjst-agent:jar")
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

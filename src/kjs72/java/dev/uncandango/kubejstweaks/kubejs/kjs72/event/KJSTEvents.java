package dev.uncandango.kubejstweaks.kubejs.kjs72.event;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.uncandango.kubejstweaks.kubejs.event.CompatibilityEventJS;
import dev.uncandango.kubejstweaks.kubejs.event.NoOpEventJS;
import dev.uncandango.kubejstweaks.kubejs.event.PreRecipeEventJS;

public class KJSTEvents {
    public static final EventGroup GROUP = EventGroup.of("KubeJSTweaks");

    public static final EventHandler schema = GROUP.startup("schema", () -> RegisterCodecEventJS.class);

    public static final EventHandler noOp = GROUP.server("noOp", () -> NoOpEventJS.class);

    public static final EventHandler preRecipes = GROUP.server("beforeRecipes", () -> PreRecipeEventJS.class);

    public static final EventHandler compatibility = GROUP.startup("checkCompatibility", () -> CompatibilityEventJS.class);
}

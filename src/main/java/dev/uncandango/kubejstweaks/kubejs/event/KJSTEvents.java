package dev.uncandango.kubejstweaks.kubejs.event;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public class KJSTEvents {
    public static final EventGroup GROUP = EventGroup.of("KubeJSTweaks");

    public static final EventHandler schema = GROUP.startup("schema", () -> RegisterCodecEventJS.class);

    public static final EventHandler noOp = GROUP.server("noOp", () -> NoOpEventJS.class);
}

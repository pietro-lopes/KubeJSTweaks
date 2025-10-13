package dev.uncandango.kubejstweaks.kubejs.kjs72.plugin;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.rhino.CachedClassInfo;
import dev.latvian.mods.rhino.CachedClassStorage;
import dev.uncandango.kubejstweaks.kubejs.event.NoOpEventJS;
import dev.uncandango.kubejstweaks.kubejs.plugin.KJSTPluginUtils;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class KJSTProbeJSPlugin extends ProbeJSPlugin {

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        applyEdits(globalClasses, NoOpEventJS.class, decl -> {
            replaceType(decl, "recipes", 0, Types.primitive("Special.RecipeId"));
            replaceType(decl, "lootTables", 0, Types.primitive("Special.LootTable"));
            replaceType(decl, "lootTablesBlock", 0, Types.primitive("Special.Block"));
            replaceType(decl, "biomeModifiers", 0, Types.primitive("Special.NeoforgeBiomeModifier"));
        });
        applyEdits(globalClasses, KJSTPluginUtils.class, decl -> {
            replaceType(decl, "readFromMod", 0, Types.primitive("Special.Mod"));
        });
    }

    private static void replaceType(ClassDecl decl, String name, int index, BaseType type) {
        for (MethodDecl method : decl.methods) {
            if (method.name.equals(name)) {
                method.params.get(index).type = type;
            }
        }
    }

    private static void applyEdits(Map<ClassPath, TypeScriptFile> globalClasses, Class<?> clazz, Consumer<ClassDecl> edits) {
        TypeScriptFile typeScriptFile = globalClasses.get(new ClassPath(clazz));
        if (typeScriptFile == null) return;
        typeScriptFile.findCode(ClassDecl.class).ifPresent(edits);
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        if (scriptDump.scriptType != ScriptType.SERVER) return Set.of();
        Set<Class<?>> classes = new HashSet<>();
        ServerScriptManager manager = (ServerScriptManager) scriptDump.manager;

        for (RecipeNamespace namespace : manager.recipeSchemaStorage.namespaces.values()) {
            for (RecipeSchemaType schemaType : namespace.values()) {
                for (RecipeKey<?> key : schemaType.schema.keys) {
                    classes.addAll(key.component.typeInfo().getContainedComponentClasses());
                }
                classes.addAll(schemaType.schema.recipeFactory.recipeType().getContainedComponentClasses());
            }
        }

        Map<Class<?>, CachedClassInfo> map = ObfuscationReflectionHelper.getPrivateValue(CachedClassStorage.class, CachedClassStorage.GLOBAL_PUBLIC, "map");
        if (map != null) {
            classes.addAll(map.keySet());
        }
        return classes;
    }
}

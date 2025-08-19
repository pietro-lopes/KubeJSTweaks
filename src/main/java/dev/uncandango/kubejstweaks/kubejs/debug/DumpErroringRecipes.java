package dev.uncandango.kubejstweaks.kubejs.debug;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.uncandango.kubejstweaks.KubeJSTweaks;
import dev.uncandango.kubejstweaks.event.ClientEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public record DumpErroringRecipes(Throwable exception, ResourceLocation recipeId, JsonElement json) {
    private static final List<DumpErroringRecipes> RECIPES = new ArrayList<>();
    private static final Path MARKDOWN = KubeJSTweaks.getLocal().resolve("erroring_recipes.md");

    private static boolean dumpErroringRecipes = false;

    public static void enable(){
        dumpErroringRecipes = true;
    }

    public static void disable(){
        clear();
        dumpErroringRecipes = false;
    }

    public static boolean isEnabled(){
        return dumpErroringRecipes;
    }

    public static void add(Throwable exception, ResourceLocation recipeId, JsonElement json){
        if (isEnabled()) RECIPES.add(new DumpErroringRecipes(exception, recipeId, json));
    }

    private static void clear(){
        RECIPES.clear();
    }

    public static List<DumpErroringRecipes> getRecipes(){
        return RECIPES;
    }

    public static void dumpToJsonFiles() throws IOException {
        String markDownText = generateMarkdown();
        Files.createDirectories(MARKDOWN.getParent());
        Files.writeString(MARKDOWN, markDownText);
    }

    private static String generateMarkdown() {
        var sb = new StringBuilder();
        ArrayListMultimap<String, DumpErroringRecipes> summary = ArrayListMultimap.create();
        RECIPES.forEach(recipe -> summary.put(recipe.exception().getMessage(), recipe));
        var sortedCat = summary.keySet().stream().sorted().toList();
        generateTableOfContents(sb, sortedCat);
        var resourceManager = ServerLifecycleHooks.getCurrentServer().getResourceManager();
        sortedCat.forEach((cat) -> {
            summary.get(cat).sort(Comparator.comparing(val -> {
                if (val != null) return val.recipeId().toString();
                return null;
            }, (val1,val2) -> {
                if (val1 != null && val2 != null) {
                    return val1.compareTo(val2);
                } else {
                  return 0;
                }
            }));
            sb.append("##### `").append(cat).append("`\n");
            sb.append("\n");
            summary.get(cat).forEach(dump -> {
                sb.append("<details>\n");
                sb.append("<summary>").append(dump.recipeId().toString()).append("</summary>\n");
                sb.append("\n");
                if (dump.exception() instanceof KubeRuntimeException kre) {
                    sb.append("`").append(kre.toString().replace(cat, "").substring(3)).append("`\n\n");
                }
                resourceManager.getResource(dump.recipeId().withPrefix("recipe/").withSuffix(".json")).ifPresent(resource -> {
                    sb.append("`").append(resource.sourcePackId()).append("`\n\n");
                });
                sb.append("```json\n");
                sb.append(toPrettyString(dump.json())).append("\n");
                sb.append("```\n");
                sb.append("\n");
                sb.append("</details>\n\n");
            });
        });

        return sb.toString();
    }

    private static void generateTableOfContents(StringBuilder sb, List<String> headers) {
        if (headers.isEmpty()) {
            sb.append("# No errors detected!\n");
            KubeJSTweaks.LOGGER.info("No error was found to be dumped.");
        } else {
            sb.append("# Errors detected\n");
            if (FMLEnvironment.dist.isClient()) {
                var player = Minecraft.getInstance().player;
                var hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("kubejstweaks.chat.click_to_open_markdown"));
                var clickEvent = new ClickEvent(ClickEvent.Action.OPEN_FILE, MARKDOWN.toAbsolutePath().toString());
                var clickMessage = Component.translatable("kubejstweaks.chat.click_here").withStyle(style -> style.withClickEvent(clickEvent).withHoverEvent(hoverEvent).withColor(ChatFormatting.GREEN));
                var message = Component.translatable("kubejstweaks.debug.erroring_recipes_summary", clickMessage).withStyle(ChatFormatting.WHITE);
                var fullMessage = Component.translatable("kubejstweaks.chat.mod_id", message).withStyle(ChatFormatting.YELLOW);
                if (player != null) {
                    player.sendSystemMessage(fullMessage);
                } else {
                    ClientEvents.MESSAGES.offer(fullMessage);
                }
            }
            headers.forEach(header -> {
                sb.append("- [`").append(header).append("`](").append(toLink(header)).append(")\n");
            });
            sb.append("\n");
        }
    }

    private static String toLink(String text) {
        return "#" + text.toLowerCase(Locale.ROOT).replaceAll(" ", "-").replaceAll("[^A-Za-z0-9_-]", "");
    }

    static String toPrettyString(JsonElement json) {
        StringWriter writer = new StringWriter();

        try {
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent("\s\s");
            jsonWriter.setSerializeNulls(true);
            jsonWriter.setLenient(true);
            jsonWriter.setHtmlSafe(false);
            Streams.write(json, jsonWriter);
        } catch (IOException ex) {
            KubeJSTweaks.LOGGER.error("Error while writing JSON", ex);
        }

        return writer.toString();
    }
}

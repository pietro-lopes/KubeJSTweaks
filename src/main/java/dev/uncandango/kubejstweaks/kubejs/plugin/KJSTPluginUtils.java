package dev.uncandango.kubejstweaks.kubejs.plugin;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.kubejs.util.JsonIO;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeJavaObject;
import dev.uncandango.kubejstweaks.KubeJSTweaks;
import dev.uncandango.kubejstweaks.impl.TempResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforgespi.locating.IModFile;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class KJSTPluginUtils {
    public static WeakReference<CloseableResourceManager> SERVER_PACK_RESOURCES = new WeakReference<>(null);
    public static CloseableResourceManager CLIENT_PACK_RESOURCES;
    public static CloseableResourceManager TEMPORARY_SERVER_PACK_RESOURCES = null;
    private static final Supplier<HttpClient> HTTP_CLIENT = Suppliers.memoize(() -> HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build());
    private static final Supplier<String> USER_AGENT = Suppliers.memoize(() -> "Java-http-client/" + System.getProperty("java.version") + ' ' +
        "KubeJSTweaks/" + LoadingModList.get().getModFileById("kubejstweaks").versionString());

    @Nullable
    public static JsonElement readJsonFromMod(Context cx, String modId, String id) {
        var type = ((KubeJSContext) cx).getType().equals(ScriptType.CLIENT) ? KJSTPackType.ASSETS : KJSTPackType.DATA;
        return readJsonFromMod(cx, modId, id, type);
    }

    @Nullable
    public static JsonElement readJsonFromMod(Context cx, String modId, String id, KJSTPackType type) {
        var rl = toJsonRL(cx, id, modId);
        modId = modId.equals("minecraft") ? "vanilla" : "mod/" + modId;
        if (type == KJSTPackType.ASSETS) {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                if (Minecraft.getInstance().getResourceManager().getNamespaces().isEmpty()) {
                    var rm = CLIENT_PACK_RESOURCES;
                    if (rm != null) {
                        var resources = rm.getResourceStack(rl);
                        var kjsSide = ((KubeJSContext) cx).getType();
                        if (!kjsSide.equals(ScriptType.CLIENT)) {
                            ConsoleJS.getCurrent(cx).warn("You are querying the assets of " + modId + " from " + kjsSide + ", remember that this is not supported on a DEDICATED SERVER.");
                        }
                        return readJsonFromResourceList(cx, modId, resources, rl);
                    }
                } else {
                    if (CLIENT_PACK_RESOURCES != null) {
                        CLIENT_PACK_RESOURCES = null;
                    }
                    var resources = Minecraft.getInstance().getResourceManager().getResourceStack(rl);
                    var kjsSide = ((KubeJSContext) cx).getType();
                    if (!kjsSide.equals(ScriptType.CLIENT)) {
                        ConsoleJS.getCurrent(cx).warn("You are querying the assets of " + modId + " from " + kjsSide + ", remember that this is not supported on a DEDICATED SERVER.");
                    }
                    return readJsonFromResourceList(cx, modId, resources, rl);
                }
            } else {
                ConsoleJS.getCurrent(cx).error("Failed to get json with id " + rl + ", not possible to get assets from dedicated server.");
            }
        }
        if (type == KJSTPackType.DATA) {
            var server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                var resources = server.getResourceManager().getResourceStack(rl);
                return readJsonFromResourceList(cx, modId, resources, rl);
            } else {
                var rm = SERVER_PACK_RESOURCES.get() == null ? loadTemporaryServerPackResources() : SERVER_PACK_RESOURCES.get();
                if (rm != null) {
                    var resources = rm.getResourceStack(rl);
                    return readJsonFromResourceList(cx, modId, resources, rl);
                }
                ConsoleJS.getCurrent(cx).error("Failed to get json with id " + rl + ", not ready yet.");
            }
        }
        return JsonNull.INSTANCE;
    }

    private static ResourceManager loadTemporaryServerPackResources() {
        if (TEMPORARY_SERVER_PACK_RESOURCES != null) {
            return TEMPORARY_SERVER_PACK_RESOURCES;
        }
        Map<IModFile, Pack.ResourcesSupplier> map = Maps.newHashMap();
        var source = ResourcePackLoader.buildPackFinder(map, PackType.SERVER_DATA);
        var packRepo = new PackRepository(source);
        ResourcePackLoader.populatePackRepository(packRepo, PackType.SERVER_DATA, false);
        packRepo.reload();
        var vanillaPack = new ServerPacksSource(new DirectoryValidator(path -> true));
        List<PackResources> packs = new ArrayList<>(packRepo.openAllSelected());
        packs.addFirst(vanillaPack.getVanillaPack());
        TEMPORARY_SERVER_PACK_RESOURCES = new TempResourceManager(PackType.SERVER_DATA, packs);
        return TEMPORARY_SERVER_PACK_RESOURCES;
    }

    private static JsonElement readJsonFromResourceList(Context cx, String modId, List<Resource> resources, ResourceLocation id) {
        List<String> modsFound = new ArrayList<>();
        Resource found = null;
        for (var resource : resources) {
            modsFound.add(resource.sourcePackId());
            if (found != null) {
                continue;
            }
            found = resource.sourcePackId().equals(modId) ? resource : null;
        }
        if (found != null) {
            try (var reader = found.open()) {
                if (modsFound.size() > 1) {
                    ConsoleJS.getCurrent(cx).warn("Multiple mods (" + modsFound + ") found for resource " + id + ", using " + found.sourcePackId());
                }
                return JsonIO.parseRaw(new String(reader.readAllBytes()));
            } catch (IOException e) {
                ConsoleJS.getCurrent(cx).error("Not possible to read json", e);
            }
        }
        ConsoleJS.getCurrent(cx).error("Not possible to read json with id " + id + ", resource not found.");
        return JsonNull.INSTANCE;
    }

    private static ResourceLocation toJsonRL(Context cx, String id, String modId) {
        var extension = id.substring(id.lastIndexOf('.') + 1);
        if (extension.equals("json")) {
            return id.indexOf(':') >= 0 ? ResourceLocation.parse(id) : ResourceLocation.fromNamespaceAndPath(modId, id);
        }
        if (extension.equals(id)) {
            id = id + ".json";
            return id.indexOf(':') >= 0 ? ResourceLocation.parse(id) : ResourceLocation.fromNamespaceAndPath(modId, id);
        }
        throw new KubeRuntimeException("Error while reading file.", new UnsupportedOperationException("Failed to read json with id " + id + ", extension " + extension + " is invalid, only json is supported")).source(SourceLine.of(cx));
    }

    public static Class<?> getClass(Object obj) {
        if (obj instanceof Class<?> clazz) {
            return clazz;
        }
        return obj.getClass();
    }

    public static Class<?> getSuperclass(Object object) {
        if (object instanceof Class<?> clazz) {
            return clazz.getSuperclass();
        }
        if (object instanceof NativeJavaObject nativeJavaObject) {
            return nativeJavaObject.unwrap().getClass().getSuperclass();
        }
        throw new IllegalStateException("Failed to get superclass of " + object);
    }

    public static void runIfModPresent(Context cx, String modId, String versionRange, Callable<Void> runnable) {
        var modFile = ModList.get().getModFileById(modId);
        if (modFile == null) {
            ConsoleJS.getCurrent(cx).debug("Mod " + modId + " not loaded, skipping task.");
            return;
        }
        if (versionRange == null) {
            versionRange = "*";
        }
        var modVersionString = modFile.versionString();
        var modVersion = new DefaultArtifactVersion(modVersionString);
        try {
            var range = VersionRange.createFromVersionSpec(versionRange);
            if (range.containsVersion(modVersion)) {
                runnable.call();
            } else {
                ConsoleJS.getCurrent(cx).debug("Mod " + modId + " with version range " + versionRange + " not loaded, skipping task.");
            }
        } catch (InvalidVersionSpecificationException e) {
            throw new KubeRuntimeException("Error while parsing range version", e).source(SourceLine.of(cx));
        } catch (Exception e) {
            throw new KubeRuntimeException("Error while executing task", e).source(SourceLine.of(cx));
        }
    }

    public static void runIfModPresent(Context cx, String modId, Callable<Void> runnable) {
        runIfModPresent(cx, modId,"*",runnable);
    }

    public static void curseForgeGetEndpoint(String url, Executor executor, Consumer<JsonElement> callback) {
        CompletableFuture.runAsync(() -> {
            var request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.curseforge.com/api/" + url))
                .timeout(Duration.ofSeconds(15))
                .setHeader("Accept-Encoding", "gzip")
                .setHeader("User-Agent", USER_AGENT.get())
                .GET()
                .build();
            try {
                final HttpResponse<InputStream> response = HTTP_CLIENT.get().send(request, HttpResponse.BodyHandlers.ofInputStream());
                int responseCode = response.statusCode();
                if (responseCode == 200) {
                    final boolean isGzipEncoded = response.headers().firstValue("Content-Encoding").orElse("").equals("gzip");

                    final String bodyStr;
                    try (InputStream inStream = isGzipEncoded ? new GZIPInputStream(response.body()) : response.body()) {
                        try (var bufferedReader = new BufferedReader(new InputStreamReader(inStream))) {
                            bodyStr = bufferedReader.lines().collect(Collectors.joining("\n"));
                        }
                    }
                    executor.execute(() -> {
                        try {
                            callback.accept(JsonUtils.fromString(bodyStr));
                        } catch (Exception e) {
                            KubeJSTweaks.LOGGER.error("Error while executing from CF Endpoint callback", e);
                        }
                    });
                } else {
                    KubeJSTweaks.LOGGER.error("Error while trying to call CF endpoint {}, response code: {}", url, responseCode);
                    KubeJSTweaks.LOGGER.warn("You are only allowed to call public endpoints (does not require API-KEY)");
                }
            } catch (IOException | InterruptedException e) {
                KubeJSTweaks.LOGGER.error("Error while trying to call CF endpoint {}, error: {}", url, e.getMessage());
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public enum KJSTPackType {
        ASSETS(PackType.CLIENT_RESOURCES),
        DATA(PackType.SERVER_DATA);

        private final PackType packType;

        KJSTPackType(PackType packType) {
            this.packType = packType;
        }
    }
}

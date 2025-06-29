package world.novium.creative.common.managers;


import com.google.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import world.novium.creative.CreativePlugin;
import world.novium.creative.utils.FlatWorldGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Getter
@RequiredArgsConstructor
@Slf4j
public class WorldManager {

    @Inject
    public CreativePlugin plugin;

    @Inject
    private TranslationManager translator;

    private final Map<String, World> loadedWorlds = new HashMap<>();
    private final File SNAPSHOT_DIR = Bukkit.getWorldContainer().toPath().resolve("snapshots").toFile();

    public void createSnapshot(UUID uuid, String snapshotName) {
        // unload world if it is loaded
        World world = Bukkit.getWorld("world_" + uuid);

        if (world != null) {
            Bukkit.unloadWorld(world, true);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                executeSnapshot(uuid, snapshotName);
                // load the world again after snapshot creation
                if (!worldExists("world_" + uuid)) {
                    generateWorld("world_" + uuid);
                } else {
                    Bukkit.createWorld(new WorldCreator("world_" + uuid));
                }
            }, 20L);
        } else {
            // if world is not loaded, just create the snapshot
            executeSnapshot(uuid, snapshotName);
        }
    }

    public void executeSnapshot(UUID uuid, String snapshotName) {
        File worldFolder = getWorldFolder(uuid);
        File snapshotFolder = new File(SNAPSHOT_DIR, "world_" + uuid);
        if (!snapshotFolder.exists() && !snapshotFolder.mkdirs()) return;

        File zipFile = new File(snapshotFolder, snapshotName + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Path worldPath = worldFolder.toPath();
            Files.walk(worldPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry entry = new ZipEntry(worldPath.relativize(path).toString());
                        try {
                            zos.putNextEntry(entry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                translator.sendPrefixed(player, "snapshot.created",
                        TagResolver.builder().tag("name", Tag.inserting(Component.text(snapshotName))).build());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                translator.sendPrefixed(player, "snapshot.create_failed",
                        TagResolver.builder().tag("name", Tag.inserting(Component.text(snapshotName))).build());
            }
        }
    }

    public boolean loadSnapshot(UUID uuid, String snapshotName) {
        File worldFolder = getWorldFolder(uuid);
        File snapshotZip = new File(new File(SNAPSHOT_DIR, "world_" + uuid), snapshotName + ".zip");

        if (!snapshotZip.exists()) return false;

        String name = "world_" + uuid;
        if (Bukkit.getWorld(name) != null) {
            Bukkit.unloadWorld(name, false);
        }

        try {
            deleteWorldDirectory(name);

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(snapshotZip))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    File newFile = new File(worldFolder, entry.getName());
                    if (entry.isDirectory()) {
                        newFile.mkdirs();
                    } else {
                        newFile.getParentFile().mkdirs();
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                        }
                    }
                    zis.closeEntry();
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String[] listSnapshots(UUID uuid) {
        File folder = new File(SNAPSHOT_DIR, "world_" + uuid);
        if (!folder.exists()) return new String[0];

        return folder.list((dir, name) -> name.endsWith(".zip"));
    }

    public boolean deleteSnapshot(UUID uuid, String snapshotName) {
        File snapshotFile = new File(new File(SNAPSHOT_DIR, "world_" + uuid), snapshotName + ".zip");
        return snapshotFile.exists() && snapshotFile.delete();
    }

    public boolean worldExists(String name) {
        return Bukkit.getWorldContainer().toPath().resolve(name).toFile().exists();
    }

    public String getWorldName(Player player) {
        return "world_" + player.getUniqueId();
    }

    public File getWorldFolder(UUID uuid) {
        String worldName = "world_" + uuid;
        return Bukkit.getWorldContainer().toPath().resolve(worldName).toFile();
    }

    public void generateWorld(String name) {
        World world = Bukkit.createWorld(new WorldCreator(name)
                .generator(new FlatWorldGenerator()));

        if (world != null) {
            WorldBorder worldBorder = world.getWorldBorder();

            worldBorder.setCenter(0, 0);
            worldBorder.setSize(512);

            loadedWorlds.put(name, world);
        }
    }

    public void createWorld(Player player) {
        String name = getWorldName(player);

        if (worldExists(name)) return;

        generateWorld(name);
    }

    public void loadWorld(Player player) {
        String name = getWorldName(player);

        if (loadedWorlds.containsKey(name)) {
            loadedWorlds.get(name);
            return;
        }
        if (!worldExists(name)) return;


        generateWorld(name);
    }

    public void unloadWorld(Player player, boolean save) {
        String name = getWorldName(player);

        World world = loadedWorlds.remove(name);
        if (world != null) {
            Bukkit.unloadWorld(world, save);
        }
    }

    public boolean deleteWorld(Player player) {
        String name = getWorldName(player);

        World world = Bukkit.getWorld(name);
        if (world != null) {
            world.getPlayers().forEach(p -> {
                translator.sendPrefixed(p, "world.deleted");
                p.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
            });
        }

        if (worldExists(name)) {
            log.info("Deleting world: {}", name);
            Bukkit.unloadWorld(name, false);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (deleteWorldDirectory(name)) {
                    log.info("World directory deleted successfully: {}", name);
                } else {
                    log.error("Failed to delete world directory: {}", name);
                }
            }, 20L);
            loadedWorlds.remove(name);
            return true;
        }
        return false;
    }

    public World getLoadedWorld(Player player) {
        String name = getWorldName(player);
        return loadedWorlds.get(name);
    }

    public World getLoadedWorld(String name) {
        return loadedWorlds.get(name);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean deleteWorldDirectory(String worldName) {
        File worldFolder = Bukkit.getWorldContainer().toPath().resolve(worldName).toFile();
        try {
            Files.walk(worldFolder.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to delete world directory: " + worldName);
            return false;
        }
        return !worldFolder.exists();
    }
}

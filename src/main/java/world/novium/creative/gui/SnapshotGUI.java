package world.novium.creative.gui;

import com.google.inject.Inject;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import world.novium.creative.common.managers.WorldManager;
import world.novium.creative.utils.MessageUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SnapshotGUI {

    private final int SNAPSHOTS_PER_PAGE = 21;

    @Inject
    private WorldManager worldManager;

    @Inject
    private PanelGUI panelGUI;

    public Gui buildSnapshotGUI(Player player, int page) {
        Gui gui = Gui.gui()
                .title(MessageUtils.parse("<gradient:#00ff00:#00ffff>Welt-Snapshots</gradient>"))
                .rows(6)
                .disableAllInteractions()
                .create();
        
        if (!worldManager.worldExists(worldManager.getWorldName(player))) {
            gui.setItem(3, 5, ItemBuilder.from(Material.BARRIER)
                    .name(MessageUtils.parse("<red>Keine Welt gefunden"))
                    .lore(MessageUtils.parse("<gray>Du musst zuerst eine Welt erstellen!"))
                    .asGuiItem());

            addNavigationItems(gui, player, page, new String[0]);
            return gui;
        }

        String[] snapshots = worldManager.listSnapshots(player.getUniqueId());

        if (snapshots.length == 0) {
            gui.setItem(3, 5, ItemBuilder.from(Material.PAPER)
                    .name(MessageUtils.parse("<yellow>Keine Snapshots vorhanden"))
                    .lore(MessageUtils.parse("<gray>Erstelle deinen ersten Snapshot!"))
                    .asGuiItem());
        } else {
            populateSnapshots(gui, player, snapshots, page);
        }

        gui.setItem(6, 2, ItemBuilder.from(Material.ARROW)
                .name(MessageUtils.parse("<green>Neuen Snapshot erstellen"))
                .lore(MessageUtils.parse(
                        "<gray>Erstelle einen Snapshot deiner aktuellen Welt."
                ), MessageUtils.parse("<yellow>Klicke, um einen automatischen Snapshot zu erstellen.")
                )
                .asGuiItem(event -> {
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                    String snapshotName = "snapshot_" + timestamp;

                    gui.close(player);
                    player.sendMessage(MessageUtils.parse("<yellow>Erstelle Snapshot..."));

                    worldManager.createSnapshot(player.getUniqueId(), snapshotName);

                    player.sendMessage(MessageUtils.parse("<green>Snapshot" + " '" + snapshotName + "' wird erstellt..."));
                }));

        addNavigationItems(gui, player, page, snapshots);

        return gui;
    }

    private void populateSnapshots(Gui gui, Player player, String[] snapshots, int page) {
        int startIndex = page * SNAPSHOTS_PER_PAGE;
        int endIndex = Math.min(startIndex + SNAPSHOTS_PER_PAGE, snapshots.length);

        int slot = 10;
        int itemsInRow = 0;

        for (int i = startIndex; i < endIndex; i++) {
            String snapshot = snapshots[i];
            String displayName = snapshot.replace(".zip", "");

            if (itemsInRow >= 7) {
                slot += 2;
                itemsInRow = 0;
            }

            List<String> lore = new ArrayList<>();
            lore.add("<gray>Snapshot: <white>" + displayName);
            lore.add("");
            lore.add("<green>Linksklick: <white>Snapshot laden");
            lore.add("<red>Rechtsklick: <white>Snapshot löschen");
            lore.add("");
            lore.add("<yellow>⚠ Warnung: Das Laden überschreibt deine aktuelle Welt!");

            List<Component> loreComponents = new ArrayList<>();

            for (String line : lore) {
                loreComponents.add(MessageUtils.parse(line));
            }



            GuiItem snapshotItem = ItemBuilder.from(Material.FILLED_MAP)
                    .name(MessageUtils.parse("<aqua>" + displayName))
                    .lore(loreComponents.toArray(new Component[0]))
                    .asGuiItem(event -> {
                        if (event.getClick() == ClickType.LEFT) {
                            // Load snapshot
                            gui.close(player);
                            player.sendMessage(MessageUtils.parse("<yellow>Lade Snapshot..."));

                            if (worldManager.loadSnapshot(player.getUniqueId(), displayName)) {
                                player.sendMessage(MessageUtils.parse("<green>Snapshot erfolgreich geladen!"));
                                worldManager.loadWorld(player);
                                if (worldManager.getLoadedWorld(player) != null) {
                                    player.teleport(worldManager.getLoadedWorld(player).getSpawnLocation());
                                }
                            } else {
                                player.sendMessage(MessageUtils.parse("<red>Fehler beim Laden des Snapshots!"));
                            }
                        } else if (event.getClick() == ClickType.RIGHT) {
                            openDeleteConfirmation(player, displayName);
                        }
                    });

            gui.setItem(slot, snapshotItem);
            slot++;
            itemsInRow++;
        }
    }

    private void openDeleteConfirmation(Player player, String snapshotName) {
        Gui confirmGui = Gui.gui()
                .title(MessageUtils.parse("<red>Snapshot löschen bestätigen"))
                .rows(3)
                .disableAllInteractions()
                .create();

        List<Component> lore = new ArrayList<>();

        lore.add(MessageUtils.parse("<gray>Möchtest du den Snapshot wirklich löschen?"));
        lore.add(MessageUtils.parse(""));
        lore.add(MessageUtils.parse("<white>" + snapshotName));
        lore.add(MessageUtils.parse(""));
        lore.add(MessageUtils.parse("<red>Diese Aktion kann nicht rückgängig gemacht werden!"));

        confirmGui.setItem(2, 5, ItemBuilder.from(Material.PAPER)
                .name(MessageUtils.parse("<yellow>Snapshot löschen?"))
                .lore(lore.toArray(new Component[0]))
                .asGuiItem());

        confirmGui.setItem(3, 3, ItemBuilder.from(Material.RED_CONCRETE)
                .name(MessageUtils.parse("<red>Ja, löschen"))
                .asGuiItem(event -> {
                    confirmGui.close(player);

                    if (worldManager.deleteSnapshot(player.getUniqueId(), snapshotName)) {
                        player.sendMessage(MessageUtils.parse("<green>Snapshot erfolgreich gelöscht!"));
                    } else {
                        player.sendMessage(MessageUtils.parse("<red>Fehler beim Löschen des Snapshots!"));
                    }

                    buildSnapshotGUI(player, 0).open(player);
                }));

        confirmGui.setItem(3, 7, ItemBuilder.from(Material.GREEN_CONCRETE)
                .name(MessageUtils.parse("<green>Abbrechen"))
                .asGuiItem(event -> {
                    confirmGui.close(player);
                    buildSnapshotGUI(player, 0).open(player);
                }));

        confirmGui.open(player);
    }

    private void addNavigationItems(Gui gui, Player player, int page, String[] snapshots) {
        int totalPages = (int) Math.ceil((double) snapshots.length / SNAPSHOTS_PER_PAGE);

        if (page > 0) {
            gui.setItem(6, 4, ItemBuilder.from(Material.ARROW)
                    .name(MessageUtils.parse("<yellow>Vorherige Seite"))
                    .lore(MessageUtils.parse("<gray>Seite " + page + " von " + Math.max(1, totalPages)))
                    .asGuiItem(event -> buildSnapshotGUI(player, page - 1).open(player)));
        }

        if (page < totalPages - 1) {
            gui.setItem(6, 6, ItemBuilder.from(Material.ARROW)
                    .name(MessageUtils.parse("<yellow>Nächste Seite"))
                    .lore(MessageUtils.parse("<gray>Seite " + (page + 2) + " von " + totalPages))
                    .asGuiItem(event -> buildSnapshotGUI(player, page + 1).open(player)));
        }

        if (totalPages > 1) {
            gui.setItem(6, 5, ItemBuilder.from(Material.BOOK)
                    .name(MessageUtils.parse("<white>Seite " + (page + 1) + " von " + totalPages))
                    .lore(MessageUtils.parse("<gray>Snapshots: " + snapshots.length))
                    .asGuiItem());
        }

        gui.setItem(6, 8, ItemBuilder.from(Material.BARRIER)
                .name(MessageUtils.parse("<red>Zurück"))
                .lore(MessageUtils.parse("<gray>Zurück zum Hauptmenü"))
                .asGuiItem(event -> {
                    gui.close(player);
                    panelGUI.buildGUI(player).open(player);
                }));

        gui.setItem(6, 9, ItemBuilder.from(Material.BARRIER)
                .name(MessageUtils.parse("<red>Schließen"))
                .asGuiItem(event -> gui.close(player)));
    }
}
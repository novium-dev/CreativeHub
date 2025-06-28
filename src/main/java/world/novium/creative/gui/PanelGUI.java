package world.novium.creative.gui;

import com.google.inject.Inject;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import world.novium.creative.managers.PlotManager;
import world.novium.creative.managers.WorldManager;
import world.novium.creative.utils.MessageUtils;

@RequiredArgsConstructor
public class PanelGUI {
    @Inject
    private WorldManager worldManager;

    @Inject
    private PlotManager plotManager;

    @Inject
    private SnapshotGUI snapshotGui;

    public Gui buildGUI(Player player) {
        Gui gui = Gui.gui()
                .title(MessageUtils.parse("<gradient:#ff0000:#ff9900>Creative Hub</gradient>"))
                .rows(2)
                .disableAllInteractions()
                .create();

        GuiItem createWorld = ItemBuilder.from(Material.RED_SAND)
                .name(MessageUtils.parse("<green>Welt erstellen"))
                .lore(MessageUtils.parse(
                        "<gray>Erstelle eine neue Welt, um deine Bauprojekte zu starten."
                ))
                .asGuiItem(event -> {
                    if (worldManager.worldExists(worldManager.getWorldName(player))) {
                        gui.close(player);
                        MessageUtils.send(player, "<red>Du hast bereits eine Welt erstellt!");
                        return;
                    }

                    worldManager.createWorld(player);
                    MessageUtils.send(player, "<green>Welt erfolgreich erstellt!");

                    player.teleport(worldManager.getLoadedWorld(player).getSpawnLocation());
                });

        GuiItem tpWorld = ItemBuilder.from(Material.ENDER_PEARL)
                .name(MessageUtils.parse("<green>Zur Welt teleportieren"))
                .asGuiItem(event -> {
                    if (!worldManager.worldExists(worldManager.getWorldName(player))) {
                        gui.close(player);
                        MessageUtils.send(player, "<red>Du hast noch keine Welt erstellt!");
                        return;
                    }

                    worldManager.loadWorld(player);
                    MessageUtils.send(player, "<green>Teleportiere dich zur Welt...");
                    player.teleport(worldManager.getLoadedWorld(player).getSpawnLocation());
                });
        GuiItem deleteWorld = ItemBuilder.from(Material.TNT)
                .name(MessageUtils.parse("<red>Welt löschen"))
                .asGuiItem(event -> {
                    if (!worldManager.worldExists(worldManager.getWorldName(player))) {
                        gui.close(player);
                        MessageUtils.send(player, "<red>Du hast noch keine Welt erstellt!");
                        return;
                    }

                    boolean success = worldManager.deleteWorld(player);
                    if (success) {
                        MessageUtils.send(player, "<green>Welt erfolgreich gelöscht!");
                    } else {
                        MessageUtils.send(player, "<red>Fehler beim Löschen der Welt!");
                    }
                });

        GuiItem worldSnapshots = ItemBuilder.from(Material.DIAMOND_PICKAXE)
                .name(MessageUtils.parse("<green>Welt-Snapshots"))
                .lore(MessageUtils.parse(
                        "<gray>Verwalte Snapshots deiner Welt, um verschiedene Bauphasen zu speichern."
                ))
                .asGuiItem(event -> snapshotGui.buildSnapshotGUI(player, 0).open(player));

        GuiItem createPlot = ItemBuilder.from(Material.GRASS_BLOCK)
                .name(MessageUtils.parse("<green>Grundstück erstellen"))
                .lore(MessageUtils.parse(
                        "<gray>Erstelle ein neues Grundstück, um deine Bauprojekte zu starten."
                ))
                .asGuiItem(event -> {
                    if (plotManager.getPlots(player).isEmpty()) {
                        player.performCommand("p auto");
                        MessageUtils.send(player, "<green>Grundstück erfolgreich erstellt!");
                    } else {
                        MessageUtils.send(player, "<red>Du hast bereits ein Grundstück!");
                    }
                });

        GuiItem tpPlot = ItemBuilder.from(Material.COMPASS)
                .name(MessageUtils.parse("<green>Zum Grundstück teleportieren"))
                .asGuiItem(event -> {
                    if (plotManager.getPlots(player).isEmpty()) {
                        gui.close(player);
                        MessageUtils.send(player, "<red>Du hast noch kein Grundstück erstellt!");
                        return;
                    }

                    player.performCommand("p home");
                });

        GuiItem deletePlot = ItemBuilder.from(Material.BARRIER)
                .name(MessageUtils.parse("<red>Grundstück löschen"))
                .asGuiItem(event -> {
                    if (plotManager.getPlots(player).isEmpty()) {
                        gui.close(player);
                        MessageUtils.send(player, "<red>Du hast noch kein Grundstück erstellt!");
                        return;
                    }

                    player.performCommand("p delete confirm");
                });

        gui.setItem(1, 4, createWorld);
        gui.setItem(1, 5, tpWorld);
        gui.setItem(1, 6, deleteWorld);
        gui.setItem(1, 7, worldSnapshots);
        gui.setItem(2, 4, createPlot);
        gui.setItem(2, 5, tpPlot);
        gui.setItem(2, 6, deletePlot);
        gui.setItem(1, 1, ItemBuilder.from(Material.BARRIER)
                .name(MessageUtils.parse("<red>Schließen"))
                .asGuiItem(event -> gui.close(player)));

        return gui;
    }
}

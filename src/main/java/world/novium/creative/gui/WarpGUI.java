package world.novium.creative.gui;

import com.google.inject.Inject;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import world.novium.creative.common.managers.TranslationManager;
import world.novium.creative.common.managers.WarpManager;
import world.novium.creative.utils.MessageUtils;

import java.util.Collection;

public class WarpGUI {

    @Inject
    private WarpManager warpManager;


    @Inject
    private TranslationManager translator;

    /**
     * Builds and returns a GUI for displaying warps.
     * This GUI will show all warps managed by the provided WarpManager,
     * allowing players to click on an item to teleport to the corresponding warp location.
     *
     * @param player The player for whom the GUI is being built.
     * @return A Gui object populated with warp items and a close button.
     */
    public Gui buildGUI(Player player) {
        Gui gui = Gui.gui()
                .title(MessageUtils.parse("<gradient:#00FFFF:#007FFF>Warps</gradient>"))
                .rows(3)
                .disableAllInteractions()
                .create();

        Collection<WarpManager.Warp> warps = warpManager.getWarps();

        int slot = 0;

        for (WarpManager.Warp warp : warps) {
            if (slot >= (gui.getRows() - 1) * 9) {
                break;
            }

            // Create a GUI item for the current warp
            GuiItem warpItem = ItemBuilder.from(Material.ENDER_PEARL)
                    .name(MessageUtils.parse("<green>" + warp.name()))
                    .lore(
                            translator.translate(player, "warp.teleport_lore",
                                    TagResolver.builder()
                                            .tag("warp", Tag.inserting(Component.text(warp.name())))
                                            .build())
                    )
                    .asGuiItem(event -> {
                        player.teleport(warp.location());
                        gui.close(player);
                        translator.sendPrefixed(player, "warp.teleported", TagResolver.builder().tag(
                                "warp", Tag.inserting(Component.text(warp.name())
                        )).build());
                    });

            gui.setItem(slot, warpItem);
            slot++;
        }

        GuiItem closeButton = ItemBuilder.from(Material.BARRIER)
                .name(MessageUtils.parse("<red>Schließen</red>"))
                .asGuiItem(event -> gui.close(player));

        gui.setItem(gui.getRows() - 1, 4, closeButton);

        return gui;
    }
}

package world.novium.creative.commands.impl;

import com.google.inject.Inject;
import dev.jorel.commandapi.CommandTree;
import dev.triumphteam.gui.guis.Gui;
import world.novium.creative.commands.Command;
import world.novium.creative.annotations.RegisterCommand;
import world.novium.creative.gui.PanelGUI;

@RegisterCommand
public class PanelCommand implements Command {
    @Inject
    private PanelGUI panelGUI;

    @Override
    public CommandTree build() {
        return new CommandTree("panel")
                .executesPlayer((player, args) -> {
                    Gui gui = panelGUI.buildGUI(player);

                    gui.open(player);
                });
    }
}

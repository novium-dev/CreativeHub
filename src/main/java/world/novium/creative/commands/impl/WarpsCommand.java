package world.novium.creative.commands.impl;

import com.google.inject.Inject;
import dev.jorel.commandapi.CommandTree;
import dev.triumphteam.gui.guis.Gui;
import world.novium.creative.commands.Command;
import world.novium.creative.common.annotations.RegisterCommand;
import world.novium.creative.gui.WarpGUI;

@RegisterCommand
public class WarpsCommand implements Command {
    @Inject
    private WarpGUI warpGUI;

    @Override
    public CommandTree build() {
        return new CommandTree("warps")
                .executesPlayer((player, args) -> {
                    Gui gui = warpGUI.buildGUI(player);

                    gui.open(player);
                });
    }
}

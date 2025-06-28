package world.novium.creative.commands.impl;

import com.google.inject.Inject;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.triumphteam.gui.guis.Gui;
import world.novium.creative.commands.Command;
import world.novium.creative.common.annotations.RegisterCommand;
import world.novium.creative.gui.WarpGUI;
import world.novium.creative.common.managers.WarpManager;
import world.novium.creative.utils.MessageUtils;

@RegisterCommand
public class WarpCommand implements Command {
    @Inject
    private WarpGUI warpGUI;

    @Inject
    private WarpManager manager;

    @Override
    public CommandTree build() {
        return new CommandTree("warp")
                .executesPlayer((player, args) -> {
                    Gui gui = warpGUI.buildGUI(player);

                    gui.open(player);
                })
                .then(new LiteralArgument("set")
                        .withPermission("novium.warp.set")
                        .then(
                                new StringArgument("warpName")
                                .then(new GreedyStringArgument("styledName")
                                        .executesPlayer((player, args) -> {
                                            String warpName = (String) args.get(0);
                                            String styledName = (String) args.get(1);

                                            if (warpName == null || warpName.isEmpty()) {
                                                MessageUtils.send(player, "<red>Warp name cannot be null or empty.");
                                                return;
                                            }

                                            if (styledName == null || styledName.isEmpty()) {
                                                styledName = warpName;
                                            }

                                            if (manager.getWarp(warpName).isPresent()) {
                                                MessageUtils.send(player, "<red>Warp '" + warpName + "' already exists. Use /warp delete to remove it first.");
                                                return;
                                            }

                                            manager.saveWarp(warpName, player.getLocation(), styledName);

                                            MessageUtils.send(player, "<green>Warp '" + warpName + "' set to your current location with styled name: " + styledName);
                                        })
                                )
                                .executesPlayer((player, args) -> {
                                    String warpName = (String) args.get(0);

                                    if (warpName == null || warpName.isEmpty()) {
                                        MessageUtils.send(player, "<red>Warp name cannot be null or empty.");
                                        return;
                                    }

                                    if (manager.getWarp(warpName).isPresent()) {
                                        MessageUtils.send(player, "<red>Warp '" + warpName + "' already exists. Use /warp delete to remove it first.");
                                        return;
                                    }

                                    manager.saveWarp(warpName, player.getLocation(), warpName);

                                    MessageUtils.send(player, "<green>Warp '" + warpName + "' set to your current location with styled name: " + warpName);
                                })
                        )
                )
                .then(new LiteralArgument("delete")
                        .withPermission("novium.warp.delete")
                        .then(new StringArgument("warpName")
                                .replaceSuggestions(ArgumentSuggestions.strings(info ->
                                        manager.getWarpNames().toArray(new String[0])
                                ))
                                .executesPlayer((player, args) -> {
                                    String warpName = (String) args.get(0);

                                    if (warpName == null || warpName.isEmpty()) {
                                        MessageUtils.send(player, "<red>Warp name cannot be null or empty.");
                                        return;
                                    }

                                    if (!manager.deleteWarp(warpName)) {
                                        MessageUtils.send(player, "<red>Warp '" + warpName + "' does not exist.");
                                        return;
                                    }

                                    MessageUtils.send(player, "<green>Warp '" + warpName + "' has been deleted.");
                                })
                        )
                )
                .then(
                        new StringArgument("warpName")
                                .replaceSuggestions(ArgumentSuggestions.strings(info ->
                                        manager.getWarpNames().toArray(new String[0])
                                ))
                                .executesPlayer((player, args) -> {
                                    String warpName = (String) args.get(0);

                                    if (warpName == null || warpName.isEmpty()) {
                                        MessageUtils.send(player, "<red>Warp name cannot be null or empty.");
                                        return;
                                    }

                                    manager.getWarp(warpName).ifPresentOrElse(
                                            warp -> {
                                                player.teleport(warp.location());
                                                MessageUtils.send(player, "<green>Teleported to warp '" + warp.name() + "'.");
                                            },
                                            () -> MessageUtils.send(player, "<red>Warp '" + warpName + "' does not exist.")
                                    );
                                })
                        );

    }
}

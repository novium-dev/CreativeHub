package world.novium.creative.commands.impl;

import com.google.inject.Inject;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import world.novium.creative.commands.Command;
import world.novium.creative.common.annotations.RegisterCommand;
import world.novium.creative.common.managers.TranslationManager;
import world.novium.creative.gui.WarpGUI;
import world.novium.creative.common.managers.WarpManager;

@RegisterCommand
public class WarpCommand implements Command {
    @Inject
    private WarpGUI warpGUI;

    @Inject
    private WarpManager manager;

    @Inject
    private TranslationManager translator;

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
                                                        translator.sendPrefixed(player, "warp.name_cannot_be_empty");
                                                        return;
                                                    }

                                                    if (styledName == null || styledName.isEmpty()) {
                                                        styledName = warpName;
                                                    }

                                                    if (manager.getWarp(warpName).isPresent()) {
                                                        translator.sendPrefixed(player, "warp.name_exists",
                                                                TagResolver.builder()
                                                                        .tag("warp", Tag.inserting(Component.text(warpName)))
                                                                        .build());
                                                        return;
                                                    }

                                                    manager.saveWarp(warpName, player.getLocation(), styledName);

                                                    translator.sendPrefixed(player, "warp.set",
                                                            TagResolver.builder()
                                                                    .tag("warp", Tag.inserting(Component.text(warpName)))
                                                                    .tag("styled", Tag.inserting(Component.text(styledName)))
                                                                    .build()
                                                    );
                                                })
                                        )
                                        .executesPlayer((player, args) -> {
                                            String warpName = (String) args.get(0);

                                            if (warpName == null || warpName.isEmpty()) {
                                                translator.sendPrefixed(player, "warp.name_cannot_be_empty");
                                                return;
                                            }

                                            if (manager.getWarp(warpName).isPresent()) {
                                                translator.sendPrefixed(player, "warp.name_exists",
                                                        TagResolver.builder()
                                                                .tag("warp", Tag.inserting(Component.text(warpName)))
                                                                .build());
                                                return;
                                            }

                                            manager.saveWarp(warpName, player.getLocation(), warpName);

                                            translator.sendPrefixed(player, "warp.set",
                                                    TagResolver.builder()
                                                            .tag("warp", Tag.inserting(Component.text(warpName)))
                                                            .tag("styled", Tag.inserting(Component.text(warpName)))
                                                            .build()
                                            );
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
                                        translator.sendPrefixed(player, "warp.name_cannot_be_empty");
                                        return;
                                    }

                                    if (!manager.deleteWarp(warpName)) {
                                        translator.sendPrefixed(player, "warp.not_exists",
                                                TagResolver.builder()
                                                        .tag("warp", Tag.inserting(Component.text(warpName)))
                                                        .build());
                                        return;
                                    }

                                    translator.sendPrefixed(player, "warp.deleted",
                                            TagResolver.builder()
                                                    .tag("warp", Tag.inserting(Component.text(warpName)))
                                                    .build());
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
                                        translator.sendPrefixed(player, "warp.name_cannot_be_empty");
                                        return;
                                    }

                                    manager.getWarp(warpName).ifPresentOrElse(
                                            warp -> {
                                                player.teleport(warp.location());
                                                translator.sendPrefixed(player, "warp.teleported",
                                                        TagResolver.builder()
                                                                .tag("warp", Tag.inserting(Component.text(warp.name())))
                                                                .build());
                                            },
                                            () -> translator.sendPrefixed(player, "warp.not_exists",
                                                    TagResolver.builder()
                                                            .tag("warp", Tag.inserting(Component.text(warpName)))
                                                            .build())
                                    );
                                })
                );
    }
}

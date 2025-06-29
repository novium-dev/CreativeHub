package world.novium.creative.commands.impl;

import com.google.inject.Inject;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import world.novium.creative.commands.Command;
import world.novium.creative.common.Settings;
import world.novium.creative.common.annotations.RegisterCommand;
import world.novium.creative.common.managers.SettingsManager;
import world.novium.creative.common.managers.TranslationManager;

@RegisterCommand
public class SettingsCommand implements Command {

    @Inject
    private SettingsManager settingsManager;

    @Inject
    private TranslationManager translator;

    @Override
    public CommandTree build() {
        return new CommandTree("settings")
                .withPermission("novium.creative.settings")
                .then(new LiteralArgument("maintenance")
                        .then(
                                new LiteralArgument("set")
                                        .withPermission("novium.creative.settings.maintenance.set")
                                        .then(new BooleanArgument("enabled")
                                                .executesPlayer((player, args) -> {
                                                    boolean enabled = (boolean) args.get("enabled");

                                                    Settings settings = settingsManager.getSettings();
                                                    settings.setMaintenanceMode(enabled);
                                                    settingsManager.update(settings);
                                                    translator.sendPrefixed(player, "settings.set",
                                                            TagResolver.builder()
                                                                    .tag("setting", Tag.inserting(Component.text("maintenance")))
                                                                    .tag("value", Tag.inserting(Component.text(enabled ? "true" : "false")))
                                                                    .build()
                                                    );
                                                })
                                        )
                        )
                        .then(new LiteralArgument("get")
                                .executesPlayer((player, args) -> {
                                    Settings settings = settingsManager.getSettings();
                                    boolean enabled = settings.isMaintenanceMode();
                                    translator.sendPrefixed(player, "settings.get",
                                            TagResolver.builder()
                                                    .tag("setting", Tag.inserting(Component.text("maintenance")))
                                                    .tag("value", Tag.inserting(Component.text(enabled ? "true" : "false")))
                                                    .build()
                                    );
                                })
                        )
                );
    }
}

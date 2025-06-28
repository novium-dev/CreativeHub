package world.novium.creative.commands.impl;

import com.google.inject.Inject;
import dev.jorel.commandapi.CommandTree;
import world.novium.creative.commands.Command;
import world.novium.creative.common.annotations.RegisterCommand;
import world.novium.creative.common.managers.TranslationManager;

@RegisterCommand
public class TestCommand implements Command {
    @Inject
    private TranslationManager translator;

    @Override
    public CommandTree build() {
        return new CommandTree("test")
                .executesPlayer((player, args) -> {
                    translator.sendPrefixed(player, "test");
                });
    }
}

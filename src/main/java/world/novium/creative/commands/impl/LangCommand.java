package world.novium.creative.commands.impl;

import com.google.inject.Inject;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import world.novium.creative.commands.Command;
import world.novium.creative.common.annotations.RegisterCommand;
import world.novium.creative.common.managers.TranslationManager;

import java.util.Objects;

@RegisterCommand
public class LangCommand implements Command {
    @Inject
    private TranslationManager translator;

    @Override
    public CommandTree build() {
        return new CommandTree("lang")
                .withPermission("novium.creative.lang")
                .then(new LiteralArgument("reload")
                        .executesPlayer((player, args) -> {
                            translator.reloadTranslations();
                            translator.sendPrefixed(player, "lang.reload.success");
                        })
                ).then(new LiteralArgument("parse")
                        .then(new GreedyStringArgument("message")
                                .executesPlayer((player, args) -> {
                                    String message = Objects.requireNonNull(args.get("message").toString());
                                    translator.send(player, message);
                                })
                        )
                );
    }
}

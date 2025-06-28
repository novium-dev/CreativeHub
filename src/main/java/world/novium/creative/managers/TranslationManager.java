package world.novium.creative.managers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import world.novium.creative.common.StartupHook;
import world.novium.creative.common.Translation;
import world.novium.creative.utils.MessageUtils;

import java.io.File;
import java.util.*;

@AllArgsConstructor
@Slf4j
public class TranslationManager implements StartupHook {
    private final Map<Locale, List<Translation>> translations = new HashMap<>();

    private final File dataFolder;

    public Optional<Translation> getTranslation(Locale language, String key) {
        Locale locale = translations.containsKey(language)
                ? language
                : Locale.ENGLISH;

        return translations.getOrDefault(locale, Collections.emptyList())
                .stream()
                .filter(translation -> translation.getKey().equals(key))
                .findFirst();
    }

    public Component getTranslationComponent(Locale language, String key, TagResolver... tagResolvers) {
        Optional<Translation> translation = getTranslation(language, key);

        if (translation.isPresent()) {
            return MessageUtils.parse(translation.get().getValue());
        } else {
            log.warn("Translation for key '{}' in language '{}' not found.", key, language);
            return MessageUtils.parse(
                "<red>Translation for key <gray>'" + key + "' <red>in language <gra>'" + language + "' <red>not found."
            );
        }
    }

    public Component translate(Player player, String key, TagResolver... tagResolvers) {
        Locale language = player.locale();

        Component translation = getTranslationComponent(language, key, tagResolvers);

        return translation;
    }

    public void send(Player player, String key, TagResolver... tagResolvers) {
        Component translation = translate(player, key, tagResolvers);

        player.sendMessage(translation);
    }

    public void sendPrefixed(Player player, String key, TagResolver... tagResolvers) {
        Component translation = translate(player, key, tagResolvers);

        player.sendMessage(MessageUtils.addPrefix(translation));
    }

    @Override
    public void onStartup() {
        File translationsFile = new File(dataFolder, "lang");
        if (!translationsFile.exists()) {
            translationsFile.mkdirs();
        }

        File[] files = translationsFile.listFiles((dir, name) -> name.endsWith(".yaml"));

        if (files == null || files.length == 0) {
            log.error("No translation files found in {}", translationsFile.getAbsolutePath());
            List<Locale> locales = Arrays.asList(Locale.GERMAN, Locale.ENGLISH);

            for (Locale locale : locales) {
                File translationFile = new File(translationsFile, locale.toLanguageTag() + ".yaml");
                if (!translationFile.exists()) {
                    try {
                        translationFile.createNewFile();
                        log.info("Created translation file: {}", translationFile.getName());
                    } catch (Exception e) {
                        log.error("Failed to create translation file: {}", translationFile.getName(), e);
                    }
                }
            }
            return;
        }

        for (File file : files) {
            log.info("Loading translations from file: {}", file.getName());
            String languageCode = file.getName().replace(".yaml", "");
            Locale language = Locale.forLanguageTag(languageCode);

            var config = YamlConfiguration.loadConfiguration(translationsFile);

            for (String key : config.getKeys(false)) {
                String message = config.getString(key);
                if (message != null) {
                    Translation translation = new Translation(key, message);
                    translations.computeIfAbsent(language, k -> new ArrayList<>()).add(translation);
                } else {
                    log.error("Translation key '{}' in file '{}' is null.", key, file.getName());
                }
            }
        }

        log.info("Loaded {} languages with translations.", translations.size());
    }
}
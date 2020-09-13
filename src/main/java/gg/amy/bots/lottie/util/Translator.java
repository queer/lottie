package gg.amy.bots.lottie.util;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import gg.amy.bots.lottie.data.GuildSettings;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author amy
 * @since 9/8/20.
 */
public class Translator {
    private static final Map<String, JsonObject> LANGS = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(Translator.class);
    private static boolean PRELOADED;

    private Translator() {
    }

    public static void preload() {
        if(PRELOADED) {
            return;
        }
        PRELOADED = true;

        IOUtils.scan("lang", e -> {
            if(e.getName().endsWith(".json")) {
                try(final InputStream is = Translator.class.getResourceAsStream('/' + e.getName())) {
                    final String json = new String(IOUtils.readFully(is));
                    loadTranslation(e.getName().replace(".json", "")
                            .replace("lang/", ""), JsonParser.object().from(json));
                } catch(final IOException | JsonParserException err) {
                    throw new RuntimeException(err);
                }
            }
        });
        LOGGER.info("Loaded {} translations.", LANGS.size());
    }

    static void loadTranslation(@Nonnull final String lang, @Nonnull final JsonObject translation) {
        LANGS.put(lang, translation);
    }

    public static String translate(@Nonnull final String lang, @Nonnull final String key) {
        if(LANGS.containsKey(lang) && !key.isEmpty()) {
            final JsonObject translations = LANGS.get(lang);
            final String[] split = key.split("\\.");
            if(split.length == 1) {
                if(translations.has(key)) {
                    return translations.getString(key);
                } else {
                    // Try to fetch with en_US
                    if(!lang.equals("en_US")) {
                        try {
                            //noinspection TailRecursion
                            return translate("en_US", key);
                        } catch(final Exception e) {
                            throw new IllegalArgumentException('`' + key + "` is not a valid key for `" + lang + "`!");
                        }
                    } else {
                        throw new IllegalArgumentException('`' + key + "` is not a valid key for `" + lang + "` or `en_US`!");
                    }
                }
            } else {
                try {
                    JsonObject target = translations;
                    for(int i = 0; i <= split.length - 2; i++) {
                        target = target.getObject(split[i]);
                    }
                    return target.getString(split[split.length - 1]);
                } catch(final Exception e) {
                    throw new IllegalArgumentException('`' + key + "` is not a valid key for `" + lang + "`!", e);
                }
            }
        } else {
            throw new IllegalArgumentException('`' + lang + "` is not a valid language!");
        }
    }

    /**
     * Shortcut to {@link #translate(String, String)}.
     */
    public static String $(@Nonnull final String lang, @Nonnull final String key) {
        return translate(lang, key);
    }

    /**
     * Translations that don't require {@link String#replace(CharSequence, CharSequence)}.
     */
    public static Translation $$(@Nonnull final String lang, @Nonnull final String key) {
        return new Translation($(lang, key));
    }

    @Value
    public static class Translation {
        String value;

        /**
         * Replace the given key with the given value.
         *
         * @param key   The key to replace, NOT prefixed with {@code $}.
         * @param value The value that replaces the key.
         * @return A new {@link Translation} with the changes applied.
         */
        public Translation t(@Nonnull final String key, @Nonnull final String value) {
            return new Translation(this.value.replace('$' + key, value));
        }

        /**
         * Replace number-named keys with the emoji that represents their
         * value.
         *
         * @param number The integer of the emoji, on the range {@code [0, 9]}.
         * @return A new {@link Translation} with the changes applied.
         */
        public Translation en(@Nonnegative final int number) {
            return new Translation(value.replace('$' + Utils.integerToName(number), Utils.numberToEmoji(number)));
        }

        /**
         * @return A string with all the changes applied.
         */
        public String $() {
            return value;
        }
    }
}

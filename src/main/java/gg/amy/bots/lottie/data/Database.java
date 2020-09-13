package gg.amy.bots.lottie.data;

/**
 * @author amy
 * @since 9/8/20.
 */
public class Database {
    public GuildSettings guildSettings(final String guildId) {
        // TODO: Don't mock this
        return GuildSettings.builder()
                .id(guildId)
                .language("en_US")
                .build();
    }

    public UserSettings userSettings(final String userId) {
        // TODO: Don't mock this
        return UserSettings.builder()
                .id(userId)
                .language("en_US")
                .build();
    }

    // TODO: Way to set guild settings
    // TODO: Actual database lmao
}

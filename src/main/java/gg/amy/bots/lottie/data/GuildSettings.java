package gg.amy.bots.lottie.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

/**
 * @author amy
 * @since 9/8/20.
 */
@Getter
@Builder
public class GuildSettings {
    private String id;
    private String language = "en_US";
}

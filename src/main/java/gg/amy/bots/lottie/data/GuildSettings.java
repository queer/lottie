package gg.amy.bots.lottie.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author amy
 * @since 9/8/20.
 */
@Getter
@Setter
@Builder
public class GuildSettings {
    private String id;
    private String language = "en_US";
    private String prefix = "!";
}

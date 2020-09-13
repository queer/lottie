package gg.amy.bots.lottie.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author amy
 * @since 9/12/20.
 */
@Getter
@Setter
@Builder
public class UserSettings {
    private String id;
    private String language = "en_US";
}

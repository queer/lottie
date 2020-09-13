package gg.amy.bots.lottie.command;

import com.mewna.catnip.entity.channel.TextChannel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.user.User;
import gg.amy.bots.lottie.Lottie;
import gg.amy.bots.lottie.data.GuildSettings;
import gg.amy.bots.lottie.data.UserSettings;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author amy
 * @since 9/12/20.
 */
@Value
@Builder
public class Context {
    Lottie lottie;
    User user;
    Guild guild;
    TextChannel channel;
    List<User> mentions;
    GuildSettings guildSettings;
    UserSettings userSettings;

    public List<Member> members() {
        return mentions.stream().map(User::id)
                .map(guild::member)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
    }

    public String effectiveLanguage() {
        if(userSettings.language() != null) {
            return userSettings.language();
        } else {
            return guildSettings.language();
        }
    }
}

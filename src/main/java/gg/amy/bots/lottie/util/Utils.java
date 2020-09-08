package gg.amy.bots.lottie.util;

import com.mewna.catnip.entity.channel.MessageChannel;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.message.ReactionUpdate;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.shard.DiscordEvent;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author amy
 * @since 9/7/20.
 */
public final class Utils {
    private static final Map<String, Integer> EMOJIS_TO_INTEGERS = Map.of(
            "0️⃣", 0,
            "1️⃣", 1,
            "2️⃣", 2,
            "3️⃣", 3,
            "4️⃣", 4,
            "5️⃣", 5,
            "️6️⃣", 6,
            "7️⃣", 7,
            "8️⃣", 8,
            "9️⃣", 9
    );
    private static final Map<Integer, String> INTEGERS_TO_EMOJIS = Map.of(
            0, "0️⃣",
            1, "1️⃣",
            2, "2️⃣",
            3, "3️⃣",
            4, "4️⃣",
            5, "5️⃣",
            6, "️6️⃣",
            7, "7️⃣",
            8, "8️⃣",
            9, "9️⃣"
    );
    private static final List<String> CANCEL_STRINGS = List.of(
            "NONE",
            "CANCEL",
            "WAIT NO",
            "OOPS",
            "FUCK",
            "WAIT HOLD UP",
            "HOLD UP",
            "WAIT FUCK"
    );

    private Utils() {
    }

    public static Completable addNumbers(final Message message, final IntStream range) {
        return Completable.mergeArray(
                range.mapToObj(INTEGERS_TO_EMOJIS::get)
                        .map(message::react)
                        .toArray(Completable[]::new)
        );
    }

    public static Single<ReactionUpdate> awaitNumberReaction(final Message message, final User user,
                                                             final IntStream range) {
        final List<Integer> numbers = range.boxed().collect(Collectors.toUnmodifiableList());
        return message.catnip().observable(DiscordEvent.MESSAGE_REACTION_ADD)
                .filter(r -> r.messageId().equals(message.id()))
                .filter(r -> user.id().equals(r.userId()))
                .filter(r -> numbers.contains(EMOJIS_TO_INTEGERS.get(r.emoji().name())))
                .take(1)
                .singleOrError();
    }

    public static Single<Message> awaitMessage(final User user, final MessageChannel source) {
        return user.catnip().observable(DiscordEvent.MESSAGE_CREATE)
                .filter(m -> m.author().id().equals(user.id()))
                .filter(m -> m.channelId().equals(source.id()))
                .filter(m -> !m.content().isEmpty())
                .take(1)
                .singleOrError();
    }

    public static String numberToEmoji(final int number) {
        return INTEGERS_TO_EMOJIS.get(number);
    }

    public static int emojiToNumber(final String emoji) {
        return EMOJIS_TO_INTEGERS.get(emoji);
    }

    public static boolean isCancel(final String string) {
        return CANCEL_STRINGS.contains(string.toUpperCase());
    }

    public static String cancelStringsFormatted() {
        return '`' + String.join("`, `", CANCEL_STRINGS) + '`';
    }
}

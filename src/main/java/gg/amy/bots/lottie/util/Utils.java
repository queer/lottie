package gg.amy.bots.lottie.util;

import com.mewna.catnip.entity.channel.MessageChannel;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.message.ReactionUpdate;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.shard.DiscordEvent;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

import javax.annotation.Nonnegative;
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
            Emojis.ZERO, 0,
            Emojis.ONE, 1,
            Emojis.TWO, 2,
            Emojis.THREE, 3,
            Emojis.FOUR, 4,
            Emojis.FIVE, 5,
            Emojis.SIX, 6,
            Emojis.SEVEN, 7,
            Emojis.EIGHT, 8,
            Emojis.NINE, 9
    );
    private static final Map<Integer, String> INTEGERS_TO_EMOJIS = Map.of(
            0, Emojis.ZERO,
            1, Emojis.ONE,
            2, Emojis.TWO,
            3, Emojis.THREE,
            4, Emojis.FOUR,
            5, Emojis.FIVE,
            6, Emojis.SIX,
            7, Emojis.SEVEN,
            8, Emojis.EIGHT,
            9, Emojis.NINE
    );
    private static final Map<Integer, String> INTEGERS_TO_NAMES = Map.of(
            0, "zero",
            1, "one",
            2, "two",
            3, "three",
            4, "four",
            5, "five",
            6, "six",
            7, "seven",
            8, "eight",
            9, "none"
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

    public static Completable addNumberReactions(final Message message, final IntStream range) {
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

    public static String integerToName(@Nonnegative final int number) {
        return INTEGERS_TO_NAMES.get(number);
    }
}

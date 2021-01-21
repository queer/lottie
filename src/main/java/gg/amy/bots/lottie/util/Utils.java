package gg.amy.bots.lottie.util;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.channel.MessageChannel;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.message.ReactionUpdate;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.shard.DiscordEvent;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.Value;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.Collection;
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

    public static Single<Message> awaitMessage(final User user, final String channelId) {
        return user.catnip().observable(DiscordEvent.MESSAGE_CREATE)
                .filter(m -> m.author().id().equals(user.id()))
                .filter(m -> m.channelId().equals(channelId))
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

    public static MentionParsedArgs findLeadingMentionsAndIds(final String input) {
        final Collection<String> ids = new ArrayList<>();
        boolean snowflakesDone = false;
        final var rest = new StringBuilder();
        final var args = new ArrayList<String>();
        for(final var arg : splitRespectingQuotes(input)) {
            if(!snowflakesDone && (arg.matches("<@!?(\\d+)>") || arg.matches("\\d+"))) {
                ids.add(arg.replace("<@", "")
                        .replace("!", "")
                        .replace(">", ""));
            } else {
                snowflakesDone = true;
                rest.append(arg).append(' ');
                args.add(arg);
            }
        }
        return new MentionParsedArgs(List.copyOf(ids), rest.toString().trim(), args);
    }

    public static List<String> splitRespectingQuotes(final String input) {
        final List<String> tokens = new ArrayList<>();

        char last = '\0';
        StringBuilder token = new StringBuilder();
        boolean insideQuotes = false;

        for(final char c : input.trim().toCharArray()) {
            if(Character.isWhitespace(c) && !insideQuotes) {
                // If it's whitespace and we AREN'T inside quotes, then we've
                // hit the end of the token and need to append it
                if(!token.toString().trim().isEmpty()) {
                    tokens.add(token.toString());
                }
                token = new StringBuilder();
            } else if(c == '"') {
                // If we hit a quote, determine if we need to start or stop
                // tokenizing
                if(insideQuotes) {
                    if(last == '\\') {
                        // If inside quotes, escape if and only if the previous
                        // char was a backslash
                        token.append(c);
                    } else {
                        // Otherwise, append the char and add the token to the
                        // list
                        insideQuotes = false;
                        tokens.add(token.append(c).toString());
                        token = new StringBuilder();
                    }
                } else if(last != '\\') {
                    // If last character is a \, it's an escape and doesn't
                    // count
                    insideQuotes = true;
                    token.append(c);
                }
            } else {
                // Probably not special, just append
                token.append(c);
            }
            last = c;
        }
        // Last token may be missing, so collect it
        if(token.length() != 0) {
            tokens.add(token.toString());
        }
        for(int i = 0; i < tokens.size(); i++) {
            var next = tokens.get(i);
            if(next.startsWith("\"") && next.endsWith("\"")) {
                next = next.substring(0, next.length() - 1).substring(1);
                tokens.set(i, next);
            }
        }

        return List.copyOf(tokens);
    }

    public static List<Single<User>> cachedOrFetchedUsers(final Catnip catnip, final Collection<String> ids) {
        return ids.stream()
                .map(id -> catnip.cache().user(Long.parseUnsignedLong(id))
                        .onErrorResumeWith(catnip.rest().user().getUser(id).toMaybe())
                        .toSingle())
                .collect(Collectors.toUnmodifiableList());
    }

    @Value
    public static class MentionParsedArgs {
        List<String> ids;
        String rest;
        List<String> args;
    }
}

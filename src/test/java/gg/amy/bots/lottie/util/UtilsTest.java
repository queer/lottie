package gg.amy.bots.lottie.util;

import gg.amy.bots.lottie.util.Utils.MentionParsedArgs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author amy
 * @since 9/12/20.
 */
class UtilsTest {

    @Test
    void numberToEmoji() {
        assertEquals(Emojis.ZERO, Utils.numberToEmoji(0));
        assertEquals(Emojis.ONE, Utils.numberToEmoji(1));
        assertEquals(Emojis.TWO, Utils.numberToEmoji(2));
        assertEquals(Emojis.THREE, Utils.numberToEmoji(3));
        assertEquals(Emojis.FOUR, Utils.numberToEmoji(4));
        assertEquals(Emojis.FIVE, Utils.numberToEmoji(5));
        assertEquals(Emojis.SIX, Utils.numberToEmoji(6));
        assertEquals(Emojis.SEVEN, Utils.numberToEmoji(7));
        assertEquals(Emojis.EIGHT, Utils.numberToEmoji(8));
        assertEquals(Emojis.NINE, Utils.numberToEmoji(9));
    }

    @Test
    void emojiToNumber() {
        assertEquals(Utils.emojiToNumber(Emojis.ZERO), 0);
        assertEquals(Utils.emojiToNumber(Emojis.ONE), 1);
        assertEquals(Utils.emojiToNumber(Emojis.TWO), 2);
        assertEquals(Utils.emojiToNumber(Emojis.THREE), 3);
        assertEquals(Utils.emojiToNumber(Emojis.FOUR), 4);
        assertEquals(Utils.emojiToNumber(Emojis.FIVE), 5);
        assertEquals(Utils.emojiToNumber(Emojis.SIX), 6);
        assertEquals(Utils.emojiToNumber(Emojis.SEVEN), 7);
        assertEquals(Utils.emojiToNumber(Emojis.EIGHT), 8);
        assertEquals(Utils.emojiToNumber(Emojis.NINE), 9);
    }

    @Test
    void integerToName() {
        assertEquals("zero", Utils.integerToName(0));
        assertEquals("one", Utils.integerToName(1));
        assertEquals("two", Utils.integerToName(2));
        assertEquals("three", Utils.integerToName(3));
        assertEquals("four", Utils.integerToName(4));
        assertEquals("five", Utils.integerToName(5));
        assertEquals("six", Utils.integerToName(6));
        assertEquals("seven", Utils.integerToName(7));
        assertEquals("eight", Utils.integerToName(8));
        assertEquals("none", Utils.integerToName(9));
    }

    @Test
    void findLeadingMentionsAndIds() {
        final MentionParsedArgs args = Utils.findLeadingMentionsAndIds("1 1234 <@!1234877> <@2134897> banana \"banana banana\"");
        assertEquals("banana banana banana", args.rest());
        assertEquals(List.of("banana", "banana banana"), args.args());
        assertEquals(List.of("1", "1234", "1234877", "2134897"), args.ids());
    }

    @Test
    void splitRespectingQuotes() {
        assertEquals(List.of("this", "is", "a", "test of the", "quote", "splitting", "system"),
                Utils.splitRespectingQuotes("this is a \"test of the\" quote splitting system"));
        assertEquals(List.of("long long test string", "banana"),
                Utils.splitRespectingQuotes("\"long long test string\" banana"));
    }
}

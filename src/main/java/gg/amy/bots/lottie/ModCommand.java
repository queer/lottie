package gg.amy.bots.lottie;

import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.user.User;
import gg.amy.bots.lottie.mod.ModAction;
import gg.amy.bots.lottie.mod.ModActionType;
import gg.amy.bots.lottie.mod.actions.BanAction;
import gg.amy.bots.lottie.mod.actions.KickAction;
import gg.amy.bots.lottie.mod.actions.MuteAction;
import gg.amy.bots.lottie.mod.actions.WarningAction;
import gg.amy.bots.lottie.util.Emojis;
import gg.amy.bots.lottie.util.Utils;
import io.reactivex.rxjava3.core.Single;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author amy
 * @since 9/7/20.
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "CodeBlock2Expr"})
public class ModCommand {
    private static final Map<ModActionType, ModAction> ACTIONS = Map.of(
            ModActionType.WARNING, new WarningAction(),
            ModActionType.MUTE, new MuteAction(),
            ModActionType.KICK, new KickAction(),
            ModActionType.BAN, new BanAction()
    );
    private static final Map<Integer, ModActionType> INTEGERS_TO_ACTION_TYPES = Map.of(
            1, ModActionType.WARNING,
            2, ModActionType.MUTE,
            3, ModActionType.KICK,
            4, ModActionType.BAN
    );

    public void run(final Message m) {
        if(m.mentionedUsers().isEmpty()) {
            m.channel().sendMessage("?");
        } else {
            m.channel().sendMessage(String.format(
                    "Moderating **%s**:\n" +
                            "%s Warn\n" +
                            "%s Mute\n" +
                            "%s Kick\n" +
                            "%s Ban",
                    m.mentionedUsers().stream().map(User::discordTag).collect(Collectors.joining(",")),
                    Utils.numberToEmoji(1),
                    Utils.numberToEmoji(2),
                    Utils.numberToEmoji(3),
                    Utils.numberToEmoji(4)
            )).subscribe(menu -> {
                Utils.addNumbers(menu, IntStream.rangeClosed(1, 4)).subscribe(() -> {
                    Utils.awaitNumberReaction(menu, m.author(), IntStream.rangeClosed(1, 4)).subscribe(r -> {
                        runAction(
                                menu,
                                m.author(),
                                m.mentionedUsers(),
                                emojiToModAction(r.emoji().name())
                        );
                    });
                });
            });
        }
    }

    private void runAction(final Message message, final User mod, final Collection<User> targets, final ModAction action) {
        message.catnip().rest().channel().deleteAllReactions(message.channelId(), message.id()).subscribe(() -> {
            message.edit(String.format("%s Type the reason for the **%s**.\nRespond with any of the following to cancel:\n%s",
                    Emojis.LOADING, action.name(), Utils.cancelStringsFormatted()))
                    .flatMap(__ -> Utils.awaitMessage(mod, message.channel()))
                    .flatMap(reason -> reason.delete().andThen(Single.just(reason)))
                    .flatMap(reason -> {
                        if(Utils.isCancel(reason.content())) {
                            return Single.just(reason);
                        } else {
                            return action.apply(message).andThen(Single.just(reason));
                        }
                    })
                    .subscribe(reason -> {
                        if(Utils.isCancel(reason.content())) {
                            message.edit("Cancelled.");
                        } else {
                            message.edit(String.format(
                                    "**%s** applied to **%s** for: %s",
                                    action.name(),
                                    targets.stream()
                                            .map(User::discordTag)
                                            .collect(Collectors.joining(", ")),
                                    reason.content()
                            ));
                        }
                    });
        });
    }

    private ModAction emojiToModAction(final String emoji) {
        return ACTIONS.get(INTEGERS_TO_ACTION_TYPES.get(Utils.emojiToNumber(emoji)));
    }
}

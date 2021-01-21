package gg.amy.bots.lottie;

import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.util.Permission;
import gg.amy.bots.lottie.command.Context;
import gg.amy.bots.lottie.command.annotation.Description;
import gg.amy.bots.lottie.command.annotation.Names;
import gg.amy.bots.lottie.command.annotation.Perms;
import gg.amy.bots.lottie.command.annotation.Reactions;
import gg.amy.bots.lottie.mod.ModAction;
import gg.amy.bots.lottie.mod.ModActionType;
import gg.amy.bots.lottie.mod.actions.BanAction;
import gg.amy.bots.lottie.mod.actions.KickAction;
import gg.amy.bots.lottie.mod.actions.MuteAction;
import gg.amy.bots.lottie.mod.actions.WarningAction;
import gg.amy.bots.lottie.util.Emojis;
import gg.amy.bots.lottie.util.Utils;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static gg.amy.bots.lottie.util.Translator.$;
import static gg.amy.bots.lottie.util.Translator.$$;

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

    @Names({"mod", "moderate"})
    @Reactions({"\uD83D\uDD28", "\uD83C\uDF4C"})
    @Description("Run through the moderation flow on a user or set of users.")
    @Perms(Permission.MANAGE_GUILD)
    public void moderate(final Context ctx) {
        if(ctx.mentions().isEmpty()) {
            ctx.channel().sendMessage("?");
        } else {
            ctx.channel().sendMessage(
                    $$(ctx.effectiveLanguage(), "action.menu.initial")
                            .t("discordTags", ctx.members().stream()
                                    .map(Member::user)
                                    .map(Maybe::blockingGet)
                                    .filter(Objects::nonNull)
                                    .map(User::discordTag)
                                    .collect(Collectors.joining(",")))
                            .en(1)
                            .en(2)
                            .en(3)
                            .en(4)
                            .$()
            ).subscribe(menu -> {
                Utils.addNumberReactions(menu, IntStream.rangeClosed(1, 4)).subscribe(() -> {
                    Utils.awaitNumberReaction(menu, ctx.user(), IntStream.rangeClosed(1, 4)).subscribe(r -> {
                        runAction(
                                menu,
                                ctx,
                                emojiToModAction(r.emoji().name())
                        );
                    });
                });
            });
        }
    }

    private void runAction(final Message message, final Context ctx, final ModAction action) {
        message.catnip().rest().channel().deleteAllReactions(message.channelId(), message.id()).subscribe(() -> {
            message.edit($$(ctx.effectiveLanguage(), "action.menu.reason-prompt")
                    .t("loading", Emojis.LOADING)
                    .t("action", action.name())
                    .t("cancelStrings", Utils.cancelStringsFormatted())
                    .$())
                    .flatMap(__ -> Utils.awaitMessage(ctx.user(), message.channelId()))
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
                            message.edit($(ctx.effectiveLanguage(), "action.menu.cancelled"));
                        } else {
                            message.edit($$(ctx.effectiveLanguage(), "action.menu.applied")
                                    .t("action", $(ctx.effectiveLanguage(), "action.actions." + action.name()))
                                    .t("discordTag", ctx.mentions().stream()
                                            .map(User::discordTag)
                                            .collect(Collectors.joining(", ")))
                                    .t("reason", reason.content())
                                    .$());
                        }
                    });
        });
    }

    private ModAction emojiToModAction(final String emoji) {
        return ACTIONS.get(INTEGERS_TO_ACTION_TYPES.get(Utils.emojiToNumber(emoji)));
    }
}

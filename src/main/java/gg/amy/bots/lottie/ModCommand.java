package gg.amy.bots.lottie;

import com.mewna.catnip.entity.channel.MessageChannel;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.message.ReactionUpdate;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.shard.DiscordEvent;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Supplier;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author amy
 * @since 9/7/20.
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "CodeBlock2Expr"})
public class ModCommand {
    private static final Map<String, Integer> EMOJIS_TO_INTEGERS = new HashMap<>() {{
        put("1️⃣", 1);
        put("2️⃣", 2);
        put("3️⃣", 3);
        put("4️⃣", 4);
    }};
    private static final Map<Integer, String> INTEGERS_TO_EMOJIS = new HashMap<>() {{
        put(1, "1️⃣");
        put(2, "2️⃣");
        put(3, "3️⃣");
        put(4, "4️⃣");
    }};
    private static final Map<Integer, String> INTEGERS_TO_ACTIONS = new HashMap<>() {{
        put(1, "warn");
        put(2, "mute");
        put(3, "kick");
        put(4, "ban");
    }};

    public void run(final Message m) {
        if(m.mentionedUsers().isEmpty()) {
            m.channel().sendMessage("?");
        } else {
            //noinspection ResultOfMethodCallIgnored
            m.channel().sendMessage(String.format(
                    "Moderating **%s**:\n" +
                            "%s Warn\n" +
                            "%s Mute\n" +
                            "%s Kick\n" +
                            "%s Ban",
                    m.mentionedUsers().stream().map(User::discordTag).collect(Collectors.joining(",")),
                    INTEGERS_TO_EMOJIS.get(1),
                    INTEGERS_TO_EMOJIS.get(2),
                    INTEGERS_TO_EMOJIS.get(3),
                    INTEGERS_TO_EMOJIS.get(4)
            )).subscribe(menu -> {
                //noinspection ResultOfMethodCallIgnored
                addNumbers(menu, IntStream.rangeClosed(1, 4)).subscribe(() -> {
                    //noinspection ResultOfMethodCallIgnored
                    listenForNumber(menu, m.author(), IntStream.rangeClosed(1, 4)).subscribe(r -> {
                        runAction(
                                menu,
                                m.author(),
                                m.mentionedUsers(),
                                INTEGERS_TO_ACTIONS.get(EMOJIS_TO_INTEGERS.get(r.emoji().name()))
                        );
                    }, Throwable::printStackTrace);
                }, Throwable::printStackTrace);
            }, Throwable::printStackTrace);
        }
    }

    private Completable addNumbers(final Message message, final IntStream range) {
        return Completable.mergeArray(
                range.mapToObj(INTEGERS_TO_EMOJIS::get)
                        .map(message::react)
                        .toArray(Completable[]::new)
        );
    }

    private Single<ReactionUpdate> listenForNumber(final Message message, final User user, final IntStream range) {
        final List<Integer> numbers = range.boxed().collect(Collectors.toUnmodifiableList());
        return message.catnip().observable(DiscordEvent.MESSAGE_REACTION_ADD)
                .filter(r -> r.messageId().equals(message.id()))
                .filter(r -> user.id().equals(r.userId()))
                .filter(r -> numbers.contains(EMOJIS_TO_INTEGERS.get(r.emoji().name())))
                .take(1)
                .singleOrError();
    }

    private void runAction(final Message menu, final User mod, final Collection<User> targets, final String action) {
        switch(action) {
            case "warn" -> processActionFor(mod, "warn", menu, targets,
                    () -> Completable.fromSingle(menu.channel().sendMessage("DEBUG: warnt")));
            case "mute" -> processActionFor(mod, "mute", menu, targets,
                    () -> Completable.fromSingle(menu.channel().sendMessage("DEBUG: muted")));
            case "kick" -> processActionFor(mod, "kick", menu, targets,
                    () -> Completable.fromSingle(menu.channel().sendMessage("DEBUG: kickt")));
            case "ban" -> processActionFor(mod, "ban", menu, targets,
                    () -> Completable.fromSingle(menu.channel().sendMessage("DEBUG: bent")));
        }
    }

    private void processActionFor(final User user, final String action, final Message source,
                                  final Collection<User> targets, final Supplier<Completable> callback) {
        source.catnip().rest().channel().deleteAllReactions(source.channelId(), source.id()).subscribe(() -> {
            source.edit(String.format("%s Type the reason for the **%s**. Respond with `NONE` (all caps) to cancel.", Emojis.LOADING, action))
                    .flatMap(__ -> awaitMessage(user, source.channel()))
                    .flatMap(reason -> reason.delete().andThen(Single.just(reason)))
                    .flatMap(reason -> {
                        if(reason.content().equals("NONE")) {
                            return Single.just(reason);
                        } else {
                            return callback.get().andThen(Single.just(reason));
                        }
                    })
                    .subscribe(reason -> {
                        if(reason.content().equals("NONE")) {
                            source.edit("Cancelled.");
                        } else {
                            source.edit(String.format(
                                    "**%s** applied to **%s** for: %s",
                                    action,
                                    targets.stream()
                                            .map(User::discordTag)
                                            .collect(Collectors.joining(", ")),
                                    reason.content()
                            ));
                        }
                    });
        });
    }

    private Single<Message> awaitMessage(final User user, final MessageChannel source) {
        return user.catnip().observable(DiscordEvent.MESSAGE_CREATE)
                .filter(m -> m.author().id().equals(user.id()))
                .filter(m -> m.channelId().equals(source.id()))
                .filter(m -> !m.content().isEmpty())
                .take(1)
                .singleOrError();
    }
}

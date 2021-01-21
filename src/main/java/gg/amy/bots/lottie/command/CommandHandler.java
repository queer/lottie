package gg.amy.bots.lottie.command;

import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.message.ReactionUpdate;
import com.mewna.catnip.entity.misc.ApplicationOwner;
import com.mewna.catnip.entity.misc.Team;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.shard.DiscordEvent;
import com.mewna.catnip.util.rx.RxHelpers;
import gg.amy.bots.lottie.Lottie;
import gg.amy.bots.lottie.command.annotation.*;
import gg.amy.bots.lottie.util.Utils;
import gg.amy.bots.lottie.util.Utils.MentionParsedArgs;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.reactivex.rxjava3.core.Single;
import lombok.Builder;
import lombok.Value;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author amy
 * @since 9/12/20.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class CommandHandler {
    private final Lottie lottie;
    private final Map<String, TextCommand> textCommands = new HashMap<>();
    private final Map<String, ReactionCommand> reactionCommands = new HashMap<>();

    public CommandHandler(final Lottie lottie) {
        this.lottie = lottie;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public CommandHandler setup() {
        try(final ScanResult res = new ClassGraph().enableAllInfo().scan()) {
            res.getClassesWithMethodAnnotation(Names.class.getName())
                    .stream().map(ClassInfo::loadClass).forEach(this::loadTextCommandsFromClass);
            res.getClassesWithMethodAnnotation(Reactions.class.getName())
                    .stream().map(ClassInfo::loadClass).forEach(this::loadReactionCommandsFromClass);
        }
        lottie.catnip().observable(DiscordEvent.MESSAGE_CREATE).subscribe(this::invokeTextCommand);
        lottie.catnip().observable(DiscordEvent.MESSAGE_REACTION_ADD).subscribe(this::invokeReactionCommand);

        return this;
    }

    private void loadTextCommandsFromClass(@Nonnull final Class<?> cls) {
        Arrays.stream(cls.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Names.class))
                .forEach(m -> {
                    try {
                        final var names = m.getDeclaredAnnotation(Names.class);
                        final var desc = m.getDeclaredAnnotation(Description.class);
                        final var perms = m.getDeclaredAnnotation(Perms.class);
                        final var owner = m.getDeclaredAnnotation(Owner.class);
                        final var cmd = TextCommand.builder()
                                .source(cls.getConstructor().newInstance())
                                .method(m)
                                .names(List.of(names.value()))
                                .perms(List.of(perms.value()))
                                .owner(owner != null)
                                .description(desc.value())
                                .build();
                        for(@Nonnull final String name : names.value()) {
                            if(textCommands.containsKey(name)) {
                                final TextCommand old = textCommands.get(name);
                                lottie.logger().warn("Overwriting text command {} with {}#{}, was {}#{}",
                                        name,
                                        cmd.source().getClass().getName(), m.getName(),
                                        old.source().getClass().getName(), old.method().getName());
                            }
                            textCommands.put(name, cmd);
                        }
                        lottie.logger().info("Loaded text commands: {}", List.of(names.value()));
                    } catch(final InstantiationException | NoSuchMethodException | InvocationTargetException
                            | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void loadReactionCommandsFromClass(@Nonnull final Class<?> cls) {
        Arrays.stream(cls.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Reactions.class))
                .forEach(m -> {
                    try {
                        final var names = m.getDeclaredAnnotation(Reactions.class);
                        final var desc = m.getDeclaredAnnotation(Description.class);
                        final var perms = m.getDeclaredAnnotation(Perms.class);
                        final var owner = m.getDeclaredAnnotation(Owner.class);
                        final ReactionCommand cmd = ReactionCommand.builder()
                                .source(cls.getConstructor().newInstance())
                                .method(m)
                                .reactions(List.of(names.value()))
                                .perms(List.of(perms.value()))
                                .owner(owner != null)
                                .description(desc.value())
                                .build();
                        for(@Nonnull final String name : names.value()) {
                            if(reactionCommands.containsKey(name)) {
                                final ReactionCommand old = reactionCommands.get(name);
                                lottie.logger().warn("Overwriting reaction command {} with {}#{}, was {}#{}",
                                        name,
                                        cmd.source().getClass().getName(), m.getName(),
                                        old.source().getClass().getName(), old.method().getName());
                            }
                            reactionCommands.put(name, cmd);
                        }
                        lottie.logger().info("Loaded reaction commands: {}", List.of(names.value()));
                    } catch(final InstantiationException | NoSuchMethodException | InvocationTargetException
                            | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }

    public void invokeTextCommand(@Nonnull final Message source) {
        RxHelpers.resolveMany(source.guild(), source.channel()).subscribe(pair -> {
            final var guild = pair.getValue0();
            final var channel = pair.getValue1();
            if(guild == null || channel == null) {
                return;
            }

            final var settings = lottie.database().guildSettings(guild.id());
            // Test prefix
            final String prefix;
            if(settings.prefix() != null) {
                prefix = settings.prefix();
            } else {
                prefix = "!";
            }
            if(source.content().toLowerCase().startsWith(prefix)) {
                final String[] split = source.content().substring(prefix.length()).split("\\s+", 2);
                final String name = split[0];
                final MentionParsedArgs args = Utils.findLeadingMentionsAndIds(split[1]);

                boolean isOwner = false;
                if(textCommands.containsKey(name.toLowerCase())) {
                    final var cmd = textCommands.get(name.toLowerCase());

                    // Test owner
                    if(cmd.owner()) {
                        final ApplicationOwner applicationOwner = lottie.info().owner();
                        if(applicationOwner.isTeam()) {
                            final Team team = lottie.info().team();
                            //noinspection ConstantConditions
                            isOwner = team.members().stream()
                                    .anyMatch(e -> e.user().idAsLong() == source.author().idAsLong());
                        } else {
                            if(source.author().idAsLong() != applicationOwner.idAsLong()) {
                                return;
                            }
                            isOwner = true;
                        }
                    }

                    // Test perms
                    if(!cmd.owner() && !isOwner && cmd.perms() != null && !cmd.perms().isEmpty()) {
                        final var member = source.member();
                        //noinspection ConstantConditions
                        if(!member.hasPermissions(channel.asGuildChannel(), cmd.perms())) {
                            return;
                        }
                    }

                    // All checks passed, invoke
                    Single.zip(Utils.cachedOrFetchedUsers(lottie.catnip(), args.ids()), user -> user)
                            .subscribe(array -> {
                                final List<User> users = Arrays.stream(array)
                                        .map(o -> (User) o)
                                        .collect(Collectors.toUnmodifiableList());
                                final var ctx = Context.builder()
                                        .lottie(lottie)
                                        .user(source.author())
                                        .guild(guild)
                                        .channel(channel.asTextChannel())
                                        .mentions(users)
                                        .guildSettings(settings)
                                        .userSettings(lottie.database().userSettings(source.author().id()))
                                        .build();

                                cmd.method.invoke(cmd.source(), ctx);
                            });
                } else {
                    // TODO: Try to help them find the right command
                }
            }
        });
    }

    public void invokeReactionCommand(@Nonnull final ReactionUpdate source) {
        RxHelpers.resolveMany(source.guild(), source.user(), source.channel()).subscribe(triplet -> {
            final var guild = triplet.getValue0();
            final var user = triplet.getValue1();
            final var channel = triplet.getValue2();

            if(guild == null || channel == null || user == null) {
                return;
            }

            final var settings = lottie.database().guildSettings(guild.id());
            if(source.emoji().unicode() && source.emoji().name() != null) {
                // TODO: Handle custom emojis
                final String emoji = source.emoji().name();
                if(emoji != null && reactionCommands.containsKey(emoji.toLowerCase())) {
                    final var cmd = reactionCommands.get(emoji.toLowerCase());
                    boolean isOwner = false;

                    // Test owner
                    if(cmd.owner()) {
                        final ApplicationOwner applicationOwner = lottie.info().owner();
                        if(applicationOwner.isTeam()) {
                            final Team team = lottie.info().team();
                            //noinspection ConstantConditions
                            isOwner = team.members().stream()
                                    .anyMatch(e -> e.user().idAsLong() == user.idAsLong());
                        } else {
                            if(user.idAsLong() != applicationOwner.idAsLong()) {
                                return;
                            }
                            isOwner = true;
                        }
                    }

                    final boolean finalIsOwner = isOwner;
                    guild.member(user.id()).subscribe(member -> {
                        // Test perms
                        if(!cmd.owner() && !finalIsOwner && cmd.perms() != null && !cmd.perms().isEmpty()) {
                            if(!member.hasPermissions(channel.asGuildChannel(), cmd.perms())) {
                                return;
                            }
                        }

                        // All checks passed, invoke
                        final List<User> users = List.of(user);
                        final var ctx = Context.builder()
                                .lottie(lottie)
                                .user(user)
                                .guild(guild)
                                .channel(channel.asTextChannel())
                                .mentions(users)
                                .guildSettings(settings)
                                .userSettings(lottie.database().userSettings(source.userId()))
                                .build();

                        try {
                            cmd.method.invoke(cmd.source(), ctx);
                        } catch(final IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    // TODO: Try to help them find the right command
                }
            }
        });
    }

    @Value
    @Builder
    public static class TextCommand {
        Object source;
        Method method;
        List<String> names;
        List<Permission> perms;
        boolean owner;
        String description;
    }

    @Value
    @Builder
    public static class ReactionCommand {
        Object source;
        Method method;
        List<String> reactions;
        List<Permission> perms;
        boolean owner;
        String description;
    }
}

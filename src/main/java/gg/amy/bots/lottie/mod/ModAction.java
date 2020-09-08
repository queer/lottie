package gg.amy.bots.lottie.mod;

import com.mewna.catnip.entity.message.Message;
import io.reactivex.rxjava3.core.Completable;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * @author amy
 * @since 9/7/20.
 */
public interface ModAction extends Function<Message, Completable> {
    String name();

    String[] names();

    ModActionType type();

    @Nonnull
    @Override
    Completable apply(Message message);
}

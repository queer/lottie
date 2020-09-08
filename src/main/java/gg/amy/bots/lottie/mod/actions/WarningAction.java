package gg.amy.bots.lottie.mod.actions;

import com.mewna.catnip.entity.message.Message;
import gg.amy.bots.lottie.mod.AbstractModAction;
import gg.amy.bots.lottie.mod.ModActionType;
import io.reactivex.rxjava3.core.Completable;

import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 9/7/20.
 */
public final class WarningAction extends AbstractModAction {
    public WarningAction() {
        super(ModActionType.WARNING, "warning", "warn");
    }

    @Nonnull
    @Override
    public Completable apply(final Message message) {
        return Completable.fromSingle(message.channel().sendMessage("DEBUG: warnt"));
    }
}

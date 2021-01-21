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
public class MuteAction extends AbstractModAction {
    public MuteAction() {
        super(ModActionType.MUTE, "mute");
    }

    @Nonnull
    @Override
    public Completable apply(final Message message) {
        return Completable.fromSingle(message.reply("DEBUG: muted", false));
    }
}

package gg.amy.bots.lottie.mod;

import lombok.Getter;

/**
 * @author amy
 * @since 9/7/20.
 */
@Getter
public abstract class AbstractModAction implements ModAction {
    private final ModActionType type;
    private final String[] names;

    public AbstractModAction(final ModActionType type, final String... names) {
        if(names.length == 0) {
            throw new IllegalArgumentException("Must provide at least 1 name for action: " + getClass().getName());
        }
        this.type = type;
        this.names = names;
    }

    public final String name() {
        return names[0];
    }
}

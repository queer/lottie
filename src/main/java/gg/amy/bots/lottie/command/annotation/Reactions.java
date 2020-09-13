package gg.amy.bots.lottie.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This command can be invoked with any of the provided reactions.
 *
 * @author amy
 * @since 9/12/20.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Reactions {
    String[] value();
}

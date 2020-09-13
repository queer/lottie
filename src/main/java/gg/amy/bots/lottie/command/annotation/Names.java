package gg.amy.bots.lottie.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This command can be used with any of the provided names.
 *
 * @author amy
 * @since 9/12/20.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Names {
    String[] value();
}

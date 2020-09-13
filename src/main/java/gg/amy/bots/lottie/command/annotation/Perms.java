package gg.amy.bots.lottie.command.annotation;

import com.mewna.catnip.entity.util.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This command requires the specified permissions.
 *
 * @author amy
 * @since 9/12/20.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Perms {
    Permission[] value();
}

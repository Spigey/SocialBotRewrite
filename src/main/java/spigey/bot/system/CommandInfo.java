package spigey.bot.system;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Define the CommandInfo annotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandInfo {
    String[] aliases() default {};
    String[] limitIds() default {};
    String description() default "";
    String limitMsg() default "You are not allowed to use this command!";
    long cooldown() default 0;
}

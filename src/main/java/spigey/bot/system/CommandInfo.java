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
    String description() default "DESCRIPTION";
    String limitMsg() default "You are not allowed to use this command!";
    long cooldown() default 0;
    String slashCommand() default "";
    boolean restrict() default true;
    String buttonId() default "";
    String usage() default "USAGE";
}

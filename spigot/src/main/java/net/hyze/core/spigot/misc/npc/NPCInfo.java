package net.hyze.core.spigot.misc.npc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NPCInfo {

    String id() default "";

    String name() default "";

    String[] lines() default {};
}
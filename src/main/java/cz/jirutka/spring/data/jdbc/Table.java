package cz.jirutka.spring.data.jdbc;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table {
    String value();

    String id() default "id";
}

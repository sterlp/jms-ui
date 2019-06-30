package org.sterl.jmsui.bl.common.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.transaction.Transactional;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

@Component
@Transactional
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BusinessManager {

    @AliasFor(annotation = Component.class)
    String value() default "";
}
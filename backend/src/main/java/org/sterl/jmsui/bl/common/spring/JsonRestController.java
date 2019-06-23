package org.sterl.jmsui.bl.common.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public @interface JsonRestController {

    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String[] value() default {"/api"};
}
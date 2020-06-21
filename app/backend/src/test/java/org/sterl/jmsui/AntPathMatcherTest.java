package org.sterl.jmsui;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;

class AntPathMatcherTest {

    @Test
    void test() {
        AntPathMatcher matcher = new AntPathMatcher();
        System.out.println(matcher.match("^\\.]*", "/ui/index.html"));
        System.out.println(matcher.match("^\\.]*", "/ui/index.html"));
    }
}

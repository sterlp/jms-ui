package org.sterl.jmsui.bl;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class JmsUiApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(JmsUiApplication.class, args);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // no caching for HTML pages
        registry.addResourceHandler("*.html")
                .addResourceLocations("classpath:/META-INF/resources/webjars/jms-ui-frontend/0.1.0-SNAPSHOT/");
        // the remaining stuff for 365 days
        // apply custom config as needed
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/jms-ui-frontend/0.1.0-SNAPSHOT/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
        
    }

    /**
     * This is needed in case we have no index.html in the static folder.
     * In the JAR solution we don't even have the static folder ...
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }
    /**
     * Taking care of any angular HTML5 routes -- we just forward them to the index.html
     * https://spring.io/blog/2015/05/13/modularizing-the-client-angular-js-and-spring-security-part-vii#using-ldquo-natural-rdquo-routes
     */
    @RequestMapping(value = "/{[path:[^\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }
}

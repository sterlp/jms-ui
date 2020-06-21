package org.sterl.jmsui.bl;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class JmsUiApplication implements WebMvcConfigurer{

    @Autowired private BuildProperties buildProperties;
    
    public static void main(String[] args) {
        SpringApplication.run(JmsUiApplication.class, args);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // no caching for HTML pages
        registry.addResourceHandler("*.html")
                .addResourceLocations("classpath:/META-INF/resources/webjars/jms-ui-frontend/" + buildProperties.getVersion() + "/")
                .setCacheControl(CacheControl.noCache());
        // the remaining stuff for 365 days
        // apply custom config as needed
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/jms-ui-frontend/" + buildProperties.getVersion() + "/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
        
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }
    @RequestMapping(value = "/")
    public String index() {
        return "index";
    }
}

package org.sterl.jmsui.bl;

import java.awt.Desktop;
import java.net.URI;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!TEST")
//@Component
public class RepoConfig implements
    ApplicationListener<WebServerInitializedEvent> /* extends RepositoryRestConfigurerAdapter */ {
/*
    @Autowired
    private EntityManager entityManager;

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.exposeIdsFor(entityManager.getMetamodel().getEntities().stream().map(e -> e.getJavaType()).collect(Collectors.toList()).toArray(new Class[0]));
    }
*/
    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        final String url = "http://localhost:" + event.getWebServer().getPort();
        final String errorMsg = "Failed to open browser. Open UI using the following URL: " + url;
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                System.err.println(errorMsg);
            }
        } else {
            final Runtime runtime = Runtime.getRuntime();
            final String myOS = System.getProperty("os.name").toLowerCase();
            try {
                
                if(myOS.contains("mac")) { // Apples
                    runtime.exec("open " + url);
                }  else if(myOS.contains("nix") || myOS.contains("nux")) { // Linux flavours 
                    runtime.exec("xdg-open " + url);
                } else if (myOS.contains("windows")) {
                    runtime.exec("explorer " + url);
                } else {
                    System.err.println(errorMsg);
                }
            } catch (Exception e) {
                System.err.println(errorMsg);
            }
        }
    }
}

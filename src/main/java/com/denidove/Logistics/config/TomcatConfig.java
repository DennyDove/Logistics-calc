// Этот кастомайзер можно попробовать применить, чтобы браузер не редиректил на https://localhost:8443/, хотя нигде в конфигурации этого прописано не было.

/*
package com.denidove.Logistics.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setRedirectPort(8080); // перенаправления на HTTPS — на тот же порт
            connector.setScheme("https");
            connector.setSecure(true);
            System.out.println("✅ Tomcat connector running on port: " + connector.getPort());
            System.out.println("➡ redirectPort: " + connector.getRedirectPort());
            System.out.println("➡ scheme: " + connector.getScheme());
        });
    }
}
*/

package com.datastax.demo.killrchat.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Arrays;

@Configuration
public class WebConfiguration implements ServletContextInitializer, EmbeddedServletContainerCustomizer {

    private final Logger log = LoggerFactory.getLogger(WebConfiguration.class);

    @Inject
    private Environment env;

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        container.setContextPath("/killrchat");
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        log.info("Web application configuration, using profiles: {}", Arrays.toString(env.getActiveProfiles()));

        log.info("Web application fully configured");
    }


}

package cc.kenai.weather.server;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("")
public class MainApp_V1 extends ResourceConfig {

    public MainApp_V1() {

        packages("cc.kenai.weather.server.v1");
        System.setProperty("log4j.configuration", "log4j.properties");
//        register(LoggingFilter.class);

    }

}

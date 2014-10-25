package cc.kenai.weather.server.main;

import org.dom4j.DocumentException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;


public class Test {

    public static void main(String... strings) throws DocumentException, IOException, NoSuchAlgorithmException {
        System.out.println(WeatherGetApi.getWeather("010200"));
    }
}

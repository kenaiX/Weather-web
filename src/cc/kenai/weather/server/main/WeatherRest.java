package cc.kenai.weather.server.main;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;


public class WeatherRest extends HttpServlet{
    public final static  String getNowWeather(String city) throws IOException, NoSuchAlgorithmException {
            if (city == null || city.length() != 6) {
                return "";
            }
            return WeatherGetApi.getWeather(city);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.print(req.getQueryString());

        String city = req.getParameter("city");

        PrintWriter out=resp.getWriter();
        try {
            System.out.println("out:"+getNowWeather(city));
            out.print(getNowWeather(city));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }finally {
            out.close();
        }
    }
}



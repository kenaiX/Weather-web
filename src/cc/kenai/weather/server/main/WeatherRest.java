package cc.kenai.weather.server.main;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class WeatherRest extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        Logger.getLogger(HttpServlet.class).debug("weather server init");
    }

    public final static void returnWeather(HttpServletResponse resp, String city) throws IOException {
        ServletOutputStream outputStream = null;
        try {
            final byte[] result = WeatherGetApi.getWeather(city);
//            resp.setCharacterEncoding("utf-8");
            resp.setStatus(200);
            resp.setHeader("content-encoding", "gzip");
            resp.setHeader("content-length", result.length + "");

            outputStream = resp.getOutputStream();
            outputStream.write(result);
            //成功
            return;
        } finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String city = req.getParameter("city");

        try {
            if (city.length() != 6) {
                resp.sendError(412);
                //不接受：无法使用请求的内容特性来响应请求的网页。
                return;
            }
            Integer.parseInt(city);
        } catch (Exception e) {
            resp.sendError(412);
            //不接受：无法使用请求的内容特性来响应请求的网页。
            return;
        }

        returnWeather(resp, city);
    }
}



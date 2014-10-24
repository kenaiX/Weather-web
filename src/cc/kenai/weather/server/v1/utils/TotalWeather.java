package cc.kenai.weather.server.v1.utils;

import net.sf.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kenai on 14-3-10.
 */
public class TotalWeather {
//    public final static Logger logger = Logger.getLogger("test");

    public static void main(String[] ss) {
        Logger logger = Logger.getLogger("test");
        logger.warning(getTotalWeather("010100").toString());
    }

    static void geArea(JSONObject jsonObject, Document doc) {
        Elements e1 = doc.getElementsByClass("weatherTopleft");
        Pattern pattern = Pattern.compile("[^(]*");
        Matcher matcher = pattern.matcher(e1.text());
        if (matcher.find()) {
            jsonObject.put("city", matcher.group());
        } else {
            jsonObject.put("city", "");
        }
    }

    static void geDayTime(JSONObject jsonObject, Document doc) {
        Elements e1 = doc.getElementsByClass("weatheH1");
        Pattern pattern = Pattern.compile("[0-9]{4}年[0-9]{1,2}月[0-9]{1,2}日");
        Matcher matcher = pattern.matcher(e1.text());
        if (matcher.find()) {
            jsonObject.put("date_y", matcher.group());
        } else {
            jsonObject.put("date_y", "");
        }
    }

    static void geWeatherToday(JSONObject jsonObject, Document doc) {
        Elements e1 = doc.getElementsByClass("ybds");


        Pattern pattern = Pattern.compile("(?<=：).+");
        Matcher matcher = pattern.matcher(e1.text());
        if (matcher.find()) {
            jsonObject.put("info", matcher.group());
        } else {
            jsonObject.put("info", "");
        }
    }

    static void geWeather(JSONObject jsonObject, Document doc) {
        Elements e1 = doc.getElementsByClass("yuBaoTable");
        int n = 0;
        boolean hasFirst = false;
        for (Element e : e1) {
            if (++n > 6) {
                break;
            }
//            Logger logger = Logger.getLogger("test");

            Elements e2 = e.getElementsByTag("tr");
            if (e2.size() == 1) {
                Elements e3 = e2.get(0).getElementsByTag("td");
                if (hasFirst) {
                    jsonObject.put("weather" + n, e3.get(3).text());
                    jsonObject.put("temp" + n, e3.get(4).text().replace("高温 ", "").replace("低温 ", "")+"~?");
                    jsonObject.put("wind" + n, e3.get(6).text());
                } else {
                    jsonObject.put("weather" + n, e3.get(3).text());
//                logger.warning("天气 : " + e3.get(3).text());
                    jsonObject.put("temp" + n, "~" + e3.get(4).text().replace("高温 ", "").replace("低温 ", ""));
//                logger.warning("气温 : " + e3.get(4).text().replace("高温 ", "").replace("低温 ", ""));
                    jsonObject.put("wind" + n, e3.get(6).text());
//                logger.warning("风速 : " + e3.get(6).text());
                }
            } else if (e2.size() == 2) {
                Elements e3 = e2.get(0).getElementsByTag("td");
                Elements e4 = e2.get(1).getElementsByTag("td");
                if (e3.get(3).text().equals(e4.get(2).text())) {
                    jsonObject.put("weather" + n, e3.get(3).text());
                } else {
                    jsonObject.put("weather" + n, e3.get(3).text() + "转" + e4.get(2).text());
                }
                jsonObject.put("temp" + n, e3.get(4).text().replace("高温 ", "").replace("低温 ", "") + "~" + e4.get(3).text().replace("高温 ", "").replace("低温 ", ""));
                if (e3.get(6).text().equals(e4.get(5).text())) {
                    jsonObject.put("wind" + n, e3.get(6).text());
                } else {
                    jsonObject.put("wind" + n, e3.get(6).text() + "转" + e4.get(5).text());
                }

            }
            hasFirst = false;
        }
    }

    static void geLifeTitle(JSONObject jsonObject, Document doc) {
        Elements e1 = doc.getElementsByClass("weatheH1");
        Iterator<Element> it = e1.iterator();
        while (it.hasNext()) {
            String s = it.next().text();
            if (s.startsWith("明日生活指数")) {
                jsonObject.put("life_title", "明日生活指数");
                break;
            } else if (s.startsWith("今日生活指数")) {
                jsonObject.put("life_title", "今日生活指数");
                break;
            }
        }

    }

    static String geLife1(JSONObject jsonObject, Document doc) {
        Element e1 = doc.getElementById("zs0");
        Elements e2 = e1.getElementsByTag("li");

        Elements e3 = e2.get(1).getElementsByTag("aside");
//        logger.warning("穿衣指数 : " + e3.text());
        jsonObject.put("index_d", e3.text());
        e3 = e2.get(7).getElementsByTag("aside");
//        logger.warning("紫外线指数 : " + e3.text());
        jsonObject.put("index_uv", e3.text());
        e3 = e2.get(4).getElementsByTag("aside");
//        logger.warning("洗车指数 : " + e3.text());
        jsonObject.put("index_xc", e3.text());
        e3 = e2.get(2).getElementsByTag("aside");
//        logger.warning("出游指数 : " + e3.text());
        jsonObject.put("index_tr", e3.text());
        e3 = e2.get(3).getElementsByTag("aside");
//        logger.warning("运动指数 : " + e3.text());
        jsonObject.put("index_sp", e3.text());
        return " ";
    }

    static void geLife2(JSONObject jsonObject, Document doc) {
        Element e1 = doc.getElementById("zs1");
        Elements e2 = e1.getElementsByTag("li");
        Elements e3 = e2.get(9).getElementsByTag("aside");
//        logger.warning("晾晒指数 : " + e3.text());
        jsonObject.put("index_ls", e3.text());
        e3 = e2.get(5).getElementsByTag("aside");
//        logger.warning("晨练指数 : " + e3.text());
        jsonObject.put("index_cl", e3.text());

    }

    public static JSONObject getTotalWeather(String city) {
//        Logger logger = Logger.getLogger("test");
        JSONObject jsonObject = new JSONObject();
        try {
            Document doc = Jsoup.connect("http://www.weather.com.cn/weather/101" + city + ".shtml").timeout(7000).get();
            //地区

            geWeatherToday(jsonObject, doc);
            geDayTime(jsonObject, doc);
            geArea(jsonObject, doc);
            geWeather(jsonObject, doc);
            geLifeTitle(jsonObject, doc);
            geLife1(jsonObject, doc);
            geLife2(jsonObject, doc);
            //


//            Elements newsHeadlines = doc.getAllElements();
//            for (Element link : newsHeadlines) {
//                logger.warning(link.className());
//            }

//            String weather = EntityUtils.toString(response.getEntity(), "utf-8");
//            jsonObject.put("weatherinfo",newsHeadlines.text());
        } catch (IOException e) {
        }


        return jsonObject;
    }
}

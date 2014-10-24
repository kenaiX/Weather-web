package cc.kenai.weather.server.v1.utils;

import cc.kenai.weather.server.v1.bace.GetWeather;
import net.sf.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetDetail extends GetWeather {
    private final static String tag_cache = "d_";

    /**
     * 错误则返回null
     */
    public static String getWeather(String city) {
        String s = getDetail_by_CACHE(city);
        if (s == null) {
            if (!GetSafe.isSafe(city)) {
                return null;
            }
            return getDetail_by_INTERNET(city);
        } else {
            return s;
        }
    }


    /**
     * 错误返回null
     */
    private final static String getDetail_by_CACHE(String key) {
        String lockey = tag_cache + key;
        String s = memMap.get(lockey);
        long now = System.currentTimeMillis();
        if (s != null) {
            if (now < memMap_Time.get(lockey)) {
                return s;
            } else {
                return null;
            }
        } else {
            s = (String) baeCache.get(lockey);
            if (s != null) {
                putMem(key, s, timeout / 2);
                return s;
            } else {
                return null;
            }
        }
    }

    /**
     * 错误时返回null。
     */
    private final static String getDetail_by_INTERNET(String city) {
        JSONObject jsonObject = new JSONObject();
        try {
            Document doc = Jsoup.connect("http://www.weather.com.cn/weather/101" + city + ".shtml").timeout(7000).get();
            boolean b = geLife1(jsonObject, doc);
            if (!b) return null;
            geWeatherToday(jsonObject, doc);
            geDayTime(jsonObject, doc);
            geArea(jsonObject, doc);
            boolean shouldXiuzheng = geWeather(jsonObject, doc);
            geLifeTitle(jsonObject, doc, shouldXiuzheng);

            geLife2(jsonObject, doc);
            String s = jsonObject.toString();
            if (s != null && s.length() > 5) {
                //成功则加入缓存
                putMem(city, s, timeout);
            }
            return s;
        } catch (IOException e) {
            return null;
        }
    }


    private final static void geArea(JSONObject jsonObject, Document doc) {
        Elements e1 = doc.getElementsByClass("weatherTopleft");
        Pattern pattern = Pattern.compile("[^(]*");
        Matcher matcher = pattern.matcher(e1.text());
        if (matcher.find()) {
            jsonObject.put("city", matcher.group());
        } else {
            jsonObject.put("city", "");
        }
    }


    private final static void geDayTime(JSONObject jsonObject, Document doc) {
        Elements e1 = doc.getElementsByClass("weatheH1");
        String s = e1.text();
        Pattern pattern = Pattern.compile("[0-9]{4}年[0-9]{1,2}月[0-9]{1,2}日");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            jsonObject.put("date_y", matcher.group());
        } else {
            jsonObject.put("date_y", "");
        }
        pattern = Pattern.compile("星期.");
        matcher = pattern.matcher(s);
        if (matcher.find()) {
            jsonObject.put("week", matcher.group());
        }
    }

    private final static void geWeatherToday(JSONObject jsonObject, Document doc) {
        Elements e1 = doc.getElementsByClass("ybds");


        Pattern pattern = Pattern.compile("(?<=：).+");
        Matcher matcher = pattern.matcher(e1.text());
        if (matcher.find()) {
            jsonObject.put("info", matcher.group());
        } else {
            jsonObject.put("info", "");
        }
    }

    /**
     * 返回是否需要修正生活指数
     */
    private final static boolean geWeather(JSONObject jsonObject, Document doc) {
        boolean shouldXiuzheng = false;
        Elements e1 = doc.getElementsByClass("weatherYubaoBox");
//        System.out.print(e1.html().replace("<!--", " ").replace("-->", ""));
        Document doc2 = Jsoup.parse(e1.html().replace("<!--", " ").replace("-->", ""));
        e1 = doc2.getElementsByClass("yuBaoTable");
        int n = 0;
        boolean notFirst = false;
        for (Element e : e1) {
            if (++n > 6) {
                break;
            }
            Elements e2 = e.getElementsByTag("tr");
            if (e2.size() == 1) {
                Elements e3 = e2.get(0).getElementsByTag("td");
                if (notFirst) {
                    if (n == 6) {
                        jsonObject.put("weather" + n, e3.get(3).text());
                        jsonObject.put("temp" + n, e3.get(4).text().replace("高温 ", "").replace("低温 ", "") + "~?");
                        jsonObject.put("wind" + n, e3.get(6).text());
                    } else {
                        n--;
                    }
                } else {
                    String week = jsonObject.getString("week");
                    if (week != null) {
                        String s = e3.get(0).text();
//                        System.out.println(week);
                        Pattern pattern = Pattern.compile("星期.");
                        Matcher matcher = pattern.matcher(s);
                        if (matcher.find()) {
                            if (!matcher.group().equals(week)) {
//                                System.out.println("not");
                                n--;
                                shouldXiuzheng = true;
                                continue;
                            }
                        }
//                        System.out.println("yes");
                    }
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
            notFirst = true;

        }
        return shouldXiuzheng;
    }

    private final static void geLifeTitle(JSONObject jsonObject, Document doc, boolean shouldXiuzheng) {
        Elements e1 = doc.getElementsByClass("weatheH1");
        Iterator<Element> it = e1.iterator();
        while (it.hasNext()) {
            String s = it.next().text();
            if (s.startsWith("明日生活指数")) {
                if (shouldXiuzheng) {
                    jsonObject.put("life_title", "今日生活指数");
                } else {
                    jsonObject.put("life_title", "明日生活指数");
                }
                break;
            } else if (s.startsWith("今日生活指数")) {
                jsonObject.put("life_title", "今日生活指数");
                break;
            }
        }

    }

    private final static boolean geLife1(JSONObject jsonObject, Document doc) {
        Element e1 = doc.getElementById("todayliving");
        if (e1 == null) {
            return false;
        }
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
        return true;
    }

    private final static void geLife2(JSONObject jsonObject, Document doc) {
        Element e1 = doc.getElementById("zs1");
        Elements e2 = e1.getElementsByTag("li");
        Elements e3 = e2.get(9).getElementsByTag("aside");
//        logger.warning("晾晒指数 : " + e3.text());
        jsonObject.put("index_ls", e3.text());
        e3 = e2.get(5).getElementsByTag("aside");
//        logger.warning("晨练指数 : " + e3.text());
        jsonObject.put("index_cl", e3.text());

    }


    //缓存部分
    private final static Map<String, String> memMap = new HashMap<String, String>();
    private final static Map<String, Long> memMap_Time = new HashMap<String, Long>();

    private final static void putMem(String key, String value, long timeout) {
        key = tag_cache + key;
        memMap.put(key, value);
        baeCache.set(key, value, timeout - 60000);
        memMap_Time.put(key, timeout + System.currentTimeMillis());
    }

    public static void main(String[] s) {
        System.out.print(getDetail_by_INTERNET("010200"));
    }
}
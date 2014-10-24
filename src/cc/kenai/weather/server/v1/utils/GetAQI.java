package cc.kenai.weather.server.v1.utils;

import cc.kenai.weather.server.v1.bace.GetWeather;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GetAQI extends GetWeather {
    private final static String tag_cache = "a_";

    public static void main(String[] s) {
        for (; ; ) {
            getWeather("beijing");
        }
    }

    /**
     * 错误则返回null
     */
    public static String getWeather(String city) {
        String s = getAQI_by_CACHE(city);
        if (s == null) {
            if (!GetSafe.isSafe(city)) {
                return null;
            }
            JSONObject j = getAQI_by_INTERNET(city);
            if (j == null) {
                return null;
            } else {
                return j.toString();
            }
        } else {
            return s;
        }
    }

    /**
     * 错误返回null
     */
    private final static String getAQI_by_CACHE(String key) {
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
    private final static JSONObject getAQI_by_INTERNET(String city) {
        if (city == null || city.length() < 1) {
            return null;
        }
        HttpClient hc = new DefaultHttpClient();
        hc.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 500);//连接时间20s
        hc.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 500);
        String s = "http://www.cnpm25.cn/city/" + city + ".html";
        String result = null;

        try {
            for (int i = 0; i < 10; i++) {
                HttpGet get = new HttpGet(s);
                final HttpResponse re = hc.execute(get);
                Pattern pattern2 = Pattern.compile("(?<=url=).*?(?=\")");
                result = EntityUtils.toString(re.getEntity());
                Matcher matcher2 = pattern2.matcher(result);
                if (matcher2.find()) {
                    s = matcher2.group();
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            //网络访问错误，直接返回
            return null;
        }
        if(result==null){
            return null;
        }
        Document doc = Jsoup.parse(result);
        Elements e1 = doc.getElementsByTag("script");
        //页面不存在，直接返回
        if(e1==null){
            return null;
        }
        String func = e1.html();
        String position = tiqu(func, "(?<=cityname = \")[^\"]*?(?=\")");
        Pattern pattern2 = Pattern.compile("(?<=jin_value = \")\\d*?(?=\")");
        Matcher matcher2 = pattern2.matcher(func);

        if (matcher2.find() && matcher2.find()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("position", position);
            jsonObject.put("aqi", matcher2.group());
            s = jsonObject.toString();
//            if (city.equals("beijing")) {
//                LogUtil.logger.error(city + "::" + new Date() + "::" + s);
//            }
            if (s != null && s.length() > 5) {
                putMem(city, s, timeout);
//                LogUtil.logger.error(city + "::" + new Date() + "::" + "save to cache");
            }
            return jsonObject;
        } else {
            return null;
        }
    }

    private final static String tiqu(String s, String s2) {
        Pattern pattern = Pattern.compile(s2);
        Matcher matcher2 = pattern.matcher(s);
        if (matcher2.find()) {
            return matcher2.group().replace("\n", "").replace("\r", "").replace(" ", "").trim();
        }
        return null;
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


}


package cc.kenai.weather.server.v1.utils;

import cc.kenai.weather.server.v1.bace.GetWeather;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GetNow extends GetWeather {
    private final static String tag_cache = "n_";

    /**
     * 错误则返回null
     */
    public final static String getWeather(String city) {
        String s = getNow_by_CACHE(city);
        if (s == null) {
            if (!GetSafe.isSafe(city)) {
                return null;
            }
            return getNow_by_INTERNET(city);
        } else {
            return s;
        }
    }

    /**
     * 错误返回null
     */
    private final static String getNow_by_CACHE(String key) {
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
    private final static String getNow_by_INTERNET(String city) {
        try {
            String now = Jsoup.connect("http://www.weather.com.cn/data/ks/101" + city + ".html").timeout(7000).get().text();
            if (now != null & now.length() > 5) {
                //成功则加入缓存
                putMem(city, now, timeout);
            }
            return now;
        } catch (IOException e) {
            return null;
        }

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

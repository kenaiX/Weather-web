package cc.kenai.weather.server.main;

import cc.kenai.weather.server.v1.utils.EncryptUtil;
import cc.kenai.weather.server.v1.utils.GetSafe;
import com.baidu.bae.api.factory.BaeFactory;
import com.baidu.bae.api.memcache.BaeCache;
import com.baidu.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WeatherGetApi {
    protected final static BaeCache baeCache = BaeFactory.getBaeCache("FZzCFfhRdpNDYXjlBgmN", "cache.duapp.com:20243", "qAxBy94hvbgsQ5bZLUsS0dLM", "A3z1f1fgqKGQTItfrRyd2Ieqcs16S1SA");
    protected final static int timeout = 55 * 60000;


    public static String getWeather(String city) {


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
        String lockey = key;
        String s = memMap.get(lockey);
        long now = System.currentTimeMillis();
        if (s != null) {
            if (now < memMap_Time.get(lockey)) {
                System.out.println("through cache");
                return s;
            } else {
                return null;
            }
        } else {
            s = (String) baeCache.get(lockey);
            if (s != null) {
                putMem(key, s, timeout / 2);
                System.out.println("through baecache");
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
        JsonObject jsonObject = new JsonObject();
        try {
            OkHttpClient client = new OkHttpClient();

            client.setConnectTimeout(3, TimeUnit.SECONDS);

            Request request = new Request.Builder()
                    .url("http://wthrcdn.etouch.cn/WeatherApi?citykey=101" + city)
                    .build();

            Response response = client.newCall(request).execute();

            SAXReader sr = new SAXReader();//获取读取xml的对象。
            Document doc = sr.read(new ByteArrayInputStream(response.body().bytes()));
            Element root = doc.getRootElement();//向外取数据，获取xml的根节点。
            now(jsonObject, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String s = jsonObject.toString();
        if (!s.isEmpty()&&s.length()>20) {
            //成功则加入缓存
            putMem(city, s, timeout);
            System.out.println("through internet");
        }else{
            System.out.println("through internet error");
        }
        return s;

    }


    //缓存部分
    private final static Map<String, String> memMap = new HashMap<String, String>();
    private final static Map<String, Long> memMap_Time = new HashMap<String, Long>();

    private final static void putMem(String key, String value, long timeout) {
        memMap.put(key, value);
        baeCache.set(key, value, timeout - 60000);
        memMap_Time.put(key, timeout + System.currentTimeMillis());
    }


    //分为大模块
    private static void now(JsonObject jsonObject, Element root) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        JsonObject nowJson = new JsonObject();
        Iterator<Element> it = root.elementIterator();//从根节点下依次遍历，获取根节点下所有子节点

        while (it.hasNext()) {//遍历该子节点
            Element o = it.next();//再获取该子节点下的子节点
            switch (o.getName()) {
                case "city":
                    nowJson.addProperty("ct", o.getText());
                    break;
                case "updatetime":
                    nowJson.addProperty("ut", o.getText());
                    break;
                case "wendu":
                    nowJson.addProperty("wd", o.getText());
                    break;
                case "fengli":
                    nowJson.addProperty("fl", o.getText());
                    break;
                case "shidu":
                    nowJson.addProperty("sd", o.getText());
                    break;
                case "fengxiang":
                    nowJson.addProperty("fx", o.getText());
                    break;
                default:
                    other(jsonObject, o);
            }

        }
        nowJson.addProperty("x", new EncryptUtil().md5Digest(nowJson.toString()).substring(0, 2));
        jsonObject.addProperty("now", nowJson.toString());
    }

    private static void other(JsonObject jsonObject, Element o) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        switch (o.getName()) {
            case "environment":
                environment(jsonObject, o);
                break;
            case "forecast":
                forecast(jsonObject, o);
                break;
            case "zhishus":
                zhishu(jsonObject, o);
                break;
        }
    }

    private static void environment(JsonObject jsonObject, Element root) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        JsonObject nowJson = new JsonObject();
        Iterator<Element> it = root.elementIterator();//从根节点下依次遍历，获取根节点下所有子节点

        while (it.hasNext()) {//遍历该子节点
            Element o = it.next();//再获取该子节点下的子节点
            switch (o.getName()) {
                case "aqi":
                    nowJson.addProperty("aqi", o.getText());
                    break;
                case "quality":
                    nowJson.addProperty("ql", o.getText());
                    break;
                case "suggest":
                    nowJson.addProperty("ss", o.getText());
                    break;
            }

        }
        nowJson.addProperty("x", new EncryptUtil().md5Digest(nowJson.toString()).substring(0, 2));
        jsonObject.addProperty("en", nowJson.toString());
    }

    private static void forecast(JsonObject jsonObject, Element root) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        JsonObject nowJson = new JsonObject();
        Iterator<Element> it = root.elementIterator();//从根节点下依次遍历，获取根节点下所有子节点

        int n = 0;
        while (it.hasNext()) {//遍历该子节点
            Element o = it.next();//再获取该子节点下的子节点
            if (o.getName().equals("weather")) {
                n++;
                forecast_(nowJson, n, o);
            }

        }

        nowJson.addProperty("x", new EncryptUtil().md5Digest(nowJson.toString()).substring(0, 2));
        jsonObject.addProperty("fc", nowJson.toString());
    }

    private static void forecast_(JsonObject json, int n, Element root) {
        JsonObject nowJson = new JsonObject();
        Iterator<Element> it = root.elementIterator();//从根节点下依次遍历，获取根节点下所有子节点

        while (it.hasNext()) {//遍历该子节点
            Element o = it.next();//再获取该子节点下的子节点
            switch (o.getName()) {
                case "date":
                    nowJson.addProperty("dt", o.getText());
                    break;
                case "high":
                    nowJson.addProperty("h", o.getText().substring(2).trim());
                    break;
                case "low":
                    nowJson.addProperty("l", o.getText().substring(2).trim());
                    break;
                case "day": {
                    Iterator<Element> itt = o.elementIterator();
                    while (itt.hasNext()) {
                        Element oo = itt.next();
                        switch (oo.getName()) {
                            case "type":
                                nowJson.addProperty("w_d", oo.getText());
                                break;
                            case "fengxiang":
                                nowJson.addProperty("fx_d", oo.getText());
                                break;
                            case "fengli":
                                nowJson.addProperty("fl_d", oo.getText());
                                break;
                        }

                    }
                    break;
                }
                case "night": {
                    Iterator<Element> itt = o.elementIterator();
                    while (itt.hasNext()) {
                        Element oo = itt.next();
                        switch (oo.getName()) {
                            case "type":
                                nowJson.addProperty("w_n", oo.getText());
                                break;
                            case "fengxiang":
                                nowJson.addProperty("fx_n", oo.getText());
                                break;
                            case "fengli":
                                nowJson.addProperty("fl_n", oo.getText());
                                break;
                        }

                    }
                    break;
                }
            }
        }

        json.addProperty("f" + n, nowJson.toString());
    }

    private static void zhishu(JsonObject jsonObject, Element root) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        JsonObject nowJson = new JsonObject();
        Iterator<Element> it = root.elementIterator();//从根节点下依次遍历，获取根节点下所有子节点

        while (it.hasNext()) {//遍历该子节点
            Element o = it.next();//再获取该子节点下的子节点
            zhishu_(nowJson, o);
        }

        nowJson.addProperty("x", new EncryptUtil().md5Digest(nowJson.toString()).substring(0, 2));
        jsonObject.addProperty("zs", nowJson.toString());


    }

    private static void zhishu_(JsonObject parent, Element root) {
        JsonObject nowJson = new JsonObject();
        Iterator<Element> it = root.elementIterator();//从根节点下依次遍历，获取根节点下所有子节点

        String name = null;
        while (it.hasNext()) {//遍历该子节点
            Element o = it.next();//再获取该子节点下的子节点
            switch (o.getName()) {
                case "name":
                    String s = o.getText();
                    if (s.equals("穿衣指数")) {
                        name = "cy";
                    } else if (s.contains("紫外线指数")) {
                        name = "zy";
                    } else if (s.contains("晾晒")) {
                        name = "ls";
                    } else if (s.contains("雨伞")) {
                        name = "ys";
                    } else if (s.contains("逛街")) {
                        name = "gj";
                    } else if (s.contains("运动")) {
                        name = "yd";
                    }
                    break;
                case "value":
                    nowJson.addProperty("quality", o.getText());
                    break;
                case "detail":
                    nowJson.addProperty("suggest", o.getText());
                    break;
            }
        }
        if (name == null) {
            return;
        }
        parent.addProperty(name, nowJson.toString());
    }

}

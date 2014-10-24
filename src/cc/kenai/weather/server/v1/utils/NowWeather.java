package cc.kenai.weather.server.v1.utils;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NowWeather {
//    final static Logger logger = Logger.getLogger("test");

    public static void main(String[] ss) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONArray jsonArray_ = new JSONArray();
//        getAQI("beijin", jsonArray, jsonArray_);
//        System.out.println("c:" + jsonArray.toString());
//        System.out.println("a:" + jsonArray_.toString());
        getAQI("beijing", jsonArray, jsonArray_);
//        logger.warning(jsonArray.toString());
//        logger.warning(jsonArray_.toString());
    }

    public static void getAQI(String city, JSONArray jsonArray, JSONArray jsonArray_) throws IOException {
//        logger.warning("aqi--start");
        if (city == null || city.length() < 1) {
            return;
        }

        HttpClient hc = new DefaultHttpClient();
        hc.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,  3000);//连接时间20s
        hc.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,  4000);

        String s = "http://www.cnpm25.cn/city/" + city + ".html";
        String result=null;
//        int n = 0;
        for (int i = 0; i < 10; i++) {
//            n++;
            HttpGet get = new HttpGet(s);
            final HttpResponse re = hc.execute(get);

            Pattern pattern2 = Pattern.compile("(?<=url=).*?(?=\")");
            result= EntityUtils.toString(re.getEntity());
            Matcher matcher2 = pattern2.matcher(result);
            if (matcher2.find()) {
                s = matcher2.group();
            } else {
                break;
            }
        }
        Document doc = Jsoup.parse(result);
        Elements e1 = doc.getElementsByTag("script");
        String func = e1.html();
        String position = tiqu(func, "(?<=cityname = \")[^\"]*?(?=\")");
        Pattern pattern2 = Pattern.compile("(?<=jin_value = \")\\d*?(?=\")");
        Matcher matcher2 = pattern2.matcher(func);
        matcher2.find();
        if (matcher2.find()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("position", position);
            jsonObject.put("aqi", matcher2.group());
            jsonArray.add(jsonObject);
        }
        if (matcher2.find()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("position", position);
            jsonObject.put("aqi", matcher2.group());
            jsonArray_.add(jsonObject);
        }
//        logger.warning(jsonArray.toString());
//        logger.warning(jsonArray_.toString());

    }
//    public static void getAQI(String city, JSONArray jsonArray, JSONArray jsonArray_) throws IOException, JSONException {
//
//        HttpClient hc = new DefaultHttpClient();
//        HttpGet get = new HttpGet("http://www.cnpm25.cn/city/" + city + ".html");
//        HttpResponse response = hc.execute(get);
//
//
//        String s = EntityUtils.toString(response.getEntity(), "utf-8");
//        Pattern pattern = Pattern.compile("function CreateChart[\\s\\S]*?CreateChart");
//        Matcher matcher = pattern.matcher(s);
//        if (matcher.find()) {
//
//            String position = tiqu(matcher.group(), "(?<=cityname = \")[^\"]*?(?=\")");
//
//
//            Pattern pattern2 = Pattern.compile("(?<=jin_value = \")\\d*?(?=\")");
//            Matcher matcher2 = pattern2.matcher(matcher.group());
//            matcher2.find();
//            if (matcher2.find()) {
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("position", position);
//                jsonObject.put("aqi", matcher2.group());
//                jsonArray.add(jsonObject);
//            }
//            if (matcher2.find()) {
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("position", position);
//                jsonObject.put("aqi", matcher2.group());
//                jsonArray_.add(jsonObject);
//            }
//
//
//        } else {
//            return;
//        }

//        pattern = Pattern.compile("<div class=\"weilai\"[\\s\\S]*?</div>");
//        matcher = pattern.matcher(s);
//        if (matcher.find()) {
//            pattern = Pattern.compile("<tr[\\s\\S]*?</tr>");
//            matcher = pattern.matcher(matcher.group());
//            while (matcher.find()) {
//                pattern = Pattern.compile("<td[\\s\\S]*?</td>");
//                final Matcher matcher2 = pattern.matcher(matcher.group());
//                JSONObject jsonObject = new JSONObject();
//                boolean isAmera = false;
//                if (matcher2.find()) {
//                    final String find = matcher2.group();
//                    String position = tiqu(find, "(?<=>)[^>]*?(?=</a)").trim();
//                    if (position != null && position.length() < 2) {
//                        position = tiqu(find, "(?<=>)[^>]*?(?=</font>)").trim();
//                    }
//                    if (position != null && position.length() > 1 && jsonArray.toString().contains(position)) {
//                        isAmera = true;
//                    }
//                    jsonObject.put("position", position);
//                }
//                if (matcher2.find()) {
//                    final String find = matcher2.group();
//                    String aqi = tiqu(find, "(?<=>)\\d*?(?=<)");
//                    jsonObject.put("aqi", aqi);
//                }
//                if (matcher2.find()) {
//                }
//                if (matcher2.find()) {
//                    final String find = matcher2.group();
//                    String pm2_5 = tiqu(find, "(?<=>)\\d*?(?=μg)");
//                    jsonObject.put("pm2_5", pm2_5);
//                }
//
//                try {
//                    if (jsonObject.getString("aqi") != null && jsonObject.getString("position") != null && jsonObject.getString("position").length() > 1) {
//                        if (isAmera) {
//                            jsonArray_.add(jsonObject);
//                        } else {
//                            jsonArray.add(jsonObject);
//                        }
//                    }
//                } catch (JSONException e) {
//
//                }
//
//            }
//        }
//    }

    static String tiqu(String s, String s2) {
        Pattern pattern = Pattern.compile(s2);
        Matcher matcher2 = pattern.matcher(s);
        if (matcher2.find()) {
            return matcher2.group().replace("\n", "").replace("\r", "").replace(" ", "").trim();
        }
        return null;
    }
}

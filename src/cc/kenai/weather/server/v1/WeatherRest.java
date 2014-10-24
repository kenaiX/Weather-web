package cc.kenai.weather.server.v1;

import cc.kenai.weather.server.v1.utils.*;
import net.sf.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


@Path("weather")
public class WeatherRest {

    static int num_detail, num_now, num_aqi;


    //    public static void main(String[] strings) throws IOException, NoSuchAlgorithmException, InterruptedException {
////        String targetURL = null;// TODO 指定URL
////        File targetFile = null;// TODO 指定上传文件
////
////        File file = new File("/Users/kenai/Desktop/test.log");
////        targetURL = "http://logcenter.duapp.com/logcenter/up";
////        HttpClient httpclient = new DefaultHttpClient();
////        HttpPost httpPost = new HttpPost(targetURL);
////        httpPost.setHeader("appname", "SmsCenter");
////        httpPost.setHeader("filename", file.getName());
////        try {
////            FileInputStream in = new FileInputStream(file);
////            InputStreamEntity s = new InputStreamEntity(in, file.length());
////            BufferedHttpEntity entity = new BufferedHttpEntity(s);
////            httpPost.setEntity(entity);
////            HttpResponse response = httpclient.execute(httpPost);
////            HttpEntity resEntity = response.getEntity();
////            if (resEntity != null) {
////                System.out.println(EntityUtils.toString(resEntity));
////            }
////        } catch (Exception ex) {
////            ex.printStackTrace();
////        } finally {
////            httpclient.getConnectionManager().shutdown();
////        }
//
//
////        final HttpGet get = new HttpGet("http://1.kenaiupdate.duapp.com/rest/1/weather/now?city=010100");
////        long time = System.currentTimeMillis();
////        while (System.currentTimeMillis() - time < 25000) {
////            Thread.sleep(100);
////            while (n < 50) {
////                new Thread() {
////                    @Override
////                    public void run() {
////                        try {
////                            final HttpClient hc = new DefaultHttpClient();
////                            HttpResponse response = hc.execute(get);
////                            String r = EntityUtils.toString(response.getEntity(), "utf-8");
//////                            System.out.println("" + response.getStatusLine().getStatusCode() + new Date());
////                            if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 203) {
////                                System.out.println(r + new Date());
////                            }
////                            n--;
////                        } catch (IOException e) {
////                            e.printStackTrace();
////                        }
////                    }
////                }.start();
////                n++;
////            }
////        }
////        System.out.println("end");
//        System.out.println(Jsoup.connect("http://www.weather.com.cn/data/ks/101" + "010100" + ".html").timeout(7000).get().text());
//    }
    static long time_detail, time_now, time_aqi;
    private static byte[] lock1 = new byte[0], lock2 = new byte[0], lock3 = new byte[0];

    public static void main(String[] strings) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String signature = "ee40dcca0b92874b1d718b5e36433e8cacf2c4e8", echostr = "5991004480486418697", timestamp = "1395306108", nonce = "1395058903";
        String[] sort = new String[]{nonce, timestamp, "789456li"};
        Arrays.sort(sort);

        String s = sort[0] + sort[1] + sort[2];
        EncryptUtil ee = new EncryptUtil();
        String realsign = ee.sh1Digest(s);
        System.out.print(realsign);


//        String ss[]={"ab","wang","hi","a","abff"};
//        MyString mySs[]=new MyString[ss.length];//创建自定义排序的数组
//        for (int i = 0; i < ss.length; i++) {
//            mySs[i]=new MyString(ss[i]);
//        }
//        Arrays.sort(mySs);//排序
//        for (int i = 0; i < mySs.length; i++) {
//            System.out.println(mySs[i].s);

    }

    @GET
    @Path("{city}")
    public final static Response getDetailWeather(@PathParam("city") String city, @QueryParam("cache") String cache) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        try {
            synchronized (lock1) {
                num_detail++;
            }
            if (city == null || city.length() != 6) {
                return Response.serverError().build();
            }
            String s = GetDetail.getWeather(city);
            if (s != null) {
                JSONObject json = JSONObject.fromObject(s);
                json.put("index_ag", "暂无");
                json.put("index_ag", "暂无");
                json.put("index48_uv", "暂无");
                json.put("index48_d", "暂无");
                json.put("fchh", "0");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("weatherinfo", json.toString());
                return creatStateCodeAndResponse(jsonObject.toString(), cache);
            } else {
                return creatStateCodeAndResponse(null, cache);
            }
        } finally {
            synchronized (lock1) {
                num_detail--;
            }
        }

    }

    @GET
    @Path("now")
    public final static Response getNowWeather(@QueryParam("city") String city, @QueryParam("cache") String cache) throws IOException, NoSuchAlgorithmException {
        try {
            synchronized (lock2) {
                num_now++;
            }
            if (city == null || city.length() != 6) {
                return Response.serverError().build();
            }
            return creatStateCodeAndResponse(GetNow.getWeather(city), cache);
        } finally {
            synchronized (lock2) {
                num_now--;
            }
        }
    }

    @GET
    @Path("aqi")
    public final static Response getAQI(@QueryParam("city") String city, @QueryParam("type") String type, @QueryParam("cache") String cache) throws IOException, NoSuchAlgorithmException {
        try {
            synchronized (lock3) {
                num_aqi++;
            }
            if (city == null || city.length() < 3) {
                return Response.serverError().build();
            }
            String result = GetAQI.getWeather(city);
            if (result != null) {
                result = "[" + result + "]";
            }
            return creatStateCodeAndResponse(result, cache);
        } finally {
            synchronized (lock3) {
                num_aqi--;
            }
        }
    }

    @GET
    @Path("admintest")
    public final static Response test() throws IOException {
//        HttpClient hc = new DefaultHttpClient();
//        hc.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);//连接时间20s
//        hc.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 4000);
//        String s = "http://www.cnpm25.cn/city/" + "beijing" + ".html";
//        HttpResponse re = null;
//        String result = null;
//        int n = 0;
//        for (int i = 0; i < 50; i++) {
//            n++;
//            HttpGet get = new HttpGet(s);
//            re = hc.execute(get);
//
//            Pattern pattern2 = Pattern.compile("(?<=url=).*?(?=\")");
//            result = EntityUtils.toString(re.getEntity());
//            Matcher matcher2 = pattern2.matcher(result);
//            if (matcher2.find()) {
//                s = matcher2.group();
//            } else {
//                break;
//            }
//
//        }
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("" + n, result);
        LogUtil.logger.warn("this is test");
        LogUtil.logger_test.warn("this is test");
        return Response.ok().build();
    }

    @GET
    @Path("lognum")
    public final static Response lognum() {
        String log = "total : " + (num_aqi + num_detail + num_now) + " detail:" + num_detail + " now" + num_now + " aqi" + num_aqi;
        LogUtil.logger.info(log);
        return Response.ok(log).build();
    }

    //检查cache和最新cache是否一致，一致则往回202
    private final static Response creatStateCodeAndResponse(String result, String cache) {
        if (result != null) {
            if (cache != null) {
                // MD5加密
                EncryptUtil eu = new EncryptUtil();
                try {
                    if (cache.toUpperCase().equals(eu.md5Digest(result))) {
                        return Response.status(202).build();
                    } else {
                        return Response.status(200).entity(result).build();
                    }
                } catch (NoSuchAlgorithmException e) {
                    return Response.status(520).entity("NoSuchAlgorithmException").build();
                } catch (UnsupportedEncodingException e) {
                    return Response.status(521).entity("UnsupportedEncodingException").build();
                }
            } else {
                return Response.status(200).entity(result).build();
            }
        } else {
            return Response.status(420).entity("null from internet").build();
        }
    }

}



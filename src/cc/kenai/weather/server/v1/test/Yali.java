package cc.kenai.weather.server.v1.test;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by kenai on 14-3-14.
 */
public class Yali {
    public static void main(String[] strings) {
//        final HttpClient hc = new DefaultHttpClient();
        for (int i = 0; i < 50; i++) {
            TestThread t = new TestThread();
            t.start();
        }

    }

    static int n;
    static long t;

    synchronized static void log(long time) {
        n++;
        t = t + time;
        if (n % 10 == 0) {
            System.out.println("times : " + n + " time : " + t / n);
        }
    }

    static class TestThread extends Thread {

        @Override
        public void run() {
            final HttpClient hc = new DefaultHttpClient();
            hc.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 500);//连接时间20s
            hc.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 500);
            while (true) {
                HttpGet get = new HttpGet("http://weather.kenai.cc/rest/1/weather/aqi?city=beijing");
                try {
                    long start = System.currentTimeMillis();
                    HttpResponse re = hc.execute(get);
                    EntityUtils.toString(re.getEntity());
                    log(System.currentTimeMillis() - start);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

//                    hc.getConnectionManager().
                }
            }
        }
    }
}


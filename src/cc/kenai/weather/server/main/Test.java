package cc.kenai.weather.server.main;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;


public class Test {

    public static void main(String... strings) throws DocumentException, IOException, NoSuchAlgorithmException {
//        System.out.println(WeatherGetApi.getWeather("010200"));



        try {
            OkHttpClient client = new OkHttpClient();

            client.setConnectTimeout(3, TimeUnit.SECONDS);

            Request request = new Request.Builder()
                    .url("http://wthrcdn.etouch.cn/WeatherApi?citykey=101010100")
                    .build();

            Response response = client.newCall(request).execute();

//            InputStreamReader ir = new InputStreamReader(response.body().byteStream(), "utf-8");
//
//            BufferedReader reader = new BufferedReader(ir);

            String result=new String(response.body().string());

            System.out.println("haha:  "+result);
        }catch (Exception e){

        }
    }
}

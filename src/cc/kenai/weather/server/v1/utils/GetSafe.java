package cc.kenai.weather.server.v1.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 3月12日：加入防止重复访问相同链接的设置，比如对于城市010100记录一个访问时间，当第二次访问时进行检测，若在时间内，则累加访问次数，反之清零访问次数
 */
public class GetSafe {
    private final static long timeout = 300000;
    private final static int times = 10;
    //缓存部分
    private final static Map<String, String> memMap = new HashMap<String, String>();


    public final static boolean isSafe(String key) {
        String s = memMap.get(key);
        long now = System.currentTimeMillis();
        if (s != null && s.length() > 5) {
            String[] ss = s.split("@");

            if (now > Long.parseLong(ss[0])) {
                memMap.put(key, timeout + System.currentTimeMillis() + "@0");
                return true;
            } else {
                int i = Integer.parseInt(ss[1]);
                if (i < times) {
                    memMap.put(key, ss[0] + "@" + (++i));
                    return true;
                } else {
//                    LogUtil.logger.error(key + "::" + new Date() + "::" + "is not safe");
                    return false;
                }
            }
        } else {
            memMap.put(key, timeout + System.currentTimeMillis() + "@0");
            return true;
        }
    }

//    private final static void putCache(String key, int times) {
//        memMap.put(key, timeout + System.currentTimeMillis() + "@" + times);
//    }
//    private final static void updateCache(String key, int times) {
//        memMap.put(key, timeout + System.currentTimeMillis() + "@" + times);
//    }


    public static void main(String[] s){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<100;i++){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    String s="3";
                    if(isSafe(s)){
                        System.out.println(s+"::"+new Date().getSeconds()+"::"+"pass");
                    }else{
                        System.out.println(s+"::"+new Date().getSeconds()+"::"+"no");
                    }
                }
            }
        }).start();
    }
}

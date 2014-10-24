package cc.kenai.weather.server.v1.bace;

import com.baidu.bae.api.factory.BaeFactory;
import com.baidu.bae.api.memcache.BaeCache;
import net.sf.json.JSONObject;

/**
 * 分为三部分
 * ：主任务部分
 * ：缓存部分
 * ：错误处理部分（主要是未抓取到的处理）
 */
public abstract class GetWeather {
    protected final static BaeCache baeCache = BaeFactory.getBaeCache("FZzCFfhRdpNDYXjlBgmN", "cache.duapp.com:20243", "qAxBy94hvbgsQ5bZLUsS0dLM", "A3z1f1fgqKGQTItfrRyd2Ieqcs16S1SA");
    protected final static int timeout = 55 * 60000;
}

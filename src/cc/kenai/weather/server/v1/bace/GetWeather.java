package cc.kenai.weather.server.v1.bace;

import com.baidu.bae.api.factory.BaeFactory;
import com.baidu.bae.api.memcache.BaeCache;
import net.sf.json.JSONObject;

/**
 * ��Ϊ������
 * �������񲿷�
 * �����沿��
 * ���������֣���Ҫ��δץȡ���Ĵ���
 */
public abstract class GetWeather {
    protected final static BaeCache baeCache = BaeFactory.getBaeCache("FZzCFfhRdpNDYXjlBgmN", "cache.duapp.com:20243", "qAxBy94hvbgsQ5bZLUsS0dLM", "A3z1f1fgqKGQTItfrRyd2Ieqcs16S1SA");
    protected final static int timeout = 55 * 60000;
}

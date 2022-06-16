package site.lonelyman.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * <p>
 * Http工具类
 * </p>
 *
 * @author LM
 * @since 2022/6/14
 */

@Slf4j
public class HttpUtils {
    private static OkHttpClient client;

    public static Response doGet(String url, String authorization) {
        try {
            client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", authorization)
                    .build();
            return client.newCall(request).execute();
        } catch (Exception e) {
            log.error("doGet error", e);
            return null;
        }
    }

}

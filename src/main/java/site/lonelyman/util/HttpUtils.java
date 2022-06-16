package site.lonelyman.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;

/**
 * <p>
 *
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

    public  static Response doPost(String url, String authorization, String body, Map<String, String> params) {
        try {
            StringBuilder sb = new StringBuilder();
//            sb.append(url).append("?");
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
            }
            client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .post(okhttp3.RequestBody.create(sb.toString(),null))
                    .build();
            return client.newCall(request).execute();
        } catch (Exception e) {
            log.error("doPost error", e);
            return null;
        }
    }

}

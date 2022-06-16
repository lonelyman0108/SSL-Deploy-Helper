package site.lonelyman.util;

import com.qiniu.http.Client;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;
import site.lonelyman.config.HelperConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.security.GeneralSecurityException;

/**
 * <p>
 *
 * </p>
 *
 * @author LM
 * @since 2022/6/14
 */

public class QiniuUtils {
    private static final String ACCESS_KEY = HelperConfig.instance.getQiniuConfig().getAccessKey();
    private static final String SECRET_KEY = HelperConfig.instance.getQiniuConfig().getSecretKey();
    public static String QBOX_AUTHORIZATION_PREFIX = "QBox ";
    public static String SSL_API = "http://api.qiniu.com/sslcert";
    public static String DOMAIN_API = "http://api.qiniu.com/domain";

    public static String signRequest(String urlString, byte[] body, String contentType) {
        URI uri = URI.create(urlString);
        String path = uri.getRawPath();
        String query = uri.getRawQuery();

        Mac mac = createMac();

        mac.update(StringUtils.utf8Bytes(path));

        if (query != null && query.length() != 0) {
            mac.update((byte) ('?'));
            mac.update(StringUtils.utf8Bytes(query));
        }
        mac.update((byte) '\n');
        if (body != null && Client.FormMime.equalsIgnoreCase(contentType)) {
            mac.update(body);
        }

        String digest = UrlSafeBase64.encodeToString(mac.doFinal());

        return ACCESS_KEY + ":" + digest;
    }

    private static Mac createMac() {
        Mac mac;
        try {
            mac = javax.crypto.Mac.getInstance("HmacSHA1");
            byte[] sk = StringUtils.utf8Bytes(SECRET_KEY);
            SecretKeySpec secretKeySpec = new SecretKeySpec(sk, "HmacSHA1");
            mac.init(secretKeySpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        return mac;
    }

}

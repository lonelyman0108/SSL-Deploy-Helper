package site.lonelyman.config;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import site.lonelyman.util.FileUtils;

/**
 * <p>
 * 配置类
 * </p>
 *
 * @author LM
 * @since 2022/6/14
 */

@Data
@Slf4j
public class HelperConfig {
    public static HelperConfig instance;

    private QiniuConfig qiniuConfig;
    private TencentCloudConfig tencentCloudConfig;
    private String domain;

    static {
        String config = FileUtils.readFile("config.json");
        if (config != null) {
            instance = JSONUtil.toBean(config, HelperConfig.class);
        } else {
            log.error("配置文件不存在");
        }
    }

    @Data
    public static class QiniuConfig {
        private String accessKey;
        private String secretKey;
    }

    @Data
    public static class TencentCloudConfig {
        private String secretId;
        private String secretKey;
    }
}

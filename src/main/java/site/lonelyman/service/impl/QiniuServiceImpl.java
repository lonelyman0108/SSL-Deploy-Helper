package site.lonelyman.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import site.lonelyman.model.Certificate;
import site.lonelyman.service.ICertificateService;
import site.lonelyman.util.HttpUtils;
import site.lonelyman.util.QiniuUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 七牛云部署服务接口实现类
 * </p>
 *
 * @author LM
 * @since 2022/6/14
 */

@Slf4j
public class QiniuServiceImpl implements ICertificateService {
    @Override
    public List<Certificate> getCertificateList() {

//        Auth.create(
//                ConfigLoader.helperConfig.getQiniu().getAccessKey(),
//                ConfigLoader.helperConfig.getQiniu().getSecretKey()
//        ).signQiniuAuthorization()

        String sign = QiniuUtils.signRequest(QiniuUtils.DOMAIN_API, null, null);

        try (Response response = HttpUtils.doGet(QiniuUtils.DOMAIN_API, QiniuUtils.QBOX_AUTHORIZATION_PREFIX + sign)) {
            if (response == null || response.body() == null) {
                log.error("获取证书列表失败");
                return null;
            } else if ("UNAUTHORIZED".equalsIgnoreCase(response.message())) {
                log.error("accessKey或secretKey不合法");
                return null;
            }
            List<Certificate> certificateList = new ArrayList<>();
            JSONUtil.parseArray(JSONUtil.parseObj(response.body().string()).get("certs")).forEach(certificate -> {
                JSONObject jsonObject = JSONUtil.parseObj(certificate);
                certificateList.add(new Certificate(
                        jsonObject.getStr("certid"),
                        jsonObject.getStr("dnsnames"),
                        LocalDateTimeUtil.of(jsonObject.getLong("not_after")),
                        LocalDateTimeUtil.between(
                                LocalDateTime.now(),
                                LocalDateTimeUtil.of(jsonObject.getLong("not_after")), ChronoUnit.DAYS) < 30
                ));
            });
            return certificateList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String uploadCertificate(String privateKey, String publicKey) {
        return null;
    }

}

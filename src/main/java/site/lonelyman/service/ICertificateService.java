package site.lonelyman.service;

import site.lonelyman.model.Certificate;

import java.util.List;

/**
 * <p>
 * 证书相关接口
 * </p>
 *
 * @author LM
 * @since 2022/6/20
 */

public interface ICertificateService {
    /**
     * 获得证书列表
     *
     * @return {@link List}<{@link Certificate}>
     */
    List<Certificate> getCertificateList();

    /**
     * 上传证书
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return {@link String}
     */
    String uploadCertificate(String privateKey, String publicKey);
}

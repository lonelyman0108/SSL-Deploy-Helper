package site.lonelyman.service;

import site.lonelyman.model.Certificate;

import java.util.List;

/**
 * <p>
 * 部署服务接口
 * </p>
 *
 * @author LM
 * @since 2022/6/14
 */

public interface DeployService {

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

    boolean createDomainRecord(String domain, String subDomain, String type, String value, boolean isOverwrite);


    boolean deleteDomainRecord(String domain, String subDomain, String type);
}

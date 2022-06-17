package site.lonelyman.service;

import site.lonelyman.model.Cdn;
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

    /**
     * 创建域记录
     *
     * @param domain      域名
     * @param subDomain   记录名
     * @param type        记录类型
     * @param value       记录值
     * @param isOverwrite 是否覆盖
     * @return boolean
     */
    boolean createDomainRecord(String domain, String subDomain, String type, String value, boolean isOverwrite);


    /**
     * 删除域名记录
     *
     * @param domain    域名
     * @param subDomain 记录名
     * @param type      记录类型
     * @return boolean
     */
    boolean deleteDomainRecord(String domain, String subDomain, String type);

    /**
     * 更新cdn证书
     *
     * @param cdnList       cdn列表
     * @param certificateId 证书id
     * @return boolean
     */
    boolean updateCdnCertificate(List<Cdn> cdnList, String certificateId);

    /**
     * 获取cdn列表
     *
     * @param domain 域名
     * @return {@link List}<{@link Cdn}>
     */
    List<Cdn> getCdnList(String domain);
}

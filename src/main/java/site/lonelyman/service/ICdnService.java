package site.lonelyman.service;

import site.lonelyman.model.Cdn;

import java.util.List;

/**
 * <p>
 * CDN相关接口
 * </p>
 *
 * @author LM
 * @since 2022/6/20
 */

public interface ICdnService {
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

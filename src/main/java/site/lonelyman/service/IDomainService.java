package site.lonelyman.service;

/**
 * <p>
 * 域名相关接口
 * </p>
 *
 * @author LM
 * @since 2022/6/20
 */

public interface IDomainService {
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
}

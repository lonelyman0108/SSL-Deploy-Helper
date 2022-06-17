package site.lonelyman;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import site.lonelyman.config.HelperConfig;
import site.lonelyman.model.Cdn;
import site.lonelyman.model.Certificate;
import site.lonelyman.service.DeployService;
import site.lonelyman.service.GenerateCertificateService;
import site.lonelyman.service.impl.TencentCloudDeployServiceImpl;
import site.lonelyman.util.FileUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 主启动类
 * </p>
 *
 * @author LM
 * @since 2022/6/14
 */

@Slf4j
public class HelperMain {

    public static void main(String[] args) {
        String configDomain = HelperConfig.instance.getDomain();
        log.info("已配置需要管理的域名:【{}】", configDomain);

        DeployService deployService = new TencentCloudDeployServiceImpl();
        //查询证书列表
        List<Certificate> certificateList = deployService.getCertificateList();

        //生成证书
        String certificateId = null;
        List<Certificate> configDomainCertificateList = certificateList.stream()
                .filter(certificate ->
                        !certificate.isExpiringSoon() && certificate.getDomain().equals(configDomain)).
                collect(Collectors.toList());
        if (CollectionUtil.isEmpty(configDomainCertificateList)) {
            log.info("【{}】证书即将过期或已过期，需要续期", configDomain);
            try {
                GenerateCertificateService generateCertificateService = new GenerateCertificateService();
                generateCertificateService.fetchCertificate(configDomain);
                certificateId = deployService.uploadCertificate(
                        FileUtils.readFile(configDomain + ".key"),
                        FileUtils.readFile(configDomain + "-chain.crt")
                );
            } catch (Exception e) {
                log.error("【{}】证书申请失败，原因：", configDomain, e);
            }
        } else {
            log.info("存在仍在有效期内的【{}】证书，不需要续期", configDomain);
            certificateId = configDomainCertificateList.get(0).getCertificateId();
        }

        if (certificateId == null) {
            log.error("【{}】证书配置失败，请检查", configDomain);
            return;
        }

        //查询该域名下的CDN列表
        List<Cdn> cdnList = deployService.getCdnList(configDomain);

        //更新CDN证书
        deployService.updateCdnCertificate(cdnList, certificateId);

    }
}

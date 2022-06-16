package site.lonelyman;

import lombok.extern.slf4j.Slf4j;
import site.lonelyman.config.HelperConfig;
import site.lonelyman.model.Certificate;
import site.lonelyman.service.DeployService;
import site.lonelyman.service.GenerateCertificateService;
import site.lonelyman.service.impl.TencentCloudDeployServiceImpl;
import site.lonelyman.util.FileUtils;

import java.util.List;

/**
 * <p>
 *
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

        log.info("证书列表:");
        for (Certificate certificate : certificateList) {
            log.info("证书ID：【{}】，域名：【{}】，到期时间：【{}】 ，是否即将过期：【{}】",
                    certificate.getCertificateId(),
                    certificate.getDomain(),
                    certificate.getCertEndTime(),
                    certificate.isExpiringSoon() ? "是" : "否");
        }

        if (certificateList.stream().noneMatch(certificate ->
                !certificate.isExpiringSoon() && certificate.getDomain().equals(configDomain))) {
            log.info("【{}】证书即将过期或已过期，需要续期", configDomain);
            try {
                GenerateCertificateService generateCertificateService = new GenerateCertificateService();
                generateCertificateService.fetchCertificate(configDomain);
                deployService.uploadCertificate(
                        FileUtils.readFile(configDomain + ".key"),
                        FileUtils.readFile(configDomain + "-chain.crt")
                );
            } catch (Exception e) {
                log.error("【{}】证书申请失败，原因：", configDomain, e);
            }
        } else {
            log.info("存在仍在有效期内的【{}】证书，不需要续期", configDomain);
        }

    }
}

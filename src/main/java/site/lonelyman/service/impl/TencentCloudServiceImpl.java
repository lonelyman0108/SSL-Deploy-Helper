package site.lonelyman.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tencentcloudapi.cdn.v20180606.CdnClient;
import com.tencentcloudapi.cdn.v20180606.models.*;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.dnspod.v20210323.DnspodClient;
import com.tencentcloudapi.dnspod.v20210323.models.*;
import com.tencentcloudapi.ssl.v20191205.SslClient;
import com.tencentcloudapi.ssl.v20191205.models.*;
import lombok.extern.slf4j.Slf4j;
import site.lonelyman.config.HelperConfig;
import site.lonelyman.model.Cdn;
import site.lonelyman.model.Certificate;
import site.lonelyman.service.ICdnService;
import site.lonelyman.service.ICertificateService;
import site.lonelyman.service.IDomainService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 腾讯云部署服务接口实现类
 * </p>
 *
 * @author LM
 * @since 2022/6/14
 */

@Slf4j
public class TencentCloudServiceImpl implements ICertificateService, ICdnService, IDomainService {
    private static final String SECRET_ID = HelperConfig.instance.getTencentCloudConfig().getSecretId();
    private static final String SECRET_KEY = HelperConfig.instance.getTencentCloudConfig().getSecretKey();
    private static final SslClient SSL_CLIENT = new SslClient(new Credential(SECRET_ID, SECRET_KEY), "");
    private static final DnspodClient DNSPOD_CLIENT = new DnspodClient(new Credential(SECRET_ID, SECRET_KEY), "");
    private static final CdnClient CDN_CLIENT = new CdnClient(new Credential(SECRET_ID, SECRET_KEY), "");


    @Override
    public List<Certificate> getCertificateList() {
        try {
            DescribeCertificatesResponse resp = SSL_CLIENT.DescribeCertificates(new DescribeCertificatesRequest());
            List<Certificate> certificateList = new ArrayList<>();
            log.info("↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ 腾讯云证书列表 ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓");
            for (Certificates certificate : resp.getCertificates()) {
                Certificate cert = new Certificate(
                        certificate.getCertificateId(),
                        certificate.getDomain(),
                        LocalDateTimeUtil.parse(certificate.getCertEndTime(), "yyyy-MM-dd HH:mm:ss"),
                        LocalDateTimeUtil.between(
                                LocalDateTime.now(),
                                LocalDateTimeUtil.parse(certificate.getCertEndTime(), "yyyy-MM-dd HH:mm:ss"), ChronoUnit.DAYS) < 30
                );
                certificateList.add(cert);
                log.info("证书ID：【{}】，域名：【{}】，到期时间：【{}】 ，是否即将过期：【{}】",
                        cert.getCertificateId(),
                        cert.getDomain(),
                        LocalDateTimeUtil.formatNormal(cert.getCertEndTime()),
                        cert.isExpiringSoon() ? "是" : "否");
            }
            log.info("↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ 腾讯云证书列表 ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑");
            return certificateList;
        } catch (TencentCloudSDKException e) {
            log.error("获取证书列表失败", e);
            return null;
        }
    }

    @Override
    public String uploadCertificate(String privateKey, String publicKey) {
        try {
            log.info("正在上传证书至腾讯云");
            UploadCertificateRequest req = new UploadCertificateRequest();
            req.setCertificatePrivateKey(privateKey);
            req.setCertificatePublicKey(publicKey);
            UploadCertificateResponse resp = SSL_CLIENT.UploadCertificate(req);
            log.info("证书上传成功，证书ID：【{}】", resp.getCertificateId());
            return resp.getCertificateId();
        } catch (TencentCloudSDKException e) {
            log.error("证书上传失败", e);
            return null;
        }
    }

    @Override
    public boolean createDomainRecord(String domain, String subDomain, String type, String value, boolean isOverwrite) {
        try {
            Long recordId = getRecordId(domain, subDomain, type);
            if (ObjectUtil.isNotNull(recordId)) {
                if (isOverwrite) {
                    ModifyRecordRequest modifyReq = new ModifyRecordRequest();
                    modifyReq.setDomain(domain);
                    modifyReq.setRecordType(type);
                    modifyReq.setRecordLine("默认");
                    modifyReq.setValue(value);
                    modifyReq.setSubDomain(subDomain);
                    modifyReq.setRecordId(recordId);
                    DNSPOD_CLIENT.ModifyRecord(modifyReq);
                } else {
                    return false;
                }
            } else {
                CreateRecordRequest createReq = new CreateRecordRequest();
                createReq.setDomain(domain);
                createReq.setRecordType(type);
                createReq.setRecordLine("默认");
                createReq.setValue(value);
                createReq.setSubDomain(subDomain);
                DNSPOD_CLIENT.CreateRecord(createReq);
            }
            return true;
        } catch (TencentCloudSDKException e) {
            return false;
        }
    }

    @Override
    public boolean deleteDomainRecord(String domain, String subDomain, String type) {
        try {
            Long recordId = getRecordId(domain, subDomain, type);
            if (ObjectUtil.isNotNull(recordId)) {
                DeleteRecordRequest deleteReq = new DeleteRecordRequest();
                deleteReq.setRecordId(recordId);
                deleteReq.setDomain(domain);
                DNSPOD_CLIENT.DeleteRecord(deleteReq);
                return true;
            } else {
                return false;
            }
        } catch (TencentCloudSDKException e) {
            return false;
        }
    }

    private Long getRecordId(String domain, String subDomain, String type) {
        Long recordId = null;
        try {
            DescribeRecordListRequest describeReq = new DescribeRecordListRequest();
            describeReq.setDomain(domain);
            DescribeRecordListResponse resp = DNSPOD_CLIENT.DescribeRecordList(describeReq);
            for (RecordListItem record : resp.getRecordList()) {
                if (record.getName().equals(subDomain) && record.getType().equals(type)) {
                    recordId = record.getRecordId();
                }
            }
        } catch (TencentCloudSDKException e) {
            return null;
        }
        return recordId;
    }

    @Override
    public boolean updateCdnCertificate(List<Cdn> cdnList, String certificateId) {
        try {
            for (Cdn cdn : cdnList) {
                UpdateDomainConfigRequest updateRequest = new UpdateDomainConfigRequest();
                Https https = new Https();
                ServerCert serverCert = new ServerCert();
                serverCert.setCertId(certificateId);
                https.setCertInfo(serverCert);
                https.setSwitch("on");
                ForceRedirect forceRedirect = new ForceRedirect();
                forceRedirect.setSwitch("on");
                updateRequest.setForceRedirect(forceRedirect);
                updateRequest.setHttps(https);
                updateRequest.setDomain(cdn.getDomain());
                CDN_CLIENT.UpdateDomainConfig(updateRequest);
                log.info("CDN域名：【{}】，证书ID：【{}】，更新成功", cdn.getDomain(), certificateId);
            }
            return true;
        } catch (TencentCloudSDKException e) {
            log.error("更新CDN证书失败", e);
            return false;
        }
    }

    @Override
    public List<Cdn> getCdnList(String domain) {
        try {
            DescribeDomainsConfigRequest describeRequest = new DescribeDomainsConfigRequest();
            DomainFilter domainFilter = new DomainFilter();
            domainFilter.setName("domain");
            domainFilter.setValue(new String[]{domain});
            domainFilter.setFuzzy(true);
            describeRequest.setFilters(new DomainFilter[]{domainFilter});
            DescribeDomainsConfigResponse describeResponse = CDN_CLIENT.DescribeDomainsConfig(describeRequest);

            List<Cdn> cdnList = new ArrayList<>();
            log.info("↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ 腾讯云CDN列表 ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓");
            for (DetailDomain detailDomain : describeResponse.getDomains()) {
                Cdn cdn = new Cdn(
                        detailDomain.getDomain(),
                        "on".equals(detailDomain.getHttps().getSwitch())
                );
                log.info("域名：【{}】，是否开启https：【{}】", cdn.getDomain(), cdn.isHttps());
                cdnList.add(cdn);
            }
            log.info("↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ 腾讯云CDN列表 ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑");
            return cdnList;
        } catch (TencentCloudSDKException e) {
            log.error("获取CDN列表失败", e);
            return null;
        }
    }
}

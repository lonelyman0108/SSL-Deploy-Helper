package site.lonelyman.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.dnspod.v20210323.DnspodClient;
import com.tencentcloudapi.dnspod.v20210323.models.*;
import com.tencentcloudapi.ssl.v20191205.SslClient;
import com.tencentcloudapi.ssl.v20191205.models.*;
import lombok.extern.slf4j.Slf4j;
import site.lonelyman.config.HelperConfig;
import site.lonelyman.model.Certificate;
import site.lonelyman.service.DeployService;

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
public class TencentCloudDeployServiceImpl implements DeployService {
    private static final String SECRET_ID = HelperConfig.instance.getTencentCloudConfig().getSecretId();
    private static final String SECRET_KEY = HelperConfig.instance.getTencentCloudConfig().getSecretKey();
    private static final SslClient SSL_CLIENT = new SslClient(new Credential(SECRET_ID, SECRET_KEY), "");
    private static final DnspodClient DNSPOD_CLIENT = new DnspodClient(new Credential(SECRET_ID, SECRET_KEY), "");


    @Override
    public List<Certificate> getCertificateList() {
        try {
            DescribeCertificatesResponse resp = SSL_CLIENT.DescribeCertificates(new DescribeCertificatesRequest());
            List<Certificate> certificateList = new ArrayList<>();
            for (Certificates certificate : resp.getCertificates()) {
                certificateList.add(new Certificate(
                        certificate.getCertificateId(),
                        certificate.getDomain(),
                        LocalDateTimeUtil.parse(certificate.getCertEndTime(), "yyyy-MM-dd HH:mm:ss"),
                        LocalDateTimeUtil.between(
                                LocalDateTime.now(),
                                LocalDateTimeUtil.parse(certificate.getCertEndTime(), "yyyy-MM-dd HH:mm:ss"), ChronoUnit.DAYS) < 30
                ));
            }
            return certificateList;
        } catch (TencentCloudSDKException e) {
            log.error("获取证书列表失败", e);
            return null;
        }
    }

    @Override
    public String uploadCertificate(String privateKey, String publicKey) {
        try {
            UploadCertificateRequest req = new UploadCertificateRequest();
            req.setCertificatePrivateKey(privateKey);
            req.setCertificatePublicKey(publicKey);
            UploadCertificateResponse resp = SSL_CLIENT.UploadCertificate(req);
            return resp.getCertificateId();
        } catch (TencentCloudSDKException e) {
            log.error("上传证书失败", e);
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
                DeleteRecordResponse response = DNSPOD_CLIENT.DeleteRecord(deleteReq);
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
}

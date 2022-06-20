package site.lonelyman.service;

import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import site.lonelyman.service.impl.TencentCloudServiceImpl;

import java.io.*;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 证书生成服务类
 * </p>
 *
 * @author LM
 * @since 2022/6/15
 */

@Slf4j
public class GenerateCertificateService {
    private static final File USER_KEY_FILE = new File("user.key");
    private static final int KEY_SIZE = 2048;

    private static final int RETRY_TIMES = 20;
    private final TencentCloudServiceImpl tencentCloudService = new TencentCloudServiceImpl();

    private KeyPair loadOrCreateUserKeyPair() throws IOException {
        if (USER_KEY_FILE.exists()) {
            try (FileReader fr = new FileReader(USER_KEY_FILE)) {
                return KeyPairUtils.readKeyPair(fr);
            }
        } else {
            KeyPair userKeyPair = KeyPairUtils.createKeyPair(KEY_SIZE);
            try (FileWriter fw = new FileWriter(USER_KEY_FILE)) {
                KeyPairUtils.writeKeyPair(userKeyPair, fw);
            }
            return userKeyPair;
        }
    }

    private Account findOrRegisterAccount(Session session, KeyPair accountKey) throws AcmeException {
        return new AccountBuilder()
                .agreeToTermsOfService()
                .useKeyPair(accountKey)
                .create(session);
    }

    private KeyPair loadOrCreateDomainKeyPair(String domain) throws IOException {
        File domainKey = new File(domain + ".key");
        if (domainKey.exists()) {
            try (FileReader fr = new FileReader(domainKey)) {
                return KeyPairUtils.readKeyPair(fr);
            }
        } else {
            KeyPair domainKeyPair = KeyPairUtils.createKeyPair(KEY_SIZE);
            try (FileWriter fw = new FileWriter(domainKey)) {
                KeyPairUtils.writeKeyPair(domainKeyPair, fw);
            }
            return domainKeyPair;
        }
    }

    public Challenge dnsChallenge(Authorization auth) throws AcmeException {
        Dns01Challenge challenge = auth.findChallenge(Dns01Challenge.TYPE);
        if (challenge == null) {
            throw new AcmeException("找不到DNS认证方式");
        }

        String domain = auth.getIdentifier().getDomain();

        log.info("正在添加【{}】的TXT记录:", domain);
        log.info("记录名:【_acme-challenge.{}】,记录值:【{}】", domain, challenge.getDigest());

        //新增TXT记录
        boolean result = tencentCloudService.createDomainRecord(
                domain,
                "_acme-challenge",
                "TXT",
                challenge.getDigest(),
                true);
        if (!result) {
            throw new AcmeException("添加【" + domain + "】的TXT记录失败");
        } else {
            log.info("添加【{}】的TXT记录成功，暂停5秒，等待解析生效", domain);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return challenge;
    }

    private void authorize(Authorization auth) throws AcmeException {
        String domain = auth.getIdentifier().getDomain();
        log.info("准备DNS认证【{}】", domain);
        if (auth.getStatus() != Status.VALID) {
            Challenge challenge = dnsChallenge(auth);
            if (challenge.getStatus() != Status.VALID) {
                challenge.trigger();
                try {
                    int attempts = 1;
                    while (challenge.getStatus() != Status.VALID && attempts <= RETRY_TIMES) {
                        System.out.println(challenge.getStatus());
                        log.info("等待域名验证...第{}/{}次", attempts, RETRY_TIMES);
                        if (challenge.getStatus() == Status.INVALID) {
                            throw new AcmeException("域名验证失败");
                        }
                        challenge.update();
                        Thread.sleep(1000L);
                        attempts++;
                    }
                } catch (InterruptedException ex) {
                    throw new AcmeException("域名验证被终止");
                }
                boolean result = tencentCloudService.deleteDomainRecord(
                        domain,
                        "_acme-challenge",
                        "TXT");
                if (result) {
                    log.info("删除【{}】TXT记录成功", domain);
                } else {
                    log.error("删除【{}】TXT记录失败，请手动删除", domain);
                }
            }
        }
        log.info("【{}】DNS认证成功", domain);
    }

    public void fetchCertificate(String domain) throws IOException, AcmeException {
        //泛域名
        List<String> domains = Arrays.asList(domain, "*." + domain);

        KeyPair userKeyPair = loadOrCreateUserKeyPair();

        Session session = new Session("acme://letsencrypt.org/staging");

        Account acct = findOrRegisterAccount(session, userKeyPair);

        KeyPair domainKeyPair = loadOrCreateDomainKeyPair(domain);

        Order order = acct.newOrder().domains(domains).create();

        for (Authorization auth : order.getAuthorizations()) {
            authorize(auth);
        }

        log.info("准备申请【{}】SSL证书", domain);
        CSRBuilder csReq = new CSRBuilder();
        csReq.addDomains(domains);
        csReq.sign(domainKeyPair);
        order.execute(csReq.getEncoded());

        try {
            int attempts = 1;
            while (order.getStatus() != Status.VALID && attempts <= RETRY_TIMES) {
                log.info("等待证书申请状态...第{}/{}次", attempts, RETRY_TIMES);
                if (order.getStatus() == Status.INVALID) {
                    throw new AcmeException("证书申请失败");
                }
                order.update();
                Thread.sleep(3000L);
                attempts++;
            }
        } catch (InterruptedException ex) {
            throw new AcmeException("证书申请被终止");
        }
        log.info("【{}】SSL证书申请成功", domain);

        Certificate certificate = order.getCertificate();

        log.info("【{}】的SSL证书下发成功", domains);

        try (FileWriter fw = new FileWriter(domain + "-chain.crt")) {
            certificate.writeCertificate(fw);
        }
    }
}

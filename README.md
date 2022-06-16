# SSL-Deploy-Helper

## 介绍
一个自动检测并更新签发脚本并上传至云服务上的小工具

## 用法
<ol>
<li>复制`config_sample.json`为`config.json`</li>
<li>修改`config.json`中的配置（目前只支持`tencentCloud`，七牛云的配置可以忽略）</li>
<li>执行`java -jar SSL-Deploy-Helper.jar`</li>
</ol>

## 功能
- [x] 检测云服务商托管的证书是否过期
- [x] 自动申请新的证书
- [x] 自动更新云服务商托管的证书
- [ ] 自动使用旧证书的服务为新证书
- [ ] 支持多个云服务商

## 依赖
- okhttp3
- hutool
- acme4j
- logback
- lombok
- qiniu-java-sdk
- tencentcloud-sdk-java
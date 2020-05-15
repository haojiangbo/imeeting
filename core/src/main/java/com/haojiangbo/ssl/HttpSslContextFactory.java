package com.haojiangbo.ssl;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.security.KeyStore;
import java.security.Security;

@Slf4j
public class HttpSslContextFactory {

    //    private static final String PROTOCOL = "SSLv2";
    /**
     * 客户端可以指明为SSLv3或者TLSv1.2
     */
    private static final String PROTOCOL = "SSLv3";
    /**
     * 针对于服务器端配置
     */
    private static SSLContext sslContext = null;

    static {
        String algorithm = Security
                .getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        SSLContext serverContext = null;
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(HttpsKeyStore.getKeyStoreStream(), HttpsKeyStore.getKeyStorePassword());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, HttpsKeyStore.getCertificatePassword());
            serverContext = SSLContext.getInstance(PROTOCOL);
            serverContext.init(kmf.getKeyManagers(), null, null);
        } catch (Exception e) {
            log.info("初始化server SSL失败", e);
            throw new Error("Failed to initialize the server SSLContext", e);
        }
        sslContext = serverContext;
    }

    public static SSLEngine createSslEngine() {
        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(false);
        sslEngine.setNeedClientAuth(false);
        return sslEngine;
    }
}


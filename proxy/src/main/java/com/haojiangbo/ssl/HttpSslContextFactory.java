package com.haojiangbo.ssl;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.security.KeyStore;

@Slf4j
public class HttpSslContextFactory {


    /**
     * 客户端可以指明为 SSLv2 | SSLv3
     */
    private static final  String PROTOCOL = "TLS";
    /**
     * TLS / JKS
     */
    private static final String  KEYSTORYTYPE = "PKCS12" ;


    private static final String  ALGORITHM= "SunX509";

    // Security
    //                .getProperty("ssl.KeyManagerFactory.algorithm");

    private static  SSLContext init(String keyStorePath,String keyStoryPassword){
        SSLContext serverContext = null;
        try {
            serverContext = SSLContext.getInstance(PROTOCOL);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(ALGORITHM);
            KeyStore ks = KeyStore.getInstance(KEYSTORYTYPE);
            ks.load(new FileInputStream(keyStorePath), keyStoryPassword.toCharArray());
            kmf.init(ks, keyStoryPassword.toCharArray());
            serverContext.init(kmf.getKeyManagers(), null, null);
        } catch (Exception e) {
            log.error("初始化server SSL失败", e.getMessage());
            throw new Error("Failed to initialize the server SSLContext", e);
        }
        return serverContext;
    }

    public static SSLEngine createSslEngine(String keyStorePath,String keyStoryPassword) {
        SSLEngine sslEngine = init(keyStorePath,keyStoryPassword).createSSLEngine();
        sslEngine.setUseClientMode(false);
        sslEngine.setNeedClientAuth(false);
        return sslEngine;
    }
}


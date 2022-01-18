package com.server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.jetbrains.annotations.NonNls;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.Executors;

public class WebhookReceiver {
    private HttpsServer httpsServer;

    private WebhookReceiver() {}

    public static WebhookReceiver start(String contextPath, HttpHandler httpHandler) throws IOException, GeneralSecurityException {
        WebhookReceiver webhookReceiver = new WebhookReceiver();
        webhookReceiver.httpsServer = HttpsServer.create(new InetSocketAddress(443), 0);
        SSLContext sslContext = SSLContext.getInstance("TLS");

        @NonNls char[] password = "password".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream fis = new FileInputStream("testkey.jks");
        ks.load(fis, password);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        webhookReceiver.httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters httpsParameters) {
                SSLContext context = getSSLContext();
                SSLEngine engine = context.createSSLEngine();
                httpsParameters.setNeedClientAuth(false);
                httpsParameters.setCipherSuites(engine.getEnabledCipherSuites());
                httpsParameters.setProtocols(engine.getEnabledProtocols());

                SSLParameters sslParameters = context.getSupportedSSLParameters();
                httpsParameters.setSSLParameters(sslParameters);
            }
        });

        webhookReceiver.httpsServer.createContext(contextPath, httpHandler);

        webhookReceiver.httpsServer.setExecutor(Executors.newCachedThreadPool());
        webhookReceiver.httpsServer.start();

        return webhookReceiver;
    }

    public void shutdown() {
        httpsServer.stop(1);
    }
}
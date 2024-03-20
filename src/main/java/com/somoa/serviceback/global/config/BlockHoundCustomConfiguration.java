package com.somoa.serviceback.global.config;

import reactor.blockhound.BlockHound;
import reactor.blockhound.integration.BlockHoundIntegration;

public class BlockHoundCustomConfiguration implements BlockHoundIntegration {

    @Override
    public void applyTo(BlockHound.Builder builder){
        builder.allowBlockingCallsInside("io.netty.util.concurrent.FastThreadLocalRunnable", "run")
                .allowBlockingCallsInside("java.io.FileInputStream", "readBytes")
                .allowBlockingCallsInside("java.security.Provider$Service", "newInstance")
                .allowBlockingCallsInside("sun.security.ssl.SSLContextImpl", "DefaultSSLContext")
                .allowBlockingCallsInside(
                "io.netty.handler.ssl.SslContext",
                "newClientContextInternal")
        ;
    }
}

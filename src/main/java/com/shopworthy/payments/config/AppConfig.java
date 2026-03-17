package com.shopworthy.payments.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${gateway.api.key:sk_live_shopworthy_gateway_abc123xyz}")
    private String gatewayApiKey;

    @Value("${gateway.api.secret:gateway-secret-do-not-share}")
    private String gatewayApiSecret;

    public String getGatewayApiKey() { return gatewayApiKey; }
    public String getGatewayApiSecret() { return gatewayApiSecret; }
}

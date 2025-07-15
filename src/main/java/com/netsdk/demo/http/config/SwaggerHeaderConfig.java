package com.netsdk.demo.http.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerHeaderConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("大华NetSDK Http服务API")
            .version("1.0")
            .description("所有接口均需在请求头传递Dh-Device-Ip、Dh-Device-Password、Dh-Device-Timestamp、Dh-Device-User（可选，默认admin）、Dh-Device-Port（可选，默认37777）"));
    }

    @Bean
    public OpenApiCustomiser globalHeaderOpenApiCustomiser() {
        return openApi -> {
            Parameter deviceIp = new Parameter()
                .in("header")
                .schema(new StringSchema())
                .name("Dh-Device-Ip")
                .description("设备IP")
                .required(true);
            Parameter devicePassword = new Parameter()
                .in("header")
                .schema(new StringSchema())
                .name("Dh-Device-Password")
                .description("加密密码串，也作为nonce唯一标识")
                .required(true);
            Parameter deviceTimestamp = new Parameter()
                .in("header")
                .schema(new StringSchema())
                .name("Dh-Device-Timestamp")
                .description("时间戳")
                .required(true);
            Parameter deviceUser = new Parameter()
                .in("header")
                .schema(new StringSchema())
                .name("Dh-Device-User")
                .description("用户名，默认admin")
                .required(false);

            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                operation.addParametersItem(deviceIp);
                operation.addParametersItem(devicePassword);
                operation.addParametersItem(deviceTimestamp);
                operation.addParametersItem(deviceUser);
            }));
        };
    }
} 
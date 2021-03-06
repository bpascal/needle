package com.codido.needle.bui.common.config;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 环境参数
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Configuration
public class EvnConfig {

    @Value("${spring.profiles.active}")
    private String evn;
}

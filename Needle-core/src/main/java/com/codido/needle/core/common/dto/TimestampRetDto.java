package com.codido.needle.core.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 取时间戳返回对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TimestampRetDto implements Serializable{

    /**
     * 时间戳
     */
    private String timestamp;
}

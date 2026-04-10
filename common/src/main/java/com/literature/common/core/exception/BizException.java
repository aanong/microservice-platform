package com.literature.common.core.exception;

import com.literature.common.core.model.ErrorCode;

public class BizException extends RuntimeException {
    private final String code;

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message) {
        super(message);
        this.code = ErrorCode.SYSTEM_ERROR;
    }

    public String getCode() {
        return code;
    }
}

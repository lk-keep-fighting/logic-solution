package com.aims.logic.ide.exception;

import com.aims.logic.sdk.dto.ApiError;
import com.aims.logic.sdk.dto.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ControllerExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity exceptionHandler(HttpServletRequest httpServletRequest, Exception e) {
        logger.error("服务错误:", e);
        return new ResponseEntity<ApiResult>(
                new ApiResult()
                        .setMsg(e.getLocalizedMessage())
                        .setError(new ApiError().setCode(500)
                                .setMsg(e.getLocalizedMessage())
                                .setDetail(e.getStackTrace()))
                        .setCode(500),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

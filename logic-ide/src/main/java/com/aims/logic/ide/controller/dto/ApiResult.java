package com.aims.logic.ide.controller.dto;

import com.aims.logic.runtime.contract.dto.LogicRunResult;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class ApiResult<T> {
    private int code = 0;
    private String msg;
    private T data;
    private Object debug;
    private ApiError error;

    public static ApiResult fromLogicRunResult(LogicRunResult res) {
        return new ApiResult()
                .setCode(res.isSuccess() ? 0 : 500)
                .setMsg(res.getMsg())
                .setDebug(res)
                .setData(res.getData());
    }

    public static ApiResult ok(Object data) {
        return new ApiResult().setData(data);
    }

    public static ApiResult error(String msg) {
        return new ApiResult().setCode(500).setMsg(msg);
    }

    public static ApiResult fromException(Exception ex) {
        return new ApiResult()
                .setCode(500)
                .setMsg(ex.getMessage())
                .setError(new ApiError()
                        .setCode(500)
                        .setMsg(ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage())
                        .setDetail(ex.getCause() != null ? ex.getCause() : ex));
    }
}

package com.aims.logic.service.dto;

import com.aims.logic.runtime.contract.dto.LogicRunResult;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class ApiResult {
    private int code = 0;
    private String msg;
    private Object data;
    private Object debug;
    private ApiError error;

    public static ApiResult fromLogicRunResult(LogicRunResult res) {
        return new ApiResult()
                .setCode(res.isSuccess() ? 0 : 500)
                .setMsg(res.getMsg())
                .setData(res.getData());
    }
}

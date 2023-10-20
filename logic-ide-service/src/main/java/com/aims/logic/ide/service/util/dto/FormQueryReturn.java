package com.aims.logic.ide.service.util.dto;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

@Data
public class FormQueryReturn {
    private boolean success;
    private FormQueryReturnResult result;
    private JSONObject error;
}

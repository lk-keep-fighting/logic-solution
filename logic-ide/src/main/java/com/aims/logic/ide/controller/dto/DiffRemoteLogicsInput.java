package com.aims.logic.ide.controller.dto;

import com.aims.logic.sdk.dto.FormQueryInput;
import lombok.Data;

@Data
public class DiffRemoteLogicsInput {
    private String hostName;
    private FormQueryInput  queryInput;
}

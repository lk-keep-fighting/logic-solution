package com.aims.logic.sdk.service;

import com.aims.logic.sdk.entity.LogicBakEntity;

public interface LogicBakService extends BaseService<LogicBakEntity, String> {
    LogicBakEntity getByIdAndVersion(String id, String version);
}

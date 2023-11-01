package com.aims.logic.sdk.service;

import com.aims.logic.sdk.entity.LogicEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

public interface LogicService extends BaseService<LogicEntity> {
    /**
     * 编辑并备份
     *
     * @param id
     * @param input
     * @return
     */
    int editAndBak(String id, LogicEntity input);

    /**
     * 发布到本地
     * @param id 路径编号
     * @return 保存路径
     */

    String pubToLocal(String id);
}

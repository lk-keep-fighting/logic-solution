package com.aims.logic.sdk;

import com.aims.logic.runtime.contract.dsl.LogicTreeNode;
import com.aims.logic.runtime.contract.dto.LongtimeRunningBizDto;
import com.aims.logic.runtime.contract.dto.UnCompletedBizDto;
import com.aims.logic.sdk.dto.Page;
import com.aims.logic.sdk.entity.LogicInstanceEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 编排数据管理接口
 */
public interface LogicDataService {
    /**
     * 根据创建时间查询业务实例
     *
     * @param createTimeFrom
     * @param createTimeTo
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<LogicInstanceEntity> queryBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, List<String> bizIds, long pageNum, long pageSize);

    /**
     * 查询超时运行的业务
     *
     * @param timeout 超时时间，单位秒
     * @return
     */
    List<LongtimeRunningBizDto> queryLongtimeRunningBiz(int timeout);

    List<UnCompletedBizDto> queryUncompletedBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning);

    /**
     * 查询未完成实例，参数为null则不根据此条件筛选
     *
     * @param createTimeFrom
     * @param createTimeTo
     * @param isRunning
     * @param isSuccess
     * @return
     */
    List<UnCompletedBizDto> queryUncompletedBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning, Boolean isSuccess);

    /**
     * 查询未完成实例，参数为null则不根据此条件筛选
     *
     * @param createTimeFrom  创建开始从……
     * @param createTimeTo    创建时间到……
     * @param isRunning       是否运行中
     * @param isSuccess       是否有异常
     * @param maxRetryTimes   最大重试次数<=
     * @param excludeLogicIds 不包含的逻辑编号
     * @return
     */

    List<UnCompletedBizDto> queryUncompletedBizExclude(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, Boolean isRunning, Boolean isSuccess, Integer maxRetryTimes, List<String> excludeLogicIds);

    /**
     * 更新业务实例重试次数
     *
     * @param logicId
     * @param bizId
     * @param retryTimes
     */
    int updateBizRetryTimes(String logicId, String bizId, int retryTimes);

    /**
     * 根据逻辑编号和bizId获取业务实例
     *
     * @param logicId
     * @param bizId
     * @return
     */
    LogicInstanceEntity getBiz(String logicId, String bizId);

    /**
     * 删除业务实例
     * 为了避免清空表，时间区间和ids至少有一个值，否则无法执行
     *
     * @param createTimeFrom
     * @param createTimeTo
     * @param ids
     * @return
     */
    int deleteBiz(LocalDateTime createTimeFrom, LocalDateTime createTimeTo, List<String> ids);

    /**
     * 尽可能找到指定版本的编排配置
     * logic_bak 表-》当前运行时（online从logic表，offline从文件）
     * 找到后保存到logic_bak
     *
     * @param logicId
     * @param version
     * @return
     */
    LogicTreeNode tryGetLogicConfigByAllWays(String logicId, String version);
}

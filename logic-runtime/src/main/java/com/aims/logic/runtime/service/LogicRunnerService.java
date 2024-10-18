package com.aims.logic.runtime.service;

import com.aims.logic.runtime.contract.dto.LogicRunResult;
import com.aims.logic.runtime.env.LogicEnvObject;
import com.alibaba.fastjson2.JSONObject;

import java.util.*;

public interface LogicRunnerService {
    /**
     * 传入自定义环境变量执行逻辑
     *
     * @param customEnv  自定义环境变量
     * @param isOverride 是否覆盖原有环境变量
     * @return 返回参数
     */
    JSONObject setEnv(JSONObject customEnv, boolean isOverride);

    /**
     * 获取json格式的环境变量，包含自定义值
     *
     * @return
     */
    JSONObject getEnvJson();

    /**
     * 获取强类型环境变量，无法获取自定义值，若需要获取自定义值，请使用getEnvJson方法
     *
     * @return
     */
    LogicEnvObject getEnv();

    /**
     * 传入自定义环境变量创建逻辑运行器
     *
     * @param env 自定义环境变量
     * @return 返回逻辑运行器
     */

    LogicRunnerService newInstance(JSONObject env);

    /**
     * 无状态-入参为json字符串
     *
     * @param logicId        逻辑编号
     * @param parsJsonString 入参json字符串
     * @return 逻辑执行结果
     */
    LogicRunResult runByJson(String logicId, String parsJsonString);

    /**
     * 无状态-入参为可变对象数组
     * ！！传入对象会按顺序转换为_p1、_p2...形式的Map<String,Object>
     *
     * @param logicId 逻辑编号
     * @param pars    可变入参对象
     * @return 逻辑执行结果
     */

    LogicRunResult runByObjectArgs(String logicId, Object... pars);

    /**
     * 无状态-入参为Map<String,Object>，保留强类型
     * 所有runBy的方法最终会转换为此方法执行
     *
     * @param logicId 逻辑编号
     * @param parsMap 入参
     * @return 返回参数
     */
    LogicRunResult runByMap(String logicId, Map<String, Object> parsMap);

    /**
     * 有状态-入参为json字符串
     *
     * @param logicId        逻辑编号
     * @param bizId          业务编号
     * @param parsJsonString 入参json字符串
     * @return 逻辑执行结果
     */
    LogicRunResult runBizByJson(String logicId, String bizId, String parsJsonString);

    /**
     * 有状态-入参为可变对象数组
     * ！！传入对象会按顺序转换为_p1、_p2...形式的Map<String,Object>
     *
     * @param logicId 逻辑编号
     * @param bizId   业务编号
     * @param pars    可变入参对象
     * @return 逻辑执行结果
     */

    LogicRunResult runBizByObjectArgs(String logicId, String bizId, Object... pars);

    /**
     * 有状态-入参为Map<String,Object>，保留强类型
     * 所有runBizBy方法会转换为此方法执行
     *
     * @param logicId 逻辑编号
     * @param bizId   业务编号
     * @param parsMap 入参Map
     * @return 逻辑执行结果
     */
    LogicRunResult runBizByMap(String logicId, String bizId, Map<String, Object> parsMap);

    /**
     * 重试存在异常的业务，通过实例缓存读取入参、临时变量和环境变量
     *
     * @param logicId
     * @param bizId
     * @return
     */
    LogicRunResult retryErrorBiz(String logicId, String bizId);

    /**
     * 更新当前业务实例入参
     *
     * @param logicId
     * @param bizId
     * @param pars
     * @return
     */
    boolean updateBizInstanceParams(String logicId, String bizId, Object... pars);

    /**
     * 先校验验证码，再执行业务逻辑
     *
     * @param logicId        逻辑编号
     * @param bizId          业务编号
     * @param verifyCode     验证码
     * @param parsJsonString 入参json
     * @return
     */

    LogicRunResult runBizByVerifyCode(String logicId, String bizId, String verifyCode, String parsJsonString);

    /**
     * 先校验验证码，再执行业务逻辑
     *
     * @param logicId    逻辑编号
     * @param bizId      业务编号
     * @param verifyCode 验证码
     * @param parsMap    入参Map
     * @return
     */

    LogicRunResult runBizByVerifyCode(String logicId, String bizId, String verifyCode, Map<String, Object> parsMap);

    void clearLog();

    void clearCompletedInstance();
    //
//    LogicRunResult runBizStepByStep(String logicId, String bizId, JSONObject pars);
//
//    LogicRunResult runBizToNextJavaMethod(String logicId, String bizId, JSONObject pars);
//
//    void updateBizAfterRunBizToNextJavaMethod(LogicRunResult result);

}

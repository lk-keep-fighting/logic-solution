package com.aims.logic.runtime.contract.dsl;

import com.aims.logic.runtime.contract.dsl.basic.BaseLASL;
import com.aims.logic.runtime.contract.enums.ConceptEnum;
import com.aims.logic.runtime.contract.enums.LogicItemTransactionScope;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class LogicItemTreeNode extends BaseLASL {
    public LogicItemTreeNode() {
        concept = ConceptEnum.LogicItem;
    }

    String id;
    String name;
    String version;
    String group;
    /**
     * 组件的唯一标识，根据name + group
     */
    String itemId;
    /**
     * 组件的唯一标识，根据name + group
     */
    public String getItemId() {
        if (StringUtils.isBlank(itemId)) {
            itemId = generateId(name, group);
        }
        return itemId;
    }

    /**
     * 组件版本控制的唯一标识，不同版本会不同
     * 资产Id可用于唯一标识特定版本的组件，只要id不变，资产对外实现的功能不变
     */
    String cbbId;
    /**
     * 组件版本控制的唯一标识，不同版本会不同
     * 资产Id可用于唯一标识特定版本的组件，只要id不变，资产对外实现的功能不变
     */
    public String getCbbId() {
        if (StringUtils.isBlank(cbbId)) {
            cbbId = generateId(name, group, version);
        }
        return cbbId;
    }

    /**
     * 节点代码
     */
    String code;
    String type;
    String script;
    /**
     * 是否关闭业务实例调用
     */
    boolean bizOff = false;
    /**
     * isBizOn=false时，传入业务标识，可以是js表达式
     */
    String bizId;


    String url;
    /**
     * 方法格式为方法名(参数名称1，参数2名称2)
     */
    String method;
    String headers;
    /**
     * http请求body
     */
    String body;
    /**
     * java等强类型参数声明
     */
    List<ParamTreeNode> params;
    /**
     * java等强类型返回值声明
     */
    ReturnTreeNode returnType;
    String timeout;
    String nextId;

    // 设置方法的值，方法格式为方法名(参数名称1，参数2名称2)
    public void setMethod(String methodName, String[] paramNames) {
        this.method = methodName + "(" + StringUtils.join(paramNames, ",") + ")";
    }

    public String getTimeout() {
        return StringUtils.isBlank(timeout) ? "5000" : timeout;
    }

    /**
     * 事务范围
     * EveryJavaNode-每个java节点开启事务
     * EveryJavaNode2-每个java节点开启事务但报错不会中断
     * EveryRequest-每次请求交互开启事务，即每个交互点
     * off-关闭事务
     */
    LogicItemTransactionScope tranScope;
    /*
    事务传播属性
     */
    int tranPropagation;
    /*
    事务组id
     */
    String tranGroupId;
    /*
    源代码
     */
    String sourceCode;
    /*
    git信息
     */
    String gitInfo;
    /*
    条件分支表达式
     */
    String condition;
    /*
    返回值接收参数
     */
    String returnAccept;
    /*
    switch分支
     */
    List<LogicItemBranch> branches;
    /*
    是否异步执行，异步执行将不等待返回结果，默认成功
     */
    boolean async = false;
    String memo;
    /*
    节点执行时实例化的对象唯一编号
    用于循环调用时的链路追踪
     */
    String objectId;


    /**
     * 生成版本的哈希值，基于 name + group + version
     *
     * @return
     */
    public String generateId(String... pars) {
        StringBuilder input = new StringBuilder();
        if (pars != null) {
            for (String par : pars) {
                input.append("|").append(par);
            }
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.toString().getBytes(StandardCharsets.UTF_8));
            // 使用前16字节而不是8字节，减少冲突概率，同时保持ID相对简短
            byte[] truncated = new byte[16];
            System.arraycopy(hash, 0, truncated, 0, Math.min(hash.length, 16));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(truncated);
        } catch (Exception e) {
            throw new RuntimeException("ID 生成失败！", e);
        }
    }
}

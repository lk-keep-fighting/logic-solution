package com.aims.logic.runtime.contract.dsl.basic;

import com.aims.logic.runtime.contract.enums.ConceptEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class StructurePropertyTreeNode extends BaseLASL {
    String name; // 数据结构属性名称
//    String label; // 数据结构属性标题
//    String description; // 数据结构属性描述
    TypeAnnotationTreeNode typeAnnotation; // 类型
//    boolean required; // 是否必填
    String defaultValue; // 默认值，JSON 字符串
}
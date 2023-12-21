package com.aims.logic.contract.dsl.basic;

import com.aims.logic.contract.enums.ConceptEnum;
import com.aims.logic.contract.enums.TypeKindEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
@Getter
@Setter
@Accessors(chain = true)
public class TypeAnnotationTreeNode extends BaseLASL {
    public TypeAnnotationTreeNode() {
        concept = ConceptEnum.TypeAnnotation;
    }
    TypeKindEnum typeKind; // 类型种类
    String typeNamespace; // 类型命名空间
    String typeName; // 类型名称
    List<TypeAnnotationTreeNode> typeArguments; // 类型参数
    List<TypeAnnotationTreeNode> returnType; // 返回类型
    boolean inferred; // 是否是推断出来的
    List<StructurePropertyTreeNode> properties; // 匿名数据结构属性
    Object ruleMap; // 规则对象
    String defaultValue;
}
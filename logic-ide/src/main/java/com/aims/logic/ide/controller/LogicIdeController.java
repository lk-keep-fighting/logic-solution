package com.aims.logic.ide.controller;

import com.aims.logic.runtime.contract.dsl.LogicTreeNode;
import com.aims.logic.runtime.contract.dsl.ParamTreeNode;
import com.aims.logic.runtime.contract.dsl.basic.TypeAnnotationTreeNode;
import com.aims.logic.runtime.contract.parser.TypeAnnotationParser;
import com.aims.logic.sdk.dto.ApiResult;
import com.aims.logic.sdk.dto.FormQueryInput;
import com.aims.logic.sdk.dto.LogicClassDto;
import com.aims.logic.sdk.dto.LogicClassMethodDto;
import com.aims.logic.sdk.entity.LogicBakEntity;
import com.aims.logic.sdk.entity.LogicEntity;
import com.aims.logic.sdk.mapper.LogicMapper;
import com.aims.logic.sdk.service.LogicBakService;
import com.aims.logic.sdk.service.LogicService;
import com.aims.logic.runtime.util.ClassUtils;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class LogicIdeController {
    private final LogicMapper logicMapper;
    private final LogicService logicService;
    private final LogicBakService logicBakService;

    @Value("${logic.scan-package-names}")
    private List<String> ScanPackageNames;
//    @Value("${logic.annotation}")
//    private String aa;

    @Autowired
    public LogicIdeController(
            LogicMapper logicMapper,
            LogicService logicService,
            LogicBakService logicBakService) {
        this.logicMapper = logicMapper;
        this.logicService = logicService;
        this.logicBakService = logicBakService;
    }


    @PostMapping("/api/ide/logic/add")
    public ApiResult<Boolean> addLogic(@RequestBody LogicEntity body) {
        var res = body.insert();
        return new ApiResult<Boolean>().setData(res);
    }

    @PostMapping("/api/ide/logics")
    public ApiResult<Page<LogicEntity>> logicList(@RequestBody FormQueryInput input) {
        var list = logicService.selectPage(input);
        return new ApiResult<Page<LogicEntity>>().setData(list);
    }

    @PutMapping("/api/ide/logic/edit/{id}")
    public ApiResult<Integer> editLogic(@PathVariable String id, @RequestBody LogicEntity body) {
        var res = logicService.editAndBak(id, body);
        return new ApiResult<Integer>().setData(res);
    }

    @DeleteMapping("/api/ide/logic/delete/{id}")
    public ApiResult<Integer> deleteLogic(@PathVariable String id) {
        var res = logicMapper.deleteById(id);
        return new ApiResult<Integer>().setData(res);
    }

    @GetMapping("/api/ide/logic/{id}")
    public ApiResult<LogicEntity> getLogic(@PathVariable String id) {
        var logicEntity = logicMapper.selectById(id);
        return new ApiResult<LogicEntity>().setData(logicEntity);
    }

    @GetMapping("/api/ide/logic/{id}/config")
    public ApiResult<LogicTreeNode> getLogicConfig(@PathVariable String id) {
        var logicEntity = logicMapper.selectById(id);
        if (logicEntity != null) {
            var config = logicEntity.getConfigJson();
            var res = JSON.isValid(config) ? JSON.parseObject(config, LogicTreeNode.class) : null;
            return new ApiResult<LogicTreeNode>().setData(res);
        }
        return new ApiResult<LogicTreeNode>();
    }

    @GetMapping("/api/ide/logic/{id}/config/{version}")
    public ApiResult<LogicTreeNode> getLogicConfigByVersion(@PathVariable String id, @PathVariable String version) {
        QueryWrapper<LogicBakEntity> queryWrapper = new QueryWrapper<>();
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("version", version);
        queryWrapper.allEq(map);
        var logicBakEntityEntity = logicBakService.getOne(queryWrapper);
        if (logicBakEntityEntity != null) {
            var config = logicBakEntityEntity.getConfigJson();
            var res = JSON.isValid(config) ? JSON.parseObject(config, LogicTreeNode.class) : null;
            return new ApiResult<LogicTreeNode>().setData(res);
        }
        int[] n = {1, 2, 3};
        return new ApiResult<>();
    }

    @GetMapping("/api/ide/asset/v1/java/classes/{packageName}")
    public ApiResult classList(@PathVariable String packageName) {
        var res = getAllClassNames(packageName);
        return new ApiResult().setData(res);
    }

    @GetMapping("/api/ide/asset/v1/java/classes")
    public ApiResult curPackageClassList() {
//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//        var res = classLoader.getDefinedPackages();
        List<LogicClassDto> classDtos = new ArrayList<>();
        for (String name : ScanPackageNames) {
            var res = getAllClassNames(name);
            classDtos.addAll(res);
        }

        return new ApiResult().setData(classDtos);
    }

    private static final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @GetMapping("/api/ide/asset/v1/java/class/{fullClassPath}/methods")
    public ApiResult<List<LogicClassMethodDto>> classMethods(@PathVariable String fullClassPath) throws ClassNotFoundException {
        List<LogicClassMethodDto> methodDtos = ClassUtils.getMethods(fullClassPath).stream()
                .map(m -> {
                    var dto = new LogicClassMethodDto().setName(m.getName());
                    var paramNames = discoverer.getParameterNames(m);
                    var paramTypes = m.getGenericParameterTypes();
                    if (paramNames != null) {
                        var pars = IntStream.range(0, paramTypes.length)
                                .mapToObj(i -> createParamTreeNode(paramNames[i], paramTypes[i]))
                                .collect(Collectors.toList());
                        dto.setParameters(pars);
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new ApiResult<List<LogicClassMethodDto>>().setData(methodDtos);
    }

    private ParamTreeNode createParamTreeNode(String paramName, Type paramType) {
        ParamTreeNode p = new ParamTreeNode(paramName)
                .setTypeAnnotation(TypeAnnotationParser.createTypeAnnotationTreeNode(paramType));
        if (paramType instanceof Class<?> clazz) {//通过NotNull注解判断是否必填
            p.setRequired(clazz.getAnnotation(NotNull.class) != null);
        }
        return p;
    }

//    @GetMapping("/api/ide/asset/v1/java/class/{fullClassPath}/methods")
//    public ApiResult<List<LogicClassMethodDto>> classMethods(@PathVariable String fullClassPath) throws ClassNotFoundException {
//        var res = ClassUtils.getDeclaredMethods(fullClassPath);
//        List<LogicClassMethodDto> methodDtos = new ArrayList<>();
//        res.forEach(m -> {
//            var dto = new LogicClassMethodDto().setName(m.getName());
//            List<ParamTreeNode> pars = new ArrayList<>();
//            var paramNames = discoverer.getParameterNames(m);
//            var paramTypes = m.getGenericParameterTypes();
////            var genericParameterTypes = m.getGenericParameterTypes();
//            if (paramNames != null) {
//                for (int i = 0; i < paramTypes.length; i++) {
//                    var p = paramTypes[i];
//                    List<TypeAnnotationTreeNode> typeArguments = new ArrayList<>();
//                    if (p instanceof ParameterizedType) {
//                        var typeP = (ParameterizedType) p;
//                        for (var arg : typeP.getActualTypeArguments()) {
//                            typeArguments.add(new TypeAnnotationTreeNode()
//                                    .setTypeName(((Class) arg).getSimpleName())
//                                    .setTypeNamespace(arg.getTypeName()));
//                        }
//                        pars.add(new ParamTreeNode(paramNames[i]).setTypeAnnotation(
//                                new TypeAnnotationTreeNode()
//                                        .setTypeName(typeP.getTypeName())
//                                        .setTypeNamespace(typeP.getTypeName())
//                                        .setTypeArguments(typeArguments))
//
//                        );
//                    } else {
//                        if (DataType.isSimpleDataType(p.getTypeName())) {
//                            String typeName = DataType.getJavaObjectType(p.getTypeName());
//                            pars.add(new ParamTreeNode(paramNames[i]).setTypeAnnotation(
//                                    new TypeAnnotationTreeNode()
//                                            .setTypeKind(TypeKindEnum.primitive)
//                                            .setTypeName(typeName)
//                                            .setTypeNamespace(typeName)));
//                        } else {
//                            pars.add(new ParamTreeNode(paramNames[i]).setTypeAnnotation(
//                                    new TypeAnnotationTreeNode()
//                                            .setTypeName(p.getTypeName())
//                                            .setTypeNamespace(p.getTypeName())));
//                        }
//                    }
//                }
//            }
//            dto.setParameters(pars);
//            methodDtos.add(dto);
//        });
//        return new ApiResult<List<LogicClassMethodDto>>().setData(methodDtos);
//    }
//

    @PostMapping("/api/ide/asset/v1/java/class/{fullClassPath}/method/{methodName}/params")
    public ApiResult<List<ParamTreeNode>> getMethodParams(@RequestBody TypeAnnotationTreeNode[] typeParames, @PathVariable String fullClassPath, @PathVariable String methodName) throws ClassNotFoundException, NoSuchMethodException {
        ApiResult<List<ParamTreeNode>> res = new ApiResult<>();
        if (typeParames != null) {
            var typeArrays = Arrays.stream(typeParames).map(p -> p.getTypeName()).toList();
            var method = ClassUtils.getDeclaredMethod(fullClassPath, methodName, typeArrays);
            List<ParamTreeNode> paramTreeNodes = new ArrayList<>();
            var paramNames = discoverer.getParameterNames(method);
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    paramTreeNodes.add(new ParamTreeNode(paramNames[i]).setTypeAnnotation(typeParames[i]));
                }
            }
            res.setData(paramTreeNodes);
        }
        return res;
    }

    public List<LogicClassDto> getAllClassNames(String packageName) {
        String packagePath = packageName.replace(".", "/");
        List<LogicClassDto> classNames = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        var res = classLoader.getResource(packagePath);
        if (res == null) return classNames;
        File packageDir = new File(res.getFile());
        if (packageDir.exists() && packageDir.isDirectory()) {
            File[] files = packageDir.listFiles();
            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile() && fileName.endsWith(".class")) {
                    String className = packageName + "." + fileName.substring(0, fileName.lastIndexOf(".class"));
                    classNames.add(new LogicClassDto(className));
                } else if (file.isDirectory()) {
                    String subPackageName = packageName + "." + fileName;
                    List<LogicClassDto> subClassNames = getAllClassNames(subPackageName);
                    classNames.addAll(subClassNames);
                }
            }
        }
        return classNames;
    }


}

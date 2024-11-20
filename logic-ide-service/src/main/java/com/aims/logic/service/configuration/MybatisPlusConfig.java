package com.aims.logic.service.configuration;

//@Configuration
//@MapperScan("com.aims.logic.sdk.mapper")
//public class MybatisPlusConfig {
//
//    /**
//     * 添加分页插件
//     */
//    @Bean("MybatisPlusInterceptor")
//    public MybatisPlusInterceptor mybatisPlusInterceptor() {
//        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
//        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));//如果配置多个插件,切记分页最后添加
//        //interceptor.addInnerInterceptor(new PaginationInnerInterceptor()); 如果有多数据源可以不配具体类型 否则都建议配上具体的DbType
//        return interceptor;
//    }
//}
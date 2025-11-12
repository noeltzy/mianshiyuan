package com.tzy.mianshiyuan.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * 配置分页插件
 * 
 * 参考文档：
 * - 安装指南：https://baomidou.com/getting-started/install/
 * - 分页插件：https://baomidou.com/plugins/pagination/
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置MyBatis-Plus拦截器，添加分页插件
     * 
     * 注意事项：
     * 1. 如果配置多个插件，切记分页插件最后添加
     * 2. 如果有多数据源可以不配具体类型，否则都建议配上具体的 DbType
     * 3. 自 v3.5.9 起，需要单独引入 mybatis-plus-jsqlparser 依赖
     * 
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件（如果配置多个插件，切记分页插件最后添加）
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置单页分页条数限制，默认无限制
        paginationInnerInterceptor.setMaxLimit(500L);
        // 设置溢出总页数后是否进行处理（默认不处理，即溢出后返回空数据）
        paginationInnerInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }
}


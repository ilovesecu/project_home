package com.ilovepc.project_home.config.rdb;

import com.ilovepc.project_home.config.rdb.annotation.HomeMaster;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("rdb.master")
@MapperScan(annotationClass = HomeMaster.class, sqlSessionFactoryRef = "masterSessionFactory")
public class MysqlConfig extends HikariConfig {
    @Value("${mybatis.mapper-locations}")
    String mapperLocation;

    @Bean(value = "homeMasterDataSource", destroyMethod = "close")
    public HikariDataSource dataSourceProperties() {
        return new HikariDataSource(this);
    }

    @Bean("masterSessionFactory")
    public SqlSessionFactory masterSessionFactory(HikariDataSource homeMasterDataSource)throws Exception {
        return new HomeSqlSessinFactory().getSqlSessionFactory(homeMasterDataSource, mapperLocation);
    }
}

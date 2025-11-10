package com.ilovepc.project_home.config.rdb;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.util.Objects;

public class HomeSqlSessinFactory {
    public SqlSessionFactory getSqlSessionFactory(DataSource dataSource, String mapperLocation) throws Exception{
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(mapperLocation));

        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBean.getObject();
        Objects.requireNonNull(sqlSessionFactory).getConfiguration().setMapUnderscoreToCamelCase(true);

        return sqlSessionFactory;
    }
}

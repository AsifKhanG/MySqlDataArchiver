package com.gr.archive.config;

import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.gr.archive.model.db.DataSource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor

public class MultipleBeansApplicationContextInitializer implements ApplicationContextInitializer {
	
	List<DataSource> dbConnections;
		
	@Override
	public void initialize(ConfigurableApplicationContext context) {
		
		ConfigurableListableBeanFactory factory = context.getBeanFactory();
		for (DataSource connection : dbConnections) {

			AtomikosDataSourceBean bean = new AtomikosDataSourceBean();
			bean.setXaDataSourceClassName(connection.getXaDataSourceClassName());
			bean.setUniqueResourceName(connection.getUniqueResourceName());
			bean.setMinPoolSize(connection.getMinPoolSize());
			bean.setMaxPoolSize(connection.getMaxPoolSize());
			bean.setBorrowConnectionTimeout(connection.getBorrowConnectionTimeout());
			bean.setMaxLifetime(connection.getMaxLifeTime());
			
			Properties connectionProps = new Properties();
			connectionProps.setProperty("user", connection.getXaProperties().getUser());
			connectionProps.setProperty("password", connection.getXaProperties().getPassword());
			connectionProps.setProperty("URL", connection.getXaProperties().getUrl());
			bean.setXaProperties(connectionProps);
			
			factory.registerSingleton(connection.getUniqueResourceName(), bean);
		}
	}
}
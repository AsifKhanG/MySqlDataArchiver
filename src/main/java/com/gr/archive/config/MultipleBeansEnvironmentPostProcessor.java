package com.gr.archive.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import com.gr.archive.model.db.DbConfig;


public class MultipleBeansEnvironmentPostProcessor implements EnvironmentPostProcessor {
	
	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

	    BindResult<DbConfig> configProps = Binder.get(environment).bind("jta.atomikos", DbConfig.class);
		application.addInitializers(new MultipleBeansApplicationContextInitializer(configProps.get().getDbConnections()));
	}
}
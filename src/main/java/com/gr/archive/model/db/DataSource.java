package com.gr.archive.model.db;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataSource {
	
    private String uniqueResourceName;
    private Integer maxPoolSize;
    private Integer minPoolSize;
    private Integer maxLifeTime;
    private Integer borrowConnectionTimeout;
    private String xaDataSourceClassName;
    private DbProperties xaProperties;
    
    @Getter
    @Setter
    public static class DbProperties {
        private String user;
        private String password;
        private String url;
    }
}

package com.gr.archive.model.db;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DbConfig {
	private List<DataSource> dbConnections;	
}
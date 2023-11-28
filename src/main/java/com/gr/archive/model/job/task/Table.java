package com.gr.archive.model.job.task;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Table {
    
	private String name;
    
    @EqualsAndHashCode.Exclude
    private String idAlias;
}

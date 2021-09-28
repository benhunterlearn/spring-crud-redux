package com.benhunter.springcrudredux.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Count {
    private Long count;
}

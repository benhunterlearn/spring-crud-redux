package com.benhunter.springcrudredux.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserDto {
    private String email;
    private String password;
}

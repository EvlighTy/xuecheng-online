package com.xuecheng.ucenter.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class FindPasswordDTO {
    private String cellphone;
    private String email;
    @NotNull @NotEmpty
    private String checkcodekey;
    @NotNull @NotEmpty
    private String checkcode;
    @NotNull @NotEmpty
    private String password;
    @NotNull @NotEmpty
    private String confirmpwd;
}

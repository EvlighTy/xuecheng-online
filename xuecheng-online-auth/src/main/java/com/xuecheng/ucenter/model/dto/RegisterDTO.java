package com.xuecheng.ucenter.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class RegisterDTO {
    @NotNull
    private String username;
    @NotNull
    private String nickname;
    @NotNull @NotEmpty
    private String password;
    private String confirmpwd;
    @NotNull @NotEmpty
    private String cellphone;
    private String email;
    @NotNull @NotEmpty
    private String checkcode;
    @NotNull @NotEmpty
    private String checkcodekey;

}

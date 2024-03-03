package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDTO;
import com.xuecheng.ucenter.model.dto.XcUserExt;

public interface AuthService {
    XcUserExt execute(AuthParamsDTO authParamsDTO);
}

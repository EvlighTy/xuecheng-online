package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.FindPasswordDTO;
import com.xuecheng.ucenter.model.dto.RegisterDTO;

public interface CommonService {
    void register(RegisterDTO registerDTO);

    void findPassword(FindPasswordDTO findPasswordDTO);

}

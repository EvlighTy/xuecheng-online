package com.xuecheng.base.handler;

import com.xuecheng.base.constant.ExMsgConstant;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.base.model.RestResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public RestResponse<String> exceptionHandler(CustomException ex){
        log.error("自定义异常:[{}]", ex.getMessage());
        return RestResponse.validfail(ex.getMessage());
    }

    @ExceptionHandler
    public RestResponse<String> exceptionHandler(MethodArgumentNotValidException ex){
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<String> exMessageList = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        String exMessage = StringUtils.join(exMessageList, "&");
        log.error("请求参数异常:[{}]", exMessage);
        return RestResponse.validfail("请求参数"+exMessage+"异常");
    }

    @ExceptionHandler
    public RestResponse<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        String message = ex.getMessage();
        log.error("数据库操作异常:[{}]",message);
        if(message.contains("Duplicate entry")) return RestResponse.validfail("用户名已存在");
        else return RestResponse.validfail("数据库操作异常");
    }

    @ExceptionHandler
    public RestResponse<String> exceptionHandler(Exception ex){
        if(ex.getMessage().equals("不允许访问")) return RestResponse.validfail("您没有操作此功能的权限");
        log.error("未知异常:[{}]", ex.getMessage());
        return RestResponse.validfail(ExMsgConstant.UNKNOWN);
    }


}

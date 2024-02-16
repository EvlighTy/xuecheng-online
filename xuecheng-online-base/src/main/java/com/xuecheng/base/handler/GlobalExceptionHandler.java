package com.xuecheng.base.handler;

import com.xuecheng.base.constant.ExMsgConstant;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.base.model.result.Result;
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
    public Result<String> exceptionHandler(CustomException ex){
        log.error("自定义异常信息:[{}]", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    public Result<String> exceptionHandler(MethodArgumentNotValidException ex){
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<String> exMessageList = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        String exMessage = StringUtils.join(exMessageList, "&");
        log.error("自定义异常信息:[{}]", exMessage);
        return Result.error(exMessage);
    }

    @ExceptionHandler
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        String message = ex.getMessage();
        log.error("数据库操作异常信息:{}",message);
        if(message.contains("Duplicate entry")) return Result.error("用户名已存在");
        else return Result.error("未知错误");
    }

    @ExceptionHandler
    public Result<String> exceptionHandler(Exception ex){
        log.error("未知异常信息:[{}]", ex.getMessage());
        return Result.error(ExMsgConstant.UNKNOWN);
    }


}

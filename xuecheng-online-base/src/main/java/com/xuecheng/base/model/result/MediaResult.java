package com.xuecheng.base.model.result;

import lombok.Data;
import lombok.ToString;

/**
 * @description 通用结果类型
 * @author Mr.M
 * @date 2022/9/13 14:44
 * @version 1.0
 */

 @Data
 @ToString
public class MediaResult<T> {

  /**
   * 响应编码,0为正常,-1错误
   */
  private int code;

  /**
   * 响应提示信息
   */
  private String msg;

  /**
   * 响应内容
   */
  private T result;


  public MediaResult() {
   this(0, "success");
  }

  public MediaResult(int code, String msg) {
   this.code = code;
   this.msg = msg;
  }

  /**
   * 错误信息的封装
   *
   * @param msg
   * @param <T>
   * @return
   */
  public static <T> MediaResult<T> validfail(String msg) {
   MediaResult<T> response = new MediaResult<T>();
   response.setCode(-1);
   response.setMsg(msg);
   return response;
  }
  public static <T> MediaResult<T> validfail(T result,String msg) {
   MediaResult<T> response = new MediaResult<T>();
   response.setCode(-1);
   response.setResult(result);
   response.setMsg(msg);
   return response;
  }



  /**
   * 添加正常响应数据（包含响应内容）
   *
   * @return MediaResult Rest服务封装相应数据
   */
  public static <T> MediaResult<T> success(T result) {
   MediaResult<T> response = new MediaResult<T>();
   response.setResult(result);
   return response;
  }
  public static <T> MediaResult<T> success(T result,String msg) {
   MediaResult<T> response = new MediaResult<T>();
   response.setResult(result);
   response.setMsg(msg);
   return response;
  }

  /**
   * 添加正常响应数据（不包含响应内容）
   *
   * @return MediaResult Rest服务封装相应数据
   */
  public static <T> MediaResult<T> success() {
   return new MediaResult<T>();
  }


  public Boolean isSuccessful() {
   return this.code == 0;
  }

 }
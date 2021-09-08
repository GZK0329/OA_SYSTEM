package com.example.emos.wx.config;


import com.example.emos.wx.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

/**
 * @Classname ExceptionAdvice
 * @Description TODO
 * @Date 2021/7/27 16:37
 * @Created by GZK0329
 */

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    /**
     * @param e TODO
     * @return {@link String} TODO
     * @description: //TODO 异常处理
     * @author GZK0329
     * @date 2021/7/27 17:10
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String exceptionHandler(Exception e) {
        log.error("执行异常", e);
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) e;
            return exception.getBindingResult().getFieldError().getDefaultMessage();
        } else if (e instanceof EmosException) {
            EmosException exception = (EmosException) e;
            return exception.getMsg();
        } else if (e instanceof UnauthorizedException) {
            return "未获得权限！";
        } else {
            return "后端执行异常";
        }
    }
}

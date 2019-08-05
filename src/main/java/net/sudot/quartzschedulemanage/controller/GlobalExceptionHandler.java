package net.sudot.quartzschedulemanage.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 控制器全局异常处理
 *
 * @author tangjialin on 2018-04-01.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    public static final String ERROR_VIEW = "forward:";
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @Resource
    private ServerProperties serverProperties;

    protected String handle(int statusCode, String message, Exception e, HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, statusCode);
        request.setAttribute(WebUtils.ERROR_MESSAGE_ATTRIBUTE, message);
        return ERROR_VIEW + serverProperties.getError().getPath();
    }

    /**
     * 处理参数验证异常
     *
     * @param e        异常信息
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return 返回响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request, HttpServletResponse response) {
        String message = e.getMessage();
        LOGGER.warn("handleIllegalArgumentException {} {}", request.getRequestURI(), message);
        return handle(HttpStatus.BAD_REQUEST.value(), message, e, request, response);
    }

    /**
     * 处理所有请求类型错误异常
     *
     * @param e        异常信息
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return 返回响应结果
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public String handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request, HttpServletResponse response) {
        String message = e.getMessage();
        LOGGER.warn("handleHttpRequestMethodNotSupportedException {} {}", request.getRequestURI(), message);
        return handle(HttpStatus.METHOD_NOT_ALLOWED.value(), message, e, request, response);
    }

    /**
     * 处理所有接口数据验证异常
     *
     * @param e        异常信息
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return 返回响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request, HttpServletResponse response) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        LOGGER.warn("handleMethodArgumentNotValidException {} {}", request.getRequestURI(), message);
        return handle(HttpStatus.BAD_REQUEST.value(), message, e, request, response);
    }

    /**
     * 处理所有不可知的异常
     *
     * @param e        异常信息
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return 返回响应结果
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.error("handleException {} {}", request.getRequestURI(), e.getMessage(), e);
        return handle(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统错误", e, request, response);
    }

}

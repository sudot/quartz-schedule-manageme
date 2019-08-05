package net.sudot.quartzschedulemanage.controller;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default implementation of {@link ErrorAttributes}. Provides the following attributes
 * when possible:
 * * <ul>
 * * <li>timestamp - The time that the errors were extracted</li>
 * * <li>code - The status code</li>
 * * <li>message - The exception message</li>
 * * <li>traceId - 跟踪ID</li>
 * * <li>path - The URL path when the exception was raised</li>
 * * </ul>
 *
 * @author tangjialin on 2019-07-31.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DefaultErrorAttributes implements ErrorAttributes, HandlerExceptionResolver, Ordered {

    private static final String ERROR_ATTRIBUTE = DefaultErrorAttributes.class.getName() + ".ERROR";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object handler, Exception ex) {
        storeErrorAttributes(request, ex);
        return null;
    }

    private void storeErrorAttributes(HttpServletRequest request, Exception ex) {
        request.setAttribute(ERROR_ATTRIBUTE, ex);
    }

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        errorAttributes.put("timestamp", System.currentTimeMillis());
        errorAttributes.put("code", getStatus(webRequest));
        errorAttributes.put("message", getErrorDetails(webRequest));
        errorAttributes.put("path", getAttribute(webRequest, WebUtils.ERROR_REQUEST_URI_ATTRIBUTE));
        return errorAttributes;
    }

    private Integer getStatus(RequestAttributes requestAttributes) {
        Integer status = getAttribute(requestAttributes, WebUtils.ERROR_STATUS_CODE_ATTRIBUTE);
        return status == null ? 999 : status;
    }

    private String getErrorDetails(RequestAttributes requestAttributes) {
        String message = getAttribute(requestAttributes, WebUtils.ERROR_MESSAGE_ATTRIBUTE);
        return StringUtils.isEmpty(message) ? "No message available" : message;
    }

    @Override
    public Throwable getError(WebRequest webRequest) {
        Throwable exception = getAttribute(webRequest, ERROR_ATTRIBUTE);
        if (exception == null) {
            exception = getAttribute(webRequest, WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
        }
        return exception;
    }

    @SuppressWarnings("unchecked")
    private <T> T getAttribute(RequestAttributes requestAttributes, String name) {
        return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
    }

}

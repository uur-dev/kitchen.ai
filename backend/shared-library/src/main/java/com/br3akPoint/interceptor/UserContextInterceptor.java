package com.br3akPoint.interceptor;

import com.br3akPoint.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String xUserId = request.getHeader("X-User-Id");
        String xEmail = request.getHeader("X-User-Email");

        if (xUserId != null && !xUserId.isBlank()) {
            UserContext.setUserId(Long.parseLong(xUserId));
        }

        if(xEmail != null && !xEmail.isBlank()) {
            UserContext.setEmail(xEmail);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserContext.clear();
    }
}

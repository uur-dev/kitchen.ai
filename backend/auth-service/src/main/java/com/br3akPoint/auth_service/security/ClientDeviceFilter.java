package com.br3akPoint.auth_service.security;

import com.br3akPoint.auth_service.constant.ServerErrors;
import com.br3akPoint.auth_service.data.DeviceContext;
import com.br3akPoint.auth_service.util.DeviceClientValidator;
import error.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class ClientDeviceFilter extends OncePerRequestFilter {

    private final DeviceClientValidator clientValidator;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Autowired
    public ClientDeviceFilter(DeviceClientValidator validator,
                              @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.clientValidator = validator;
        this.handlerExceptionResolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String appId = request.getHeader("X-App-Id");
        String signature = request.getHeader("X-Signature");

        if (appId == null || appId.isBlank()) {
            handlerExceptionResolver.resolveException(request, response, null, BusinessException.badRequest(ServerErrors.Missing_App_ID));
            return;
        }
        if (signature == null || signature.isBlank()) {
            handlerExceptionResolver.resolveException(request, response, null, BusinessException.badRequest(ServerErrors.Missing_Signatures));
            return;
        }

        try {
            DeviceContext deviceContext = clientValidator.validate(appId, signature);
            //set device type in request context
            request.setAttribute("device_context", deviceContext);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }
}

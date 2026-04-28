package com.br3akPoint.api_gateway.security;

import com.br3akPoint.api_gateway.constant.ServerError;
import com.br3akPoint.api_gateway.data.UserRequestData;
import com.br3akPoint.error.BusinessException;
import com.br3akPoint.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JWTAuthValidationFilter extends OncePerRequestFilter {
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JWTUtil jwtUtil;

    @Autowired
    public JWTAuthValidationFilter(JWTUtil jwtUtil,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.handlerExceptionResolver = resolver;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tokenHeaderValue = request.getHeader("Authorization");

        if(tokenHeaderValue == null || tokenHeaderValue.isBlank()) {
            handlerExceptionResolver.resolveException(request, response, null, BusinessException.unauthorized(ServerError.Token_Missing));
            return;
        }

        String authToken = tokenHeaderValue.replace("Bearer ", "");

        //Step 1: First Check Auth Token in Blacklist list (Redis)
        //Step 2: If Token is not blacklisted, then decode token
        try {
            //get claims
            var claims = jwtUtil.extractAll(authToken);
            //get user id
            var userId = claims.get("userId", Long.class);
            var email = (String) claims.get("email", String.class);

            UserRequestData userRequestData = new UserRequestData(userId, email);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userRequestData, null, null);
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
        } catch(ExpiredJwtException ex) {
            handlerExceptionResolver.resolveException(request, response, null, BusinessException.unauthorized(ServerError.Auth_Token_Expired));
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, BusinessException.unauthorized(ServerError.Invalid_Auth_Token));
        }
    }
}

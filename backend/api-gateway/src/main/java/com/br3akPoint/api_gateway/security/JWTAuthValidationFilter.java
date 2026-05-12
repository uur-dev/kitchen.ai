package com.br3akPoint.api_gateway.security;

import com.br3akPoint.api_gateway.constant.ServerError;
import com.br3akPoint.api_gateway.data.UserRequestData;
import error.BusinessException;
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
import service.RedisService;
import util.JWTUtil;

import java.io.IOException;

@Component
public class JWTAuthValidationFilter extends OncePerRequestFilter {
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JWTUtil jwtUtil;
    private final RedisService redisService;

    @Autowired
    public JWTAuthValidationFilter(JWTUtil jwtUtil,
            RedisService redisService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.handlerExceptionResolver = resolver;
        this.jwtUtil = jwtUtil;
        this.redisService = redisService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tokenHeaderValue = request.getHeader("Authorization");

        if(tokenHeaderValue == null || tokenHeaderValue.isBlank()) {
            handlerExceptionResolver.resolveException(request, response, null, BusinessException.unauthorized(ServerError.Token_Missing));
            return;
        }

        String authToken = tokenHeaderValue.replace("Bearer ", "");
        ///Step 1: If Token is not blacklisted, then decode token
        try {
            //get claims
            var claims = jwtUtil.extractAll(authToken);
            //get user id
            var userId = claims.get("userId", Long.class);
            var email = (String) claims.get("email", String.class);
            var deviceType = (String) claims.get("device_type", String.class);
            var deviceId = (String) claims.get("device_id", String.class);
            var tokenJTI = (String) claims.get("jti", String.class);

            ///Step 2: First Check Auth Token in Blacklist list (Redis)
            //redis key
            String blackListKey = "blacklist:token:" + tokenJTI;
            // try to get value
            var blackListed = redisService.getString(blackListKey);
            if(blackListed.isPresent() && blackListed.get().equals("true")) {
                handlerExceptionResolver.resolveException(request, response, null, BusinessException.unauthorized(ServerError.Blacklisted_Token));
                return;
            }

            UserRequestData userRequestData = UserRequestData.builder()
                    .userId(userId)
                    .email(email)
                    .deviceType(deviceType)
                    .deviceId(deviceId)
                    .build();

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

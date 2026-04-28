package com.br3akPoint.auth_service.service;

import com.br3akPoint.auth_service.constant.DeviceTypeEnum;
import com.br3akPoint.auth_service.constant.ServerErrors;
import com.br3akPoint.auth_service.data.dto.response.LoginAuthDTO;
import com.br3akPoint.auth_service.entity.AppUser;
import com.br3akPoint.auth_service.entity.AuthSession;
import com.br3akPoint.auth_service.repository.AuthSessionRepository;
import com.br3akPoint.auth_service.repository.UserRepository;
import com.br3akPoint.error.BusinessException;
import com.br3akPoint.util.JWTUtil;
import com.br3akPoint.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordUtil passwordUtil;
    private final JWTUtil jwtUtil;

    @Autowired
    public AuthService(UserRepository userRepository, AuthSessionRepository authRepo, PasswordUtil passwordUtil, JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.authSessionRepository = authRepo;
        this.passwordUtil = passwordUtil;
        this.jwtUtil = jwtUtil;
    }

    public void registerUser(String email, String password) throws  Exception {
        //check if user already exist
        boolean userExist = userRepository.existsByEmail(email);
        if(!userExist) {
            //create new user
            AppUser user = AppUser.builder()
                    .email(email)
                    .password(passwordUtil.encodePassword(password))
                    .build();

            userRepository.save(user);
        }

        throw BusinessException.recordAlreadyExist(ServerErrors.User_Already_Exist);
    }

    public LoginAuthDTO loginUser(String email, String password, String deviceType) throws Exception {
        //check if user exist
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(()-> BusinessException.notFound(ServerErrors.Invalid_User_or_Password));

        boolean isCorrectPassword = passwordUtil.authenticate(password, user.getPassword());

        if(isCorrectPassword) {
            //generate access token
            String accessToken = jwtUtil.generateAccessToken(user.getId(), email, Map.of("device_type", deviceType));
            String refreshToken = jwtUtil.generateRefreshToken();
            var refreshTokenExpiry = jwtUtil.getRefreshTokenExpiry();

            //create new auth session
            AuthSession session = AuthSession.builder()
                    .user(user)
                    .refreshToken(refreshToken)
                    .expiry(refreshTokenExpiry.toInstant())
                    .deviceType(DeviceTypeEnum.valueOf(deviceType))
                    .build();

            //save
            authSessionRepository.save(session);
            //generate response

            return LoginAuthDTO.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fcmToken(session.getFcmToken())
                    .providerId(user.getSocialId())
                    .socialProvide(user.getSocialProvider() != null ? user.getSocialProvider().name() : null)
                    .accessToken(accessToken)
                    .refreshTokenExpiry(refreshTokenExpiry.toInstant())
                    .refreshToken(refreshToken)
                    .build();
        }

        throw BusinessException.unauthorized(ServerErrors.Invalid_User_or_Password);
    }
}

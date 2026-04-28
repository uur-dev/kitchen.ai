package com.br3akPoint.auth_service.data.dto.request;

import com.br3akPoint.auth_service.constant.ValidationConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDTO {
    @NotBlank(message = ValidationConstant.Email_Required)
    @Email(message = ValidationConstant.Invalid_Email)
    private String email;

    @NotBlank(message = ValidationConstant.Password_Required)
    @Size(min = ValidationConstant.Password_Min_Size, message = ValidationConstant.Invalid_Password_Length)
    @Pattern(
            regexp = ValidationConstant.Password_Regex,
            message = ValidationConstant.Invalid_Password
    )
    private String password;
}

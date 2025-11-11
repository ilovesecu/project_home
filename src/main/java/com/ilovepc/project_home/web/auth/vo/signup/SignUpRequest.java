package com.ilovepc.project_home.web.auth.vo.signup;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequest {
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    private String email;
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 5, message = "비밀번호는 5자 이상입니다.")
    private String password;
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max=10, message="닉네임은 2~10자 사이 입니다.")
    private String nickname;
}

package com.campus.dto.req;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class LoginReq {
    @NotBlank(message = "账号不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;
}

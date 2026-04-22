package com.campus.dto.req;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class LoginReq {
    @NotBlank(message = "账号不能为空")
    private String username;  // 支持用户名或手机号
    @NotBlank(message = "密码不能为空")
    private String password;
}

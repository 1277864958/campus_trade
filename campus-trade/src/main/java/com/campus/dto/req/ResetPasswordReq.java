package com.campus.dto.req;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class ResetPasswordReq {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "手机号不能为空")
    private String phone;
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度6~30位")
    private String newPassword;
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;
}

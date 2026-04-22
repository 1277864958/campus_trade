package com.campus.dto.req;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class ChangePasswordReq {
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 30, message = "新密码长度6~30位")
    private String newPassword;
}

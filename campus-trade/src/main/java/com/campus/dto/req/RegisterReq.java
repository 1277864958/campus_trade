package com.campus.dto.req;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

// ── 用户注册 ──────────────────────────────────────────────────
@Data
public class RegisterReq {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度3~20位")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能含字母/数字/下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度6~30位")
    private String password;

    @Size(max = 20, message = "昵称最多20字")
    private String nickname;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;
}

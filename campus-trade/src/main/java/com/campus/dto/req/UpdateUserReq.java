package com.campus.dto.req;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class UpdateUserReq {
    @Size(max = 20, message = "昵称最多20字")
    private String nickname;
    private String avatarUrl;
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    @Email(message = "邮箱格式不正确")
    private String email;
}

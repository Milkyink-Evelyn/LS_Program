package cn.edu.sdu.java.server.payload.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户列表项，同时也用于 /api/users/me。
 */
@Getter
@Setter
public class UserItem {

    private Integer id;
    private String username;
    private String realName;
    private String email;
    private String role;

    public UserItem() {
    }

    public UserItem(Integer id, String username, String realName, String email, String role) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.email = email;
        this.role = role;
    }
}

package cn.edu.sdu.java.server.payload.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 医生列表项 / 详情。
 */
@Getter
@Setter
public class DoctorItem {

    private Integer id;
    private Integer userId;
    private String username;
    private String realName;
    private Integer deptId;
    private String deptName;
    private String title;
    private String introduction;

    public DoctorItem() {
    }

    public DoctorItem(Integer id, Integer userId, String username, String realName,
                      Integer deptId, String deptName, String title, String introduction) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.deptId = deptId;
        this.deptName = deptName;
        this.title = title;
        this.introduction = introduction;
    }
}

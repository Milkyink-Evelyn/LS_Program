package cn.edu.sdu.java.server.payload.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 科室列表项。
 */
@Getter
@Setter
public class DepartmentItem {

    private Integer id;
    private String deptName;
    private Integer doctorCount;

    public DepartmentItem() {
    }

    public DepartmentItem(Integer id, String deptName, Integer doctorCount) {
        this.id = id;
        this.deptName = deptName;
        this.doctorCount = doctorCount;
    }
}

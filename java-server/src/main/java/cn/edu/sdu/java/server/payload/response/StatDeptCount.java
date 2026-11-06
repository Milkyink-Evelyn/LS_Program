package cn.edu.sdu.java.server.payload.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 科室挂号量统计。
 */
@Getter
@Setter
public class StatDeptCount {

    private String deptName;
    private Long count;

    public StatDeptCount() {
    }

    public StatDeptCount(String deptName, Long count) {
        this.deptName = deptName;
        this.count = count;
    }
}

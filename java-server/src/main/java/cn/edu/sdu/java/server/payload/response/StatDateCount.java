package cn.edu.sdu.java.server.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 按日期统计的挂号量，date 序列化为 yyyy-MM-dd。
 */
@Getter
@Setter
public class StatDateCount {

    private LocalDate date;
    private Long count;

    public StatDateCount() {
    }

    public StatDateCount(LocalDate date, Long count) {
        this.date = date;
        this.count = count;
    }
}

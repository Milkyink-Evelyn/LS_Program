package cn.edu.sdu.java.server.payload.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * 挂号详情：在列表项基础上附带病历，未就诊时 medicalRecord 为 null。
 */
@Getter
@Setter
public class AppointmentDetail extends AppointmentItem {

    private MedicalRecordItem medicalRecord;

    public AppointmentDetail() {
    }

    public AppointmentDetail(AppointmentItem item) {
        BeanUtils.copyProperties(item, this);
    }
}

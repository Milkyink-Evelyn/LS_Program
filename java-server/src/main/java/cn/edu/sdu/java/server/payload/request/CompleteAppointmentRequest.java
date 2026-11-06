package cn.edu.sdu.java.server.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 医生接诊并提交病历。
 */
@Getter
@Setter
public class CompleteAppointmentRequest {

    @NotBlank(message = "症状、诊断、处方不能为空")
    @Size(max = 500, message = "症状、诊断、处方不能超过 500 字")
    private String symptom;

    @NotBlank(message = "症状、诊断、处方不能为空")
    @Size(max = 500, message = "症状、诊断、处方不能超过 500 字")
    private String diagnosis;

    @NotBlank(message = "症状、诊断、处方不能为空")
    @Size(max = 500, message = "症状、诊断、处方不能超过 500 字")
    private String prescription;
}

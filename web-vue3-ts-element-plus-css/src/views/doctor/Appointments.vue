<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import PageHeader from "~/components/PageHeader.vue";
import StatusTag from "~/components/StatusTag.vue";
import TablePager from "~/components/TablePager.vue";
import TimeSlotTag from "~/components/TimeSlotTag.vue";
import {
  completeAppointment,
  listDoctorAppointments,
} from "~/api/appointment";
import { getRecordByAppointment } from "~/api/medicalRecord";
import { usePagination } from "~/composables/usePagination";
import type { Appointment, AppointmentStatus, MedicalRecord } from "~/types";

const tab = ref("0");
const selectedDate = ref("");
const loading = ref(false);
const rows = ref<Appointment[]>([]);
const { page, size, total, pageRows, resetPage } = usePagination(rows);

const completeVisible = ref(false);
const completeSaving = ref(false);
const current = ref<Appointment | null>(null);
const formRef = ref<FormInstance>();
const form = reactive({ symptom: "", diagnosis: "", prescription: "" });
const rules: FormRules = {
  symptom: [{ required: true, message: "请填写症状", trigger: "blur" }],
  diagnosis: [{ required: true, message: "请填写诊断", trigger: "blur" }],
  prescription: [{ required: true, message: "请填写处方", trigger: "blur" }],
};

const recordVisible = ref(false);
const recordLoading = ref(false);
const record = ref<MedicalRecord | null>(null);
const statusParam = computed<AppointmentStatus | undefined>(() =>
  tab.value === "all" ? undefined : (Number(tab.value) as AppointmentStatus)
);

async function load() {
  loading.value = true;
  try {
    rows.value = await listDoctorAppointments({
      status: statusParam.value,
      date: selectedDate.value || undefined,
    });
  } finally {
    loading.value = false;
  }
}

function reload() {
  resetPage();
  load();
}

function openComplete(row: Appointment) {
  current.value = row;
  form.symptom = "";
  form.diagnosis = "";
  form.prescription = "";
  completeVisible.value = true;
  formRef.value?.clearValidate();
}

async function submitComplete() {
  if (!formRef.value || !current.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  try {
    await ElMessageBox.confirm("提交后该挂号将变为已诊，是否继续？", "接诊确认", {
      type: "warning",
      confirmButtonText: "提交接诊",
      cancelButtonText: "取消",
    });
  } catch {
    return;
  }
  completeSaving.value = true;
  try {
    await completeAppointment(current.value.id, {
      symptom: form.symptom,
      diagnosis: form.diagnosis,
      prescription: form.prescription,
    });
    ElMessage.success("接诊成功");
    completeVisible.value = false;
    await load();
  } finally {
    completeSaving.value = false;
  }
}

async function openRecord(row: Appointment) {
  recordVisible.value = true;
  recordLoading.value = true;
  record.value = null;
  try {
    record.value = await getRecordByAppointment(row.id);
  } finally {
    recordLoading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="view">
    <PageHeader
      eyebrow="CONSULTATION"
      title="就诊管理"
      description="接诊待诊患者并填写病历；已诊记录可查看病历。"
    >
      <el-button :loading="loading" @click="load">刷新</el-button>
    </PageHeader>

    <section class="panel">
      <div class="toolbar" style="margin-bottom: 14px">
        <el-tabs v-model="tab" style="width: 100%" @tab-change="reload">
          <el-tab-pane label="待诊" name="0" />
          <el-tab-pane label="已诊" name="1" />
          <el-tab-pane label="全部" name="all" />
        </el-tabs>
      </div>
      <div class="toolbar" style="margin-bottom: 16px">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="全部日期"
          clearable
          style="width: 150px"
          @change="reload"
        />
        <span class="tiny muted">共 {{ total }} 条记录</span>
      </div>

      <el-table v-loading="loading" :data="pageRows">
        <el-table-column prop="patientName" label="患者" width="110" />
        <el-table-column prop="workDate" label="就诊日期" width="120" />
        <el-table-column label="时段" width="90">
          <template #default="{ row }"><TimeSlotTag :time-slot="row.timeSlot" /></template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column prop="createTime" label="挂号时间" min-width="160" />
        <el-table-column label="操作" min-width="120" align="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link @click="openComplete(row)">接诊</el-button>
            <el-button v-else-if="row.status === 1" link @click="openRecord(row)">查看病历</el-button>
            <span v-else class="tiny muted">—</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无就诊记录" />
        </template>
      </el-table>

      <TablePager v-model:page="page" v-model:size="size" :total="total" />
    </section>

    <el-dialog append-to-body v-model="completeVisible" title="接诊并填写病历" width="620px">
      <div v-if="current" class="patient-strip">
        <span>{{ current.patientName }}</span>
        <small>{{ current.workDate }} · {{ current.timeSlot === 0 ? "上午" : "下午" }}</small>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="症状" prop="symptom">
          <el-input v-model="form.symptom" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="诊断" prop="diagnosis">
          <el-input v-model="form.diagnosis" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="处方" prop="prescription">
          <el-input v-model="form.prescription" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" :loading="completeSaving" @click="submitComplete">提交接诊</el-button>
      </template>
    </el-dialog>

    <el-dialog append-to-body v-model="recordVisible" title="病历详情" width="560px">
      <div v-loading="recordLoading">
        <div v-if="record" class="detail-list">
          <div class="detail-row"><span class="k">患者</span><span class="v">{{ current?.patientName }}</span></div>
          <div class="detail-row"><span class="k">科室</span><span class="v">{{ record.deptName }}</span></div>
          <div class="detail-row"><span class="k">就诊日期</span><span class="v">{{ record.workDate }} {{ record.timeSlot === 0 ? "上午" : "下午" }}</span></div>
          <div class="detail-row"><span class="k">症状</span><span class="v">{{ record.symptom }}</span></div>
          <div class="detail-row"><span class="k">诊断</span><span class="v">{{ record.diagnosis }}</span></div>
          <div class="detail-row"><span class="k">处方</span><span class="v">{{ record.prescription }}</span></div>
          <div class="detail-row"><span class="k">记录时间</span><span class="v">{{ record.createTime }}</span></div>
        </div>
      </div>
      <template #footer><el-button @click="recordVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.patient-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f3f0e7;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 16px;
  font-size: 13px;
  font-weight: 600;
}
.patient-strip small {
  font-size: 10.5px;
  color: #827d74;
  font-weight: 400;
}
</style>

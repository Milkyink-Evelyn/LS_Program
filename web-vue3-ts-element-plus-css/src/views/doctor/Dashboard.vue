<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import PageHeader from "~/components/PageHeader.vue";
import StatusTag from "~/components/StatusTag.vue";
import TimeSlotTag from "~/components/TimeSlotTag.vue";
import {
  completeAppointment,
  listDoctorAppointments,
} from "~/api/appointment";
import { listMySchedules } from "~/api/schedule";
import type { Appointment, Schedule } from "~/types";
import { todayString } from "~/utils/format";

const today = todayString();
const loading = ref(false);
const waiting = ref<Appointment[]>([]);
const diagnosed = ref<Appointment[]>([]);
const schedules = ref<Schedule[]>([]);

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

async function load() {
  loading.value = true;
  const [wait, done, mySchedules] = await Promise.allSettled([
    listDoctorAppointments({ date: today, status: 0 }),
    listDoctorAppointments({ date: today, status: 1 }),
    listMySchedules({ date: today }),
  ]);
  if (wait.status === "fulfilled") waiting.value = wait.value;
  if (done.status === "fulfilled") diagnosed.value = done.value;
  if (mySchedules.status === "fulfilled") schedules.value = mySchedules.value;
  loading.value = false;
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
    await ElMessageBox.confirm("提交后挂号状态将变为已诊，是否继续？", "接诊确认", {
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

onMounted(load);
</script>

<template>
  <div class="view">
    <PageHeader
      eyebrow="CLINIC"
      title="医生工作台"
      :description="`${today} · 今日待诊、已诊和排班概览`"
    >
      <el-button :loading="loading" @click="load">刷新</el-button>
    </PageHeader>

    <div class="stat-grid">
      <div class="stat-card accent">
        <div class="stat-label"><span class="stat-icon"><el-icon><AlarmClock /></el-icon></span>今日待诊</div>
        <div class="stat-value">{{ waiting.length }}<small>人</small></div>
      </div>
      <div class="stat-card green">
        <div class="stat-label"><span class="stat-icon"><el-icon><CircleCheck /></el-icon></span>今日已诊</div>
        <div class="stat-value">{{ diagnosed.length }}<small>人</small></div>
      </div>
      <div class="stat-card">
        <div class="stat-label"><span class="stat-icon"><el-icon><Calendar /></el-icon></span>今日排班</div>
        <div class="stat-value">{{ schedules.length }}<small>个</small></div>
      </div>
      <div class="stat-card">
        <div class="stat-label"><span class="stat-icon"><el-icon><FirstAidKit /></el-icon></span>今日号源</div>
        <div class="stat-value">{{ schedules.reduce((sum, item) => sum + item.remainQuota, 0) }}<small>剩余</small></div>
      </div>
    </div>

    <section class="panel" v-loading="loading">
      <div class="panel-title">
        <div><h3>今日待诊</h3><div class="sub">最多显示 5 条，可在此直接接诊</div></div>
        <router-link class="tiny muted" to="/doctor/appointments">全部就诊 →</router-link>
      </div>
      <el-table :data="waiting.slice(0, 5)">
        <el-table-column prop="patientName" label="患者" width="120" />
        <el-table-column label="时段" width="95">
          <template #default="{ row }"><TimeSlotTag :time-slot="row.timeSlot" /></template>
        </el-table-column>
        <el-table-column prop="createTime" label="挂号时间" min-width="160" />
        <el-table-column label="状态" width="95">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="操作" min-width="90" align="right">
          <template #default="{ row }">
            <el-button link @click="openComplete(row)">接诊</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="今日暂无待诊患者" />
        </template>
      </el-table>
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

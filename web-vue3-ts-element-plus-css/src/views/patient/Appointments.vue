<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import PageHeader from "~/components/PageHeader.vue";
import StatusTag from "~/components/StatusTag.vue";
import TablePager from "~/components/TablePager.vue";
import TimeSlotTag from "~/components/TimeSlotTag.vue";
import {
  cancelAppointment,
  listMyAppointments,
} from "~/api/appointment";
import { getRecordByAppointment } from "~/api/medicalRecord";
import { usePagination } from "~/composables/usePagination";
import type { Appointment, AppointmentStatus, MedicalRecord } from "~/types";

const tab = ref("all");
const selectedDate = ref("");
const loading = ref(false);
const rows = ref<Appointment[]>([]);
const { page, size, total, pageRows, resetPage } = usePagination(rows);
const recordVisible = ref(false);
const recordLoading = ref(false);
const record = ref<MedicalRecord | null>(null);
const current = ref<Appointment | null>(null);

const statusParam = computed<AppointmentStatus | undefined>(() =>
  tab.value === "all" ? undefined : (Number(tab.value) as AppointmentStatus)
);

async function load() {
  loading.value = true;
  try {
    rows.value = await listMyAppointments({
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

async function cancel(row: Appointment) {
  try {
    await ElMessageBox.confirm("确认取消该挂号吗？取消后号源会释放。", "取消挂号", {
      type: "warning",
      confirmButtonText: "取消挂号",
      cancelButtonText: "保留",
    });
    await cancelAppointment(row.id);
    ElMessage.success("挂号已取消");
    await load();
  } catch {
    /* 取消或后端拒绝 */
  }
}

async function openRecord(row: Appointment) {
  current.value = row;
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
      eyebrow="MY VISITS"
      title="我的挂号"
      description="查看预约记录；待诊状态可取消，已诊状态可查看病历。"
    >
      <el-button :loading="loading" @click="load">刷新</el-button>
    </PageHeader>

    <section class="panel">
      <el-tabs v-model="tab" @tab-change="reload">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待诊" name="0" />
        <el-tab-pane label="已诊" name="1" />
        <el-tab-pane label="已取消" name="2" />
      </el-tabs>

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
      </div>

      <el-table v-loading="loading" :data="pageRows">
        <el-table-column prop="deptName" label="科室" width="110" />
        <el-table-column prop="doctorName" label="医生" width="110" />
        <el-table-column prop="workDate" label="就诊日期" width="120" />
        <el-table-column label="时段" width="90">
          <template #default="{ row }"><TimeSlotTag :time-slot="row.timeSlot" /></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column prop="createTime" label="挂号时间" min-width="160" />
        <el-table-column label="操作" min-width="120" align="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="danger" @click="cancel(row)">取消挂号</el-button>
            <el-button v-else-if="row.status === 1" link @click="openRecord(row)">查看病历</el-button>
            <span v-else class="tiny muted">—</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无挂号记录">
            <el-button type="primary" size="small" @click="$router.push('/patient/home')">去预约挂号</el-button>
          </el-empty>
        </template>
      </el-table>

      <TablePager v-model:page="page" v-model:size="size" :total="total" />
    </section>

    <el-dialog append-to-body v-model="recordVisible" title="病历详情" width="560px">
      <div v-loading="recordLoading">
        <div v-if="record" class="detail-list">
          <div class="detail-row"><span class="k">科室</span><span class="v">{{ record.deptName }}</span></div>
          <div class="detail-row"><span class="k">医生</span><span class="v">{{ record.doctorName }}</span></div>
          <div class="detail-row"><span class="k">就诊时间</span><span class="v">{{ record.workDate }} {{ record.timeSlot === 0 ? "上午" : "下午" }}</span></div>
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

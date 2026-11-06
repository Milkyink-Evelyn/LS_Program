<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import PageHeader from "~/components/PageHeader.vue";
import StatusTag from "~/components/StatusTag.vue";
import TablePager from "~/components/TablePager.vue";
import TimeSlotTag from "~/components/TimeSlotTag.vue";
import { listDepartments } from "~/api/department";
import { listDoctors } from "~/api/doctor";
import { getAppointment, listAppointments } from "~/api/appointment";
import { usePagination } from "~/composables/usePagination";
import type {
  Appointment,
  AppointmentDetail,
  AppointmentStatus,
  Department,
  Doctor,
} from "~/types";

const loading = ref(false);
const rows = ref<Appointment[]>([]);
const { page, size, total, pageRows, resetPage } = usePagination(rows);
const departments = ref<Department[]>([]);
const doctors = ref<Doctor[]>([]);
const filters = reactive<{
  date: string;
  deptId?: number;
  doctorId?: number;
  status?: AppointmentStatus;
  keyword: string;
}>({ date: "", deptId: undefined, doctorId: undefined, status: undefined, keyword: "" });

const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<AppointmentDetail | null>(null);

async function load() {
  loading.value = true;
  try {
    rows.value = await listAppointments({
      date: filters.date || undefined,
      deptId: filters.deptId,
      doctorId: filters.doctorId,
      status: filters.status,
      keyword: filters.keyword.trim() || undefined,
    });
  } finally {
    loading.value = false;
  }
}

async function loadBase() {
  const [deptList, doctorList] = await Promise.all([listDepartments(), listDoctors()]);
  departments.value = deptList;
  doctors.value = doctorList;
}

function search() {
  resetPage();
  load();
}

function resetFilters() {
  filters.date = "";
  filters.deptId = undefined;
  filters.doctorId = undefined;
  filters.status = undefined;
  filters.keyword = "";
  search();
}

async function openDetail(row: Appointment) {
  detailVisible.value = true;
  detailLoading.value = true;
  detail.value = null;
  try {
    detail.value = await getAppointment(row.id);
  } finally {
    detailLoading.value = false;
  }
}

onMounted(async () => {
  await loadBase();
  await load();
});
</script>

<template>
  <div class="view">
    <PageHeader
      eyebrow="APPOINTMENTS"
      title="挂号管理"
      description="查看全部挂号记录和病历；管理员只读，不执行取消或接诊。"
    >
      <el-button :loading="loading" @click="load">刷新</el-button>
    </PageHeader>

    <section class="panel">
      <div class="toolbar" style="margin-bottom: 16px">
        <el-date-picker v-model="filters.date" type="date" value-format="YYYY-MM-DD" placeholder="就诊日期" clearable style="width: 150px" />
        <el-select v-model="filters.deptId" placeholder="全部科室" clearable style="width: 130px">
          <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id" />
        </el-select>
        <el-select v-model="filters.doctorId" placeholder="全部医生" clearable style="width: 150px">
          <el-option v-for="d in doctors" :key="d.id" :label="d.realName" :value="d.id" />
        </el-select>
        <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 120px">
          <el-option label="待诊" :value="0" />
          <el-option label="已诊" :value="1" />
          <el-option label="已取消" :value="2" />
        </el-select>
        <el-input v-model="filters.keyword" placeholder="患者姓名" clearable style="width: 150px" />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <span class="tiny muted">共 {{ total }} 条记录</span>
      </div>

      <el-table v-loading="loading" :data="pageRows">
        <el-table-column prop="id" label="挂号单号" width="100" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="deptName" label="科室" width="100" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="workDate" label="就诊日期" width="115" />
        <el-table-column label="时段" width="85">
          <template #default="{ row }"><TimeSlotTag :time-slot="row.timeSlot" /></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column prop="createTime" label="挂号时间" min-width="160" />
        <el-table-column label="操作" min-width="90" align="right">
          <template #default="{ row }">
            <el-button link @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无挂号记录" />
        </template>
      </el-table>

      <TablePager v-model:page="page" v-model:size="size" :total="total" />
    </section>

    <el-dialog append-to-body v-model="detailVisible" title="挂号详情" width="620px">
      <div v-loading="detailLoading">
        <template v-if="detail">
          <div class="detail-list">
            <div class="detail-row"><span class="k">挂号单号</span><span class="v">{{ detail.id }}</span></div>
            <div class="detail-row"><span class="k">患者</span><span class="v">{{ detail.patientName }}</span></div>
            <div class="detail-row"><span class="k">科室 / 医生</span><span class="v">{{ detail.deptName }} · {{ detail.doctorName }}</span></div>
            <div class="detail-row"><span class="k">就诊时间</span><span class="v">{{ detail.workDate }} {{ detail.timeSlot === 0 ? "上午" : "下午" }}</span></div>
            <div class="detail-row"><span class="k">状态</span><span class="v"><StatusTag :status="detail.status" /></span></div>
            <div class="detail-row"><span class="k">挂号时间</span><span class="v">{{ detail.createTime }}</span></div>
          </div>
          <div class="record-block">
            <h4>病历</h4>
            <template v-if="detail.medicalRecord">
              <div class="detail-row"><span class="k">症状</span><span class="v">{{ detail.medicalRecord.symptom }}</span></div>
              <div class="detail-row"><span class="k">诊断</span><span class="v">{{ detail.medicalRecord.diagnosis }}</span></div>
              <div class="detail-row"><span class="k">处方</span><span class="v">{{ detail.medicalRecord.prescription }}</span></div>
              <div class="detail-row"><span class="k">记录时间</span><span class="v">{{ detail.medicalRecord.createTime }}</span></div>
            </template>
            <el-empty v-else :image-size="50" description="尚未就诊" />
          </div>
        </template>
      </div>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.record-block {
  margin-top: 18px;
  border-top: 1px solid #e7e1d5;
  padding-top: 15px;
}
.record-block h4 {
  font-size: 13px;
  margin-bottom: 9px;
}
</style>

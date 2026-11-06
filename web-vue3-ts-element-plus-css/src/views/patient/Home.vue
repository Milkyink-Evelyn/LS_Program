<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import PageHeader from "~/components/PageHeader.vue";
import TablePager from "~/components/TablePager.vue";
import TimeSlotTag from "~/components/TimeSlotTag.vue";
import { listDepartments } from "~/api/department";
import { listDoctors } from "~/api/doctor";
import { createAppointment } from "~/api/appointment";
import { listSchedules } from "~/api/schedule";
import { usePagination } from "~/composables/usePagination";
import type { Department, Doctor, Schedule } from "~/types";
import { addDays, dateString, todayString, weekdayText } from "~/utils/format";

const router = useRouter();
const loading = ref(false);
const departments = ref<Department[]>([]);
const doctors = ref<Doctor[]>([]);
const schedules = ref<Schedule[]>([]);
const { page, size, total, pageRows, resetPage } = usePagination(schedules);
const selectedDept = ref<Department | null>(null);
const selectedDoctor = ref<number | undefined>(undefined);
const selectedDate = ref(todayString());
const bookingId = ref<number | null>(null);

const dateCards = computed(() =>
  Array.from({ length: 7 }, (_, index) => {
    const date = addDays(new Date(), index);
    const value = dateString(date);
    return {
      value,
      day: index === 0 ? "今天" : index === 1 ? "明天" : weekdayText(value),
      label: value.slice(5),
    };
  })
);

async function loadDepartments() {
  departments.value = await listDepartments();
}

async function chooseDept(dept: Department) {
  selectedDept.value = dept;
  selectedDoctor.value = undefined;
  schedules.value = [];
  await loadDoctors(dept.id);
  await loadSchedules();
}

async function loadDoctors(deptId: number) {
  try {
    doctors.value = await listDoctors({ deptId });
  } catch {
    doctors.value = [];
  }
}

async function chooseDoctor(id?: number) {
  selectedDoctor.value = id;
  await loadSchedules();
}

async function loadSchedules() {
  if (!selectedDept.value) return;
  loading.value = true;
  resetPage();
  try {
    schedules.value = await listSchedules({
      deptId: selectedDept.value.id,
      doctorId: selectedDoctor.value,
      date: selectedDate.value,
    });
  } finally {
    loading.value = false;
  }
}

async function chooseDate(date: string) {
  selectedDate.value = date;
  await loadSchedules();
}

async function book(row: Schedule) {
  if (row.remainQuota <= 0) return;
  try {
    await ElMessageBox.confirm(
      `确认挂号：${row.deptName} · ${row.doctorName} · ${row.workDate} ${row.timeSlot === 0 ? "上午" : "下午"}？`,
      "确认挂号",
      { type: "warning", confirmButtonText: "确认挂号", cancelButtonText: "取消" }
    );
  } catch {
    return;
  }
  bookingId.value = row.id;
  try {
    await createAppointment({ scheduleId: row.id });
    ElMessage.success("挂号成功，请按时就诊");
    router.push("/patient/appointments");
  } finally {
    bookingId.value = null;
    await loadSchedules();
  }
}

onMounted(loadDepartments);
</script>

<template>
  <div class="view">
    <PageHeader
      eyebrow="BOOK A VISIT"
      title="预约挂号"
      description="选择科室、医生和日期，查看号源后确认挂号。"
    />

    <div class="booking-shell">
      <section class="step-panel">
        <div class="step-head"><span>01</span><div><b>选择科室</b><small>先确定就诊科室</small></div></div>
        <div v-if="departments.length" class="dept-grid">
          <button
            v-for="dept in departments"
            :key="dept.id"
            class="dept-card"
            :class="{ active: selectedDept?.id === dept.id }"
            type="button"
            @click="chooseDept(dept)"
          >
            <span>{{ dept.deptName.slice(0, 1) }}</span>
            <div><b>{{ dept.deptName }}</b><small>{{ dept.doctorCount ?? 0 }} 位医生</small></div>
          </button>
        </div>
        <el-empty v-else description="暂无科室数据" :image-size="60" />
      </section>

      <section class="step-panel" :class="{ disabled: !selectedDept }">
        <div class="step-head"><span>02</span><div><b>选择医生</b><small>{{ selectedDept ? selectedDept.deptName : "请先选择科室" }}</small></div></div>
        <template v-if="selectedDept">
          <button class="doctor-card all" :class="{ active: !selectedDoctor }" type="button" @click="chooseDoctor(undefined)">
            <div class="doctor-avatar all-avatar">全</div>
            <div><b>全部医生</b><small>不限定医生，查看全部号源</small></div>
          </button>
          <div v-if="doctors.length" class="doctor-list">
            <button
              v-for="doctor in doctors"
              :key="doctor.id"
              class="doctor-card"
              :class="{ active: selectedDoctor === doctor.id }"
              type="button"
              @click="chooseDoctor(doctor.id)"
            >
              <div class="doctor-avatar">{{ doctor.realName.slice(0, 1) }}</div>
              <div>
                <b>{{ doctor.realName }}</b>
                <small>{{ doctor.title }} · {{ doctor.deptName }}</small>
                <em>{{ doctor.introduction || "暂无简介" }}</em>
              </div>
            </button>
          </div>
          <el-empty v-else description="该科室暂无医生" :image-size="50" />
        </template>
        <el-empty v-else description="选择科室后显示医生" :image-size="50" />
      </section>

      <section class="step-panel wide" :class="{ disabled: !selectedDept }">
        <div class="step-head"><span>03</span><div><b>选择日期与号源</b><small>仅显示今天及以后</small></div></div>
        <div class="date-strip">
          <button
            v-for="item in dateCards"
            :key="item.value"
            type="button"
            class="date-chip"
            :class="{ active: selectedDate === item.value }"
            @click="chooseDate(item.value)"
          >
            <b>{{ item.day }}</b>
            <small>{{ item.label }}</small>
          </button>
        </div>

        <div v-if="selectedDept" v-loading="loading" class="schedule-wrap">
          <el-table :data="pageRows">
            <el-table-column prop="doctorName" label="医生" width="110" />
            <el-table-column prop="title" label="职称" width="120" />
            <el-table-column label="时段" width="95">
              <template #default="{ row }"><TimeSlotTag :time-slot="row.timeSlot" /></template>
            </el-table-column>
            <el-table-column label="剩余号" width="105">
              <template #default="{ row }">
                <span class="quota" :class="{ full: row.remainQuota <= 0, low: row.remainQuota > 0 && row.remainQuota <= 3 }">
                  {{ row.remainQuota }} / {{ row.totalQuota }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="120" align="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  :disabled="row.remainQuota <= 0"
                  :loading="bookingId === row.id"
                  @click="book(row)"
                >
                  {{ row.remainQuota <= 0 ? "已满" : "挂号" }}
                </el-button>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="该日期暂无号源" :image-size="60" />
            </template>
          </el-table>

          <TablePager v-model:page="page" v-model:size="size" :total="total" />
        </div>
        <el-empty v-else description="请先完成第 1 步" :image-size="50" />
      </section>
    </div>
  </div>
</template>

<style scoped>
.booking-shell {
  display: grid;
  grid-template-columns: 0.9fr 1fr;
  gap: 16px;
  align-items: start;
}
.step-panel {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 20px;
}
.step-panel.wide {
  grid-column: 1 / -1;
}
.step-panel.disabled {
  opacity: 0.72;
}
.step-head {
  display: flex;
  align-items: center;
  gap: 11px;
  margin-bottom: 16px;
}
.step-head > span {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--ink);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 10px;
}
.step-head b {
  display: block;
  font-size: 14px;
  letter-spacing: -0.02em;
}
.step-head small {
  display: block;
  margin-top: 3px;
  font-size: 10.5px;
  color: var(--muted);
}
.dept-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.dept-card {
  display: flex;
  align-items: center;
  gap: 9px;
  text-align: left;
  background: #f7f4ec;
  border: 1px solid transparent;
  border-radius: 12px;
  padding: 10px;
}
.dept-card > span {
  width: 31px;
  height: 31px;
  flex: none;
  border-radius: 9px;
  background: #e8e2d3;
  display: grid;
  place-items: center;
  font-size: 13px;
  font-weight: 600;
}
.dept-card b {
  display: block;
  font-size: 12px;
  font-weight: 600;
}
.dept-card small {
  display: block;
  font-size: 9.5px;
  color: var(--muted);
  margin-top: 2px;
}
.dept-card.active {
  border-color: var(--ink);
  background: var(--accent-soft);
}
.doctor-list {
  display: flex;
  flex-direction: column;
  gap: 7px;
  max-height: 380px;
  overflow: auto;
}
.doctor-card {
  width: 100%;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  text-align: left;
  background: #f7f4ec;
  border: 1px solid transparent;
  border-radius: 12px;
  padding: 11px;
}
.doctor-card.all {
  margin-bottom: 7px;
  align-items: center;
}
.doctor-card.active {
  border-color: var(--ink);
  background: var(--accent-soft);
}
.doctor-avatar {
  width: 32px;
  height: 32px;
  flex: none;
  border-radius: 50%;
  background: var(--ink);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 12px;
}
.all-avatar {
  background: #bdb5a4;
}
.doctor-card b {
  display: block;
  font-size: 12.5px;
}
.doctor-card small {
  display: block;
  font-size: 10px;
  color: var(--muted);
  margin-top: 2px;
}
.doctor-card em {
  display: block;
  font-style: normal;
  font-size: 10px;
  color: #9b958a;
  line-height: 1.5;
  margin-top: 4px;
}
.date-strip {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 2px;
  margin-bottom: 15px;
}
.date-chip {
  flex: none;
  min-width: 62px;
  border: 1px solid var(--line);
  background: #f7f4ec;
  border-radius: 11px;
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.date-chip b {
  font-size: 11px;
}
.date-chip small {
  font-size: 9.5px;
  color: var(--muted);
}
.date-chip.active {
  background: var(--ink);
  border-color: var(--ink);
  color: #fff;
}
.date-chip.active small {
  color: rgba(255, 255, 255, 0.6);
}
.schedule-wrap {
  min-height: 120px;
}
@media (max-width: 900px) {
  .booking-shell {
    grid-template-columns: 1fr;
  }
  .step-panel.wide {
    grid-column: auto;
  }
}
</style>

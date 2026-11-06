<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import PageHeader from "~/components/PageHeader.vue";
import TablePager from "~/components/TablePager.vue";
import TimeSlotTag from "~/components/TimeSlotTag.vue";
import { listDepartments } from "~/api/department";
import { listDoctors } from "~/api/doctor";
import {
  createSchedule,
  deleteSchedule,
  listSchedules,
  updateSchedule,
} from "~/api/schedule";
import { usePagination } from "~/composables/usePagination";
import type { Department, Doctor, Schedule, TimeSlot } from "~/types";
import { todayString } from "~/utils/format";

const loading = ref(false);
const rows = ref<Schedule[]>([]);
const { page, size, total, pageRows, resetPage } = usePagination(rows);
const departments = ref<Department[]>([]);
const doctors = ref<Doctor[]>([]);
const filters = reactive<{
  date: string;
  deptId?: number;
  doctorId?: number;
  timeSlot?: TimeSlot;
}>({
  date: todayString(),
  deptId: undefined,
  doctorId: undefined,
  timeSlot: undefined,
});

const dialog = ref(false);
const saving = ref(false);
const formRef = ref<FormInstance>();
const form = reactive({
  doctorId: undefined as number | undefined,
  workDate: todayString(),
  timeSlot: 0 as TimeSlot,
  totalQuota: 20,
});
const rules: FormRules = {
  doctorId: [{ required: true, message: "请选择医生", trigger: "change" }],
  workDate: [{ required: true, message: "请选择日期", trigger: "change" }],
  timeSlot: [{ required: true, message: "请选择时段", trigger: "change" }],
  totalQuota: [{ required: true, message: "请输入总号数", trigger: "blur" }],
};

const editDialog = ref(false);
const editSaving = ref(false);
const editing = ref<Schedule | null>(null);
const editQuota = ref(20);
const bookedCount = computed(() =>
  editing.value ? editing.value.totalQuota - editing.value.remainQuota : 0
);

const availableDoctors = computed(() =>
  filters.deptId
    ? doctors.value.filter((d) => d.deptId === filters.deptId)
    : doctors.value
);

function quotaClass(row: Schedule) {
  if (row.remainQuota <= 0) return "full";
  if (row.remainQuota <= 3) return "low";
  return "";
}

function disablePast(date: Date) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return date.getTime() < today.getTime();
}

async function load() {
  loading.value = true;
  try {
    rows.value = await listSchedules({
      date: filters.date || undefined,
      deptId: filters.deptId,
      doctorId: filters.doctorId,
      timeSlot: filters.timeSlot,
    });
  } finally {
    loading.value = false;
  }
}

async function loadBase() {
  const [deptList, doctorList] = await Promise.all([
    listDepartments(),
    listDoctors(),
  ]);
  departments.value = deptList;
  doctors.value = doctorList;
}

function search() {
  resetPage();
  load();
}

function resetFilters() {
  filters.date = todayString();
  filters.deptId = undefined;
  filters.doctorId = undefined;
  filters.timeSlot = undefined;
  search();
}

function openCreate() {
  form.doctorId = undefined;
  form.workDate = todayString();
  form.timeSlot = 0;
  form.totalQuota = 20;
  dialog.value = true;
  formRef.value?.clearValidate();
}

async function save() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  saving.value = true;
  try {
    await createSchedule({
      doctorId: form.doctorId!,
      workDate: form.workDate,
      timeSlot: Number(form.timeSlot) as TimeSlot,
      totalQuota: Number(form.totalQuota),
    });
    ElMessage.success("排班已新增");
    dialog.value = false;
    await load();
  } finally {
    saving.value = false;
  }
}

function openEdit(row: Schedule) {
  editing.value = row;
  editQuota.value = row.totalQuota;
  editDialog.value = true;
}

async function saveQuota() {
  if (!editing.value) return;
  if (editQuota.value < bookedCount.value) {
    ElMessage.warning("总号数不能小于已挂号数");
    return;
  }
  editSaving.value = true;
  try {
    await updateSchedule(editing.value.id, { totalQuota: editQuota.value });
    ElMessage.success("号数已更新");
    editDialog.value = false;
    await load();
  } finally {
    editSaving.value = false;
  }
}

async function remove(row: Schedule) {
  try {
    await ElMessageBox.confirm(
      `确定删除 ${row.workDate} ${row.timeSlot === 0 ? "上午" : "下午"} ${row.doctorName} 的排班吗？`,
      "删除排班",
      { type: "warning", confirmButtonText: "删除", cancelButtonText: "取消" }
    );
    await deleteSchedule(row.id);
    ElMessage.success("排班已删除");
    await load();
  } catch {
    /* 取消或后端拒绝 */
  }
}

watch(
  () => filters.deptId,
  () => {
    if (filters.doctorId && !availableDoctors.value.some((d) => d.id === filters.doctorId)) {
      filters.doctorId = undefined;
    }
  }
);

onMounted(async () => {
  await loadBase();
  await load();
});
</script>

<template>
  <div class="view">
    <PageHeader
      eyebrow="SCHEDULING"
      title="排班管理"
      description="按日期、科室和医生维护号源；剩余号由后端根据挂号记录计算。"
    >
      <el-button type="primary" @click="openCreate">新增排班</el-button>
    </PageHeader>

    <section class="panel">
      <div class="toolbar" style="margin-bottom: 16px">
        <el-date-picker
          v-model="filters.date"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择日期"
          style="width: 150px"
        />
        <el-select v-model="filters.deptId" placeholder="全部科室" clearable style="width: 140px">
          <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id" />
        </el-select>
        <el-select v-model="filters.doctorId" placeholder="全部医生" clearable style="width: 170px">
          <el-option v-for="d in availableDoctors" :key="d.id" :label="d.realName" :value="d.id" />
        </el-select>
        <el-select v-model="filters.timeSlot" placeholder="全部时段" clearable style="width: 120px">
          <el-option label="上午" :value="0" />
          <el-option label="下午" :value="1" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <span class="tiny muted">共 {{ total }} 条排班</span>
      </div>

      <el-table v-loading="loading" :data="pageRows">
        <el-table-column prop="workDate" label="日期" width="120" />
        <el-table-column label="时段" width="90">
          <template #default="{ row }"><TimeSlotTag :time-slot="row.timeSlot" /></template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室" width="110" />
        <el-table-column prop="doctorName" label="医生" width="110" />
        <el-table-column prop="title" label="职称" width="120" />
        <el-table-column prop="totalQuota" label="总号" width="80" />
        <el-table-column label="剩余号" width="110">
          <template #default="{ row }">
            <span class="quota" :class="quotaClass(row)">{{ row.remainQuota }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="165" align="right">
          <template #default="{ row }">
            <el-button link @click="openEdit(row)">编辑号数</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="当前筛选条件下暂无排班" />
        </template>
      </el-table>

      <TablePager v-model:page="page" v-model:size="size" :total="total" />
    </section>

    <el-dialog append-to-body v-model="dialog" title="新增排班" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="医生" prop="doctorId">
          <el-select v-model="form.doctorId" filterable placeholder="选择医生" style="width: 100%">
            <el-option
              v-for="d in doctors"
              :key="d.id"
              :label="`${d.realName} · ${d.deptName} · ${d.title}`"
              :value="d.id"
            />
          </el-select>
        </el-form-item>
        <div class="two-col">
          <el-form-item label="日期" prop="workDate">
            <el-date-picker
              v-model="form.workDate"
              type="date"
              value-format="YYYY-MM-DD"
              :disabled-date="disablePast"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="时段" prop="timeSlot">
            <el-select v-model="form.timeSlot" style="width: 100%">
              <el-option label="上午" :value="0" />
              <el-option label="下午" :value="1" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="总号数" prop="totalQuota">
          <el-input-number v-model="form.totalQuota" :min="1" :max="200" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog append-to-body v-model="editDialog" title="修改总号数" width="420px">
      <div class="detail-list" style="margin-bottom: 16px">
        <div class="detail-row"><span class="k">医生</span><span class="v">{{ editing?.doctorName }}</span></div>
        <div class="detail-row"><span class="k">日期</span><span class="v">{{ editing?.workDate }}</span></div>
        <div class="detail-row"><span class="k">已挂号</span><span class="v">{{ bookedCount }} 人</span></div>
      </div>
      <el-form label-position="top">
        <el-form-item label="新的总号数">
          <el-input-number v-model="editQuota" :min="bookedCount" :max="200" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="saveQuota">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 14px;
}
@media (max-width: 620px) {
  .two-col {
    grid-template-columns: 1fr;
  }
}
</style>

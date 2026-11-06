<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import PageHeader from "~/components/PageHeader.vue";
import TablePager from "~/components/TablePager.vue";
import { listDepartments } from "~/api/department";
import {
  createDoctor,
  deleteDoctor,
  listDoctors,
  updateDoctor,
} from "~/api/doctor";
import { usePagination } from "~/composables/usePagination";
import type { Department, Doctor } from "~/types";

const loading = ref(false);
const rows = ref<Doctor[]>([]);
const { page, size, total, pageRows, resetPage } = usePagination(rows);
const departments = ref<Department[]>([]);
const filters = reactive<{ deptId?: number; keyword: string }>({
  deptId: undefined,
  keyword: "",
});
const dialog = ref(false);
const saving = ref(false);
const editing = ref<Doctor | null>(null);
const formRef = ref<FormInstance>();
const form = reactive({
  username: "",
  password: "",
  realName: "",
  deptId: undefined as number | undefined,
  title: "",
  introduction: "",
  newPassword: "",
});
const titles = ["主任医师", "副主任医师", "主治医师", "住院医师"];
const rules: FormRules = {
  username: [
    { required: true, message: "请输入登录名", trigger: "blur" },
    { pattern: /^[A-Za-z0-9_]{4,20}$/, message: "4-20 位字母、数字或下划线", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码至少 6 位", trigger: "blur" },
  ],
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  deptId: [{ required: true, message: "请选择科室", trigger: "change" }],
  title: [{ required: true, message: "请选择职称", trigger: "change" }],
  introduction: [{ max: 500, message: "最多 500 字", trigger: "blur" }],
};

async function load() {
  loading.value = true;
  try {
    rows.value = await listDoctors({
      deptId: filters.deptId,
      keyword: filters.keyword.trim() || undefined,
    });
  } finally {
    loading.value = false;
  }
}

async function loadDepartments() {
  departments.value = await listDepartments();
}

function search() {
  resetPage();
  load();
}

function resetFilters() {
  filters.deptId = undefined;
  filters.keyword = "";
  search();
}

function openCreate() {
  editing.value = null;
  Object.assign(form, {
    username: "",
    password: "",
    realName: "",
    deptId: undefined,
    title: "",
    introduction: "",
    newPassword: "",
  });
  dialog.value = true;
  formRef.value?.clearValidate();
}

function openEdit(row: Doctor) {
  editing.value = row;
  Object.assign(form, {
    username: row.username,
    password: "",
    realName: row.realName,
    deptId: row.deptId,
    title: row.title,
    introduction: row.introduction || "",
    newPassword: "",
  });
  dialog.value = true;
  formRef.value?.clearValidate();
}

async function save() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  saving.value = true;
  try {
    if (editing.value) {
      await updateDoctor(editing.value.id, {
        realName: form.realName.trim(),
        deptId: form.deptId!,
        title: form.title,
        introduction: form.introduction,
        newPassword: form.newPassword || undefined,
      });
      ElMessage.success("医生信息已更新");
    } else {
      await createDoctor({
        username: form.username.trim(),
        password: form.password,
        realName: form.realName.trim(),
        deptId: form.deptId!,
        title: form.title,
        introduction: form.introduction,
      });
      ElMessage.success("医生已新增，登录账号同时创建");
    }
    dialog.value = false;
    await load();
  } catch {
    /* 请求层已提示 */
  } finally {
    saving.value = false;
  }
}

async function remove(row: Doctor) {
  try {
    await ElMessageBox.confirm(
      `删除“${row.realName}”后，其登录账号也会被删除。是否继续？`,
      "删除医生",
      { type: "warning", confirmButtonText: "删除", cancelButtonText: "取消" }
    );
    await deleteDoctor(row.id);
    ElMessage.success("医生已删除");
    await load();
  } catch {
    /* 取消或后端拒绝 */
  }
}

onMounted(async () => {
  await Promise.all([loadDepartments(), load()]);
});
</script>

<template>
  <div class="view">
    <PageHeader
      eyebrow="MASTER DATA"
      title="医生管理"
      description="医生账号与医生档案一起创建；登录名不可修改。"
    >
      <el-button type="primary" @click="openCreate">新增医生</el-button>
    </PageHeader>

    <section class="panel">
      <div class="toolbar" style="margin-bottom: 16px">
        <el-select v-model="filters.deptId" placeholder="全部科室" clearable style="width: 160px">
          <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id" />
        </el-select>
        <el-input v-model="filters.keyword" placeholder="姓名 / 登录名" clearable style="width: 210px" />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <span class="spacer" />
        <span class="tiny muted">共 {{ total }} 位医生</span>
      </div>

      <el-table v-loading="loading" :data="pageRows">
        <el-table-column type="index" label="#" width="52" />
        <el-table-column prop="realName" label="姓名" width="110" />
        <el-table-column prop="username" label="登录名" width="130" />
        <el-table-column prop="deptName" label="科室" width="110" />
        <el-table-column prop="title" label="职称" width="120" />
        <el-table-column prop="introduction" label="简介" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" min-width="150" align="right">
          <template #default="{ row }">
            <el-button link @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无医生数据" />
        </template>
      </el-table>

      <TablePager v-model:page="page" v-model:size="size" :total="total" />
    </section>

    <el-dialog append-to-body v-model="dialog" :title="editing ? '编辑医生' : '新增医生'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="登录名" prop="username">
            <el-input v-model="form.username" :disabled="!!editing" maxlength="20" />
          </el-form-item>
          <el-form-item v-if="!editing" label="初始密码" prop="password">
            <el-input v-model="form.password" type="password" show-password />
          </el-form-item>
          <el-form-item v-else label="重置密码（留空不修改）">
            <el-input v-model="form.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" maxlength="30" />
          </el-form-item>
          <el-form-item label="科室" prop="deptId">
            <el-select v-model="form.deptId" placeholder="选择科室" style="width: 100%">
              <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="职称" prop="title">
            <el-select v-model="form.title" filterable allow-create placeholder="选择或输入职称" style="width: 100%">
              <el-option v-for="t in titles" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="简介" prop="introduction">
          <el-input v-model="form.introduction" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}
@media (max-width: 620px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>

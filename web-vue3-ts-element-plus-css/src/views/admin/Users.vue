<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import PageHeader from "~/components/PageHeader.vue";
import TablePager from "~/components/TablePager.vue";
import {
  createUser,
  deleteUser,
  listUsers,
  updateUser,
} from "~/api/user";
import { usePagination } from "~/composables/usePagination";
import { useAppStore } from "~/stores/app";
import type { UserItem } from "~/types";
import { roleText } from "~/utils/format";

const store = useAppStore();
const loading = ref(false);
const rows = ref<UserItem[]>([]);
const { page, size, total, pageRows, resetPage } = usePagination(rows);
const filters = reactive<{ role?: "ADMIN" | "PATIENT"; keyword: string }>({
  role: undefined,
  keyword: "",
});
const dialog = ref(false);
const saving = ref(false);
const editing = ref<UserItem | null>(null);
const formRef = ref<FormInstance>();
const form = reactive({
  username: "",
  password: "",
  realName: "",
  email: "",
  role: "PATIENT" as "ADMIN" | "PATIENT",
  newPassword: "",
});
const rules: FormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { pattern: /^[A-Za-z0-9_]{4,20}$/, message: "4-20 位字母、数字或下划线", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码至少 6 位", trigger: "blur" },
  ],
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  email: [{ type: "email", message: "邮箱格式不正确", trigger: "blur" }],
};

async function load() {
  loading.value = true;
  try {
    rows.value = await listUsers({
      role: filters.role,
      keyword: filters.keyword.trim() || undefined,
    });
  } finally {
    loading.value = false;
  }
}

function search() {
  resetPage();
  load();
}

function resetFilters() {
  filters.role = undefined;
  filters.keyword = "";
  search();
}

function openCreate() {
  editing.value = null;
  Object.assign(form, {
    username: "",
    password: "",
    realName: "",
    email: "",
    role: "PATIENT",
    newPassword: "",
  });
  dialog.value = true;
  formRef.value?.clearValidate();
}

function openEdit(row: UserItem) {
  editing.value = row;
  Object.assign(form, {
    username: row.username,
    password: "",
    realName: row.realName,
    email: row.email || "",
    role: (row.role === "ADMIN" ? "ADMIN" : "PATIENT") as "ADMIN" | "PATIENT",
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
      await updateUser(editing.value.id, {
        realName: form.realName.trim(),
        email: form.email.trim() || undefined,
        newPassword: form.newPassword || undefined,
      });
      ElMessage.success("用户信息已更新");
    } else {
      await createUser({
        username: form.username.trim(),
        password: form.password,
        realName: form.realName.trim(),
        email: form.email.trim() || undefined,
        role: form.role,
      });
      ElMessage.success("用户已新增");
    }
    dialog.value = false;
    await load();
  } finally {
    saving.value = false;
  }
}

async function remove(row: UserItem) {
  if (row.id === store.user?.id) {
    ElMessage.warning("不能删除当前登录账号");
    return;
  }
  try {
    await ElMessageBox.confirm(`确定删除用户“${row.realName}”吗？`, "删除用户", {
      type: "warning",
      confirmButtonText: "删除",
      cancelButtonText: "取消",
    });
    await deleteUser(row.id);
    ElMessage.success("用户已删除");
    await load();
  } catch {
    /* 取消或后端拒绝 */
  }
}

onMounted(load);
</script>

<template>
  <div class="view">
    <PageHeader
      eyebrow="ACCESS"
      title="用户管理"
      description="维护管理员和患者账号；医生账号请在医生管理中新增。"
    >
      <el-button type="primary" @click="openCreate">新增用户</el-button>
    </PageHeader>

    <section class="panel">
      <div class="toolbar" style="margin-bottom: 16px">
        <el-select v-model="filters.role" placeholder="全部角色" clearable style="width: 140px">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="患者" value="PATIENT" />
        </el-select>
        <el-input v-model="filters.keyword" placeholder="用户名 / 真实姓名" clearable style="width: 220px" />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <span class="spacer" />
        <span class="tiny muted">共 {{ total }} 个用户</span>
      </div>

      <el-table v-loading="loading" :data="pageRows">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="{ row }">{{ row.email || "—" }}</template>
        </el-table-column>
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'warning' : 'success'" effect="light" size="small">
              {{ roleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="190" align="right">
          <template #default="{ row }">
            <el-button link @click="openEdit(row)">编辑</el-button>
            <el-button link @click="openEdit(row)">重置密码</el-button>
            <el-button link type="danger" :disabled="row.id === store.user?.id" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无用户数据" />
        </template>
      </el-table>

      <TablePager v-model:page="page" v-model:size="size" :total="total" />
    </section>

    <el-dialog append-to-body v-model="dialog" :title="editing ? '编辑用户' : '新增用户'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="two-col">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" :disabled="!!editing" maxlength="20" />
          </el-form-item>
          <el-form-item v-if="!editing" label="密码" prop="password">
            <el-input v-model="form.password" type="password" show-password />
          </el-form-item>
          <el-form-item v-else label="新密码（留空不修改）">
            <el-input v-model="form.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" maxlength="30" />
          </el-form-item>
          <el-form-item label="邮箱（可选）" prop="email">
            <el-input v-model="form.email" type="email" />
          </el-form-item>
        </div>
        <el-form-item v-if="!editing" label="角色">
          <el-radio-group v-model="form.role">
            <el-radio label="PATIENT">患者</el-radio>
            <el-radio label="ADMIN">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <div class="inline-note" style="padding: 9px 11px; border-radius: 8px; font-size: 11px; color: #827d74">
          医生账号请在医生管理中新增，会同时创建登录账号和医生档案。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
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


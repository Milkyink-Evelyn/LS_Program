<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import PageHeader from "~/components/PageHeader.vue";
import TablePager from "~/components/TablePager.vue";
import {
  createDepartment,
  deleteDepartment,
  listDepartments,
  updateDepartment,
} from "~/api/department";
import { usePagination } from "~/composables/usePagination";
import type { Department } from "~/types";

const loading = ref(false);
const rows = ref<Department[]>([]);
const { page, size, total, pageRows } = usePagination(rows);
const dialog = ref(false);
const saving = ref(false);
const editing = ref<Department | null>(null);
const formRef = ref<FormInstance>();
const form = reactive({ deptName: "" });
const rules: FormRules = {
  deptName: [
    { required: true, message: "请输入科室名称", trigger: "blur" },
    { max: 20, message: "最多 20 个字", trigger: "blur" },
  ],
};

async function load() {
  loading.value = true;
  try {
    rows.value = await listDepartments();
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editing.value = null;
  form.deptName = "";
  dialog.value = true;
  formRef.value?.clearValidate();
}

function openEdit(row: Department) {
  editing.value = row;
  form.deptName = row.deptName;
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
      await updateDepartment(editing.value.id, { deptName: form.deptName.trim() });
      ElMessage.success("科室已更新");
    } else {
      await createDepartment({ deptName: form.deptName.trim() });
      ElMessage.success("科室已新增");
    }
    dialog.value = false;
    await load();
  } catch {
    // 请求层已提示
  } finally {
    saving.value = false;
  }
}

async function remove(row: Department) {
  try {
    await ElMessageBox.confirm(`确定删除科室“${row.deptName}”吗？`, "删除科室", {
      type: "warning",
      confirmButtonText: "删除",
      cancelButtonText: "取消",
    });
    await deleteDepartment(row.id);
    ElMessage.success("科室已删除");
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
      eyebrow="MASTER DATA"
      title="科室管理"
      description="维护校医院科室；科室下存在医生时后端会拒绝删除。"
    >
      <el-button type="primary" @click="openCreate">新增科室</el-button>
    </PageHeader>

    <section class="panel">
      <div class="panel-title">
        <div><h3>科室列表</h3><div class="sub">共 {{ total }} 个科室</div></div>
        <el-button :loading="loading" size="small" @click="load">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="pageRows">
        <el-table-column type="index" label="#" width="52" />
        <el-table-column prop="deptName" label="科室名称" min-width="160" />
        <el-table-column label="医生数" width="110">
          <template #default="{ row }">
            <span class="muted">{{ row.doctorCount ?? 0 }} 人</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="160" align="right">
          <template #default="{ row }">
            <el-button link @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无科室数据，点击右上角新增" />
        </template>
      </el-table>

      <TablePager v-model:page="page" v-model:size="size" :total="total" />
    </section>

    <el-dialog append-to-body v-model="dialog" :title="editing ? '编辑科室' : '新增科室'" width="430px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="科室名称" prop="deptName">
          <el-input v-model="form.deptName" maxlength="20" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

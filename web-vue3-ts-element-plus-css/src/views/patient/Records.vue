<script setup lang="ts">
import { onMounted, ref } from "vue";
import PageHeader from "~/components/PageHeader.vue";
import TablePager from "~/components/TablePager.vue";
import TimeSlotTag from "~/components/TimeSlotTag.vue";
import { listMyRecords } from "~/api/medicalRecord";
import { usePagination } from "~/composables/usePagination";
import type { MedicalRecord } from "~/types";

const selectedDate = ref("");
const loading = ref(false);
const rows = ref<MedicalRecord[]>([]);
const { page, size, total, pageRows, resetPage } = usePagination(rows);
const visible = ref(false);
const current = ref<MedicalRecord | null>(null);

async function load() {
  loading.value = true;
  try {
    rows.value = await listMyRecords({
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

function open(row: MedicalRecord) {
  current.value = row;
  visible.value = true;
}

onMounted(load);
</script>

<template>
  <div class="view">
    <PageHeader
      eyebrow="MEDICAL RECORDS"
      title="我的病历"
      description="仅显示本人已诊记录的病历，处方保留换行。"
    >
      <el-button :loading="loading" @click="load">刷新</el-button>
    </PageHeader>

    <section class="panel">
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
        <el-table-column prop="workDate" label="就诊日期" width="120" />
        <el-table-column label="时段" width="90">
          <template #default="{ row }"><TimeSlotTag :time-slot="row.timeSlot" /></template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室" width="110" />
        <el-table-column prop="doctorName" label="医生" width="110" />
        <el-table-column prop="diagnosis" label="诊断" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="开单时间" min-width="160" />
        <el-table-column label="操作" min-width="90" align="right">
          <template #default="{ row }">
            <el-button link @click="open(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无病历记录" />
        </template>
      </el-table>

      <TablePager v-model:page="page" v-model:size="size" :total="total" />
    </section>

    <el-dialog append-to-body v-model="visible" title="病历详情" width="580px">
      <div v-if="current" class="detail-list">
        <div class="detail-row"><span class="k">医生</span><span class="v">{{ current.doctorName }} · {{ current.deptName }}</span></div>
        <div class="detail-row"><span class="k">就诊时间</span><span class="v">{{ current.workDate }} {{ current.timeSlot === 0 ? "上午" : "下午" }}</span></div>
        <div class="detail-row"><span class="k">症状</span><span class="v">{{ current.symptom }}</span></div>
        <div class="detail-row"><span class="k">诊断</span><span class="v">{{ current.diagnosis }}</span></div>
        <div class="detail-row"><span class="k">处方</span><span class="v">{{ current.prescription }}</span></div>
        <div class="detail-row"><span class="k">记录时间</span><span class="v">{{ current.createTime }}</span></div>
      </div>
      <template #footer><el-button @click="visible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

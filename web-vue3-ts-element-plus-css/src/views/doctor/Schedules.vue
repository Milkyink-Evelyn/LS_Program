<script setup lang="ts">
import { onMounted, ref } from "vue";
import PageHeader from "~/components/PageHeader.vue";
import TablePager from "~/components/TablePager.vue";
import TimeSlotTag from "~/components/TimeSlotTag.vue";
import { listMySchedules } from "~/api/schedule";
import { usePagination } from "~/composables/usePagination";
import type { Schedule } from "~/types";
import { addDays, dateString, todayString } from "~/utils/format";

const selectedDate = ref(todayString());
const loading = ref(false);
const rows = ref<Schedule[]>([]);
const { page, size, total, pageRows, resetPage } = usePagination(rows);

async function load() {
  loading.value = true;
  try {
    rows.value = await listMySchedules({ date: selectedDate.value });
  } finally {
    loading.value = false;
  }
}

function reload() {
  resetPage();
  load();
}

function setToday() {
  selectedDate.value = todayString();
  reload();
}

function setTomorrow() {
  selectedDate.value = dateString(addDays(new Date(), 1));
  reload();
}

onMounted(load);
</script>

<template>
  <div class="view">
    <PageHeader
      eyebrow="MY CLINIC"
      title="我的排班"
      description="只显示当前登录医生的排班，号源为只读。"
    >
      <el-button :loading="loading" @click="load">刷新</el-button>
    </PageHeader>

    <section class="panel">
      <div class="toolbar" style="margin-bottom: 16px">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          value-format="YYYY-MM-DD"
          style="width: 160px"
          @change="reload"
        />
        <el-button @click="setToday">今天</el-button>
        <el-button @click="setTomorrow">明天</el-button>
        <span class="spacer" />
        <span class="tiny muted">共 {{ total }} 个排班</span>
      </div>

      <el-table v-loading="loading" :data="pageRows">
        <el-table-column prop="workDate" label="日期" width="120" />
        <el-table-column label="时段" width="90">
          <template #default="{ row }"><TimeSlotTag :time-slot="row.timeSlot" /></template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室" width="140" />
        <el-table-column prop="totalQuota" label="总号" width="90" />
        <el-table-column label="已挂号" width="100">
          <template #default="{ row }">{{ row.totalQuota - row.remainQuota }}</template>
        </el-table-column>
        <el-table-column label="剩余号" min-width="110">
          <template #default="{ row }">
            <span class="quota" :class="{ full: row.remainQuota <= 0, low: row.remainQuota > 0 && row.remainQuota <= 3 }">
              {{ row.remainQuota }}
            </span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="当前日期没有排班" />
        </template>
      </el-table>

      <TablePager v-model:page="page" v-model:size="size" :total="total" />
    </section>
  </div>
</template>

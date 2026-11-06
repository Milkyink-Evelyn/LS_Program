<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import PageHeader from "~/components/PageHeader.vue";
import LineChart from "~/components/charts/LineChart.vue";
import PieChart from "~/components/charts/PieChart.vue";
import BarChart from "~/components/charts/BarChart.vue";
import {
  getDeptStats,
  getDoctorTop5,
  getLast7Days,
} from "~/api/statistics";
import type { ChartPoint, DeptStat, DoctorStat } from "~/types";

const loading = ref(true);
const lineData = ref<ChartPoint[]>([]);
const pieData = ref<DeptStat[]>([]);
const barData = ref<DoctorStat[]>([]);

const totalAppointments = computed(() =>
  lineData.value.reduce((sum, item) => sum + item.count, 0)
);
const deptCount = computed(() => pieData.value.length);
const doctorCount = computed(() => barData.value.length);

async function load() {
  loading.value = true;
  const [line, pie, bar] = await Promise.allSettled([
    getLast7Days(),
    getDeptStats(),
    getDoctorTop5(),
  ]);
  if (line.status === "fulfilled") lineData.value = line.value;
  if (pie.status === "fulfilled") pieData.value = pie.value;
  if (bar.status === "fulfilled") barData.value = bar.value;
  loading.value = false;
}

onMounted(load);
</script>

<template>
  <div class="view">
    <PageHeader
      eyebrow="OVERVIEW"
      title="数据看板"
      description="近 7 天挂号、科室占比与医生接诊量，数据均来自后端统计接口。"
    >
      <el-button :loading="loading" @click="load">刷新数据</el-button>
    </PageHeader>

    <div v-loading="loading" class="stat-grid">
      <div class="stat-card accent">
        <div class="stat-label"><span class="stat-icon"><el-icon><DataLine /></el-icon></span>近 7 天挂号</div>
        <div class="stat-value">{{ totalAppointments }}<small>人次</small></div>
        <div class="stat-note">排除已取消记录</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label"><span class="stat-icon"><el-icon><OfficeBuilding /></el-icon></span>产生挂号科室</div>
        <div class="stat-value">{{ deptCount }}<small>个</small></div>
        <div class="stat-note">只统计有挂号记录的科室</div>
      </div>
      <div class="stat-card">
        <div class="stat-label"><span class="stat-icon"><el-icon><FirstAidKit /></el-icon></span>接诊医生 Top5</div>
        <div class="stat-value">{{ doctorCount }}<small>位</small></div>
        <div class="stat-note">按已诊挂号统计</div>
      </div>
      <div class="stat-card">
        <div class="stat-label"><span class="stat-icon"><el-icon><Calendar /></el-icon></span>统计周期</div>
        <div class="stat-value" style="font-size: 22px; letter-spacing: -0.03em">最近 7 天</div>
        <div class="stat-note">含没有数据的日期</div>
      </div>
    </div>

    <section class="panel" v-loading="loading">
      <div class="panel-title">
        <div><h3>挂号趋势</h3><div class="sub">按挂号创建日期统计，单位：人次</div></div>
        <span class="tiny muted">LAST 7 DAYS</span>
      </div>
      <LineChart v-if="lineData.length" :data="lineData" />
      <el-empty v-else description="暂无挂号统计数据" />
    </section>

    <div class="grid-2">
      <section class="panel" v-loading="loading">
        <div class="panel-title">
          <div><h3>科室占比</h3><div class="sub">各科室挂号人数占比</div></div>
          <span class="tiny muted">DEPARTMENT</span>
        </div>
        <PieChart v-if="pieData.length" :data="pieData" />
        <el-empty v-else description="暂无科室统计数据" />
      </section>
      <section class="panel" v-loading="loading">
        <div class="panel-title">
          <div><h3>医生接诊量</h3><div class="sub">已诊挂号数量 Top5</div></div>
          <span class="tiny muted">DOCTOR</span>
        </div>
        <BarChart v-if="barData.length" :data="barData" />
        <el-empty v-else description="暂无医生接诊数据" />
      </section>
    </div>
  </div>
</template>

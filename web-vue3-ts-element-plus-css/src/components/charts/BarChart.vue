<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import * as echarts from "echarts";
import type { DoctorStat } from "~/types";

const props = defineProps<{ data: DoctorStat[] }>();
const el = ref<HTMLDivElement>();
let chart: echarts.ECharts | null = null;

const render = () => {
  if (!chart || !props.data.length) return;
  const rows = [...props.data].reverse();
  chart.setOption({
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" },
      formatter: (params: unknown) => {
        const item = (params as Array<{ dataIndex: number }>)[0];
        const row = rows[item.dataIndex];
        return `${row.doctorName} · ${row.deptName}<br/>接诊 ${row.count} 人`;
      },
    },
    grid: { left: 8, right: 26, top: 12, bottom: 16, containLabel: true },
    xAxis: {
      type: "value",
      minInterval: 1,
      splitLine: { lineStyle: { color: "#eee9dd", type: "dashed" } },
      axisLabel: { color: "#8b8579", fontSize: 10 },
    },
    yAxis: {
      type: "category",
      data: rows.map((i) => i.doctorName),
      axisTick: { show: false },
      axisLine: { show: false },
      axisLabel: { color: "#49453e", fontSize: 11 },
    },
    series: [
      {
        type: "bar",
        barWidth: 13,
        data: rows.map((i) => i.count),
        itemStyle: { color: "#1b1b1b", borderRadius: [0, 8, 8, 0] },
        showBackground: true,
        backgroundStyle: { color: "#f1ede3", borderRadius: [0, 8, 8, 0] },
      },
    ],
  });
};

const resize = () => chart?.resize();

onMounted(() => {
  if (el.value) {
    chart = echarts.init(el.value);
    render();
    window.addEventListener("resize", resize);
  }
});

watch(() => props.data, render, { deep: true });

onBeforeUnmount(() => {
  window.removeEventListener("resize", resize);
  chart?.dispose();
  chart = null;
});
</script>

<template>
  <div ref="el" style="width: 100%; height: 320px" />
</template>

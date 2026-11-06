<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import * as echarts from "echarts";
import type { ChartPoint } from "~/types";

const props = defineProps<{ data: ChartPoint[] }>();
const el = ref<HTMLDivElement>();
let chart: echarts.ECharts | null = null;

const render = () => {
  if (!chart || !props.data.length) return;
  chart.setOption({
    color: ["#1b1b1b"],
    tooltip: { trigger: "axis" },
    grid: { left: 34, right: 18, top: 22, bottom: 28 },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: props.data.map((i) => i.date.slice(5)),
      axisLine: { lineStyle: { color: "#ded8ca" } },
      axisTick: { show: false },
      axisLabel: { color: "#8b8579", fontSize: 10 },
    },
    yAxis: {
      type: "value",
      minInterval: 1,
      splitLine: { lineStyle: { color: "#eee9dd", type: "dashed" } },
      axisLabel: { color: "#8b8579", fontSize: 10 },
    },
    series: [
      {
        type: "line",
        smooth: true,
        symbolSize: 5,
        data: props.data.map((i) => i.count),
        lineStyle: { width: 2, color: "#1b1b1b" },
        itemStyle: { color: "#1b1b1b" },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(223,240,126,.55)" },
            { offset: 1, color: "rgba(223,240,126,0)" },
          ]),
        },
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

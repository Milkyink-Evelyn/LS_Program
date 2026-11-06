<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import * as echarts from "echarts";
import type { DeptStat } from "~/types";

const props = defineProps<{ data: DeptStat[] }>();
const el = ref<HTMLDivElement>();
let chart: echarts.ECharts | null = null;

const render = () => {
  if (!chart || !props.data.length) return;
  chart.setOption({
    color: ["#1b1b1b", "#dff07e", "#9cb889", "#b9ae94", "#7b7568", "#d9cdb6"],
    tooltip: { trigger: "item", formatter: "{b}: {c} 人 ({d}%)" },
    legend: {
      bottom: 0,
      icon: "circle",
      itemWidth: 7,
      itemHeight: 7,
      textStyle: { color: "#827d74", fontSize: 10 },
    },
    series: [
      {
        type: "pie",
        radius: ["44%", "68%"],
        center: ["50%", "44%"],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: "#fbfaf6", borderWidth: 3 },
        label: { show: false },
        data: props.data.map((i) => ({ name: i.deptName, value: i.count })),
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

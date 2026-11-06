<script setup lang="ts">
import { computed } from "vue";
import type { AppointmentStatus } from "~/types";
import { statusTagType, statusText } from "~/utils/format";

const props = defineProps<{ status: AppointmentStatus }>();
const type = computed(() => statusTagType(props.status));
</script>

<template>
  <el-tag :type="type" effect="light" size="small">{{ statusText(status) }}</el-tag>
</template>

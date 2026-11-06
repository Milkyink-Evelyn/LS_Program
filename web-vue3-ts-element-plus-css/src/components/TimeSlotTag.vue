<script setup lang="ts">
import type { TimeSlot } from "~/types";
import { timeSlotText } from "~/utils/format";

defineProps<{ timeSlot: TimeSlot }>();
</script>

<template>
  <span class="slot-tag">{{ timeSlotText(timeSlot) }}</span>
</template>

<style scoped>
.slot-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 8px;
  border-radius: 6px;
  background: #f0ecdf;
  color: #5f5a50;
  font-size: 11px;
}
</style>

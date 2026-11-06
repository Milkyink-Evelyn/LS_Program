<script setup lang="ts">
defineProps<{
  page: number;
  size: number;
  total: number;
}>();

const emit = defineEmits<{
  (e: "update:page", value: number): void;
  (e: "update:size", value: number): void;
}>();
</script>

<template>
  <div v-if="total > 0" class="table-pager">
    <el-pagination
      :current-page="page"
      :page-size="size"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @current-change="emit('update:page', $event)"
      @size-change="emit('update:size', $event)"
    />
  </div>
</template>

<style scoped>
.table-pager {
  display: flex;
  justify-content: flex-end;
  padding-top: 14px;
}
</style>

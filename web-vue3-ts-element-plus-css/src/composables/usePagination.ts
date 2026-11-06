import { computed, ref, watch, type Ref } from "vue";

/** 前端分页：列表数据一次性加载，按页切片展示。 */
export function usePagination<T>(source: Ref<T[]>, initialSize = 10) {
  const page = ref(1);
  const size = ref(initialSize);
  const total = computed(() => source.value.length);
  const pageRows = computed(() => {
    const start = (page.value - 1) * size.value;
    return source.value.slice(start, start + size.value);
  });

  watch(total, (value) => {
    const maxPage = Math.max(1, Math.ceil(value / size.value));
    if (page.value > maxPage) page.value = maxPage;
  });

  watch(size, () => {
    page.value = 1;
  });

  function resetPage() {
    page.value = 1;
  }

  return { page, size, total, pageRows, resetPage };
}

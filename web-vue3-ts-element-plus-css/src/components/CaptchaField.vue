<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { getValidateCode } from "~/api/auth";

const props = defineProps<{
  modelValue: string;
  validateCodeId: number;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
  (e: "update:validateCodeId", id: number): void;
}>();

const img = ref("");
const loading = ref(false);

async function refresh() {
  loading.value = true;
  try {
    const res = await getValidateCode();
    img.value = res.img;
    emit("update:validateCodeId", res.validateCodeId);
  } catch {
    img.value = "";
    ElMessage.error("验证码加载失败，请点击重试");
  } finally {
    loading.value = false;
  }
}

function onInput(value: string) {
  emit("update:modelValue", value.replace(/\D/g, "").slice(0, 4));
}

onMounted(refresh);
defineExpose({ refresh });
</script>

<template>
  <div class="captcha-field">
    <el-input
      :model-value="props.modelValue"
      maxlength="4"
      placeholder="4 位数字验证码"
      @update:model-value="onInput"
    />
    <button class="captcha-img" type="button" title="点击刷新验证码" @click="refresh">
      <img v-if="img" :src="img" alt="验证码" />
      <span v-else>{{ loading ? "加载中" : "点击重试" }}</span>
    </button>
  </div>
</template>

<style scoped>
.captcha-field {
  display: flex;
  gap: 10px;
  width: 100%;
}
.captcha-field .el-input {
  flex: 1;
}
.captcha-img {
  width: 124px;
  height: 34px;
  border-radius: 9px;
  border: 1px solid #e1dccf;
  background: #fffdf8;
  overflow: hidden;
  display: grid;
  place-items: center;
  padding: 0;
}
.captcha-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.captcha-img span {
  font-size: 11px;
  color: #827d74;
}
</style>

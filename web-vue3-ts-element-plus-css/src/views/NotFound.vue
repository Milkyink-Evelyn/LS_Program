<script setup lang="ts">
import { useRouter } from "vue-router";
import { useAppStore } from "~/stores/app";
import { roleHome } from "~/router";

const router = useRouter();
const store = useAppStore();

function goHome() {
  if (store.user) router.push(roleHome[store.user.role]);
  else router.push("/login");
}
</script>

<template>
  <div class="not-found">
    <div class="nf-mark">404</div>
    <h1>页面不存在</h1>
    <p>链接可能已失效，或者地址输入有误。</p>
    <button class="nf-btn" @click="goHome">返回首页</button>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 100vh;
  background: #f2efe6;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 9px;
  padding: 20px;
  text-align: center;
}
.nf-mark {
  font-size: clamp(86px, 15vw, 180px);
  line-height: 1;
  letter-spacing: -0.1em;
  color: #dcd4c3;
  font-weight: 700;
}
.not-found h1 {
  font-size: 25px;
  letter-spacing: -0.04em;
}
.not-found p {
  font-size: 12px;
  color: #827d74;
}
.nf-btn {
  margin-top: 15px;
  border: 0;
  border-radius: 999px;
  background: #171717;
  color: #fff;
  height: 40px;
  padding: 0 20px;
  font-size: 12px;
}
</style>

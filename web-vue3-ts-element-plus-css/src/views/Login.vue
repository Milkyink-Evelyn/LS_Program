<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import type { FormInstance, FormRules } from "element-plus";
import AuthShell from "~/components/AuthShell.vue";
import { login } from "~/api/auth";
import { useAppStore } from "~/stores/app";
import { roleHome } from "~/router";
import { demoAccounts } from "~/config/demoAccounts";

const router = useRouter();
const route = useRoute();
const store = useAppStore();
const formRef = ref<FormInstance>();
const loading = ref(false);

// 实验要求：默认填充管理员账号密码，打开即可一键登录
const form = reactive({
  username: typeof route.query.username === "string" ? route.query.username : "admin",
  password: "123456",
});

const rules: FormRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码至少 6 位", trigger: "blur" },
  ],
};

async function submit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  loading.value = true;
  try {
    const res = await login({
      username: form.username.trim(),
      password: form.password,
    });
    store.setLogin(res);
    router.replace(roleHome[store.user!.role]);
  } catch {
    // 请求层已经给出错误提示
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <AuthShell
    title="校医院预约挂号系统"
    subtitle="请使用管理员、医生或患者账号登录。登录页不启用验证码。"
  >
    <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="submit">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" size="large" autocomplete="username" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          size="large"
          autocomplete="current-password"
          @keyup.enter="submit"
        />
      </el-form-item>
      <button class="auth-submit" type="button" :disabled="loading" @click="submit">
        {{ loading ? "登录中..." : "登 录" }}
      </button>
    </el-form>

    <div class="auth-links">
      <router-link to="/register">注册账号</router-link>
      <router-link to="/forgot-password">忘记密码</router-link>
      <router-link to="/about">关于项目</router-link>
    </div>

    <div class="demo-box">
      <div class="demo-title">演示账号（密码均为 123456）</div>
      <div v-for="item in demoAccounts" :key="item.role" class="demo-row">
        <span>{{ item.label }}</span>
        <code>{{ item.username }}</code>
      </div>
    </div>
  </AuthShell>
</template>

<style scoped>
.demo-box {
  margin-top: 17px;
  border-top: 1px dashed #e1e6eb;
  padding-top: 13px;
}
.demo-title {
  font-size: 9.5px;
  letter-spacing: 0.08em;
  color: #a39c90;
  margin-bottom: 7px;
}
.demo-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 10.5px;
  color: #7d8790;
  line-height: 1.9;
}
.demo-row code {
  color: #5c656d;
  font-family: "SFMono-Regular", Consolas, monospace;
  background: #f4f2ed;
  padding: 1px 6px;
  border-radius: 3px;
}
</style>


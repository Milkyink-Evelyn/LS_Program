<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import AuthShell from "~/components/AuthShell.vue";
import CaptchaField from "~/components/CaptchaField.vue";
import { register } from "~/api/auth";

const router = useRouter();
const formRef = ref<FormInstance>();
const captchaRef = ref<InstanceType<typeof CaptchaField>>();
const loading = ref(false);
const form = reactive({
  username: "",
  password: "",
  confirm: "",
  realName: "",
  email: "",
  validateCode: "",
  validateCodeId: 0,
});
const rules: FormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { pattern: /^[A-Za-z0-9_]{4,20}$/, message: "4-20 位字母、数字或下划线", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码至少 6 位", trigger: "blur" },
  ],
  confirm: [
    {
      validator: (_rule, value, callback) => {
        if (!value) callback(new Error("请再次输入密码"));
        else if (value !== form.password) callback(new Error("两次密码不一致"));
        else callback();
      },
      trigger: "blur",
    },
  ],
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  email: [{ type: "email", message: "邮箱格式不正确", trigger: "blur" }],
  validateCode: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { pattern: /^\d{4}$/, message: "请输入 4 位数字验证码", trigger: "blur" },
  ],
};

async function submit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  if (!form.validateCodeId) {
    ElMessage.warning("请先获取验证码");
    return;
  }

  loading.value = true;
  try {
    await register({
      username: form.username.trim(),
      password: form.password,
      realName: form.realName.trim(),
      email: form.email.trim() || undefined,
      validateCodeId: form.validateCodeId,
      validateCode: form.validateCode,
    });
    ElMessage.success("注册成功，请登录");
    router.replace({ path: "/login", query: { username: form.username.trim() } });
  } catch {
    captchaRef.value?.refresh();
    form.validateCode = "";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <AuthShell
    title="患者注册"
    subtitle="注册角色固定为患者；注册后可用新账号登录预约挂号。"
    wide
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
      <div class="two-col">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" maxlength="20" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" maxlength="30" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm">
          <el-input v-model="form.confirm" type="password" show-password />
        </el-form-item>
      </div>
      <el-form-item label="邮箱（可选）" prop="email">
        <el-input v-model="form.email" type="email" />
      </el-form-item>
      <el-form-item label="验证码" prop="validateCode">
        <CaptchaField
          ref="captchaRef"
          v-model="form.validateCode"
          v-model:validate-code-id="form.validateCodeId"
        />
      </el-form-item>
      <button class="auth-submit" type="button" :disabled="loading" @click="submit">
        {{ loading ? "注册中..." : "注 册" }}
      </button>
    </el-form>
    <div class="auth-links">
      <span>已有账号？</span>
      <router-link to="/login">返回登录</router-link>
    </div>
  </AuthShell>
</template>

<style scoped>
.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 14px;
}
@media (max-width: 620px) {
  .two-col {
    grid-template-columns: 1fr;
  }
}
</style>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import AuthShell from "~/components/AuthShell.vue";
import CaptchaField from "~/components/CaptchaField.vue";
import { resetPassword } from "~/api/auth";

const router = useRouter();
const formRef = ref<FormInstance>();
const captchaRef = ref<InstanceType<typeof CaptchaField>>();
const loading = ref(false);
const form = reactive({
  username: "",
  realName: "",
  newPassword: "",
  confirm: "",
  validateCode: "",
  validateCodeId: 0,
});
const rules: FormRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码至少 6 位", trigger: "blur" },
  ],
  confirm: [
    {
      validator: (_rule, value, callback) => {
        if (!value) callback(new Error("请再次输入新密码"));
        else if (value !== form.newPassword) callback(new Error("两次密码不一致"));
        else callback();
      },
      trigger: "blur",
    },
  ],
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
    await resetPassword({
      username: form.username.trim(),
      realName: form.realName.trim(),
      newPassword: form.newPassword,
      validateCodeId: form.validateCodeId,
      validateCode: form.validateCode,
    });
    ElMessage.success("密码重置成功，请使用新密码登录");
    router.replace("/login");
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
    title="重置密码"
    subtitle="使用用户名、真实姓名和验证码完成身份校验。"
    wide
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
      <div class="two-col">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirm">
          <el-input v-model="form.confirm" type="password" show-password />
        </el-form-item>
      </div>
      <el-form-item label="验证码" prop="validateCode">
        <CaptchaField
          ref="captchaRef"
          v-model="form.validateCode"
          v-model:validate-code-id="form.validateCodeId"
        />
      </el-form-item>
      <button class="auth-submit" type="button" :disabled="loading" @click="submit">
        {{ loading ? "提交中..." : "重置密码" }}
      </button>
    </el-form>
    <div class="auth-links">
      <span>返回</span>
      <router-link to="/login">去登录</router-link>
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

<script setup lang="ts">
import AuthShell from "~/components/AuthShell.vue";
</script>

<template>
  <AuthShell
    title="忘记密码"
    subtitle="系统为课程演示系统，不提供短信或邮件找回。"
  >
    <div class="forgot-box">
      <p>方式一：携带学生证或校园卡到校医院信息中心，由工作人员重置。</p>
      <p>方式二：联系系统管理员，由管理员在用户管理中重置密码。</p>
      <p class="note">如需自助重置，可使用“重置密码”，需要验证码并核对用户名与真实姓名。</p>
    </div>
    <router-link class="auth-submit forgot-btn" to="/reset-password">去重置密码</router-link>
    <div class="auth-links">
      <span>想起密码了？</span>
      <router-link to="/login">返回登录</router-link>
    </div>
  </AuthShell>
</template>

<style scoped>
.forgot-box {
  font-size: 12px;
  line-height: 1.9;
  color: #626b73;
  border-left: 2px solid #9d1c20;
  padding-left: 14px;
  margin-bottom: 20px;
}
.forgot-box p + p {
  margin-top: 9px;
}
.forgot-box .note {
  color: #9a7a42;
}
.forgot-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
}
</style>

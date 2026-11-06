<script setup lang="ts">
defineProps<{
  title: string;
  subtitle?: string;
  wide?: boolean;
}>();
</script>

<template>
  <div class="auth-page">
    <div class="auth-bg"></div>
    <div class="auth-shade"></div>

    <header class="auth-header">
      <div class="auth-brand">
        <img src="/sdu-logo.svg" alt="山东大学" />
        <span class="brand-line"></span>
        <span class="brand-name">校医院预约挂号系统</span>
      </div>
      <span class="brand-school">山东大学</span>
    </header>

    <main class="auth-main">
      <section class="auth-card" :class="{ wide }">
        <div class="auth-card-top">
          <div class="auth-kicker">SHANDONG UNIVERSITY</div>
          <h1>{{ title }}</h1>
          <p v-if="subtitle">{{ subtitle }}</p>
        </div>
        <slot />
      </section>
    </main>

    <footer class="auth-footer">
      <span>© 2026 山东大学校医院预约挂号系统</span>
      <span>Web 技术课程设计 · 教学演示</span>
    </footer>
  </div>
</template>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: #cfdfe9;
}
.auth-bg {
  position: absolute;
  inset: 0;
  background-image: url("/campus.jpg");
  background-size: cover;
  background-position: center 42%;
  transform: scale(1.01);
}
.auth-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    rgba(13, 38, 58, 0.16) 0%,
    rgba(238, 244, 248, 0.08) 48%,
    rgba(243, 247, 249, 0.7) 100%
  );
}
.auth-header {
  position: absolute;
  z-index: 3;
  top: 0;
  left: 0;
  right: 0;
  height: 78px;
  padding: 0 42px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  background: linear-gradient(180deg, rgba(8, 32, 51, 0.48), transparent);
}
.auth-brand {
  display: flex;
  align-items: center;
  gap: 17px;
}
.auth-brand img {
  width: 205px;
  height: 63px;
  object-fit: contain;
  object-position: left center;
}
.brand-line {
  width: 1px;
  height: 32px;
  background: rgba(255, 255, 255, 0.5);
}
.brand-name {
  font-size: 20px;
  letter-spacing: 0.04em;
  white-space: nowrap;
}
.brand-school {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(255, 255, 255, 0.42);
  padding: 5px 11px;
  border-radius: 2px;
  letter-spacing: 0.12em;
}
.auth-main {
  position: relative;
  z-index: 2;
  min-height: 100vh;
  padding: 100px 7vw 64px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
.auth-card {
  width: 400px;
  max-width: 100%;
  background: #fffefb;
  border: 1px solid #d8dfe5;
  box-shadow: 0 22px 52px rgba(8, 28, 44, 0.2);
  border-radius: 3px;
  padding: 26px 28px 24px;
}
.auth-card.wide {
  width: 540px;
}
.auth-card-top {
  margin-bottom: 20px;
}
.auth-kicker {
  font-size: 9px;
  letter-spacing: 0.22em;
  color: #a59e91;
  margin-bottom: 9px;
}
.auth-card h1 {
  font-size: 25px;
  font-weight: 650;
  letter-spacing: -0.04em;
  color: #202a33;
}
.auth-card p {
  margin-top: 6px;
  font-size: 11.5px;
  line-height: 1.6;
  color: #8b939b;
}
.auth-footer {
  position: absolute;
  z-index: 3;
  left: 0;
  right: 0;
  bottom: 0;
  min-height: 44px;
  padding: 12px 42px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  color: rgba(255, 255, 255, 0.88);
  background: rgba(8, 29, 46, 0.3);
  font-size: 10.5px;
}
.auth-card :deep(.el-form-item) {
  margin-bottom: 15px;
}
.auth-card :deep(.el-form-item__label) {
  font-size: 11.5px;
  color: #6f7780;
  padding-bottom: 3px;
}
.auth-card :deep(.el-input__wrapper) {
  border-radius: 2px;
  min-height: 41px;
  box-shadow: 0 0 0 1px #d6dde4 inset !important;
  background: #fff;
}
.auth-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #9d1c20 inset !important;
}
.auth-submit {
  width: 100%;
  height: 42px;
  border: 0;
  border-radius: 2px;
  background: #9d1c20;
  color: #fff;
  font-size: 13.5px;
  letter-spacing: 0.1em;
  margin-top: 3px;
}
.auth-submit:hover {
  background: #81171a;
}
.auth-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.auth-links {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  font-size: 11px;
  color: #8b939b;
}
.auth-links a {
  color: #9d1c20;
}
.auth-links a:hover {
  text-decoration: underline;
}
@media (max-width: 900px) {
  .auth-header {
    height: 66px;
    padding: 0 18px;
  }
  .auth-brand img {
    width: 150px;
    height: 48px;
  }
  .brand-line {
    display: none;
  }
  .brand-name {
    font-size: 16px;
  }
  .brand-school {
    display: none;
  }
  .auth-main {
    justify-content: center;
    padding: 86px 16px 62px;
  }
  .auth-card,
  .auth-card.wide {
    width: min(100%, 540px);
    padding: 20px 18px;
  }
  .auth-footer {
    padding: 10px 16px;
    font-size: 9.5px;
    flex-direction: column;
    gap: 3px;
  }
}
</style>

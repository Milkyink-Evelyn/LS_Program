<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useAppStore } from "~/stores/app";
import { roleHome } from "~/router";
import { teamMembers } from "~/config/team";

const router = useRouter();
const store = useAppStore();
const isLoggedIn = computed(() => Boolean(store.token && store.user));

const features = [
  { icon: "Calendar", title: "预约挂号", desc: "按科室、医生和日期查询号源，确认后生成待诊记录。" },
  { icon: "FirstAidKit", title: "医生诊疗", desc: "医生查看本人排班和待诊患者，提交症状、诊断与处方。" },
  { icon: "DataAnalysis", title: "数据统计", desc: "管理员查看近 7 天挂号趋势、科室占比和医生接诊量。" },
];

function enter() {
  if (store.user) router.push(roleHome[store.user.role]);
  else router.push("/login");
}
</script>

<template>
  <div class="about">
    <header class="about-nav">
      <div class="about-brand">
        <img src="/sdu-logo.svg" alt="山东大学" />
        <span>校医院预约挂号系统</span>
      </div>
      <div class="about-nav-right">
        <button v-if="!isLoggedIn" class="ghost-pill" @click="router.push('/login')">登录</button>
        <button class="dark-pill" @click="enter">{{ isLoggedIn ? "返回系统" : "进入系统" }}</button>
      </div>
    </header>

    <main>
      <section class="hero">
        <div class="hero-copy">
          <div class="hero-kicker">SHANDONG UNIVERSITY · CAMPUS HEALTH</div>
          <h1>让校园就医<br /><em>更简单一点。</em></h1>
          <p>
            校医院预约挂号系统覆盖管理员、医生、患者三条链路：维护科室与排班，
            查看号源并预约，完成接诊与病历记录。
          </p>
          <div class="hero-actions">
            <button class="dark-pill large" @click="enter">{{ isLoggedIn ? "返回系统" : "开始使用" }}</button>
            <span class="hero-note">Web 技术课程设计 · 教学演示</span>
          </div>
        </div>
        <div class="hero-art">
          <div class="art-window">
            <div class="art-top"><span></span><span></span><span></span><b>界面示意</b></div>
            <div class="art-body">
              <div class="art-hero"><span>09:00</span><b>上午门诊</b><small>内科 · 主任医师</small></div>
              <div class="art-row"><span>剩余号源</span><b>—</b></div>
              <div class="art-row"><span>已预约</span><b>—</b></div>
              <div class="art-bar"><i></i></div>
            </div>
          </div>
          <div class="art-orb"></div>
          <div class="art-sticker">CAMPUS<br />CARE</div>
        </div>
      </section>

      <section class="about-section">
        <div class="section-head">
          <span>01 / FEATURES</span>
          <h2>三条链路，<br />一套系统。</h2>
        </div>
        <div class="feature-grid">
          <article v-for="(item, index) in features" :key="item.title" class="feature-card" :class="`f-${index + 1}`">
            <div class="feature-icon"><el-icon><component :is="item.icon" /></el-icon></div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
            <span class="feature-no">0{{ index + 1 }}</span>
          </article>
        </div>
      </section>

      <section class="about-section stack-section">
        <div class="section-head">
          <span>02 / STACK</span>
          <h2>前端与后端，<br />都有明确分工。</h2>
        </div>
        <div class="stack-grid">
          <div class="stack-card">
            <b>前端</b>
            <span>Vue 3</span><span>TypeScript</span><span>Element Plus</span><span>Pinia</span><span>ECharts</span>
          </div>
          <div class="stack-card dark">
            <b>后端</b>
            <span>Spring Boot 3</span><span>Spring Data JPA</span><span>MySQL</span><span>JWT</span>
          </div>
        </div>
      </section>

      <section class="about-section preview-section">
        <div class="section-head">
          <span>03 / PREVIEW</span>
          <h2>界面预览</h2>
        </div>
        <el-carousel height="430px" :interval="4500" arrow="hover" class="preview-carousel">
          <el-carousel-item>
            <img src="/app/hospital-login.png" alt="登录界面" />
          </el-carousel-item>
          <el-carousel-item>
            <img src="/app/hospital-dashboard.png" alt="数据看板界面" />
          </el-carousel-item>
        </el-carousel>
        <p class="preview-note">实际页面截图，数据为空时显示空态；演示数据由后端导入。</p>
      </section>

      <section class="about-section team-section">
        <div class="section-head">
          <span>04 / TEAM</span>
          <h2>课程项目团队</h2>
        </div>
        <div class="team-grid">
          <article v-for="member in teamMembers" :key="member.studentId" class="team-card">
            <div class="team-avatar">{{ member.name.slice(0, 1) }}</div>
            <div>
              <h3>{{ member.name }}</h3>
              <p>{{ member.studentId }}</p>
              <span>{{ member.duty }}</span>
            </div>
          </article>
        </div>
      </section>

      <section class="about-contact">
        <div>
          <span>校医院预约挂号系统</span>
          <h2>课程设计 · 教学演示</h2>
        </div>
        <button class="dark-pill" @click="enter">{{ isLoggedIn ? "返回系统" : "进入系统" }}</button>
      </section>
    </main>

    <footer class="about-footer">
      <span>© 2026 山东大学校医院预约挂号系统</span>
      <span>Vue 3 + Element Plus · Spring Boot + MySQL</span>
    </footer>
  </div>
</template>

<style scoped>
.about {
  min-height: 100vh;
  background: #f2efe6;
  color: #171717;
}
.about-nav {
  max-width: 1380px;
  margin: 0 auto;
  padding: 24px 38px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}
.about-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}
.about-brand img {
  width: 128px;
  height: 40px;
  object-fit: contain;
  filter: brightness(0) saturate(100%);
}
.about-brand span {
  font-size: 12px;
  letter-spacing: 0.08em;
  color: #686254;
  border-left: 1px solid #d3ccbd;
  padding-left: 12px;
}
.about-nav-right {
  display: flex;
  gap: 8px;
}
.ghost-pill,
.dark-pill {
  border-radius: 999px;
  height: 37px;
  padding: 0 18px;
  font-size: 11.5px;
  border: 1px solid #d0c8b7;
  background: transparent;
  color: #4f4a40;
}
.dark-pill {
  background: #171717;
  border-color: #171717;
  color: #fff;
}
.dark-pill.large {
  height: 44px;
  padding: 0 24px;
  font-size: 12.5px;
}
.ghost-pill:hover {
  background: #fffdf8;
}
main {
  max-width: 1380px;
  margin: 0 auto;
  padding: 12px 38px 60px;
}
.hero {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 42px;
  align-items: center;
  min-height: 540px;
  padding: 30px 0 56px;
}
.hero-kicker {
  font-size: 9.5px;
  letter-spacing: 0.22em;
  color: #8e8779;
  margin-bottom: 22px;
}
.hero h1 {
  font-size: clamp(48px, 6.4vw, 88px);
  line-height: 0.96;
  letter-spacing: -0.075em;
  font-weight: 650;
}
.hero h1 em {
  font-style: normal;
  color: #9d1c20;
}
.hero p {
  max-width: 470px;
  margin-top: 25px;
  color: #706a5e;
  font-size: 13px;
  line-height: 1.9;
}
.hero-actions {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 28px;
  flex-wrap: wrap;
}
.hero-note {
  font-size: 10px;
  color: #989183;
}
.hero-art {
  position: relative;
  min-height: 390px;
  display: grid;
  place-items: center;
}
.art-window {
  width: min(100%, 390px);
  background: #fbfaf6;
  border: 1px solid #dcd5c5;
  border-radius: 24px;
  box-shadow: 18px 24px 0 #e4ddcc;
  overflow: hidden;
  animation: hero-float 5s ease-in-out infinite;
}
@keyframes hero-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
.art-top {
  height: 42px;
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 0 16px;
  border-bottom: 1px solid #ece6d8;
}
.art-top > span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #d6cebd;
}
.art-top b {
  margin-left: auto;
  font-size: 9px;
  color: #9c9588;
  letter-spacing: 0.08em;
}
.art-body {
  padding: 16px;
}
.art-hero {
  background: #191919;
  color: #fff;
  border-radius: 14px;
  padding: 18px;
  position: relative;
  overflow: hidden;
}
.art-hero::after {
  content: "";
  position: absolute;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: #dff07e;
  right: -54px;
  bottom: -54px;
  opacity: 0.9;
}
.art-hero span {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.55);
}
.art-hero b {
  display: block;
  font-size: 22px;
  letter-spacing: -0.04em;
  margin-top: 6px;
}
.art-hero small {
  display: block;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 5px;
}
.art-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 13px 4px;
  border-bottom: 1px dashed #e8e1d4;
  font-size: 11px;
  color: #777063;
}
.art-row b {
  font-size: 22px;
  color: #171717;
  letter-spacing: -0.04em;
}
.art-bar {
  height: 6px;
  background: #e9e3d6;
  border-radius: 99px;
  margin-top: 15px;
  overflow: hidden;
}
.art-bar i {
  display: block;
  width: 62%;
  height: 100%;
  background: #9d1c20;
  border-radius: 99px;
}
.art-orb {
  position: absolute;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: #dff07e;
  top: 6px;
  right: 2%;
  filter: blur(0);
  z-index: -1;
}
.art-sticker {
  position: absolute;
  right: 0;
  bottom: 18px;
  background: #9d1c20;
  color: #fff;
  font-size: 10px;
  letter-spacing: 0.16em;
  line-height: 1.35;
  padding: 13px;
  border-radius: 50%;
  transform: rotate(10deg);
}
.about-section {
  padding: 60px 0;
  border-top: 1px solid #ddd6c7;
}
.section-head {
  margin-bottom: 27px;
}
.section-head > span {
  font-size: 9.5px;
  letter-spacing: 0.2em;
  color: #918a7c;
}
.section-head h2 {
  margin-top: 12px;
  font-size: clamp(30px, 4.4vw, 54px);
  line-height: 1.03;
  letter-spacing: -0.06em;
  font-weight: 650;
}
.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.feature-card {
  position: relative;
  min-height: 240px;
  padding: 21px;
  border-radius: 20px;
  background: #fbfaf6;
  border: 1px solid #ddd6c7;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  overflow: hidden;
}
.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 30px rgba(75, 63, 42, 0.08);
}
.feature-card.f-2 {
  background: #dff07e;
  border-color: #d2e26e;
}
.feature-card.f-3 {
  background: #191919;
  color: #fff;
  border-color: #191919;
}
.feature-icon {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.07);
  display: grid;
  place-items: center;
}
.f-3 .feature-icon {
  background: rgba(255, 255, 255, 0.1);
}
.feature-card h3 {
  margin-top: 28px;
  font-size: 18px;
  letter-spacing: -0.03em;
}
.feature-card p {
  margin-top: 8px;
  font-size: 11.5px;
  line-height: 1.8;
  color: #777063;
}
.f-3 p {
  color: rgba(255, 255, 255, 0.58);
}
.feature-no {
  position: absolute;
  right: 18px;
  top: 18px;
  font-size: 10px;
  color: #aaa292;
}
.f-3 .feature-no {
  color: rgba(255, 255, 255, 0.4);
}
.stack-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.stack-card {
  min-height: 180px;
  border: 1px solid #ddd6c7;
  border-radius: 20px;
  padding: 22px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  align-content: center;
  background: #fbfaf6;
}
.stack-card b {
  flex-basis: 100%;
  margin-bottom: 9px;
  font-size: 18px;
}
.stack-card span {
  border: 1px solid #d8d0c0;
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 10.5px;
  color: #6f685b;
}
.stack-card.dark {
  background: #191919;
  border-color: #191919;
  color: #fff;
}
.stack-card.dark span {
  border-color: rgba(255, 255, 255, 0.18);
  color: rgba(255, 255, 255, 0.68);
}
.team-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.preview-carousel {
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid #ddd6c7;
  background: #191919;
}
.preview-carousel :deep(.el-carousel__item) {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #191919;
}
.preview-carousel img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.preview-note {
  margin-top: 9px;
  font-size: 10px;
  color: #9d9688;
}
.team-card {
  display: flex;
  gap: 12px;
  align-items: center;
  border-top: 1px solid #ddd6c7;
  padding: 18px 4px;
}
.team-avatar {
  width: 43px;
  height: 43px;
  flex: none;
  border-radius: 50%;
  background: #191919;
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 15px;
}
.team-card h3 {
  font-size: 13px;
}
.team-card p {
  font-size: 10px;
  color: #928b7d;
  margin-top: 2px;
}
.team-card span {
  display: inline-block;
  margin-top: 4px;
  font-size: 10px;
  color: #5e584d;
}
.team-hint {
  margin-top: 10px;
  font-size: 10px;
  color: #a39b8d;
}
.about-contact {
  margin-top: 32px;
  padding: 30px 30px;
  border-radius: 22px;
  background: #191919;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}
.about-contact span {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.48);
  letter-spacing: 0.12em;
}
.about-contact h2 {
  margin-top: 6px;
  font-size: 25px;
  letter-spacing: -0.04em;
}
.about-contact .dark-pill {
  background: #dff07e;
  border-color: #dff07e;
  color: #171717;
  flex: none;
}
.about-footer {
  max-width: 1380px;
  margin: 0 auto;
  padding: 20px 38px 30px;
  display: flex;
  justify-content: space-between;
  gap: 14px;
  font-size: 10px;
  color: #9d9688;
  border-top: 1px solid #ddd6c7;
}
@media (max-width: 900px) {
  .about-nav,
  main,
  .about-footer {
    padding-left: 18px;
    padding-right: 18px;
  }
  .hero {
    grid-template-columns: 1fr;
    gap: 24px;
    min-height: 0;
    padding-top: 12px;
  }
  .hero-art {
    min-height: 340px;
  }
  .feature-grid,
  .team-grid {
    grid-template-columns: 1fr;
  }
  .feature-card {
    min-height: 190px;
  }
  .stack-grid {
    grid-template-columns: 1fr;
  }
  .about-contact {
    flex-direction: column;
    align-items: flex-start;
  }
  .about-footer {
    flex-direction: column;
  }
}
</style>



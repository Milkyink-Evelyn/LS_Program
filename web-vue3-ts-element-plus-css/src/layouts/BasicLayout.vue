<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, type Component } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import * as Icons from "@element-plus/icons-vue";
import { menus, isMenuGroup, type MenuGroup } from "~/config/menu";
import { demoAccounts } from "~/config/demoAccounts";
import { login } from "~/api/auth";
import { roleHome } from "~/router";
import { useAppStore } from "~/stores/app";
import type { Role } from "~/types";
import { roleText } from "~/utils/format";

const route = useRoute();
const router = useRouter();
const store = useAppStore();
const mobileOpen = ref(false);

const role = computed(() => store.user?.role ?? "PATIENT");
const menu = computed(() => menus[role.value]);
const openeds = computed(() =>
  menu.value
    .map((item, index) => (isMenuGroup(item) ? `group-${index}` : ""))
    .filter(Boolean)
);

function iconOf(name: string): Component {
  const icons = Icons as unknown as Record<string, Component>;
  return icons[name] || Icons.Menu;
}

function isGroup(item: unknown): item is MenuGroup {
  return isMenuGroup(item as MenuGroup);
}

function closeMobile() {
  mobileOpen.value = false;
}

async function switchAccount(target: Role) {
  if (store.user?.role === target) {
    ElMessage.info("当前已是该账号");
    return;
  }
  const account = demoAccounts.find((item) => item.role === target);
  if (!account) return;

  try {
    await ElMessageBox.confirm(
      "切换后当前登录状态将退出，是否继续？",
      "切换账号",
      { type: "warning", confirmButtonText: "切换", cancelButtonText: "取消" }
    );
  } catch {
    return;
  }

  try {
    const res = await login({
      username: account.username,
      password: account.password,
    });
    store.setLogin(res);
    router.replace(roleHome[store.user!.role]);
    ElMessage.success(`已切换为${roleText(res.role)}账号`);
  } catch {
    store.logout();
    router.replace("/login");
  }
}

async function logout() {
  try {
    await ElMessageBox.confirm("确定退出当前账号吗？", "退出登录", {
      type: "warning",
      confirmButtonText: "退出",
      cancelButtonText: "取消",
    });
    store.logout();
    router.replace("/login");
  } catch {
    /* 取消 */
  }
}

function onCommand(command: string) {
  if (command === "profile") router.push("/profile");
  else if (command === "about") router.push("/about");
  else if (command === "logout") logout();
}

function onKey(event: KeyboardEvent) {
  if (event.key === "Escape") mobileOpen.value = false;
}

onMounted(() => document.addEventListener("keydown", onKey));
onBeforeUnmount(() => document.removeEventListener("keydown", onKey));
</script>

<template>
  <div class="shell">
    <aside class="rail" :class="{ open: mobileOpen }">
      <router-link class="rail-brand" :to="roleHome[role]" @click="closeMobile">
        <img src="/sdu-logo.svg" alt="山东大学" />
        <div>
          <b>校医院</b>
          <span>预约挂号系统</span>
        </div>
      </router-link>

      <div class="rail-scroll">
        <el-menu
          class="rail-menu"
          :default-active="route.path"
          :default-openeds="openeds"
          background-color="transparent"
          text-color="rgba(255,255,255,.66)"
          active-text-color="#ffffff"
          router
        >
          <template v-for="(item, index) in menu" :key="index">
            <el-sub-menu v-if="isGroup(item)" :index="`group-${index}`">
              <template #title>
                <el-icon><component :is="iconOf(item.icon)" /></el-icon>
                <span>{{ item.title }}</span>
              </template>
              <el-menu-item
                v-for="child in item.children"
                :key="child.path"
                :index="child.path"
                @click="closeMobile"
              >
                {{ child.title }}
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="item.path" @click="closeMobile">
              <el-icon><component :is="iconOf(item.icon)" /></el-icon>
              <span>{{ item.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </div>

      <div class="rail-foot">
        <div class="rail-user">
          <img src="/user.png" alt="头像" />
          <div>
            <b>{{ store.user?.realName || store.user?.username }}</b>
            <span>{{ roleText(role) }}</span>
          </div>
        </div>
        <button class="rail-logout" title="退出登录" @click="logout">
          <el-icon><SwitchButton /></el-icon>
        </button>
      </div>
    </aside>

    <div class="scrim" :class="{ show: mobileOpen }" @click="closeMobile"></div>

    <div class="workspace">
      <header class="topbar">
        <button class="mobile-trigger" type="button" @click="mobileOpen = true">
          <el-icon><Menu /></el-icon>
        </button>

        <div class="top-crumb">
          <span class="system-name">校医院预约挂号系统</span>
          <span class="crumb-sep">/</span>
          <span class="crumb-current">{{ route.meta.title }}</span>
        </div>

        <div class="top-actions">
          <el-dropdown trigger="click" @command="switchAccount">
            <button class="switch-btn" type="button">
              <el-icon><Switch /></el-icon>
              <span>切换账号</span>
              <el-icon class="chev"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="item in demoAccounts"
                  :key="item.role"
                  :command="item.role"
                  :disabled="item.role === role"
                >
                  {{ item.label }} · {{ item.username }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-dropdown trigger="click" @command="onCommand">
            <button class="user-btn" type="button">
              <img src="/user.png" alt="头像" />
              <span class="user-copy">
                <b>{{ store.user?.realName || store.user?.username }}</b>
                <small>{{ roleText(role) }}</small>
              </span>
              <el-icon class="chev"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="about">关于项目</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="workspace-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>

      <footer class="workspace-foot">
        <span>© 2026 山东大学校医院预约挂号系统 · Web技术课程设计</span>
        <span>前端 Vue 3 + Element Plus · 后端 Spring Boot + MySQL</span>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  display: flex;
  background: var(--cream);
}

.rail {
  position: sticky;
  top: 14px;
  margin: 14px 0 14px 14px;
  width: 236px;
  height: calc(100vh - 28px);
  flex: none;
  border-radius: 24px;
  background: #181818;
  color: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 50;
}

.rail-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 24px 20px 23px;
  color: #fff;
}
.rail-brand img {
  width: 88px;
  height: 38px;
  object-fit: contain;
  object-position: left center;
  flex: none;
}
.rail-brand div {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
  min-width: 0;
}
.rail-brand b {
  font-size: 13.5px;
  font-weight: 600;
  letter-spacing: 0.06em;
}
.rail-brand span {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.48);
  letter-spacing: 0.12em;
  margin-top: 3px;
}

.rail-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0 10px;
}
.rail-scroll::-webkit-scrollbar {
  width: 0;
}

.rail-menu {
  border: 0;
  background: transparent;
}
.rail-menu:not(.el-menu--collapse) {
  width: 100%;
}
.rail-menu :deep(.el-menu-item),
.rail-menu :deep(.el-sub-menu__title) {
  height: 42px;
  line-height: 42px;
  border-radius: 12px;
  margin: 2px 0;
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.62);
  gap: 6px;
}
.rail-menu :deep(.el-menu-item:hover),
.rail-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.07);
  color: #fff;
}
.rail-menu :deep(.el-menu-item.is-active) {
  background: #3a3a3a;
  color: #fff;
  font-weight: 600;
}
.rail-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #fff;
}
.rail-menu :deep(.el-menu--inline) {
  padding-left: 0;
}
.rail-menu :deep(.el-menu--inline .el-menu-item) {
  min-width: 0;
  padding-left: 45px !important;
  height: 35px;
  line-height: 35px;
  font-size: 11.5px;
}
.rail-menu :deep(.el-sub-menu__icon-arrow) {
  color: rgba(255, 255, 255, 0.38);
}

.rail-foot {
  border-top: 1px solid rgba(255, 255, 255, 0.09);
  padding: 14px 12px 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.rail-user {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1;
}
.rail-user img {
  width: 33px;
  height: 33px;
  border-radius: 50%;
  object-fit: cover;
  background: #3a3a3a;
  flex: none;
}
.rail-user div {
  min-width: 0;
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}
.rail-user b {
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rail-user span {
  font-size: 9.5px;
  color: rgba(255, 255, 255, 0.44);
}
.rail-logout {
  border: 0;
  background: transparent;
  color: rgba(255, 255, 255, 0.52);
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  border-radius: 8px;
}
.rail-logout:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.workspace {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.topbar {
  min-height: 74px;
  padding: 0 32px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: sticky;
  top: 0;
  z-index: 30;
  background: rgba(242, 239, 230, 0.88);
  backdrop-filter: blur(16px);
}
.mobile-trigger {
  display: none;
  border: 0;
  background: var(--surface);
  color: var(--ink);
  width: 34px;
  height: 34px;
  border-radius: 10px;
  place-items: center;
}
.top-crumb {
  display: flex;
  align-items: center;
  gap: 9px;
  min-width: 0;
}
.system-name {
  font-size: 11.5px;
  color: var(--muted);
  white-space: nowrap;
}
.crumb-sep {
  color: #bcb5a7;
  font-size: 11px;
}
.crumb-current {
  font-size: 11.5px;
  color: var(--ink-soft);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.top-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}
.switch-btn,
.user-btn {
  border: 1px solid #dfd9cb;
  background: rgba(255, 253, 248, 0.78);
  color: var(--ink-soft);
  height: 36px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  font-size: 11px;
}
.switch-btn:hover,
.user-btn:hover {
  background: #fffdf8;
  border-color: #cfc7b7;
}
.user-btn {
  padding: 2px 10px 2px 3px;
  gap: 7px;
}
.user-btn img {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
  background: #ece6d8;
}
.user-copy {
  display: flex;
  flex-direction: column;
  text-align: left;
  line-height: 1.25;
  min-width: 0;
}
.user-copy b {
  font-size: 11px;
  font-weight: 600;
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user-copy small {
  font-size: 9px;
  color: var(--muted);
}
.chev {
  color: #a09a8e;
  font-size: 11px;
}

.workspace-main {
  flex: 1;
  width: 100%;
  max-width: 1560px;
  margin: 0 auto;
  padding: 22px 32px 36px;
}

.workspace-foot {
  border-top: 1px solid #e2ddd1;
  padding: 15px 32px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: #9d978b;
  font-size: 10px;
}

.scrim {
  display: none;
}

@media (max-width: 980px) {
  .rail {
    position: fixed;
    left: 0;
    top: 0;
    margin: 0;
    height: 100vh;
    border-radius: 0 22px 22px 0;
    transform: translateX(-102%);
    transition: transform 0.22s ease;
    box-shadow: 8px 0 32px rgba(0, 0, 0, 0.18);
  }
  .rail.open {
    transform: none;
  }
  .scrim {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(18, 18, 18, 0.4);
    z-index: 40;
    opacity: 0;
    pointer-events: none;
    transition: opacity 0.2s;
  }
  .scrim.show {
    opacity: 1;
    pointer-events: auto;
  }
  .mobile-trigger {
    display: grid;
  }
  .topbar {
    padding: 0 16px;
    min-height: 64px;
  }
  .system-name {
    display: none;
  }
  .crumb-sep {
    display: none;
  }
  .workspace-main {
    padding: 14px 16px 24px;
  }
  .workspace-foot {
    padding: 13px 16px;
    flex-direction: column;
    gap: 4px;
  }
}

@media (max-width: 560px) {
  .switch-btn span {
    display: none;
  }
  .switch-btn {
    padding: 0 10px;
  }
  .user-copy {
    display: none;
  }
  .user-btn {
    padding: 2px;
  }
}
</style>



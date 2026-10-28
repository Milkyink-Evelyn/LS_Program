<template>
    <el-container class="main-container">
        <el-container v-if="userInfo.username.length > 0">
            <el-header height="80" class="header-container">
                <div class="flex justify-between items-center h-full px-4">
                    <div>
                        <el-image style="height: 50px; margin-left: 10px;" src="/conlinkinfo.png" />
                    </div>
                    <div class="flex items-center gap-4">
                        <span class="username-text">{{ userInfo.username }}</span>
                        <el-dropdown class="role-dropdown">
                            <span class="el-dropdown-link">
                                角色切换
                                <el-icon class="el-icon--right">
                                    <arrow-down />
                                </el-icon>
                            </span>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item @click="changeUser('ROLE_ADMIN')">管理员</el-dropdown-item>
                                    <el-dropdown-item @click="changeUser('ROLE_STUDENT')">学生</el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                        <span class="database-text">数据库用户名：{{ systemConfig.dataBaseUserName }}</span>
                        <el-button type="danger" size="small" @click="logout">退出</el-button>
                    </div>
                </div>
            </el-header>
            <el-container class="body-container">
                <el-aside width="230px" class="sidebar-container">
                    <el-scrollbar class="scrollbar">
                        <el-menu background-color="#2c3039" text-color="#ffffff" active-text-color="#409eff" router
                            :default-openeds="['0']" class="sidebar-menu">
                            <template v-for="(v, i) in systemConfig.menuList" :key="i">
                                <template v-if="v.sList.length > 0">
                                    <el-sub-menu :index="i.toString()">
                                        <template v-slot:title>
                                            <i class="el-icon-location"></i>
                                            <span>{{ v.title }}</span>
                                        </template>
                                        <el-menu-item-group v-for="(item, j) in v.sList" :key="j">
                                            <el-menu-item :index="'/' + item.path" @click="routerPath(item.path)">{{
                                                item.title
                                            }}</el-menu-item>
                                        </el-menu-item-group>
                                    </el-sub-menu>
                                </template>
                                <template v-else>
                                    <el-menu-item :index="'/' + v.path" @click="routerPath(v.path)">
                                        <i class="el-icon-menu"></i>
                                        <span>{{ v.title }}</span>
                                    </el-menu-item>
                                </template>
                            </template>
                        </el-menu>
                    </el-scrollbar>
                </el-aside>
                <el-main class="main-content">
                    <router-view />
                </el-main>
            </el-container>
        </el-container>
        <el-main v-else>
            <router-view />
        </el-main>
    </el-container>
</template>

<script lang="ts" setup name="Navi">
import { container } from '~/inverfiy.config';
import { ID_APP_PRESENTER } from '~/types';
import { AppPresenter } from "~/infrastructure/presenters/app-presenter";
import { ref, onMounted, onUnmounted } from 'vue';
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';
import { useAppStore } from "~/stores/app";
const appPresenter = container.get<AppPresenter>(ID_APP_PRESENTER);

// 组件状态
const isCollapse = ref(false);
const timer = ref(null);

// 获取store和router
const appStore = useAppStore();
const router = useRouter();

// 使用storeToRefs从pinia store获取响应式状态
const { systemConfig, userInfo } = storeToRefs(appStore);

// 生命周期钩子
onMounted(async () => {
    logout();
});

onUnmounted(() => {
    if (timer.value) {
        clearInterval(timer.value);
    }
});

// 方法
const logout = () => {
    appStore.logout();
    router.push({ path: "/Login" });
};



const routerPath = (path: string) => {
    console.log("routerPath", path);
    if (path == null || path == undefined || path == "") {
        return;
    }
    router.push({ path: "/" + path });
};

const changeUser = async (type: string) => {
    console.log("type", type);
    if (type == appStore.userInfo.role) {
        return;
    }
    let loginReq = {
        username: '',
        password: '',
        code: ''
    };
    if (type === 'ROLE_ADMIN') {
        loginReq.username = 'admin';
        loginReq.password = '123456';
    } else if (type === 'ROLE_STUDENT') {
        loginReq.username = '2022030001';
        loginReq.password = '123456';
    }
    console.log("login", loginReq);
    await appPresenter.enterApp(loginReq);
};
</script>

<style scoped>
.main-container {
    height: 100vh;
    overflow: hidden;
}

/* Header 样式 */
.header-container {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-bottom: 3px solid #e0e6ed;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    z-index: 1000;
}

.username-text {
    color: #ffffff;
    font-weight: 600;
    font-size: 14px;
}

.database-text {
    color: #ffffff;
    font-size: 12px;
    opacity: 0.9;
}

.role-dropdown {
    color: #ffffff;
}

.role-dropdown .el-dropdown-link {
    color: #ffffff;
    cursor: pointer;
    font-size: 14px;
    transition: color 0.3s ease;
}

.role-dropdown .el-dropdown-link:hover {
    color: #f0f0f0;
}

/* 主体容器 */
.body-container {
    height: calc(100vh - 80px);
    overflow: hidden;
}

/* 侧边栏样式 */
.sidebar-container {
    background-color: #2c3039;
    border-right: 3px solid #e0e6ed;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
    height: 100%;
    overflow: hidden;
}

.scrollbar {
    height: 100%;
    background-color: #2c3039;
}

.sidebar-menu {
    border: none;
    height: 100%;
}

.sidebar-menu :deep(.el-sub-menu__title) {
    padding-left: 20px !important;
    height: 50px;
    line-height: 50px;
    border-bottom: 1px solid #34383f;
}

.sidebar-menu :deep(.el-menu-item) {
    padding-left: 40px !important;
    height: 45px;
    line-height: 45px;
    border-bottom: 1px solid #34383f;
    transition: all 0.3s ease;
}

.sidebar-menu :deep(.el-menu-item:hover) {
    background-color: #409eff !important;
    color: #ffffff !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
    background-color: #409eff !important;
    color: #ffffff !important;
    border-right: 3px solid #67c23a;
}

/* 主内容区域 */
.main-content {
    background-color: #f5f7fa;
    border-left: 1px solid #e0e6ed;
    padding: 0;
    height: calc(100vh - 80px);
    overflow-y: auto;
    overflow-x: hidden;
    display: block;
}

.tags-container {
    background-color: #ffffff;
    padding: 10px 15px;
    border-bottom: 2px solid #e0e6ed;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    align-items: center;
    min-height: 50px;
}

.tags-container :deep(.el-tag) {
    cursor: pointer;
    transition: all 0.3s ease;
    border-radius: 20px;
    font-weight: 500;
}

.tags-container :deep(.el-tag:hover) {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .sidebar-container {
        width: 200px !important;
    }

    .header-container .flex {
        padding: 0 10px;
    }

    .username-text,
    .database-text {
        display: none;
    }
}
</style>

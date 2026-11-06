<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import PageHeader from "~/components/PageHeader.vue";
import {
  changePassword,
  getMe,
  updateProfile,
} from "~/api/auth";
import { useAppStore } from "~/stores/app";
import { roleText } from "~/utils/format";

const router = useRouter();
const store = useAppStore();
const loading = ref(false);
const savingProfile = ref(false);
const savingPassword = ref(false);
const profileRef = ref<FormInstance>();
const passwordRef = ref<FormInstance>();
const profile = reactive({ realName: "", email: "" });
const password = reactive({ oldPassword: "", newPassword: "", confirm: "" });

const profileRules: FormRules = {
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  email: [{ type: "email", message: "邮箱格式不正确", trigger: "blur" }],
};
const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码至少 6 位", trigger: "blur" },
  ],
  confirm: [
    {
      validator: (_rule, value, callback) => {
        if (!value) callback(new Error("请再次输入新密码"));
        else if (value !== password.newPassword) callback(new Error("两次密码不一致"));
        else callback();
      },
      trigger: "blur",
    },
  ],
};

async function load() {
  loading.value = true;
  try {
    const me = await getMe();
    profile.realName = me.realName;
    profile.email = me.email || "";
    store.updateUser({ realName: me.realName, email: me.email });
  } finally {
    loading.value = false;
  }
}

async function saveProfile() {
  if (!profileRef.value) return;
  const valid = await profileRef.value.validate().catch(() => false);
  if (!valid) return;
  savingProfile.value = true;
  try {
    await updateProfile({
      realName: profile.realName.trim(),
      email: profile.email.trim() || undefined,
    });
    store.updateUser({
      realName: profile.realName.trim(),
      email: profile.email.trim() || null,
    });
    ElMessage.success("资料已更新");
  } finally {
    savingProfile.value = false;
  }
}

async function savePassword() {
  if (!passwordRef.value) return;
  const valid = await passwordRef.value.validate().catch(() => false);
  if (!valid) return;
  savingPassword.value = true;
  try {
    await changePassword({
      oldPassword: password.oldPassword,
      newPassword: password.newPassword,
    });
    ElMessage.success("密码修改成功，请重新登录");
    store.logout();
    router.replace("/login");
  } finally {
    savingPassword.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="view">
    <PageHeader
      eyebrow="ACCOUNT"
      title="个人中心"
      description="维护真实姓名和邮箱；修改密码后需要重新登录。"
    >
      <el-button :loading="loading" @click="load">同步资料</el-button>
    </PageHeader>

    <div class="profile-grid" v-loading="loading">
      <section class="panel">
        <div class="panel-title">
          <div><h3>基本资料</h3><div class="sub">修改后 Header 立即更新</div></div>
        </div>
        <div class="profile-id">
          <img src="/user.png" alt="头像" />
          <div>
            <b>{{ store.user?.realName || store.user?.username }}</b>
            <span>{{ store.user?.username }} · {{ roleText(store.user?.role || "") }}</span>
          </div>
        </div>
        <el-form ref="profileRef" :model="profile" :rules="profileRules" label-position="top">
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="profile.realName" maxlength="30" />
          </el-form-item>
          <el-form-item label="邮箱（可选）" prop="email">
            <el-input v-model="profile.email" type="email" />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input :model-value="store.user?.username" disabled />
          </el-form-item>
          <el-form-item label="角色">
            <el-input :model-value="roleText(store.user?.role || '')" disabled />
          </el-form-item>
          <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存资料</el-button>
        </el-form>
      </section>

      <section class="panel">
        <div class="panel-title">
          <div><h3>修改密码</h3><div class="sub">修改成功后会自动退出登录</div></div>
        </div>
        <el-form ref="passwordRef" :model="password" :rules="passwordRules" label-position="top">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="password.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="password.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirm">
            <el-input v-model="password.confirm" type="password" show-password />
          </el-form-item>
          <el-button type="primary" :loading="savingPassword" @click="savePassword">修改密码</el-button>
        </el-form>
      </section>
    </div>
  </div>
</template>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-items: start;
}
.profile-id {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px;
  background: #f4f1e8;
  border-radius: 13px;
  margin-bottom: 18px;
}
.profile-id img {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  object-fit: cover;
  background: #e4ddce;
}
.profile-id b {
  display: block;
  font-size: 14px;
}
.profile-id span {
  display: block;
  font-size: 10.5px;
  color: var(--muted);
  margin-top: 3px;
}
@media (max-width: 850px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>

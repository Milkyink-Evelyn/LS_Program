<template>
    <div class="password-container">
        <div class="password-card">
            <!-- 页面标题 -->
            <div class="password-header">
                <div class="header-icon">
                    <el-icon>
                        <Lock />
                    </el-icon>
                </div>
                <h2 class="header-title">修改密码</h2>
            </div>

            <!-- 密码要求提示 -->
            <div class="password-notice">
                <el-alert title="密码安全要求" description="密码需包含三种字符类型（大小写字母、数字、特殊字符）及以上，且密码长度为8-20位" type="info"
                    :closable="false" show-icon>
                </el-alert>
            </div>

            <!-- 密码修改表单 -->
            <el-form :model="data" :rules="rules" ref="passwordForm" label-width="100px" class="password-form"
                size="large">

                <el-form-item label="旧密码" prop="oldPassword">
                    <el-input v-model="data.oldPassword" type="password" placeholder="请输入当前密码" show-password clearable
                        prefix-icon="Lock">
                    </el-input>
                </el-form-item>

                <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="data.newPassword" type="password" placeholder="请输入新密码" show-password clearable
                        prefix-icon="Key" @input="checkPasswordStrength">
                    </el-input>
                    <div class="password-strength" v-if="data.newPassword">
                        <span class="strength-label">密码强度：</span>
                        <div class="strength-bar">
                            <div class="strength-fill" :class="strengthClass" :style="{ width: strengthPercent + '%' }">
                            </div>
                        </div>
                        <span class="strength-text" :class="strengthClass">{{ strengthText }}</span>
                    </div>
                </el-form-item>

                <el-form-item label="确认密码" prop="checkPassword">
                    <el-input v-model="data.checkPassword" type="password" placeholder="请再次输入新密码" show-password
                        clearable prefix-icon="CircleCheck">
                    </el-input>
                </el-form-item>

                <el-form-item class="form-buttons">
                    <el-button type="primary" size="large" @click="submit()" :loading="loading" class="submit-btn">
                        <el-icon>
                            <Check />
                        </el-icon>
                        确认修改
                    </el-button>
                    <el-button size="large" @click="reset()" class="reset-btn">
                        <el-icon>
                            <RefreshLeft />
                        </el-icon>
                        重置
                    </el-button>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>
<script lang="ts" setup name="Password">
import { Lock, Key, CircleCheck, Check, RefreshLeft } from '@element-plus/icons-vue';
import type { PasswordData } from "~/domain/models/info";
import { container } from '~/inverfiy.config';
import { ID_PASSWORD_PRESENTER } from '~/types';
import { PasswordPresenter } from "~/domain/presenters/password-presenter";
import { ref, computed } from "vue";
import type { FormInstance, FormRules } from 'element-plus';

const presenter = container.get<PasswordPresenter>(ID_PASSWORD_PRESENTER);
const passwordForm = ref<FormInstance>();
const loading = ref(false);

let data = ref<PasswordData>({} as PasswordData);

// 表单验证规则
const rules = ref<FormRules>({
    oldPassword: [
        { required: true, message: '请输入旧密码', trigger: 'blur' }
    ],
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 8, max: 20, message: '密码长度应为8-20位', trigger: 'blur' },
        {
            validator: (rule: any, value: string, callback: Function) => {
                if (!value) {
                    callback();
                    return;
                }
                const hasUpper = /[A-Z]/.test(value);
                const hasLower = /[a-z]/.test(value);
                const hasNumber = /\d/.test(value);
                const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(value);

                const typeCount = [hasUpper, hasLower, hasNumber, hasSpecial].filter(Boolean).length;

                if (typeCount < 3) {
                    callback(new Error('密码需包含至少三种字符类型（大小写字母、数字、特殊字符）'));
                } else {
                    callback();
                }
            },
            trigger: 'blur'
        }
    ],
    checkPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
            validator: (rule: any, value: string, callback: Function) => {
                if (value !== data.value.newPassword) {
                    callback(new Error('两次输入的密码不一致'));
                } else {
                    callback();
                }
            },
            trigger: 'blur'
        }
    ]
});

// 密码强度检测
const strengthPercent = ref(0);
const strengthText = ref('');
const strengthClass = ref('');

const checkPasswordStrength = () => {
    const password = data.value.newPassword;
    if (!password) {
        strengthPercent.value = 0;
        strengthText.value = '';
        strengthClass.value = '';
        return;
    }

    let score = 0;
    const checks = {
        length: password.length >= 8,
        hasUpper: /[A-Z]/.test(password),
        hasLower: /[a-z]/.test(password),
        hasNumber: /\d/.test(password),
        hasSpecial: /[!@#$%^&*(),.?":{}|<>]/.test(password)
    };

    // 基础分数
    if (checks.length) score += 20;
    if (checks.hasUpper) score += 20;
    if (checks.hasLower) score += 20;
    if (checks.hasNumber) score += 20;
    if (checks.hasSpecial) score += 20;

    // 长度加分
    if (password.length >= 12) score += 10;
    if (password.length >= 16) score += 10;

    strengthPercent.value = Math.min(score, 100);

    if (score < 40) {
        strengthText.value = '弱';
        strengthClass.value = 'weak';
    } else if (score < 70) {
        strengthText.value = '中等';
        strengthClass.value = 'medium';
    } else {
        strengthText.value = '强';
        strengthClass.value = 'strong';
    }
};

const submit = async () => {
    if (!passwordForm.value) return;

    try {
        const isValid = await passwordForm.value.validate();
        if (isValid) {
            loading.value = true;
            await presenter.submit(data.value);
        }
    } catch (error) {
        console.error('表单验证失败:', error);
    } finally {
        loading.value = false;
    }
};

const reset = async () => {
    if (passwordForm.value) {
        passwordForm.value.resetFields();
    }
    data.value = {} as PasswordData;
    strengthPercent.value = 0;
    strengthText.value = '';
    strengthClass.value = '';
    await presenter.reset(data.value);
};
</script>

<style lang="scss" scoped>
/* 主容器样式 */
.password-container {
    min-height: auto;
    padding: 20px;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    display: flex;
    justify-content: center;
    align-items: flex-start;
    padding-top: 40px;
    overflow: visible;
}

/* 密码修改卡片 */
.password-card {
    width: 100%;
    max-width: 600px;
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.15);
    border: 1px solid rgba(102, 126, 234, 0.1);
    overflow: hidden;
    transition: all 0.3s ease;

    &:hover {
        box-shadow: 0 12px 40px rgba(102, 126, 234, 0.2);
        transform: translateY(-2px);
    }
}

/* 页面头部 */
.password-header {
    padding: 30px 30px 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    display: flex;
    align-items: center;
    gap: 16px;
}

.header-icon {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    backdrop-filter: blur(10px);
    border: 2px solid rgba(255, 255, 255, 0.3);
}

.header-title {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 密码要求提示 */
.password-notice {
    padding: 10px 30px 0;

    :deep(.el-alert) {
        border-radius: 8px;
        border: none;
        background: #f0f9ff;

        .el-alert__icon {
            color: #409eff;
        }

        .el-alert__title {
            color: #409eff;
            font-weight: 600;
        }

        .el-alert__description {
            color: #666;
            line-height: 1.6;
        }
    }
}

/* 表单样式 */
.password-form {
    padding: 20px 30px 30px;

    :deep(.el-form-item__label) {
        color: #2c3e50;
        font-weight: 600;
        font-size: 14px;
    }

    :deep(.el-input) {
        border-radius: 8px;

        .el-input__wrapper {
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
            transition: all 0.3s ease;

            &:hover {
                box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
            }

            &.is-focus {
                box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
            }
        }

        .el-input__prefix {
            color: #667eea;
        }
    }

    .el-form-item {
        margin-bottom: 24px;
    }
}

/* 密码强度指示器 */
.password-strength {
    margin-top: 8px;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;
}

.strength-label {
    color: #666;
    white-space: nowrap;
}

.strength-bar {
    flex: 1;
    height: 6px;
    background: #f0f2f5;
    border-radius: 3px;
    overflow: hidden;
    min-width: 100px;
}

.strength-fill {
    height: 100%;
    transition: all 0.3s ease;
    border-radius: 3px;

    &.weak {
        background: linear-gradient(90deg, #ff6b6b, #ee5a52);
    }

    &.medium {
        background: linear-gradient(90deg, #feca57, #ff9ff3);
    }

    &.strong {
        background: linear-gradient(90deg, #48dbfb, #0abde3);
    }
}

.strength-text {
    font-weight: 600;
    white-space: nowrap;

    &.weak {
        color: #ff6b6b;
    }

    &.medium {
        color: #feca57;
    }

    &.strong {
        color: #0abde3;
    }
}

/* 按钮组 */
.form-buttons {
    margin-top: 30px;
    text-align: center;

    .el-form-item__content {
        display: flex;
        justify-content: center;
        gap: 16px;
    }
}

.submit-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 25px;
    padding: 12px 30px;
    font-weight: 600;
    font-size: 16px;
    min-width: 140px;
    transition: all 0.3s ease;

    &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
    }

    &:active {
        transform: translateY(0);
    }
}

.reset-btn {
    border: 2px solid #667eea;
    color: #667eea;
    background: transparent;
    border-radius: 25px;
    padding: 10px 30px;
    font-weight: 600;
    font-size: 16px;
    min-width: 140px;
    transition: all 0.3s ease;

    &:hover {
        background: #667eea;
        color: white;
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
    }

    &:active {
        transform: translateY(0);
    }
}

/* 响应式设计 */
@media (max-width: 768px) {
    .password-container {
        padding: 10px;
        padding-top: 20px;
        min-height: auto;
    }

    .password-card {
        max-width: 100%;
        margin: 0;
    }

    .password-header,
    .password-form {
        padding: 15px 20px 20px;
    }

    .header-title {
        font-size: 20px;
    }

    .header-icon {
        width: 40px;
        height: 40px;
        font-size: 20px;
    }

    .form-buttons .el-form-item__content {
        flex-direction: column;
        align-items: center;
        gap: 12px;
    }

    .submit-btn,
    .reset-btn {
        width: 100%;
        max-width: 200px;
    }

    .strength-bar {
        min-width: 80px;
    }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
    .password-card {
        background: #2c3e50;
        border-color: rgba(255, 255, 255, 0.1);

        :deep(.el-form-item__label) {
            color: #ecf0f1;
        }
    }

    .password-notice :deep(.el-alert) {
        background: rgba(64, 158, 255, 0.1);
        color: #ecf0f1;
    }
}

/* 表单验证错误样式优化 */
:deep(.el-form-item.is-error) {
    .el-input__wrapper {
        box-shadow: 0 0 0 1px #f56c6c inset, 0 2px 8px rgba(245, 108, 108, 0.2);
    }

    .el-form-item__error {
        color: #f56c6c;
        font-size: 12px;
        margin-top: 4px;
        font-weight: 500;
    }
}

/* 加载状态按钮样式 */
:deep(.el-button.is-loading) {
    pointer-events: none;

    .el-icon {
        animation: rotating 2s linear infinite;
    }
}

@keyframes rotating {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}
</style>

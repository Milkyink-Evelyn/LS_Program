<template>
    <div class="main-view-container">
        <!-- 成绩编辑对话框 -->
        <el-dialog v-model="editVisible" :close-on-click-modal="false" width="50%" class="custom-dialog"
            destroy-on-close>
            <template #header>
                <div class="dialog-header">
                    <div class="header-icon">
                        <el-icon>
                            <TrendCharts />
                        </el-icon>
                    </div>
                    <h3 class="dialog-title">成绩信息编辑</h3>
                </div>
            </template>

            <div class="edit-form-container">
                <el-form ref="form" :model="itemData" class="base-table-edit-form" label-width="80px">
                    <el-form-item label="学生">
                        <el-select v-model="itemData.personId" placeholder="请选择学生" class="form-select">
                            <el-option v-for="item in data.studentList" :key="item.id" :value="item.id"
                                :label="item.title" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="课程">
                        <el-select v-model="itemData.courseId" placeholder="请选择课程" class="form-select">
                            <el-option v-for="item in data.courseList" :key="item.id" :value="item.id"
                                :label="item.title" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="成绩">
                        <el-input v-model="itemData.mark" placeholder="请输入成绩（0-100）" class="form-input" type="number" />
                    </el-form-item>
                </el-form>
            </div>

            <template #footer>
                <div class="dialog-footer">
                    <el-button type="success" @click="itemSubmit()" class="action-btn success-btn">
                        <el-icon>
                            <Check />
                        </el-icon>
                        提交
                    </el-button>
                    <el-button type="info" plain @click="editVisible = false" class="action-btn cancel-btn">
                        <el-icon>
                            <Close />
                        </el-icon>
                        取消
                    </el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 主要内容区域 -->
        <div class="main-content">
            <!-- 页面头部 -->
            <div class="page-header">
                <div class="header-content">
                    <div class="header-icon">
                        <el-icon>
                            <TrendCharts />
                        </el-icon>
                    </div>
                    <h2 class="page-title">成绩管理</h2>
                </div>
            </div>

            <!-- 操作栏 -->
            <div class="action-bar">
                <div class="action-left">
                    <el-button type="success" @click="addItem()" class="action-btn add-btn">
                        <el-icon>
                            <Plus />
                        </el-icon>
                        添加成绩
                    </el-button>
                </div>
                <div class="action-right">
                    <div class="search-group">
                        <div class="filter-item">
                            <span class="filter-label">学生</span>
                            <el-select v-model="data.personId" placeholder="请选择学生" class="filter-select">
                                <el-option v-for="item in data.studentList" :key="item.id" :value="item.id"
                                    :label="item.title" />
                            </el-select>
                        </div>
                        <div class="filter-item">
                            <span class="filter-label">课程</span>
                            <el-select v-model="data.courseId" placeholder="请选择课程" class="filter-select">
                                <el-option v-for="item in data.courseList" :key="item.id" :value="item.id"
                                    :label="item.title" />
                            </el-select>
                        </div>
                        <el-button type="primary" @click="doQuery()" class="search-btn">
                            <el-icon>
                                <Search />
                            </el-icon>
                            查询
                        </el-button>
                    </div>
                </div>
            </div>

            <!-- 数据表格 -->
            <div class="table-container">
                <el-table :data="data.dataList" stripe class="base-table-table">
                    <el-table-column label="序号" width="70">
                        <template v-slot="scope">
                            <div class="sequence-number">
                                {{ scope.$index + 1 }}
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="180">
                        <template v-slot="scope">
                            <div class="table-actions">
                                <el-button type="primary" size="small" @click="editItem(scope.$index)"
                                    class="table-btn">
                                    <el-icon>
                                        <Edit />
                                    </el-icon>
                                    编辑
                                </el-button>
                                <el-button type="danger" size="small" @click="deleteItem(scope.$index)"
                                    class="table-btn delete-btn">
                                    <el-icon>
                                        <Delete />
                                    </el-icon>
                                    删除
                                </el-button>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="studentNum" label="学号" width="140" />
                    <el-table-column prop="studentName" label="姓名" width="100" />
                    <el-table-column prop="className" label="班级" width="200" show-overflow-tooltip />
                    <el-table-column prop="courseNum" label="课程号" width="140" />
                    <el-table-column prop="courseName" label="课程名" width="200" show-overflow-tooltip />
                    <el-table-column label="学分" width="100">
                        <template v-slot="scope">
                            <el-tag type="info" size="small">{{ scope.row.credit }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="成绩" width="100">
                        <template v-slot="scope">
                            <el-tag :type="getScoreType(scope.row.mark)" size="small" class="score-tag">
                                {{ scope.row.mark }}
                            </el-tag>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup name="Score">
import type { ScoreData, ScoreItem } from "~/domain/models/teaching";
import { container } from '~/inverfiy.config';
import { ID_SCORE_PRESENTER } from '~/types';
import { ScorePresenter } from "~/domain/presenters/score-presenter";
import { ref } from "vue";
import {
    TrendCharts, Edit, Delete, Plus, Search,
    Check, Close
} from '@element-plus/icons-vue'

const presenter = container.get<ScorePresenter>(ID_SCORE_PRESENTER);
let data = ref<ScoreData>({} as ScoreData);
let itemData = ref<ScoreItem>({} as ScoreItem);
let editVisible = ref(false);

presenter.scoreInit().then((res) => {
    data.value = res;
});

// 根据成绩返回对应的标签类型
const getScoreType = (mark: number): 'success' | 'warning' | 'info' | 'danger' => {
    if (mark >= 90) return 'success';
    if (mark >= 80) return 'warning';
    if (mark >= 60) return 'info';
    return 'danger';
};

const doQuery = async () => {
    await presenter.getScoreList(data.value);
};

const addItem = async () => {
    itemData.value = presenter.addItem(data.value);
    editVisible.value = true;
};

const editItem = async (index: number) => {
    itemData.value = presenter.editItem(data.value, index);
    editVisible.value = true;
};

const itemSubmit = async () => {
    await presenter.itemSubmit(itemData.value, data.value);
    editVisible.value = false;
};

const deleteItem = async (index: number) => {
    await presenter.deleteItem(data.value, index);
};
</script>

<style lang="scss" scoped>
/* 成绩标签样式 */
.score-tag {
    font-weight: 600;
    font-size: 12px;
    padding: 4px 8px;
    border-radius: 6px;
}
</style>
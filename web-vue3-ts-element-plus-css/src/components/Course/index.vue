<template>
    <div class="main-view-container">
        <!-- 课程编辑对话框 -->
        <el-dialog v-model="editVisible" :close-on-click-modal="false" width="60%" class="custom-dialog"
            destroy-on-close>
            <template #header>
                <div class="dialog-header">
                    <div class="header-icon">
                        <el-icon>
                            <Document />
                        </el-icon>
                    </div>
                    <h3 class="dialog-title">课程信息编辑</h3>
                </div>
            </template>

            <div class="edit-form-container">
                <el-form ref="form" :model="itemData" class="base-table-edit-form" label-width="100px">
                    <el-form-item label="课程号">
                        <el-input v-model="itemData.num" placeholder="请输入课程号" class="form-input" />
                    </el-form-item>
                    <el-form-item label="课程名">
                        <el-input v-model="itemData.name" placeholder="请输入课程名" class="form-input" />
                    </el-form-item>
                    <el-form-item label="学分">
                        <el-input v-model="itemData.credit" placeholder="请输入学分" class="form-input" />
                    </el-form-item>
                    <el-form-item label="资料路径">
                        <el-input v-model="itemData.coursePath" placeholder="请输入资料路径" class="form-input" />
                    </el-form-item>
                    <el-form-item label="前序课">
                        <el-select v-model="itemData.preCourseId" placeholder="请选择前序课程" class="form-select">
                            <el-option v-for="item in data.courseSelectList" :key="item.id" :value="item.id"
                                :label="item.title" />
                        </el-select>
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
                            <Document />
                        </el-icon>
                    </div>
                    <h2 class="page-title">课程管理</h2>
                </div>
            </div>

            <!-- 操作栏 -->
            <div class="action-bar">
                <div class="action-left">
                    <el-button type="success" @click="addItem()" class="action-btn add-btn">
                        <el-icon>
                            <Plus />
                        </el-icon>
                        添加课程
                    </el-button>
                </div>
                <div class="action-right">
                    <div class="search-group">
                        <el-input v-model="data.numName" placeholder="请输入课程号或课程名" class="search-input">
                            <template #prepend>课程号或课程名</template>
                        </el-input>
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
                    <el-table-column prop="num" label="课程号" width="140" />
                    <el-table-column prop="name" label="课程名" width="200" show-overflow-tooltip />
                    <el-table-column label="学分" width="100">
                        <template v-slot="scope">
                            <el-tag type="info" size="small">{{ scope.row.credit }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="coursePath" label="材料路径" width="280" show-overflow-tooltip />
                    <el-table-column prop="preCourse" label="前序课" width="200" show-overflow-tooltip />
                </el-table>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup name="Course">
import type { CourseData, CourseItem } from "~/domain/models/teaching";
import { container } from '~/inverfiy.config';
import { ID_COURSE_PRESENTER } from '~/types';
import { CoursePresenter } from "~/domain/presenters/course-presenter";
import { ref } from "vue";
import {
    Document, Edit, Delete, Plus, Search,
    Check, Close
} from '@element-plus/icons-vue'
const presenter = container.get<CoursePresenter>(ID_COURSE_PRESENTER);
let data = ref<CourseData>({} as CourseData);
let itemData = ref<CourseItem>({} as CourseItem);
let editVisible = ref(false);
presenter.courseInit().then((res) => {
    data.value = res;
});
const doQuery = async () => {
    await presenter.getCourseList(data.value);
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

<style lang="scss" scoped></style>
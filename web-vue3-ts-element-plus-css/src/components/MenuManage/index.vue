<template>
    <el-dialog :title="dialogTitle" v-model="editVisible" :close-on-click-modal="false">
        <el-form ref="form" :model="editedNode" class="edit-form" label-width="120px">
            <el-form-item label="父节点">
                <el-input v-model="editedNode.parentTitle" readonly />
            </el-form-item>
            <el-form-item label="主键ID">
                <el-input v-model="editedNode.id" placeholder="请输入主键ID" :readonly="data.readonly" />
            </el-form-item>
            <el-form-item label="菜单名称">
                <el-input v-model="editedNode.id" placeholder="请输入菜单名称" />
            </el-form-item>
            <el-form-item label="菜单标题">
                <el-input v-model="editedNode.value" placeholder="请输入菜单标题" />
            </el-form-item>
            <el-form-item label="角色">
                <el-checkbox v-model="data.roleAdmin" label="管理员" />
                <el-checkbox v-model="data.roleStudent" label="学生" />
                <el-checkbox v-model="data.roleTeacher" label="教师" />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button type="primary" plain @click="confirm()">提交</el-button>
                <el-button type="primary" plain @click="editVisible = false">取消</el-button>
            </span>
        </template>
    </el-dialog>
    <div class="main-view-container">
        <div>
            <el-row :gutter="20" style="margin-bottom: 10px;">
                <el-col :span="12" style="text-align: left;">
                    <el-button type="danger" plain @click="addItemFirst()">添加一级菜单</el-button>
                </el-col>
            </el-row>
        </div>
        <div class="custom-tree-container" v-if="data.nodes !== null">
            <el-tree :data="data.nodes" show-checkbox node-key="id" default-expand-all :expand-on-click-node="false">
                <template #default="{ node, data }">
                    <span class="custom-tree-node">
                        <span>{{ node.label }}</span>
                        <span>
                            <a @click="addItem(data)"> 添加 </a>
                            <a style="margin-left: 5px" @click="editItem(data)"> 修改 </a>
                            <a style="margin-left: 5px" @click="deleteItem(data)"> 删除 </a>
                        </span>
                    </span>
                </template>
            </el-tree>
        </div>
    </div>
</template>
<script lang="ts" setup name="MenuManage">
import type { MenuManageData, TreeNode } from "~/domain/models/system";
import { container } from '~/inverfiy.config';
import { ID_MENU_MANAGE_PRESENTER } from '~/types';
import { MenuManagePresenter } from "~/domain/presenters/menu-manage-presenter";
import { ref } from "vue";
const presenter = container.get<MenuManagePresenter>(ID_MENU_MANAGE_PRESENTER);
let data = ref<MenuManageData>({} as MenuManageData);
let editedNode = ref<TreeNode>({} as TreeNode);
let editVisible = ref(false);
let dialogTitle = ref("添加字典名对话框");
presenter.menuManageInit().then((res) => {
    data.value = res;
});
const addItemFirst = async () => {
    editedNode.value = {} as TreeNode;
    editedNode.value.pid = null;
    editedNode.value.parentTitle = '';
    data.value.roleAdmin = false;
    data.value.roleStudent = false;
    data.value.roleTeacher = false;
    data.value.readonly = false;
    data.value.editType = 0;
    dialogTitle.value = '添加一级菜单对话框';
    editVisible.value = true;
};
const addItem = async (node: TreeNode) => {
    editedNode.value = {} as TreeNode;
    data.value.currentNode = node
    editedNode.value.pid = data.value.currentNode.id
    editedNode.value.parentTitle = data.value.currentNode.title
    data.value.roleAdmin = false;
    data.value.roleStudent = false;
    data.value.roleTeacher = false;
    data.value.readonly = false;
    data.value.editType = 1;
    dialogTitle.value = "添加字典项对话框";
    editVisible.value = true;
};
const editItem = async (node: TreeNode) => {
    data.value.currentNode = node;
    editedNode.value = data.value.currentNode;
    data.value.roleAdmin = false
    data.value.roleStudent = false
    data.value.roleTeacher = false
    if (
        data.value.currentNode.userTypeIds != null &&
        data.value.currentNode.userTypeIds != undefined &&
        data.value.currentNode.userTypeIds != ''
    ) {
        if (data.value.currentNode.userTypeIds.indexOf('1') >= 0) {
            data.value.roleAdmin = true
        } else {
            data.value.roleAdmin = false
        }
        if (data.value.currentNode.userTypeIds.indexOf('2') >= 0) {
            data.value.roleStudent = true
        } else {
            data.value.roleStudent = false
        }
        if (data.value.currentNode.userTypeIds.indexOf('3') >= 0) {
            data.value.roleTeacher = true
        } else {
            data.value.roleTeacher = false
        }
    }
    dialogTitle.value = "修改菜单对话框";
    editVisible.value = true;
};
const confirm = async () => {
    await presenter.confirm(data.value, editedNode.value,);
    editVisible.value = false;
};
const deleteItem = async (node: TreeNode) => {
    await presenter.deleteItem(data.value, node);
};
</script>

<style lang="scss" scoped></style>
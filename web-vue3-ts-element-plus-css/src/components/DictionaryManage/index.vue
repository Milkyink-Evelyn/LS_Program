<template>
    <div class="main-view-container">
        <!-- 字典编辑对话框 -->
        <el-dialog v-model="editVisible" :close-on-click-modal="false" width="50%" class="custom-dialog"
            destroy-on-close>
            <template #header>
                <div class="dialog-header">
                    <div class="header-icon">
                        <el-icon>
                            <Setting />
                        </el-icon>
                    </div>
                    <h3 class="dialog-title">{{ dialogTitle }}</h3>
                </div>
            </template>

            <div class="edit-form-container">
                <el-form ref="form" :model="editedNode" class="dictionary-edit-form" label-width="100px">
                    <el-form-item label="字典名">
                        <el-input v-model="editedNode.parentTitle" readonly class="readonly-input" />
                    </el-form-item>
                    <el-form-item label="数据值">
                        <el-input v-model="editedNode.value" placeholder="请输入数据值" class="form-input" />
                    </el-form-item>
                    <el-form-item label="数据标题">
                        <el-input v-model="editedNode.title" placeholder="请输入数据标题" class="form-input" />
                    </el-form-item>
                </el-form>
            </div>

            <template #footer>
                <div class="dialog-footer">
                    <el-button type="success" @click="confirm()" class="action-btn success-btn">
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
                            <Setting />
                        </el-icon>
                    </div>
                    <h2 class="page-title">字典管理</h2>
                </div>
            </div>

            <!-- 操作栏 -->
            <div class="action-bar">
                <div class="action-buttons">
                    <el-button type="success" @click="addItemFirst()" class="action-btn primary-btn">
                        <el-icon>
                            <Plus />
                        </el-icon>
                        添加字典名
                    </el-button>
                    <el-button type="primary" @click="addItem()" class="action-btn secondary-btn">
                        <el-icon>
                            <DocumentAdd />
                        </el-icon>
                        添加数据项
                    </el-button>
                    <el-button type="warning" @click="editItem()" class="action-btn edit-btn">
                        <el-icon>
                            <Edit />
                        </el-icon>
                        修改
                    </el-button>
                    <el-button type="danger" @click="deleteItem()" class="action-btn delete-btn">
                        <el-icon>
                            <Delete />
                        </el-icon>
                        删除
                    </el-button>
                </div>
            </div>

            <!-- 树形结构区域 -->
            <div class="tree-container" v-if="data.nodes !== null">
                <div class="tree-header">
                    <div class="tree-title">
                        <el-icon>
                            <Management />
                        </el-icon>
                        <span>字典数据结构</span>
                    </div>
                    <div class="tree-description">
                        点击节点选择，使用上方按钮进行操作
                    </div>
                </div>
                <div class="tree-content">
                    <el-tree :data="data.nodes" :props="data.defaultProps" accordion @node-click="onNodeClick"
                        class="custom-tree" node-key="id" :highlight-current="true" :expand-on-click-node="false">
                        <template #default="{ node, data: nodeData }">
                            <div class="tree-node">
                                <div class="node-icon">
                                    <el-icon v-if="!node.isLeaf">
                                        <Folder />
                                    </el-icon>
                                    <el-icon v-else>
                                        <Document />
                                    </el-icon>
                                </div>
                                <span class="node-label">{{ nodeData.title }}</span>
                                <div class="node-value" v-if="nodeData.value">
                                    <el-tag size="small" type="info">{{ nodeData.value }}</el-tag>
                                </div>
                            </div>
                        </template>
                    </el-tree>
                </div>
            </div>

            <!-- 空状态 -->
            <div class="empty-state" v-else>
                <div class="empty-icon">
                    <el-icon>
                        <Document />
                    </el-icon>
                </div>
                <div class="empty-text">暂无字典数据</div>
                <div class="empty-description">点击"添加字典名"开始创建数据字典</div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup name="DictionaryManage">
import type { DictionaryManageData, TreeNode } from "~/domain/models/system";
import { container } from '~/inverfiy.config';
import { ID_DICTIONARY_MANAGE_PRESENTER } from '~/types';
import { DictionaryManagePresenter } from "~/domain/presenters/dictionary-manage-presenter";
import { ref } from "vue";
import {
    Setting, Edit, Delete, Plus, DocumentAdd, Check, Close,
    Management, Folder, Document
} from '@element-plus/icons-vue'
const presenter = container.get<DictionaryManagePresenter>(ID_DICTIONARY_MANAGE_PRESENTER);
let data = ref<DictionaryManageData>({} as DictionaryManageData);
let editedNode = ref<TreeNode>({} as TreeNode);
let editVisible = ref(false);
let dialogTitle = ref("添加字典名对话框");
presenter.dictionaryManageInit().then((res) => {
    data.value = res;
});
const onNodeClick = async (node: TreeNode) => {
    data.value.currentNode = node;
};
const addItemFirst = async () => {
    editedNode.value = {} as TreeNode;
    editedNode.value.pid = null;
    editedNode.value.parentTitle = "";
    dialogTitle.value = "添加字典名对话框";
    editVisible.value = true;
};
const addItem = async () => {
    editedNode.value = {} as TreeNode;
    editedNode.value.pid = data.value.currentNode.id;
    editedNode.value.parentTitle = data.value.currentNode.title;
    dialogTitle.value = "添加字典项对话框";
    editVisible.value = true;
};
const editItem = async () => {
    editedNode.value = data.value.currentNode;
    dialogTitle.value = "修改菜单对话框";
    editVisible.value = true;
};
const confirm = async () => {
    await presenter.confirm(data.value, editedNode.value,);
    editVisible.value = false;
};
const deleteItem = async () => {
    await presenter.deleteItem(data.value);
};
</script>

<style lang="scss" scoped></style>
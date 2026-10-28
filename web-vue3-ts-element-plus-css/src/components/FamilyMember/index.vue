<template>
    <div class="main-view-container">
        <div class="main-content">
            <!-- 操作栏 -->
            <div class="action-bar">
                <div class="action-left">
                    <el-button class="add-btn" @click="addRow()">
                        <el-icon>
                            <Plus />
                        </el-icon>
                        添加家庭成员
                    </el-button>
                </div>
                <div class="action-right">
                    <span class="info-text">共 {{ data.dataList?.length || 0 }} 条记录</span>
                </div>
            </div>

            <!-- 表格容器 -->
            <div class="table-container">
                <el-table :data="data.dataList" class="base-table-table">
                    empty-text="暂无家庭成员信息">
                    <el-table-column label="序号" fixed="left" width="80">
                        <template v-slot="scope">
                            <div class="sequence-number">{{ scope.$index + 1 }}</div>
                        </template>
                    </el-table-column>
                    <el-table-column label="关系" width="150">
                        <template v-slot="scope">
                            <el-input v-model="scope.row.relation" class="form-input" placeholder="请输入关系"
                                size="small" />
                        </template>
                    </el-table-column>
                    <el-table-column label="姓名" width="150">
                        <template v-slot="scope">
                            <el-input v-model="scope.row.name" class="form-input" placeholder="请输入姓名" size="small" />
                        </template>
                    </el-table-column>
                    <el-table-column label="性别" width="150">
                        <template v-slot="scope">
                            <el-select v-model="scope.row.gender" class="form-select" placeholder="请选择性别" size="small"
                                clearable>
                                <el-option v-for="items in data.genderList" :key="items.value" :label="items.title"
                                    :value="items.value" />
                            </el-select>
                        </template>
                    </el-table-column>
                    <el-table-column label="年龄" width="120">
                        <template v-slot="scope">
                            <el-input v-model="scope.row.age" class="form-input" placeholder="请输入年龄" size="small"
                                type="number" />
                        </template>
                    </el-table-column>
                    <el-table-column label="工作单位" min-width="200">
                        <template v-slot="scope">
                            <el-input v-model="scope.row.unit" class="form-input" placeholder="请输入工作单位" size="small" />
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="180" fixed="right">
                        <template v-slot="scope">
                            <div class="table-actions">
                                <el-button type="primary" plain size="small" class="table-btn"
                                    @click="saveItem(scope.$index)">
                                    <el-icon>
                                        <Check />
                                    </el-icon>
                                    保存
                                </el-button>
                                <el-button type="danger" plain size="small" class="table-btn delete-btn"
                                    @click="deleteItem(scope.$index)">
                                    <el-icon>
                                        <Delete />
                                    </el-icon>
                                    删除
                                </el-button>
                            </div>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup name="FamilyMember">
import { ref, watch, defineProps } from "vue";
import { FamilyMemberData, FamilyMemberItem } from "~/domain/models/person";
import { ID_FAMILY_MEMBER_PRESENTER } from '~/types';
import { container } from '~/inverfiy.config';
import { FamilyMemberPresenter } from "~/domain/presenters/family-member-presenter";
import { User, Plus, Check, Delete } from '@element-plus/icons-vue';

const presenter = container.get<FamilyMemberPresenter>(ID_FAMILY_MEMBER_PRESENTER);
let data = ref<FamilyMemberData>({ personId: 0 } as FamilyMemberData);
const props = defineProps({
    personId: {
        type: Number,
        required: true
    },
});
const getData = async () => {
    if (data.value.personId > 0) {
        await presenter.familyMemberInit(data.value);
    } else {
        data.value.dataList = [] as FamilyMemberItem[];
    }
};
watch(
    () => props.personId,
    val => {
        data.value.personId = val;
        getData();
    },
    { immediate: true }
)
const addRow = () => {
    data.value.dataList.push({ personId: data.value.personId } as FamilyMemberItem);
};
const deleteItem = async (index: number) => {
    await presenter.deleteItem(data.value, index);
};
const saveItem = async (index: number) => {
    await presenter.saveItem(data.value, index);
};
</script>

<style lang="scss" scoped></style>

<template>
  <div class="main-view-container">
    <!-- 家庭成员对话框 -->
    <el-dialog v-model="memberVisible" :close-on-click-modal="false" width="70%" class="custom-dialog" destroy-on-close>
      <template #header>
        <div class="dialog-header">
          <div class="header-icon">
            <el-icon>
              <UserFilled />
            </el-icon>
          </div>
          <h3 class="dialog-title">家庭成员信息</h3>
        </div>
      </template>
      <FamilyMember :personId="memberPersonId" />
      <template #footer>
        <div class="dialog-footer">
          <el-button type="info" plain @click="memberVisible = false" class="dialog-btn">
            <el-icon>
              <Close />
            </el-icon>
            关闭
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 个人简介对话框 -->
    <el-dialog v-model="introduceVisible" :close-on-click-modal="false" width="90%" class="custom-dialog"
      destroy-on-close>
      <template #header>
        <div class="dialog-header">
          <div class="header-icon">
            <el-icon>
              <User />
            </el-icon>
          </div>
          <h3 class="dialog-title">个人简介</h3>
        </div>
      </template>
      <StudentIntroduce :personId="introducePersonId" />
      <template #footer>
        <div class="dialog-footer">
          <el-button type="info" plain @click="introduceVisible = false" class="dialog-btn">
            <el-icon>
              <Close />
            </el-icon>
            关闭
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 学生信息编辑对话框 -->
    <el-dialog v-model="editVisible" :close-on-click-modal="false" width="60%" class="custom-dialog" destroy-on-close>
      <template #header>
        <div class="dialog-header">
          <div class="header-icon">
            <el-icon>
              <Edit />
            </el-icon>
          </div>
          <h3 class="dialog-title">学生信息编辑</h3>
        </div>
      </template>

      <div class="edit-form-container">
        <el-form ref="form" :model="itemData" label-width="100px" class="base-table-edit-form">
          <div class="form-content">
            <div class="form-left">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="学号">
                    <el-input v-model="itemData.num" placeholder="请输入学号" class="form-input" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="姓名">
                    <el-input v-model="itemData.name" placeholder="请输入姓名" class="form-input" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="学院">
                    <el-input v-model="itemData.dept" placeholder="请输入学院" class="form-input" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="专业">
                    <el-input v-model="itemData.major" placeholder="请输入专业" class="form-input" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="班级">
                    <el-input v-model="itemData.className" placeholder="请输入班级" class="form-input" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="证件号码">
                    <el-input v-model="itemData.card" placeholder="请输入证件号码" class="form-input" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="性别">
                    <el-select v-model="itemData.gender" style="width: 100%" class="form-select">
                      <el-option v-for="item in data.genderList" :key="item.value" :value="item.value"
                        :label="item.title" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="出生日期">
                    <el-date-picker v-model="data.birthday" type="date" style="width: 100%;" placeholder="选择出生日期"
                      class="form-date" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="邮箱">
                    <el-input v-model="itemData.email" placeholder="请输入邮箱" class="form-input" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="电话">
                    <el-input v-model="itemData.phone" placeholder="请输入电话" class="form-input" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="16">
                <el-col :span="24">
                  <el-form-item label="地址">
                    <el-input v-model="itemData.address" placeholder="请输入地址" class="form-input" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <div class="form-right">
              <div class="photo-container">
                <div class="photo-label">个人照片</div>
                <div class="photo-wrapper">
                  <img :src="data.imgStr" alt="个人照片" class="person-photo" />
                </div>
              </div>
            </div>
          </div>
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
          <el-upload :headers="data.authHeader" :action="'/api/base/uploadPhotoBlobWeb'" :data="{
            remoteFile: itemData.personId,
          }" accept=".jpg" :show-file-list="false" :limit="10" :multiple="false" :on-success="onSuccessPhoto"
            style="display: inline-block; margin-left: 10px;">
            <el-button type="primary" plain class="action-btn upload-btn">
              <el-icon>
                <Upload />
              </el-icon>
              照片上传
            </el-button>
          </el-upload>
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
              <User />
            </el-icon>
          </div>
          <h2 class="page-title">学生管理</h2>
        </div>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar">
        <div class="action-left">
          <el-button type="success" @click="addItem()" class="action-btn add-btn">
            <el-icon>
              <Plus />
            </el-icon>
            添加学生
          </el-button>
        </div>
        <div class="action-right">
          <div class="search-group">
            <el-input v-model="data.numName" placeholder="请输入学号或姓名" class="search-input">
              <template #prepend>学号或姓名</template>
            </el-input>
            <el-button type="primary" @click="doQuery()" class="search-btn">
              <el-icon>
                <Search />
              </el-icon>
              查询
            </el-button>
            <el-button type="warning" @click="doExport()" class="export-btn">
              <el-icon>
                <Download />
              </el-icon>
              导出
            </el-button>
          </div>
        </div>
      </div>

      <!-- 数据表格 -->
      <div class="table-container">
        <el-table :data="data.dataList" stripe class="base-table-table">
          <el-table-column label="序号" width="70" fixed>
            <template v-slot="scope">
              <div class="sequence-number">
                {{ scope.$index + 1 }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="380" fixed="right">
            <template v-slot="scope">
              <div class="table-actions">
                <el-button type="primary" size="small" @click="editItem(scope.$index)" class="table-btn">
                  <el-icon>
                    <Edit />
                  </el-icon>
                  基本信息
                </el-button>
                <el-button type="warning" size="small" @click="familyMember(scope.row.personId)" class="table-btn">
                  <el-icon>
                    <UserFilled />
                  </el-icon>
                  家庭成员
                </el-button>
                <el-button type="info" size="small" @click="introduce(scope.row.personId)" class="table-btn">
                  <el-icon>
                    <User />
                  </el-icon>
                  个人简介
                </el-button>
                <el-button type="danger" size="small" @click="deleteItem(scope.$index)" class="table-btn delete-btn">
                  <el-icon>
                    <Delete />
                  </el-icon>
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="num" label="学号" width="140" />
          <el-table-column prop="name" label="姓名" width="80" />
          <el-table-column prop="dept" label="院系" width="140" show-overflow-tooltip />
          <el-table-column prop="major" label="专业" width="100" show-overflow-tooltip />
          <el-table-column prop="className" label="班级" width="100" show-overflow-tooltip />
          <el-table-column prop="card" label="证件号码" width="160" show-overflow-tooltip />
          <el-table-column prop="genderName" label="性别" width="80" />
          <el-table-column prop="birthday" label="出生日期" width="140" />
          <el-table-column prop="email" label="邮箱" width="240" show-overflow-tooltip />
          <el-table-column prop="phone" label="电话" width="140" />
          <el-table-column prop="address" label="地址" show-overflow-tooltip />
        </el-table>

        <!-- 分页器 -->
        <div class="pagination-container">
          <el-pagination size="default" background :total="data.pagination.total"
            :current-page="data.pagination.currentPage" :page-size="data.pagination.pageSize"
            @current-change="handleChangePage" layout="total, prev, pager, next, jumper" class="custom-pagination" />
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup name="Student">
import type { StudentData, StudentItem } from "~/domain/models/person";
import { container } from '~/inverfiy.config';
import { ID_STUDENT_PRESENTER } from '~/types';
import { StudentPresenter } from "~/domain/presenters/student-presenter";
import { ref } from "vue";
import {
  User, UserFilled, Edit, Delete, Plus, Search, Download,
  Check, Close, Upload
} from '@element-plus/icons-vue'
const presenter = container.get<StudentPresenter>(ID_STUDENT_PRESENTER);
let data = ref<StudentData>({ pagination: {} } as StudentData);
let itemData = ref<StudentItem>({} as StudentItem);
let editVisible = ref(false);
let memberVisible = ref(false);
let memberPersonId = ref(0);
let introduceVisible = ref(false);
let introducePersonId = ref(0);
presenter.studentInit().then((res) => {
  data.value = res;
});
const handleChangePage = async (val: number) => {
  data.value.pagination.currentPage = val;
  await presenter.getStudentDataPage(data.value);
};
const doQuery = async () => {
  data.value.pagination.currentPage = 1;
  await presenter.getStudentDataPage(data.value);
};
const doExport = async () => {
  await presenter.doExport(data.value);
};
const addItem = async () => {
  itemData.value = presenter.addItem(data.value);
  editVisible.value = true;
};
const editItem = async (index: number) => {
  itemData.value = await presenter.editItem(data.value, index);
  editVisible.value = true;
};
const itemSubmit = async () => {
  await presenter.itemSubmit(itemData.value, data.value);
  editVisible.value = false;
};
const familyMember = async (personId: number) => {
  memberPersonId.value = personId;
  memberVisible.value = true;
};
const introduce = async (personId: number) => {
  introducePersonId.value = personId;
  introduceVisible.value = true;
  console.log("个人简介", introducePersonId.value);
};
const deleteItem = async (index: number) => {
  await presenter.deleteItem(data.value, index);
};
const onSuccessPhoto = async (res: any) => {
  await presenter.onSuccessPhoto(data.value, res);
};

</script>
<style lang="scss" scoped></style>
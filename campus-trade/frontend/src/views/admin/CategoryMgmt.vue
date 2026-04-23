<template>
  <div class="mgmt-page">
    <div class="page-header">
      <div class="page-title-section">
        <h2 class="page-title">分类管理</h2>
        <p class="page-desc">Organize product categories and subcategories</p>
      </div>
      <el-button class="add-btn" :icon="Plus" @click="openAdd(null)">
        新增一级分类
      </el-button>
    </div>

    <div class="content-card">
      <el-table :data="categories" v-loading="loading" row-key="id" default-expand-all class="modern-table">
        <el-table-column prop="name" label="分类名称">
          <template #default="{ row }">
            <span class="category-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="icon" label="图标" width="120">
          <template #default="{ row }">
            <span class="icon-badge" v-if="row.icon">{{ row.icon }}</span>
            <span class="no-icon" v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80">
          <template #default="{ row }">
            <span class="sort-num">{{ row.sort }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" class="btn-outline-primary" @click="openAdd(row.id)" v-if="!row.parentId">
              新增子分类
            </el-button>
            <el-button size="small" class="btn-outline-danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" title="新增分类" width="420px" class="modern-dialog">
      <el-form :model="form" label-width="80px" class="modern-form">
        <el-form-item label="分类名称">
          <el-input v-model="form.name" maxlength="50" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="icon-xxx" />
        </el-form-item>
        <el-form-item label="排序权重">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="btn-cancel" @click="dialog = false">取消</el-button>
          <el-button class="btn-save" @click="save" :loading="saving">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../../utils/request'
const categories=ref([]),loading=ref(false),dialog=ref(false),saving=ref(false)
const form=reactive({name:'',icon:'',sort:0,parentId:null})
onMounted(load)
async function load(){
  loading.value=true
  try{const res=await request.get('/categories');categories.value=res.data.data||[]}
  finally{loading.value=false}
}
function openAdd(parentId){Object.assign(form,{name:'',icon:'',sort:0,parentId});dialog.value=true}
async function save(){
  saving.value=true
  try{
    await request.post('/admin/categories',null,{params:{name:form.name,parentId:form.parentId,icon:form.icon,sort:form.sort}})
    ElMessage.success('添加成功');dialog.value=false;load()
  }finally{saving.value=false}
}
async function del(row){
  await ElMessageBox.confirm(`确认删除分类「${row.name}」？`,'删除',{type:'warning'})
  await request.delete(`/admin/categories/${row.id}`)
  ElMessage.success('已删除');load()
}
</script>
<style scoped>
.mgmt-page {
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-title-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.page-title {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.5px;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-desc {
  font-size: 13px;
  color: var(--text-muted);
  font-weight: 500;
}

.add-btn {
  background: var(--gradient-primary) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 10px !important;
  font-weight: 600 !important;
  padding: 10px 20px !important;
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease !important;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4) !important;
}

.content-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  padding: 24px;
  overflow: hidden;
}

.content-card :deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-border-color: rgba(226, 232, 240, 0.6);
  border: none;
}

.content-card :deep(.el-table::before) {
  display: none;
}

.content-card :deep(.el-table th.el-table__cell) {
  background: rgba(248, 250, 252, 0.8);
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-muted);
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
  padding: 14px 12px;
}

.content-card :deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid rgba(241, 245, 249, 0.8);
  padding: 14px 12px;
  font-size: 13px;
  color: var(--text-primary);
}

.content-card :deep(.el-table__body tr:hover > td) {
  background: rgba(102, 126, 234, 0.03) !important;
}

.content-card :deep(.el-table__expand-icon) {
  color: var(--primary);
}

.category-name {
  font-weight: 600;
  color: var(--text-primary);
}

.icon-badge {
  display: inline-block;
  padding: 2px 10px;
  background: rgba(102, 126, 234, 0.08);
  color: var(--primary);
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.no-icon {
  color: var(--text-muted);
}

.sort-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: rgba(241, 245, 249, 0.8);
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.btn-outline-primary {
  border: 1px solid rgba(102, 126, 234, 0.3) !important;
  color: var(--primary) !important;
  background: rgba(102, 126, 234, 0.04) !important;
  border-radius: 8px !important;
  font-weight: 500 !important;
  transition: all 0.25s ease !important;
}

.btn-outline-primary:hover {
  background: var(--primary) !important;
  color: #fff !important;
  border-color: var(--primary) !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25) !important;
}

.btn-outline-danger {
  border: 1px solid rgba(239, 68, 68, 0.3) !important;
  color: #ef4444 !important;
  background: rgba(239, 68, 68, 0.04) !important;
  border-radius: 8px !important;
  font-weight: 500 !important;
  transition: all 0.25s ease !important;
}

.btn-outline-danger:hover {
  background: #ef4444 !important;
  color: #fff !important;
  border-color: #ef4444 !important;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.25) !important;
}

.modern-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.modern-dialog :deep(.el-dialog__header) {
  background: var(--gradient-primary);
  padding: 18px 24px;
  margin: 0;
}

.modern-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-weight: 700;
  font-size: 16px;
}

.modern-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(255, 255, 255, 0.8);
}

.modern-dialog :deep(.el-dialog__body) {
  padding: 28px 24px;
}

.modern-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-secondary);
}

.modern-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(226, 232, 240, 0.8) inset;
  transition: all 0.25s ease;
}

.modern-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(102, 126, 234, 0.3) inset;
}

.modern-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2) inset, 0 0 0 1px var(--primary) inset !important;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-cancel {
  border-radius: 10px !important;
  font-weight: 500 !important;
  padding: 8px 20px !important;
  border: 1px solid rgba(226, 232, 240, 0.8) !important;
  color: var(--text-secondary) !important;
  transition: all 0.25s ease !important;
}

.btn-cancel:hover {
  border-color: var(--text-muted) !important;
  color: var(--text-primary) !important;
}

.btn-save {
  background: var(--gradient-primary) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 10px !important;
  font-weight: 600 !important;
  padding: 8px 24px !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.25s ease !important;
}

.btn-save:hover {
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4) !important;
  transform: translateY(-1px);
}
</style>

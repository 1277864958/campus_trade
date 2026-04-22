<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">分类管理</h2>
      <el-button type="primary" :icon="Plus" @click="openAdd(null)">新增一级分类</el-button>
    </div>
    <el-table :data="categories" v-loading="loading" row-key="id" border default-expand-all>
      <el-table-column prop="name" label="分类名称"/>
      <el-table-column prop="icon" label="图标"/>
      <el-table-column prop="sort" label="排序" width="80"/>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="openAdd(row.id)" v-if="!row.parentId">新增子分类</el-button>
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="新增分类" width="380px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称"><el-input v-model="form.name" maxlength="50"/></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="icon-xxx"/></el-form-item>
        <el-form-item label="排序权重"><el-input-number v-model="form.sort" :min="0"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog=false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">保存</el-button>
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
.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px}
.page-title{font-size:22px;font-weight:700}
</style>

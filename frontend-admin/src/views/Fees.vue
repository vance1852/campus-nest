<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="query" @submit.prevent="loadData">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="(item, key) in statusMap" :key="key" :label="item.label" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用类型">
          <el-select v-model="query.feeType" placeholder="全部类型" clearable style="width: 140px">
            <el-option v-for="(item, key) in feeTypeMap" :key="key" :label="item" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="账单周期">
          <el-input v-model="query.billCycle" placeholder="如: 2024-01" style="width: 140px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>{{ isStudent ? '我的缴费记录' : '缴费管理' }}</span>
          <el-button v-if="!isStudent" type="primary" :icon="Plus" @click="openCreateDialog">新增账单</el-button>
        </div>
      </template>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column v-if="!isStudent" prop="studentName" label="学生姓名" width="100" />
        <el-table-column v-if="!isStudent" prop="studentNo" label="学号" width="120" />
        <el-table-column prop="roomInfo" label="宿舍" width="150" />
        <el-table-column prop="feeType" label="费用类型" width="100" align="center">
          <template #default="{ row }">{{ feeTypeMap[row.feeType] }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100" align="right">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="billCycle" label="账单周期" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="缴费时间" width="180" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column v-if="!isStudent" label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="success" @click="confirmPayment(row)">确认缴费</el-button>
            <el-button v-if="row.status === 0" link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button v-if="row.status === 0" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" 
          :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" 
          @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>
    
    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑账单' : '新增账单'" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="选择学生" prop="studentId">
          <el-select v-model="form.studentId" placeholder="请选择学生" filterable style="width: 100%" :disabled="isEdit">
            <el-option v-for="s in students" :key="s.id" :label="`${s.name} (${s.studentNo})`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用类型" prop="feeType">
          <el-select v-model="form.feeType" placeholder="请选择费用类型" style="width: 100%">
            <el-option v-for="(item, key) in feeTypeMap" :key="key" :label="item" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input v-model.number="form.amount" type="number" step="0.01" placeholder="请输入金额" style="width: 100%" />
        </el-form-item>
        <el-form-item label="账单周期" prop="billCycle">
          <el-input v-model="form.billCycle" placeholder="如: 2024-01" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { feeApi, studentApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const isStudent = computed(() => userInfo.value?.role === 3)

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = ref(false)
const students = ref([])

const query = reactive({ current: 1, size: 10, status: null, feeType: null, billCycle: '' })
const form = reactive({ studentId: null, feeType: null, amount: null, billCycle: '' })

const rules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  feeType: [{ required: true, message: '请选择费用类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  billCycle: [{ required: true, message: '请输入账单周期', trigger: 'blur' }]
}

const feeTypeMap = {
  1: '住宿费',
  2: '水电费',
  3: '物业费',
  4: '其他'
}

const statusMap = {
  0: { label: '未缴费', type: 'warning' },
  1: { label: '已缴费', type: 'success' }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = isStudent.value ? await feeApi.myPage(query) : await feeApi.page(query)
    tableData.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

const loadStudents = async () => {
  const res = await studentApi.page({ current: 1, size: 1000 })
  students.value = res.records
}

const resetQuery = () => {
  query.status = null
  query.feeType = null
  query.billCycle = ''
  query.current = 1
  loadData()
}

const openCreateDialog = async () => {
  isEdit.value = false
  Object.assign(form, { studentId: null, feeType: null, amount: null, billCycle: '' })
  await loadStudents()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, { id: row.id, studentId: row.studentId, feeType: row.feeType, amount: row.amount, billCycle: row.billCycle })
  dialogVisible.value = true
}

const confirmPayment = (row) => {
  ElMessageBox.confirm(`确认学生「${row.studentName}」的「${feeTypeMap[row.feeType]}」费用已缴费？`, '确认缴费', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await feeApi.confirmPayment(row.id)
    ElMessage.success('缴费确认成功')
    loadData()
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该账单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await feeApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await feeApi.update(form.id, form)
      ElMessage.success('编辑成功')
    } else {
      await feeApi.create(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally { submitLoading.value = false }
}

onMounted(async () => {
  if (!userInfo.value) {
    await userStore.getInfo()
  }
  loadData()
})
</script>

<style lang="scss" scoped>
.page-container {
  .search-card {
    margin-bottom: 16px;
    
    :deep(.el-card__body) {
      padding: 14px 20px;
    }
  }
  
  .table-card {
    :deep(.el-card__body) {
      padding: 0;
    }
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .pagination-wrapper {
    padding: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>

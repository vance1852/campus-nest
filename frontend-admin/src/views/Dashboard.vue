<template>
  <div class="page-container">
    <!-- 管理员/宿管视图 -->
    <template v-if="userInfo?.role !== 3">
      <el-row :gutter="16">
        <el-col :span="6" v-for="item in adminStatCards" :key="item.key">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-info">
                <div class="stat-value">{{ stats[item.key] || 0 }}</div>
                <div class="stat-label">{{ item.label }}</div>
              </div>
              <el-icon class="stat-icon" :style="{ color: item.color }">
                <component :is="item.icon" />
              </el-icon>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
    
    <!-- 学生视图 -->
    <template v-else>
      <div class="student-cards">
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="姓名">{{ stats.studentName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学号">{{ stats.studentNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="宿舍">
              <span v-if="stats.dormInfo !== '未分配宿舍'" class="dorm-tag success">{{ stats.dormInfo }}</span>
              <span v-else class="dorm-tag info">{{ stats.dormInfo }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Tools /></el-icon>
              <span>我的维修申请</span>
            </div>
          </template>
          <div class="repair-stats">
            <div class="mini-stat" v-for="item in studentRepairCards" :key="item.key">
              <div class="mini-stat-value" :style="{ color: item.color }">{{ stats[item.key] || 0 }}</div>
              <div class="mini-stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Money /></el-icon>
              <span>我的缴费</span>
            </div>
          </template>
          <div class="repair-stats">
            <div class="mini-stat" v-for="item in studentFeeCards" :key="item.key">
              <div class="mini-stat-value" :style="{ color: item.color }">{{ stats[item.key] || 0 }}</div>
              <div class="mini-stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </div>
    </template>
    
    <el-card style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>最新公告</span>
          <el-button link type="primary" @click="$router.push('/announcements')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="announcements" stripe>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeMap[row.type]?.type">{{ typeMap[row.type]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
      </el-table>
      <el-empty v-if="!announcements.length" description="暂无公告" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { dashboardApi, announcementApi } from '../api'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const stats = ref({})
const announcements = ref([])

const adminStatCards = [
  { key: 'buildingCount', label: '楼栋数', icon: 'OfficeBuilding', color: '#409EFF' },
  { key: 'roomCount', label: '房间数', icon: 'House', color: '#67C23A' },
  { key: 'studentCount', label: '学生数', icon: 'User', color: '#E6A23C' },
  { key: 'pendingRepairs', label: '待处理维修', icon: 'Tools', color: '#F56C6C' },
  { key: 'unpaidFees', label: '待缴费账单', icon: 'Money', color: '#F56C6C' }
]

const studentRepairCards = [
  { key: 'myRepairTotal', label: '总申请', color: '#409EFF' },
  { key: 'myRepairPending', label: '待处理', color: '#E6A23C' },
  { key: 'myRepairProcessing', label: '处理中', color: '#409EFF' },
  { key: 'myRepairCompleted', label: '已完成', color: '#67C23A' }
]

const studentFeeCards = [
  { key: 'myUnpaidFees', label: '待缴费', color: '#F56C6C' }
]

const typeMap = {
  1: { label: '通知', type: 'primary' },
  2: { label: '规章', type: 'warning' },
  3: { label: '活动', type: 'success' }
}

onMounted(async () => {
  // 确保用户信息已加载，避免刷新时角色判断错误
  if (!userStore.userInfo) {
    await userStore.getInfo()
  }
  stats.value = await dashboardApi.stats()
  announcements.value = await announcementApi.latest(5)
})
</script>

<style lang="scss" scoped>
.stat-card {
  margin-bottom: 16px;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.stat-icon {
  font-size: 48px;
  opacity: 0.8;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.info-card {
  margin-bottom: 16px;
  
  .card-header {
    justify-content: flex-start;
    font-weight: 600;
  }
}

.student-cards {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  
  .info-card {
    flex: 1;
    min-width: 280px;
    margin-bottom: 0;
    display: flex;
    flex-direction: column;
    
    :deep(.el-card__body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }
  }
}

.dorm-tag {
  display: inline-block;
  padding: 0 8px;
  height: 22px;
  line-height: 22px;
  font-size: 12px;
  border-radius: 4px;
  
  &.success {
    background-color: #f0f9eb;
    color: #67c23a;
    border: 1px solid #e1f3d8;
  }
  
  &.info {
    background-color: #f4f4f5;
    color: #909399;
    border: 1px solid #e9e9eb;
  }
}

.repair-stats {
  display: flex;
  flex-wrap: wrap;
}

.mini-stat {
  text-align: center;
  padding: 16px 0;
  width: 50%;
}

.mini-stat-value {
  font-size: 32px;
  font-weight: bold;
}

.mini-stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}
</style>

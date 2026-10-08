<template>
  <div class="report-manage">
    <h2 class="page-title">举报处理</h2>

    <!-- 状态筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="statusFilter" @change="handleFilterChange">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="PENDING">待处理</el-radio-button>
        <el-radio-button value="HANDLED">已处理</el-radio-button>
        <el-radio-button value="IGNORED">已忽略</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="list" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column label="类型" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.targetType === 'COMMENT' ? 'warning' : 'success'" size="small">
            {{ row.targetType === 'COMMENT' ? '评论' : '文档' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="举报目标" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.targetType === 'COMMENT'">
            {{ row.commentUserName || '匿名' }}：{{ row.commentContent }}
          </span>
          <span v-else>{{ row.docTitle || ('文档#' + row.docId) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="reporterName" label="举报人" width="110" />
      <el-table-column
        prop="reason"
        label="举报原因"
        min-width="180"
        show-overflow-tooltip
      />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理时间" width="160" align="center">
        <template #default="{ row }">
          {{ row.handleTime ? formatTime(row.handleTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'PENDING'"
            type="primary"
            link
            @click="openHandle(row)"
          >处理</el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && list.length === 0" description="暂无举报记录" />

    <div class="pagination" v-if="total > 0">
      <el-pagination
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 处理弹窗 -->
    <el-dialog v-model="dialogVisible" title="处理举报" width="480px">
      <div v-if="currentReport" class="report-info">
        <p>
          <strong>举报类型：</strong>
          {{ currentReport.targetType === 'COMMENT' ? '评论' : '文档' }}
        </p>
        <p>
          <strong>举报目标：</strong>
          <span v-if="currentReport.targetType === 'COMMENT'">
            {{ currentReport.commentUserName || '匿名' }}：{{ currentReport.commentContent }}
          </span>
          <span v-else>{{ currentReport.docTitle || ('文档#' + currentReport.docId) }}</span>
        </p>
        <p><strong>举报人：</strong>{{ currentReport.reporterName }}</p>
        <p><strong>举报原因：</strong>{{ currentReport.reason }}</p>
      </div>
      <el-form label-width="90px" style="margin-top: 16px">
        <el-form-item label="处理方式">
          <el-radio-group v-model="handleForm.status">
            <el-radio value="HANDLED">
              {{ currentReport && currentReport.targetType === 'COMMENT' ? '删除评论' : '下架文档' }}
            </el-radio>
            <el-radio value="IGNORED">忽略举报</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="handleForm.handleRemark"
            type="textarea"
            :rows="3"
            placeholder="可选"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getReportPage, handleReport } from '@/api/report'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const statusFilter = ref('')
const loading = ref(false)

const dialogVisible = ref(false)
const currentReport = ref(null)
const handleForm = reactive({ status: 'HANDLED', handleRemark: '' })

function formatTime(str) {
  if (!str) return ''
  const d = new Date(str)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function statusType(s) {
  return { PENDING: 'warning', HANDLED: 'success', IGNORED: 'info' }[s] || 'info'
}

function statusText(s) {
  return { PENDING: '待处理', HANDLED: '已处理', IGNORED: '已忽略' }[s] || s
}

async function loadList() {
  loading.value = true
  try {
    const data = await getReportPage({
      page: page.value,
      size: size.value,
      status: statusFilter.value || undefined
    })
    list.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
  page.value = 1
  loadList()
}

function handlePageChange(p) {
  page.value = p
  loadList()
}

function openHandle(row) {
  currentReport.value = row
  handleForm.status = 'HANDLED'
  handleForm.handleRemark = ''
  dialogVisible.value = true
}

async function handleSave() {
  try {
    await handleReport(currentReport.value.id, {
      status: handleForm.status,
      handleRemark: handleForm.handleRemark
    })
    ElMessage.success('处理成功')
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // 拦截器已提示
  }
}

onMounted(loadList)
</script>

<style scoped>
.report-manage {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 22px;
  margin-bottom: 16px;
  color: #1f2937;
}

.filter-bar {
  margin-bottom: 16px;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}

.report-info p {
  margin: 8px 0;
  font-size: 14px;
  color: #374151;
}
</style>

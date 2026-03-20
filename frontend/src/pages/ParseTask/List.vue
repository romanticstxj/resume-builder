<template>
  <div class="task-list-container">
    <div class="page-header">
      <h2>解析任务</h2>
      <t-button theme="primary" @click="openImportDialog">
        <template #icon><t-icon name="add" /></template>
        新建任务
      </t-button>
    </div>

    <div class="task-table">
      <t-table
        :data="tasks"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @page-change="handlePageChange"
      >
        <template #status="{ row }">
          <t-tag :theme="getTaskStatusTheme(row.status)">
            {{ getTaskStatusText(row.status) }}
          </t-tag>
        </template>

        <template #progress="{ row }">
          <t-progress
            :percentage="row.progress"
            :label="false"
            size="small"
            theme="primary"
          />
        </template>

        <template #createdAt="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>

        <template #action="{ row }">
          <t-space>
            <t-button
              v-if="row.status === 'completed'"
              theme="primary"
              variant="text"
              size="small"
              @click="handleViewResult(row)"
            >
              查看结果
            </t-button>
            <t-button
              v-if="row.status === 'failed'"
              theme="danger"
              variant="text"
              size="small"
              @click="handleRetry(row)"
            >
              重试
            </t-button>
            <t-button
              theme="default"
              variant="text"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </t-button>
          </t-space>
        </template>
      </t-table>
    </div>

    <!-- 简历导入对话框 -->
    <ResumeImport ref="importDialogRef" @success="handleImportSuccess" />

    <!-- 查看结果对话框 -->
    <t-dialog
      v-model:visible="resultVisible"
      header="解析结果"
      width="700px"
      :footer="false"
      @close="handleCloseResult"
    >
      <div v-if="currentResult" class="result-preview">
        <div class="result-header">
          <h3>{{ currentResult.title }}</h3>
          <t-space>
            <t-button theme="default" variant="text" @click="handleCloseResult">关闭</t-button>
            <t-button theme="primary" @click="handleConfirmImport" :loading="saving">
              导入简历
            </t-button>
          </t-space>
        </div>

        <div class="result-content">
          <t-form :data="currentResult.content" label-width="100px">
            <div class="section-title">基本信息</div>
            <t-form-item label="姓名">
              <t-input v-model="currentResult.content.name" placeholder="请输入姓名" />
            </t-form-item>
            <t-form-item label="邮箱">
              <t-input v-model="currentResult.content.email" placeholder="请输入邮箱" />
            </t-form-item>
            <t-form-item label="电话">
              <t-input v-model="currentResult.content.phone" placeholder="请输入电话" />
            </t-form-item>

            <div class="section-title">工作经历</div>
            <div
              v-for="(exp, index) in currentResult.content.experience"
              :key="index"
              class="preview-item"
            >
              <t-form-item label="公司名称">
                <t-input v-model="exp.company" placeholder="请输入公司名称" />
              </t-form-item>
              <t-form-item label="职位">
                <t-input v-model="exp.position" placeholder="请输入职位" />
              </t-form-item>
            </div>

            <div class="section-title">教育经历</div>
            <div
              v-for="(edu, index) in currentResult.content.education"
              :key="index"
              class="preview-item"
            >
              <t-form-item label="学校名称">
                <t-input v-model="edu.school" placeholder="请输入学校名称" />
              </t-form-item>
              <t-form-item label="专业">
                <t-input v-model="edu.major" placeholder="请输入专业" />
              </t-form-item>
            </div>

            <div class="section-title">项目经历</div>
            <div
              v-for="(proj, index) in currentResult.content.projects"
              :key="index"
              class="preview-item"
            >
              <t-form-item label="项目名称">
                <t-input v-model="proj.name" placeholder="请输入项目名称" />
              </t-form-item>
              <t-form-item label="项目描述">
                <t-textarea
                  v-model="proj.description"
                  placeholder="请输入项目描述"
                  :autosize="{ minRows: 2 }"
                />
              </t-form-item>
            </div>

            <div class="section-title">专业技能</div>
            <div
              v-for="(skill, index) in currentResult.content.skills"
              :key="index"
              class="preview-item"
            >
              <t-form-item label="技能名称">
                <t-input v-model="skill.name" placeholder="请输入技能名称" />
              </t-form-item>
            </div>
          </t-form>
        </div>
      </div>
    </t-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import { getTaskList } from '@/api/ai'
import { createResumeFromParsed } from '@/api/ai'
import ResumeImport from '@/components/ResumeImport.vue'

const columns = [
  { colKey: 'fileName', title: '文件名', ellipsis: true },
  { colKey: 'fileSize', title: '文件大小', cell: (h, { row }) => formatFileSize(row.fileSize) },
  { colKey: 'status', title: '状态' },
  { colKey: 'progress', title: '进度' },
  { colKey: 'createdAt', title: '创建时间' },
  { colKey: 'action', title: '操作', width: 200 }
]

const loading = ref(false)
const tasks = ref([])
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const importDialogRef = ref(null)
const resultVisible = ref(false)
const currentResult = ref(null)
const saving = ref(false)

const pollingTimer = ref(null)

const loadTasks = async () => {
  loading.value = true
  try {
    const res = await getTaskList({
      page: pagination.value.current - 1,
      size: pagination.value.pageSize
    })
    tasks.value = res.data
    pagination.value.total = res.total
  } catch (err) {
    MessagePlugin.error('加载任务列表失败: ' + (err.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handlePageChange = (pageInfo) => {
  pagination.value.current = pageInfo.current
  loadTasks()
}

const openImportDialog = () => {
  importDialogRef.value?.open()
}

const handleImportSuccess = () => {
  loadTasks()
}

const handleViewResult = (row) => {
  if (row.parseResult) {
    currentResult.value = row.parseResult
    resultVisible.value = true
  }
}

const handleCloseResult = () => {
  resultVisible.value = false
  currentResult.value = null
}

const handleConfirmImport = () => {
  if (!currentResult.value) return

  saving.value = true
  createResumeFromParsed(currentResult.value)
    .then(res => {
      MessagePlugin.success('简历导入成功')
      handleCloseResult()
      // 跳转到简历列表
      window.location.href = '#/resumes'
    })
    .catch(err => {
      MessagePlugin.error('简历导入失败: ' + (err.message || '未知错误'))
    })
    .finally(() => {
      saving.value = false
    })
}

const handleRetry = (row) => {
  MessagePlugin.info('请使用"新建任务"重新上传文件')
}

const handleDelete = (row) => {
  const dialog = DialogPlugin({
    header: '确认删除',
    body: '确定要删除这个任务吗？',
    confirmBtn: {
      theme: 'danger',
      content: '删除'
    },
    onConfirm: async () => {
      try {
        // TODO: 调用删除任务API
        MessagePlugin.success('删除成功')
        dialog.hide()
        loadTasks()
      } catch (err) {
        MessagePlugin.error('删除失败: ' + (err.message || '未知错误'))
      }
    }
  })
}

const getTaskStatusText = (status) => {
  const statusMap = {
    pending: '等待中',
    processing: '解析中',
    completed: '已完成',
    failed: '失败'
  }
  return statusMap[status] || status
}

const getTaskStatusTheme = (status) => {
  const themeMap = {
    pending: 'default',
    processing: 'primary',
    completed: 'success',
    failed: 'danger'
  }
  return themeMap[status] || 'default'
}

const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 轮询更新进行中的任务状态
const startPolling = () => {
  pollingTimer.value = setInterval(() => {
    // 检查是否有进行中的任务
    const hasProcessing = tasks.value.some(task =>
      ['pending', 'processing'].includes(task.status)
    )

    if (hasProcessing) {
      loadTasks()
    }
  }, 5000) // 每5秒查询一次
}

const stopPolling = () => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

onMounted(() => {
  loadTasks()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.task-list-container {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.task-table {
  background: var(--td-bg-color-container);
  border-radius: 8px;
  padding: 20px;
}

.result-preview {
  max-height: 600px;
  overflow-y: auto;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--td-component-border);
  margin-bottom: 20px;
}

.result-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.result-content {
  padding-right: 10px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--td-text-color-primary);
  margin: 20px 0 12px;
  padding-top: 16px;
  border-top: 1px dashed var(--td-component-border);
}

.preview-item {
  background: var(--td-bg-color-container);
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 12px;
}
</style>

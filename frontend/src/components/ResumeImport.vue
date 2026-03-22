<template>
  <t-dialog
    v-model:visible="visible"
    header="导入简历"
    width="700px"
    :footer="false"
    @close="handleClose"
  >
    <div class="import-container">
      <!-- 上传区域 -->
      <div v-if="!currentTask && !parsedData" class="upload-area">
        <div
          class="upload-zone"
          :class="{ 'drag-over': isDragOver }"
          @dragover.prevent="handleDragOver"
          @dragleave.prevent="handleDragLeave"
          @drop.prevent="handleDrop"
        >
          <t-icon name="cloud-upload" size="64px" />
          <p class="upload-text">拖拽简历文件到此处</p>
          <p class="upload-hint">或</p>
          <input
            type="file"
            accept=".pdf,.doc,.docx,.txt"
            @change="handleFileSelect"
            style="display: none"
            ref="fileInput"
          />
          <t-button variant="outline" @click="triggerFileInput" :loading="uploading">
            <template #icon><t-icon name="browse" /></template>
            选择文件
          </t-button>
          <p class="upload-tips">支持格式：PDF、Word (.doc, .docx)、Text（最大10MB）</p>
        </div>
      </div>

      <!-- 任务进度 -->
      <div v-else-if="currentTask && !parsedData" class="progress-area">
        <div class="progress-header">
          <t-space direction="vertical" :size="12">
            <t-space align="center">
              <t-icon name="time" size="24px" />
              <span class="file-name">{{ currentTask.fileName }}</span>
            </t-space>
            <t-space align="center">
              <t-tag :theme="getTaskStatusTheme(currentTask.status)">
                {{ getTaskStatusText(currentTask.status) }}
              </t-tag>
              <span class="progress-text">进度: {{ currentTask.progress }}%</span>
            </t-space>
          </t-space>
        </div>

        <div class="progress-bar">
          <t-progress
            :percentage="currentTask.progress"
            :label="false"
            theme="primary"
            size="large"
          />
        </div>

        <div v-if="currentTask.errorMessage" class="error-message">
          <t-alert theme="error" :message="currentTask.errorMessage" />
        </div>

        <div class="progress-actions">
          <t-space>
            <t-button theme="default" variant="text" @click="handleCancel" :disabled="isProcessing">
              取消
            </t-button>
            <t-button
              v-if="currentTask.status === 'completed'"
              theme="primary"
              @click="handleViewResult"
            >
              查看结果
            </t-button>
            <t-button
              v-if="currentTask.status === 'failed'"
              theme="primary"
              @click="handleRetry"
            >
              重试
            </t-button>
          </t-space>
        </div>
      </div>

      <!-- 解析结果预览 -->
      <div v-else-if="parsedData" class="preview-area">
        <div class="preview-header">
          <h3>解析结果</h3>
          <t-space>
            <t-button theme="default" variant="text" @click="handleBackToTask">
              <template #icon><t-icon name="chevron-left" /></template>
              返回
            </t-button>
            <t-button theme="default" variant="text" @click="handleCancel">取消</t-button>
            <t-button theme="primary" @click="handleConfirm" :loading="saving">
              确认导入
            </t-button>
          </t-space>
        </div>

        <div class="preview-content">
          <t-form :data="parsedData.content" label-width="100px">
            <t-form-item label="简历标题">
              <t-input v-model="parsedData.title" placeholder="请输入简历标题" />
            </t-form-item>

            <div class="section-title">基本信息</div>
            <t-form-item label="姓名">
              <t-input v-model="parsedData.content.name" placeholder="请输入姓名" />
            </t-form-item>
            <t-form-item label="邮箱">
              <t-input v-model="parsedData.content.email" placeholder="请输入邮箱" />
            </t-form-item>
            <t-form-item label="电话">
              <t-input v-model="parsedData.content.phone" placeholder="请输入电话" />
            </t-form-item>

            <div class="section-title">个人简介</div>
            <t-form-item label="简介">
              <t-textarea
                v-model="parsedData.content.summary"
                placeholder="请输入个人简介"
                :autosize="{ minRows: 3 }"
              />
            </t-form-item>

            <div class="section-title">工作经历</div>
            <div
              v-for="(exp, index) in parsedData.content.experience"
              :key="index"
              class="preview-item"
            >
              <t-form-item label="公司名称">
                <t-input v-model="exp.company" placeholder="请输入公司名称" />
              </t-form-item>
              <t-form-item label="职位">
                <t-input v-model="exp.position" placeholder="请输入职位" />
              </t-form-item>
              <t-form-item label="时间">
                <t-input v-model="exp.period" placeholder="如：2020-2023" />
              </t-form-item>
              <t-form-item label="描述">
                <t-textarea
                  v-model="exp.description"
                  placeholder="请输入工作描述"
                  :autosize="{ minRows: 2 }"
                />
              </t-form-item>
            </div>

            <div class="section-title">教育经历</div>
            <div
              v-for="(edu, index) in parsedData.content.education"
              :key="index"
              class="preview-item"
            >
              <t-form-item label="学校名称">
                <t-input v-model="edu.school" placeholder="请输入学校名称" />
              </t-form-item>
              <t-form-item label="专业">
                <t-input v-model="edu.major" placeholder="请输入专业" />
              </t-form-item>
              <t-form-item label="学位">
                <t-input v-model="edu.degree" placeholder="如：本科" />
              </t-form-item>
              <t-form-item label="时间">
                <t-input v-model="edu.period" placeholder="如：2016-2020" />
              </t-form-item>
            </div>

            <div class="section-title">项目经历</div>
            <div
              v-for="(proj, index) in parsedData.content.projects"
              :key="index"
              class="preview-item"
            >
              <t-form-item label="项目名称">
                <t-input v-model="proj.name" placeholder="请输入项目名称" />
              </t-form-item>
              <t-form-item label="时间">
                <t-input v-model="proj.period" placeholder="如：2022-2023" />
              </t-form-item>
              <t-form-item label="项目描述">
                <t-textarea
                  v-model="proj.description"
                  placeholder="请输入项目描述"
                  :autosize="{ minRows: 2 }"
                />
              </t-form-item>
              <t-form-item label="技术栈">
                <t-input
                  v-model="proj.technologiesString"
                  :value="proj.technologies?.join(', ')"
                  @change="(val) => proj.technologies = val.split(',').map(s => s.trim())"
                  placeholder="请输入技术栈，用逗号分隔"
                />
              </t-form-item>
            </div>

            <div class="section-title">专业技能</div>
            <div
              v-for="(skill, index) in parsedData.content.skills"
              :key="index"
              class="preview-item"
            >
              <t-form-item label="技能名称">
                <t-input v-model="skill.name" placeholder="请输入技能名称" />
              </t-form-item>
              <t-form-item label="熟练度">
                <t-slider
                  v-model.number="skill.level"
                  :min="0"
                  :max="100"
                  :marks="{0: '0%', 50: '50%', 100: '100%'}"
                />
              </t-form-item>
            </div>

            <div class="section-title">个人总结</div>
            <t-form-item label="总结">
              <t-textarea
                v-model="parsedData.content.personalSummary"
                placeholder="请输入个人总结"
                :autosize="{ minRows: 3 }"
              />
            </t-form-item>

            <div class="section-title">荣誉奖项</div>
            <div
              v-for="(honor, index) in parsedData.content.honors"
              :key="index"
              class="preview-item"
            >
              <t-form-item label="奖项名称">
                <t-input v-model="honor.name" placeholder="请输入奖项名称" />
              </t-form-item>
              <t-form-item label="获奖时间">
                <t-input v-model="honor.date" placeholder="如：2023年" />
              </t-form-item>
              <t-form-item label="奖项描述">
                <t-textarea
                  v-model="honor.description"
                  placeholder="请输入奖项描述"
                  :autosize="{ minRows: 2 }"
                />
              </t-form-item>
            </div>

            <div class="section-title">个人作品</div>
            <div
              v-for="(work, index) in parsedData.content.works"
              :key="index"
              class="preview-item"
            >
              <t-form-item label="作品名称">
                <t-input v-model="work.name" placeholder="请输入作品名称" />
              </t-form-item>
              <t-form-item label="作品描述">
                <t-textarea
                  v-model="work.description"
                  placeholder="请输入作品描述"
                  :autosize="{ minRows: 2 }"
                />
              </t-form-item>
              <t-form-item label="作品链接">
                <t-input v-model="work.url" placeholder="请输入作品链接" />
              </t-form-item>
            </div>
          </t-form>
        </div>
      </div>
    </div>
  </t-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { submitParseTask, getTaskStatus, createResumeFromParsed } from '@/api/ai'

const emit = defineEmits(['success', 'close'])
const router = useRouter()

const visible = ref(false)
const isDragOver = ref(false)
const currentTask = ref(null)
const parsedData = ref(null)
const saving = ref(false)
const uploading = ref(false)
const fileInput = ref(null)
// polling removed: component no longer polls task status periodically

// 是否正在处理中（pending 或 processing）
const isProcessing = computed(() => {
  return currentTask.value && ['pending', 'processing'].includes(currentTask.value.status)
})

const open = () => {
  visible.value = true
}

const handleClose = () => {
  visible.value = false
  resetState()
}

const resetState = () => {
  currentTask.value = null
  parsedData.value = null
}

const handleDragOver = () => {
  isDragOver.value = true
}

const handleDragLeave = () => {
  isDragOver.value = false
}

const handleDrop = (e) => {
  isDragOver.value = false
  const files = e.dataTransfer.files
  if (files.length > 0) {
    uploadFile(files[0])
  }
}

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (e) => {
  const files = e.target.files
  if (files.length > 0) {
    uploadFile(files[0])
  }
}

const uploadFile = (file) => {
  uploading.value = true
  MessagePlugin.info('正在提交解析任务...')

  return submitParseTask(file)
    .then(res => {
      MessagePlugin.success('任务提交成功，正在跳转到任务列表以便跟踪进度')
      uploading.value = false
      // 通知父组件（用于刷新列表）
      emit('success', res)
      // 关闭当前导入对话框
      handleClose()
      // 跳转到解析任务列表，并带上新任务的 id 以便高亮/定位（父页面可以读取）
      router.push({ path: '/parse-tasks', query: { taskId: res.id } })
    })
    .catch(err => {
      MessagePlugin.error('任务提交失败: ' + (err.message || '未知错误'))
      uploading.value = false
      throw err
    })
}

// polling helpers removed; status updates should be fetched via parents/list page when needed

const handleViewResult = () => {
  if (currentTask.value && currentTask.value.parseResult) {
    parsedData.value = currentTask.value.parseResult
  }
}

const handleBackToTask = () => {
  parsedData.value = null
}

const handleRetry = () => {
  // 重新上传文件
  parsedData.value = null
  currentTask.value = null
}

const handleCancel = () => {
  if (isProcessing.value) {
    MessagePlugin.warning('任务正在进行中，请等待完成')
    return
  }
  handleClose()
}

const handleConfirm = () => {
  if (!parsedData.value) return

  saving.value = true
  createResumeFromParsed(parsedData.value)
    .then(res => {
      MessagePlugin.success('简历导入成功')
      emit('success', res)
      handleClose()
    })
    .catch(err => {
      MessagePlugin.error('简历导入失败: ' + (err.message || '未知错误'))
    })
    .finally(() => {
      saving.value = false
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

// no cleanup required for polling

defineExpose({ open })
</script>

<style scoped>
.import-container {
  padding: 20px 0;
}

.upload-area {
  min-height: 400px;
}

.upload-zone {
  border: 2px dashed var(--td-component-border);
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  transition: all 0.3s;
}

.upload-zone:hover,
.upload-zone.drag-over {
  border-color: var(--td-brand-color);
  background: var(--td-bg-color-container-hover);
}

.upload-text {
  font-size: 16px;
  color: var(--td-text-color-primary);
  margin: 20px 0 10px;
}

.upload-hint {
  color: var(--td-text-color-secondary);
  margin: 10px 0;
}

.upload-tips {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
  margin-top: 30px;
}

.progress-area {
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.progress-header {
  margin-bottom: 40px;
  text-align: center;
}

.file-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--td-text-color-primary);
}

.progress-text {
  color: var(--td-text-color-secondary);
  font-size: 14px;
}

.progress-bar {
  width: 100%;
  max-width: 500px;
  margin-bottom: 30px;
}

.error-message {
  width: 100%;
  max-width: 500px;
  margin-bottom: 20px;
}

.progress-actions {
  margin-top: 20px;
}

.preview-area {
  display: flex;
  flex-direction: column;
  max-height: 600px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--td-component-border);
  margin-bottom: 20px;
}

.preview-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.preview-content {
  flex: 1;
  overflow-y: auto;
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


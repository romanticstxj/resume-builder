<template>
  <div class="resume-editor">
    <div class="editor-header">
      <t-button variant="text" @click="handleBack">
        <template #icon><t-icon name="chevron-left" /></template>
        返回
      </t-button>
      <div class="header-actions">
        <t-button theme="default" @click="handleSave" :loading="saving">
          <template #icon><t-icon name="save" /></template>
          保存
        </t-button>
        <t-button theme="primary" @click="handlePreview" :disabled="!resumeId">
          预览
        </t-button>
        <t-dropdown :options="exportOptions" @click="handleExport">
          <t-button theme="primary" variant="outline">
            <template #icon><t-icon name="download" /></template>
            导出
            <t-icon name="chevron-down" style="margin-left: 4px" />
          </t-button>
        </t-dropdown>
      </div>
    </div>

    <div class="editor-content">
      <div class="left-panel">
        <div class="panel-header">
          <h3>基本信息</h3>
        </div>
        <t-form :data="resumeData" label-width="80px">
          <t-form-item label="简历标题">
            <t-input v-model="resumeData.title" placeholder="请输入简历标题" />
          </t-form-item>
          
          <t-form-item label="选择模板">
            <t-select
              v-model="resumeData.templateId"
              placeholder="请选择模板"
              clearable
              :loading="templateLoading"
              @focus="fetchTemplates"
              @change="handleTemplateChange"
            >
              <t-option
                v-for="template in templates"
                :key="template.id"
                :value="template.id"
                :label="template.name"
              />
            </t-select>
          </t-form-item>

          <div class="section-title">个人信息</div>
          <t-form-item label="姓名">
            <t-input v-model="resumeData.content.name" placeholder="请输入姓名" />
          </t-form-item>
          <t-form-item label="邮箱">
            <t-input v-model="resumeData.content.email" placeholder="请输入邮箱" />
          </t-form-item>
          <t-form-item label="电话">
            <t-input v-model="resumeData.content.phone" placeholder="请输入电话" />
          </t-form-item>

          <div class="section-title">个人简介</div>
          <t-form-item label="简介">
            <t-textarea
              v-model="resumeData.content.summary"
              placeholder="请输入个人简介"
              :autosize="{ minRows: 4 }"
            />
          </t-form-item>

          <div class="section-title">工作经历</div>
          <div
            v-for="(exp, index) in resumeData.content.experience"
            :key="index"
            class="experience-item"
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
                :autosize="{ minRows: 3 }"
              />
            </t-form-item>
            <t-button
              theme="danger"
              variant="text"
              size="small"
              @click="removeExperience(index)"
            >
              删除此经历
            </t-button>
          </div>
          <t-button theme="default" variant="dashed" block @click="addExperience">
            <template #icon><t-icon name="add" /></template>
            添加工作经历
          </t-button>

          <div class="section-title">教育经历</div>
          <div
            v-for="(edu, index) in resumeData.content.education"
            :key="index"
            class="education-item"
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
            <t-button
              theme="danger"
              variant="text"
              size="small"
              @click="removeEducation(index)"
            >
              删除此经历
            </t-button>
          </div>
          <t-button theme="default" variant="dashed" block @click="addEducation">
            <template #icon><t-icon name="add" /></template>
            添加教育经历
          </t-button>
        </t-form>
      </div>

      <div class="right-panel">
        <div class="preview-container">
          <div class="preview-placeholder" v-if="!resumeId">
            <t-icon name="eye-opened" size="48px" />
            <p>保存后可预览</p>
          </div>
          <ResumeRenderer
            v-else
            :resume-data="resumeData"
            :template-config="currentTemplate"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getResumeById, createResume, updateResume } from '@/api/resume'
import { getTemplateList, getTemplateById } from '@/api/template'
import { MessagePlugin } from 'tdesign-vue-next'
import ResumeRenderer from '@/components/ResumeRenderer.vue'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const resumeId = computed(() => isEdit.value ? Number(route.params.id) : null)

const loading = ref(false)
const saving = ref(false)
const templateLoading = ref(false)
const templates = ref([])
const currentTemplate = ref({})

const resumeData = ref({
  title: '',
  templateId: null,
  content: {
    name: '',
    email: '',
    phone: '',
    summary: '',
    experience: [],
    education: []
  }
})

const exportOptions = [
  { content: '导出 PDF', value: 'pdf' },
  { content: '导出 Word', value: 'word' }
]

const fetchResume = async () => {
  if (!isEdit.value) return

  loading.value = true
  try {
    const res = await getResumeById(resumeId.value)
    resumeData.value = {
      title: res.title,
      templateId: res.templateId,
      content: typeof res.content === 'string' ? JSON.parse(res.content) : (res.content || resumeData.value.content)
    }
    // 加载模板配置
    if (res.templateId) {
      const template = await getTemplateById(res.templateId)
      currentTemplate.value = template
    }
  } catch (error) {
    MessagePlugin.error('获取简历失败')
  } finally {
    loading.value = false
  }
}

const fetchTemplates = async () => {
  if (templates.value.length > 0) return

  templateLoading.value = true
  try {
    const res = await getTemplateList({ page: 1, pageSize: 100 })
    templates.value = Array.isArray(res) ? res : []
  } catch (error) {
    MessagePlugin.error('获取模板失败')
  } finally {
    templateLoading.value = false
  }
}

// 检查是否从模板创建简历
const initFromTemplate = async () => {
  const templateId = route.query.templateId
  if (templateId && !isEdit.value) {
    try {
      const template = await getTemplateById(templateId)
      if (template) {
        resumeData.value.templateId = template.id
        resumeData.value.title = template.name + '的简历'
        currentTemplate.value = template
        // 解析模板内容作为初始内容
        try {
          const templateContent = typeof template.content === 'string' ? JSON.parse(template.content) : template.content
          resumeData.value.content = { ...resumeData.value.content, ...templateContent }
        } catch (e) {
          console.error('解析模板内容失败:', e)
        }
        MessagePlugin.success('已应用模板')
      }
    } catch (error) {
      console.error('加载模板失败:', error)
    }
  }
}

const handleSave = async () => {
  if (!resumeData.value.title) {
    MessagePlugin.warning('请输入简历标题')
    return
  }

  saving.value = true
  try {
    const saveData = {
      ...resumeData.value,
      content: JSON.stringify(resumeData.value.content)
    }
    if (isEdit.value) {
      await updateResume(resumeId.value, saveData)
      MessagePlugin.success('更新成功')
    } else {
      const res = await createResume(saveData)
      resumeId.value = res.id
      router.replace(`/resumes/${res.id}/edit`)
      MessagePlugin.success('创建成功')
    }
  } catch (error) {
    MessagePlugin.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    saving.value = false
  }
}

const handlePreview = async () => {
  if (!resumeId.value) {
    MessagePlugin.warning('请先保存简历')
    return
  }
  try {
    const response = await fetch(`/api/resumes/${resumeId.value}/preview`)
    const html = await response.text()
    const previewWindow = window.open('', '_blank')
    previewWindow.document.write(html)
    previewWindow.document.close()
  } catch (error) {
    MessagePlugin.error('预览失败')
  }
}

const handleExport = async ({ value }) => {
  if (!resumeId.value) {
    MessagePlugin.warning('请先保存简历')
    return
  }

  try {
    const response = await fetch(`/api/resumes/${resumeId.value}/export/${value}`)
    if (!response.ok) throw new Error('导出失败')

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${resumeData.value.title}.${value === 'word' ? 'docx' : 'html'}`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)

    MessagePlugin.success('导出成功')
  } catch (error) {
    MessagePlugin.error('导出失败')
  }
}

const handleBack = () => {
  router.push('/resumes')
}

const handleTemplateChange = async (templateId) => {
  if (!templateId) {
    currentTemplate.value = {}
    return
  }
  try {
    const template = await getTemplateById(templateId)
    currentTemplate.value = template
  } catch (error) {
    console.error('加载模板失败:', error)
  }
}

const addExperience = () => {
  if (!resumeData.value.content.experience) {
    resumeData.value.content.experience = []
  }
  resumeData.value.content.experience.push({
    company: '',
    position: '',
    period: '',
    description: ''
  })
}

const removeExperience = (index) => {
  resumeData.value.content.experience.splice(index, 1)
}

const addEducation = () => {
  if (!resumeData.value.content.education) {
    resumeData.value.content.education = []
  }
  resumeData.value.content.education.push({
    school: '',
    major: '',
    degree: '',
    period: ''
  })
}

const removeEducation = (index) => {
  resumeData.value.content.education.splice(index, 1)
}

onMounted(() => {
  fetchResume()
  fetchTemplates()
  initFromTemplate()
})
</script>

<style scoped>
.resume-editor {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid var(--td-border-level-1-color);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.editor-content {
  display: flex;
  min-height: calc(100vh - 180px);
}

.left-panel {
  flex: 1;
  padding: 24px;
  border-right: 1px solid var(--td-border-level-1-color);
  overflow-y: auto;
  max-height: calc(100vh - 180px);
}

.panel-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 24px 0 16px;
  padding-top: 16px;
  border-top: 1px dashed var(--td-border-level-1-color);
}

.experience-item,
.education-item {
  background: #f9f9f9;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.right-panel {
  width: 400px;
  background: #f5f7fa;
  padding: 24px;
  overflow-y: auto;
  max-height: calc(100vh - 180px);
}

.preview-container {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.preview-placeholder {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.preview-placeholder p {
  margin-top: 16px;
}

.preview-content h2 {
  font-size: 24px;
  margin-bottom: 8px;
  text-align: center;
}

.preview-meta {
  text-align: center;
  color: #666;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #333;
}

.preview-section {
  margin-bottom: 24px;
}

.preview-section h4 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
  padding-left: 12px;
  border-left: 3px solid var(--td-brand-color);
}

.preview-item {
  margin-bottom: 16px;
}

.preview-item h5 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.item-meta {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}
</style>

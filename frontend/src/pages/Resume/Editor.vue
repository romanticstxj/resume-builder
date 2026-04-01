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
          <h3>{{ uiLabels.basicInfo }}</h3>
        </div>
        <t-form :data="resumeData" label-width="80px">
          <t-form-item :label="uiLabels.resumeTitle">
            <t-input v-model="resumeData.title" placeholder="请输入简历标题" />
          </t-form-item>

          <t-form-item :label="uiLabels.selectTemplate">
            <t-select v-model="resumeData.templateId" placeholder="请选择模板" clearable :loading="templateLoading" @focus="fetchTemplates" @change="handleTemplateChange">
              <t-option v-for="template in templates" :key="template.id" :value="template.id" :label="template.name" />
            </t-select>
          </t-form-item>

          <t-form-item :label="uiLabels.language">
            <t-radio-group v-model="resumeData.content.language">
              <t-radio value="zh">中文</t-radio>
              <t-radio value="en">English</t-radio>
            </t-radio-group>
          </t-form-item>

          <!-- 个人信息 -->
          <div class="section-title">{{ uiLabels.personalInfo }}</div>
          <t-form-item :label="uiLabels.photo">
            <div class="photo-upload" @click="handlePhotoClick">
              <img v-if="resumeData.content.photo" :src="resumeData.content.photo" class="photo-preview" />
              <div v-else class="photo-placeholder">
                <t-icon name="user-circle" size="32px" />
                <span>{{ uiLabels.photoHint }}</span>
              </div>
            </div>
            <input ref="photoInput" type="file" accept="image/*" style="display:none" @change="onPhotoSelected" />
          </t-form-item>
          <t-form-item :label="uiLabels.name"><t-input v-model="resumeData.content.name" placeholder="请输入姓名" /></t-form-item>
          <t-form-item :label="uiLabels.email"><t-input v-model="resumeData.content.email" placeholder="请输入邮箱" /></t-form-item>
          <t-form-item :label="uiLabels.phone"><t-input v-model="resumeData.content.phone" placeholder="请输入电话" /></t-form-item>
          <t-form-item :label="uiLabels.city"><t-input v-model="resumeData.content.city" placeholder="如：上海 / Shanghai" /></t-form-item>
          <t-form-item :label="uiLabels.jobTarget"><t-input v-model="resumeData.content.jobTarget" placeholder="如：前端工程师 / Frontend Engineer" /></t-form-item>

          <!-- 个人简介 -->
          <div class="section-title">{{ t('summary') }}</div>
          <t-form-item :label="uiLabels.description">
            <RichTextEditor v-model="resumeData.content.summary" />
          </t-form-item>

          <!-- 专业技能 -->
          <div class="section-title">{{ t('skills') }}</div>
          <t-form-item :label="uiLabels.skillName">
            <RichTextEditor v-model="resumeData.content.skillsText" />
          </t-form-item>

          <!-- 工作经历 -->
          <div class="section-title">{{ t('experience') }}</div>
          <div v-for="(exp, index) in resumeData.content.experience" :key="index" class="experience-item">
            <t-form-item :label="uiLabels.company"><t-input v-model="exp.company" placeholder="请输入公司名称" /></t-form-item>
            <t-form-item :label="uiLabels.position"><t-input v-model="exp.position" placeholder="请输入职位" /></t-form-item>
            <t-form-item :label="uiLabels.period"><t-input v-model="exp.period" placeholder="如：2020-2023" /></t-form-item>
            <t-form-item :label="uiLabels.description"><RichTextEditor v-model="exp.description" /></t-form-item>
            <t-button theme="danger" variant="text" size="small" @click="removeExperience(index)">{{ uiLabels.deleteBtn }}</t-button>
          </div>
          <t-button theme="default" variant="dashed" block @click="addExperience">
            <template #icon><t-icon name="add" /></template>
            {{ uiLabels.addBtn }} {{ t('experience') }}
          </t-button>

          <!-- 项目经历 -->
          <div class="section-title">{{ t('projects') }}</div>
          <div v-for="(proj, index) in resumeData.content.projects" :key="index" class="experience-item">
            <t-form-item :label="uiLabels.projectName"><t-input v-model="proj.name" placeholder="请输入项目名称" /></t-form-item>
            <t-form-item :label="uiLabels.projectCompany"><t-input v-model="proj.company" :placeholder="uiLang === 'en' ? 'e.g. Google, Alibaba' : '如：阿里巴巴、字节跳动'" /></t-form-item>
            <t-form-item :label="uiLabels.projectRole"><t-input v-model="proj.title" placeholder="如：前端负责人、全栈开发" /></t-form-item>
            <t-form-item :label="uiLabels.period"><t-input v-model="proj.period" placeholder="如：2022-2023" /></t-form-item>
            <t-form-item :label="uiLabels.projectDesc"><RichTextEditor v-model="proj.description" /></t-form-item>
            <t-form-item :label="uiLabels.techStack"><t-input v-model="proj.technologiesString" placeholder="请输入技术栈，用逗号分隔" /></t-form-item>
            <t-button theme="danger" variant="text" size="small" @click="removeProject(index)">{{ uiLabels.deleteBtn }}</t-button>
          </div>
          <t-button theme="default" variant="dashed" block @click="addProject">
            <template #icon><t-icon name="add" /></template>
            {{ uiLabels.addBtn }} {{ t('projects') }}
          </t-button>

          <!-- 个人作品 -->
          <div class="section-title">{{ t('works') }}</div>
          <div v-for="(work, index) in resumeData.content.works" :key="index" class="education-item">
            <t-form-item :label="uiLabels.workName"><t-input v-model="work.name" placeholder="请输入作品名称" /></t-form-item>
            <t-form-item :label="uiLabels.workRole"><t-input v-model="work.role" :placeholder="uiLang === 'en' ? 'e.g. Lead Developer' : '如：主要开发者、设计师'" /></t-form-item>
            <t-form-item :label="uiLabels.workDesc"><RichTextEditor v-model="work.description" /></t-form-item>
            <t-form-item :label="uiLabels.workUrl"><t-input v-model="work.url" placeholder="请输入作品链接" /></t-form-item>
            <t-button theme="danger" variant="text" size="small" @click="removeWork(index)">{{ uiLabels.deleteBtn }}</t-button>
          </div>
          <t-button theme="default" variant="dashed" block @click="addWork">
            <template #icon><t-icon name="add" /></template>
            {{ uiLabels.addBtn }} {{ t('works') }}
          </t-button>

          <!-- 教育经历 -->
          <div class="section-title">{{ t('education') }}</div>
          <div v-for="(edu, index) in resumeData.content.education" :key="index" class="education-item">
            <t-form-item :label="uiLabels.school"><t-input v-model="edu.school" placeholder="请输入学校名称" /></t-form-item>
            <t-form-item :label="uiLabels.major"><t-input v-model="edu.major" placeholder="请输入专业" /></t-form-item>
            <t-form-item :label="uiLabels.degree"><t-input v-model="edu.degree" placeholder="如：本科" /></t-form-item>
            <t-form-item :label="uiLabels.period"><t-input v-model="edu.period" placeholder="如：2016-2020" /></t-form-item>
            <t-button theme="danger" variant="text" size="small" @click="removeEducation(index)">{{ uiLabels.deleteBtn }}</t-button>
          </div>
          <t-button theme="default" variant="dashed" block @click="addEducation">
            <template #icon><t-icon name="add" /></template>
            {{ uiLabels.addBtn }} {{ t('education') }}
          </t-button>

          <!-- 荣誉证书 -->
          <div class="section-title">{{ t('honors') }}</div>
          <div v-for="(honor, index) in resumeData.content.honors" :key="index" class="education-item">
            <t-form-item :label="uiLabels.honorName"><t-input v-model="honor.name" placeholder="请输入奖项名称" /></t-form-item>
            <t-form-item :label="uiLabels.honorDate"><t-input v-model="honor.date" placeholder="如：2023年" /></t-form-item>
            <t-form-item :label="uiLabels.honorDesc"><RichTextEditor v-model="honor.description" /></t-form-item>
            <t-button theme="danger" variant="text" size="small" @click="removeHonor(index)">{{ uiLabels.deleteBtn }}</t-button>
          </div>
          <t-button theme="default" variant="dashed" block @click="addHonor">
            <template #icon><t-icon name="add" /></template>
            {{ uiLabels.addBtn }} {{ t('honors') }}
          </t-button>
        </t-form>
      </div>

      <!-- 拖拽分隔条 -->
      <div class="resize-handle" @mousedown="startResize"></div>

      <div class="right-panel" :style="{ width: rightPanelWidth + 'px' }">
        <div class="preview-container">
          <div class="preview-placeholder" v-if="!resumeId">
            <t-icon name="eye-opened" size="48px" />
            <p>保存后可预览</p>
          </div>
          <ResumeRenderer v-else :resume-data="resumeData" :template-config="currentTemplate" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getResumeById, createResume, updateResume, previewResume, exportResumeWord } from '@/api/resume'
import { getTemplateList, getTemplateById } from '@/api/template'
import { MessagePlugin } from 'tdesign-vue-next'
import ResumeRenderer from '@/components/ResumeRenderer.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import { getSectionLabel, DEFAULT_SECTION_ORDER } from '@/config/sectionRegistry'

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
    name: '', email: '', phone: '', photo: '', city: '', jobTarget: '',
    summary: '', experience: [], education: [], projects: [],
    skills: [], skillsText: '', honors: [], works: []
  }
})

const uiLang = computed(() => resumeData.value.content.language === 'en' ? 'en' : 'zh')
const t = (key) => getSectionLabel(key, uiLang.value)

const uiLabels = computed(() => uiLang.value === 'en' ? {
  basicInfo: 'Basic Info', resumeTitle: 'Resume Title', selectTemplate: 'Template',
  language: 'Language', personalInfo: 'Personal Info', name: 'Name', email: 'Email',
  phone: 'Phone', photo: 'Photo', photoHint: 'Click to upload photo',
  addBtn: 'Add', deleteBtn: 'Delete', company: 'Company', position: 'Position',
  period: 'Period', description: 'Description', school: 'School', major: 'Major',
  degree: 'Degree', projectName: 'Project', projectRole: 'Role', projectCompany: 'Company', projectDesc: 'Description',
  techStack: 'Tech Stack', skillName: 'Skill', proficiency: 'Level',
  honorName: 'Award', honorDate: 'Date', honorDesc: 'Description',
  workName: 'Work', workDesc: 'Description', workUrl: 'URL', workRole: 'Role',
  city: 'City', jobTarget: 'Target Role',
} : {
  basicInfo: '基本信息', resumeTitle: '简历标题', selectTemplate: '选择模板',
  language: '简历语言', personalInfo: '个人信息', name: '姓名', email: '邮箱',
  phone: '电话', photo: '照片', photoHint: '点击上传照片',
  addBtn: '添加', deleteBtn: '删除', company: '公司名称', position: '职位',
  period: '时间', description: '描述', school: '学校名称', major: '专业',
  degree: '学位', projectName: '项目名称', projectRole: '担任角色', projectCompany: '所属公司', projectDesc: '项目描述',
  techStack: '技术栈', skillName: '技能名称', proficiency: '熟练度',
  honorName: '奖项名称', honorDate: '获奖时间', honorDesc: '奖项描述',
  workName: '作品名称', workDesc: '作品描述', workUrl: '作品链接', workRole: '担任角色',
  city: '所在城市', jobTarget: '求职方向',
})

const photoInput = ref(null)
const handlePhotoClick = () => { photoInput.value.value = ''; photoInput.value.click() }
const onPhotoSelected = (e) => {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 2 * 1024 * 1024) { MessagePlugin.warning('照片不超过 2MB'); return }
  const reader = new FileReader()
  reader.onload = (evt) => { resumeData.value.content.photo = evt.target.result }
  reader.readAsDataURL(file)
}

const exportOptions = [
  { content: '导出 PDF', value: 'pdf' },
  { content: '导出 Word', value: 'word' },
  { content: '导出 JSON', value: 'json' }
]

const parseTemplateConfig = (template) => {
  const rawOrder = typeof template.sectionOrder === 'string' ? JSON.parse(template.sectionOrder) : template.sectionOrder
  const knownKeys = DEFAULT_SECTION_ORDER
  let order = Array.isArray(rawOrder) ? rawOrder.filter(k => knownKeys.includes(k)) : []
  knownKeys.forEach(k => { if (!order.includes(k)) order.push(k) })
  return {
    layout: template.layout || 'classic',
    themeConfig: typeof template.themeConfig === 'string' ? JSON.parse(template.themeConfig) : template.themeConfig,
    sectionConfig: typeof template.sectionConfig === 'string' ? JSON.parse(template.sectionConfig) : template.sectionConfig,
    sectionOrder: order
  }
}

const handleExport = async ({ value }) => {
  if (!resumeId.value) { MessagePlugin.warning('请先保存简历'); return }
  try {
    const saveData = { ...resumeData.value, content: JSON.stringify(resumeData.value.content) }
    await updateResume(resumeId.value, saveData)
  } catch { MessagePlugin.warning('自动保存失败，导出内容可能不是最新的') }
  try {
    if (value === 'pdf') {
      const html = await previewResume(resumeId.value)
      const w = window.open('', '_blank')
      if (w) { w.document.write(html); w.document.close() }
      else MessagePlugin.error('无法打开打印窗口，请允许弹出窗口')
    } else if (value === 'word') {
      const blob = await exportResumeWord(resumeId.value)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url; a.download = `${resumeData.value.title}.doc`
      document.body.appendChild(a); a.click(); document.body.removeChild(a)
      URL.revokeObjectURL(url); MessagePlugin.success('导出成功')
    } else if (value === 'json') {
      const { photo, ...contentWithoutPhoto } = resumeData.value.content
      const payload = { _version: '1', title: resumeData.value.title, content: contentWithoutPhoto }
      const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url; a.download = `${resumeData.value.title}.resume.json`
      document.body.appendChild(a); a.click(); document.body.removeChild(a)
      URL.revokeObjectURL(url); MessagePlugin.success('JSON 导出成功')
    }
  } catch (error) { console.error('导出失败:', error); MessagePlugin.error('导出失败') }
}

const fetchResume = async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const res = await getResumeById(resumeId.value)
    const loaded = typeof res.content === 'string' ? JSON.parse(res.content) : (res.content || {})
    if (Array.isArray(loaded.skills) && !loaded.skillsText) {
      loaded.skillsText = loaded.skills.map(s => s.name || s).filter(Boolean).join(', ')
    }
    resumeData.value = {
      title: res.title, templateId: res.templateId,
      content: { ...resumeData.value.content, ...loaded }
    }
    if (res.templateId) {
      const template = await getTemplateById(res.templateId)
      currentTemplate.value = parseTemplateConfig(template)
    }
  } catch (error) { MessagePlugin.error('获取简历失败') }
  finally { loading.value = false }
}

const fetchTemplates = async () => {
  if (templates.value.length > 0) return
  templateLoading.value = true
  try {
    const res = await getTemplateList({ page: 1, pageSize: 100 })
    templates.value = Array.isArray(res) ? res : []
  } catch (error) { MessagePlugin.error('获取模板失败') }
  finally { templateLoading.value = false }
}

const initFromTemplate = async () => {
  const templateId = route.query.templateId
  if (templateId && !isEdit.value) {
    try {
      const template = await getTemplateById(templateId)
      if (template) {
        resumeData.value.templateId = template.id
        resumeData.value.title = template.name + '的简历'
        currentTemplate.value = parseTemplateConfig(template)
        MessagePlugin.success('已应用模板')
      }
    } catch (error) { console.error('加载模板失败:', error) }
  }
}

const handleSave = async () => {
  if (!resumeData.value.title) { MessagePlugin.warning('请输入简历标题'); return }
  if (!resumeData.value.content.name) MessagePlugin.warning('请输入姓名')
  saving.value = true
  try {
    const saveData = { ...resumeData.value, content: JSON.stringify(resumeData.value.content) }
    if (isEdit.value) {
      await updateResume(resumeId.value, saveData); MessagePlugin.success('更新成功')
    } else {
      const res = await createResume(saveData)
      router.replace(`/resumes/${res.id}/edit`); MessagePlugin.success('创建成功')
    }
  } catch (error) { MessagePlugin.error(isEdit.value ? '更新失败' : '创建失败') }
  finally { saving.value = false }
}

const handlePreview = async () => {
  if (!resumeId.value) { MessagePlugin.warning('请先保存简历'); return }
  try {
    const html = await previewResume(resumeId.value)
    const w = window.open('', '_blank')
    if (w) { w.document.write(html); w.document.close() }
    else MessagePlugin.error('无法打开预览窗口，请允许弹出窗口')
  } catch (error) { MessagePlugin.error('预览失败') }
}

const handleBack = () => router.push('/resumes')

const handleTemplateChange = async (templateId) => {
  if (!templateId) { currentTemplate.value = {}; return }
  try {
    const template = await getTemplateById(templateId)
    currentTemplate.value = parseTemplateConfig(template)
  } catch (error) { console.error('加载模板失败:', error) }
}

const addExperience = () => {
  if (!resumeData.value.content.experience) resumeData.value.content.experience = []
  resumeData.value.content.experience.push({ company: '', position: '', period: '', description: '' })
}
const removeExperience = (index) => resumeData.value.content.experience.splice(index, 1)

const addEducation = () => {
  if (!resumeData.value.content.education) resumeData.value.content.education = []
  resumeData.value.content.education.push({ school: '', major: '', degree: '', period: '' })
}
const removeEducation = (index) => resumeData.value.content.education.splice(index, 1)

const addProject = () => {
  if (!resumeData.value.content.projects) resumeData.value.content.projects = []
  resumeData.value.content.projects.push({ name: '', company: '', title: '', period: '', description: '', technologiesString: '' })
}
const removeProject = (index) => resumeData.value.content.projects.splice(index, 1)

const addHonor = () => {
  if (!resumeData.value.content.honors) resumeData.value.content.honors = []
  resumeData.value.content.honors.push({ name: '', date: '', description: '' })
}
const removeHonor = (index) => resumeData.value.content.honors.splice(index, 1)

const addWork = () => {
  if (!resumeData.value.content.works) resumeData.value.content.works = []
  resumeData.value.content.works.push({ name: '', role: '', description: '', url: '' })
}
const removeWork = (index) => resumeData.value.content.works.splice(index, 1)

const rightPanelWidth = ref(420)

const startResize = (e) => {
  e.preventDefault()
  const startX = e.clientX
  const startWidth = rightPanelWidth.value
  const onMove = (ev) => {
    // moving left increases right panel width
    const delta = startX - ev.clientX
    rightPanelWidth.value = Math.max(280, Math.min(900, startWidth + delta))
  }
  const onUp = () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  }
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

onMounted(() => { fetchResume(); fetchTemplates(); initFromTemplate() })
</script>

<style scoped>
.resume-editor { background: #fff; border-radius: 8px; overflow: hidden; }
.editor-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; border-bottom: 1px solid var(--td-border-level-1-color); }
.header-actions { display: flex; gap: 12px; }
.editor-content { display: flex; min-height: calc(100vh - 180px); }
.left-panel { flex: 1; padding: 24px; border-right: 1px solid var(--td-border-level-1-color); overflow-y: auto; max-height: calc(100vh - 180px); }
.panel-header h3 { font-size: 18px; font-weight: 600; margin-bottom: 20px; }
.section-title { font-size: 16px; font-weight: 600; margin: 24px 0 16px; padding-top: 16px; border-top: 1px dashed var(--td-border-level-1-color); }
.experience-item, .education-item { background: #f9f9f9; padding: 16px; border-radius: 8px; margin-bottom: 12px; }
.right-panel { background: #f5f7fa; padding: 24px; overflow-y: auto; max-height: calc(100vh - 180px); flex-shrink: 0; }
.resize-handle { width: 5px; background: var(--td-border-level-1-color); cursor: col-resize; flex-shrink: 0; transition: background 0.2s; }
.resize-handle:hover { background: var(--td-brand-color); }
.preview-container { background: #fff; border-radius: 8px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.preview-placeholder { text-align: center; padding: 80px 20px; color: #999; }
.preview-placeholder p { margin-top: 16px; }
.photo-upload { width: 80px; height: 100px; border: 2px dashed var(--td-border-level-2-color); border-radius: 6px; cursor: pointer; overflow: hidden; display: flex; align-items: center; justify-content: center; transition: border-color 0.2s; }
.photo-upload:hover { border-color: var(--td-brand-color); }
.photo-preview { width: 100%; height: 100%; object-fit: cover; }
.photo-placeholder { display: flex; flex-direction: column; align-items: center; gap: 4px; color: #999; font-size: 11px; text-align: center; padding: 8px; }
</style>

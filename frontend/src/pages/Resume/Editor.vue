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

          <div class="section-title">项目经历</div>
          <div
            v-for="(proj, index) in resumeData.content.projects"
            :key="index"
            class="project-item"
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
                :autosize="{ minRows: 3 }"
              />
            </t-form-item>
            <t-form-item label="技术栈">
              <t-input v-model="proj.technologiesString" placeholder="请输入技术栈，用逗号分隔" />
            </t-form-item>
            <t-button
              theme="danger"
              variant="text"
              size="small"
              @click="removeProject(index)"
            >
              删除此项目
            </t-button>
          </div>
          <t-button theme="default" variant="dashed" block @click="addProject">
            <template #icon><t-icon name="add" /></template>
            添加项目经历
          </t-button>

          <div class="section-title">专业技能</div>
          <div
            v-for="(skill, index) in resumeData.content.skills"
            :key="index"
            class="skill-item"
          >
            <t-form-item label="技能名称">
              <t-input v-model="skill.name" placeholder="请输入技能名称" />
            </t-form-item>
            <t-form-item label="熟练度">
              <t-slider v-model.number="skill.level" :min="0" :max="100" :marks="{0: '0%', 50: '50%', 100: '100%'}" />
            </t-form-item>
            <t-button
              theme="danger"
              variant="text"
              size="small"
              @click="removeSkill(index)"
            >
              删除此技能
            </t-button>
          </div>
          <t-button theme="default" variant="dashed" block @click="addSkill">
            <template #icon><t-icon name="add" /></template>
            添加专业技能
          </t-button>

          <div class="section-title">个人总结</div>
          <t-form-item label="总结">
            <t-textarea
              v-model="resumeData.content.personalSummary"
              placeholder="请输入个人总结"
              :autosize="{ minRows: 4 }"
            />
          </t-form-item>

          <div class="section-title">荣誉奖项</div>
          <div
            v-for="(honor, index) in resumeData.content.honors"
            :key="index"
            class="honor-item"
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
            <t-button
              theme="danger"
              variant="text"
              size="small"
              @click="removeHonor(index)"
            >
              删除此奖项
            </t-button>
          </div>
          <t-button theme="default" variant="dashed" block @click="addHonor">
            <template #icon><t-icon name="add" /></template>
            添加荣誉奖项
          </t-button>

          <div class="section-title">个人作品</div>
          <div
            v-for="(work, index) in resumeData.content.works"
            :key="index"
            class="work-item"
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
            <t-button
              theme="danger"
              variant="text"
              size="small"
              @click="removeWork(index)"
            >
              删除此作品
            </t-button>
          </div>
          <t-button theme="default" variant="dashed" block @click="addWork">
            <template #icon><t-icon name="add" /></template>
            添加个人作品
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
import { getResumeById, createResume, updateResume, previewResume, exportResumeWord } from '@/api/resume'
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
    education: [],
    projects: [],
    skills: [],
    personalSummary: '',
    honors: [],
    works: []
  }
})

const exportOptions = [
  { content: '导出 PDF', value: 'pdf' },
  { content: '导出 Word', value: 'word' }
]

const handleExport = async ({ value }) => {
  if (!resumeId.value) {
    MessagePlugin.warning('请先保存简历')
    return
  }

  try {
    if (value === 'pdf') {
      // PDF导出：从后端获取HTML，用浏览器打印
      const html = await previewResume(resumeId.value)
      const printWindow = window.open('', '_blank')
      if (printWindow) {
        printWindow.document.write(html)
        printWindow.document.close()
      } else {
        MessagePlugin.error('无法打开打印窗口，请允许弹出窗口')
      }
    } else if (value === 'word') {
      // Word导出：从后端获取blob并触发下载
      const blob = await exportResumeWord(resumeId.value)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${resumeData.value.title}.docx`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
      MessagePlugin.success('导出成功')
    }
  } catch (error) {
    console.error('导出失败:', error)
    MessagePlugin.error('导出失败')
  }
}

const generateWordHtml = () => {
  // 使用与PDF相同的HTML结构，确保样式一致
  const themeConfig = currentTemplate.value.themeConfig || {}
  const sectionConfig = currentTemplate.value.sectionConfig || {}
  const fontSizePx = typeof themeConfig.fontSize === 'number' ? `${themeConfig.fontSize}px` : (themeConfig.fontSize || '14px')
  const primaryColor = themeConfig.primaryColor || '#2c3e50'
  const secondaryColor = themeConfig.secondaryColor || '#3498db'
  const textColor = themeConfig.textColor || '#333333'

  const hasHeaderBackground = sectionConfig.header?.background === true
  const isHeaderCenter = sectionConfig.header?.style === 'center'

  const html = `<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
<head>
  <meta charset="utf-8">
  <meta name=ProgId content=Word.Document>
  <meta name=Generator content="Microsoft Word 15">
  <meta name=Originator content="Microsoft Word 15">
  <title>${resumeData.value.title}</title>
  <style>
    @page {
      size: A4;
      margin: 0;
    }

    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }

    body {
      font-family: ${themeConfig.fontFamily || 'Arial, sans-serif'};
      font-size: ${fontSizePx};
      color: ${textColor};
      background: #fff;
      padding: 0;
      margin: 0;
    }

    .resume-page {
      width: 210mm;
      min-height: 297mm;
      margin: 0 auto;
      background: #fff;
      padding: ${hasHeaderBackground ? '0 40px 40px 40px' : '40px'};
      position: relative;
    }

    .resume-header {
      margin-bottom: 30px;
      ${isHeaderCenter ? 'text-align: center;' : ''}
      ${hasHeaderBackground ? `
      background: ${primaryColor};
      color: #fff;
      padding: 40px;
      margin: -40px -40px 30px -40px;
      mso-background-themecolor: ${primaryColor};
      ` : ''}
    }

    .name {
      font-size: 32px;
      font-weight: 700;
      color: ${hasHeaderBackground ? '#fff' : primaryColor};
      margin: 0 0 10px;
      padding: 0;
      line-height: 1.2;
      ${hasHeaderBackground ? 'mso-color-alt: #fff;' : ''}
    }

    .contact-info {
      font-size: 14px;
      color: ${hasHeaderBackground ? 'rgba(255, 255, 255, 0.9)' : '#666'};
      margin: 0;
      padding: 0;
      line-height: 1.5;
      ${hasHeaderBackground ? 'mso-color-alt: rgba(255, 255, 255, 0.9);' : ''}
    }

    .resume-section {
      margin-bottom: 25px;
      page-break-inside: avoid;
    }

    .section-title {
      font-size: 18px;
      font-weight: 600;
      color: ${primaryColor};
      padding-bottom: 8px;
      margin-bottom: 15px;
      border-bottom: 2px solid ${primaryColor};
    }

    .section-content {
      line-height: 1.6;
      color: ${textColor};
    }

    .experience-item,
    .project-item,
    .honor-item,
    .work-item {
      margin-bottom: 20px;
    }

    .item-header {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      margin-bottom: 8px;
    }

    .item-title {
      font-size: 16px;
      font-weight: 600;
      margin: 0;
    }

    .item-period {
      font-size: 13px;
      color: #666;
    }

    .item-description,
    .item-meta {
      font-size: 14px;
      line-height: 1.6;
      color: ${textColor};
      margin: 0;
    }

    .education-item {
      margin-bottom: 15px;
    }

    .skills-container {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
    }

    .skill-item {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .skill-name {
      font-weight: 500;
    }

    .skill-level {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .skill-bar {
      width: 100px;
      height: 8px;
      background: #e0e0e0;
      border-radius: 4px;
      overflow: hidden;
    }

    .skill-fill {
      height: 100%;
      background: ${primaryColor};
    }

    .skill-text {
      font-size: 12px;
      color: #666;
    }

    .item-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 8px;
    }

    .item-tags .tag {
      display: inline-block;
      padding: 2px 8px;
      background: ${secondaryColor};
      color: #fff;
      border-radius: 3px;
      font-size: 12px;
    }

    .work-link {
      display: inline-block;
      margin-top: 8px;
      color: ${secondaryColor};
      text-decoration: none;
    }
  </style>
</head>
<body>
  <div class="resume-page">
    ${generateResumeContent()}
  </div>
</body>
</html>`
  return html
}

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
      const layout = template.layout || 'classic'
      const themeConfig = typeof template.themeConfig === 'string'
        ? JSON.parse(template.themeConfig)
        : template.themeConfig
      const sectionConfig = typeof template.sectionConfig === 'string'
        ? JSON.parse(template.sectionConfig)
        : template.sectionConfig
      const sectionOrder = typeof template.sectionOrder === 'string'
        ? JSON.parse(template.sectionOrder)
        : template.sectionOrder

      currentTemplate.value = {
        layout,
        themeConfig,
        sectionConfig,
        sectionOrder
      }
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

        // 加载模板配置
        const layout = template.layout || 'classic'
        const themeConfig = typeof template.themeConfig === 'string'
          ? JSON.parse(template.themeConfig)
          : template.themeConfig
        const sectionConfig = typeof template.sectionConfig === 'string'
          ? JSON.parse(template.sectionConfig)
          : template.sectionConfig
        const sectionOrder = typeof template.sectionOrder === 'string'
          ? JSON.parse(template.sectionOrder)
          : template.sectionOrder

        currentTemplate.value = {
          layout,
          themeConfig,
          sectionConfig,
          sectionOrder
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

  if (!resumeData.value.content.name) {
    MessagePlugin.warning('请输入姓名')
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
    const html = await previewResume(resumeId.value)
    const previewWindow = window.open('', '_blank')
    if (previewWindow) {
      previewWindow.document.write(html)
      previewWindow.document.close()
    } else {
      MessagePlugin.error('无法打开预览窗口，请允许弹出窗口')
    }
  } catch (error) {
    MessagePlugin.error('预览失败')
  }
}

const generateFullHtml = () => {
  const themeConfig = currentTemplate.value.themeConfig || {}
  const sectionConfig = currentTemplate.value.sectionConfig || {}
  const fontSizePx = typeof themeConfig.fontSize === 'number' ? `${themeConfig.fontSize}px` : (themeConfig.fontSize || '14px')
  const primaryColor = themeConfig.primaryColor || '#2c3e50'
  const secondaryColor = themeConfig.secondaryColor || '#3498db'
  const textColor = themeConfig.textColor || '#333333'

  const hasHeaderBackground = sectionConfig.header?.background === true
  const isHeaderCenter = sectionConfig.header?.style === 'center'

  const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${resumeData.value.title}</title>
  <style>
    @page {
      size: A4;
      margin: 0;
    }

    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }

    body {
      font-family: ${themeConfig.fontFamily || 'Arial, sans-serif'};
      font-size: ${fontSizePx};
      color: ${textColor};
      background: #fff;
      padding: 0;
      margin: 0;
    }

    .resume-page {
      width: 210mm;
      min-height: 297mm;
      margin: 0 auto;
      background: #fff;
      padding: ${hasHeaderBackground ? '0 40px 40px 40px' : '40px'};
      position: relative;
    }

    .resume-header {
      margin-bottom: 30px;
      ${isHeaderCenter ? 'text-align: center;' : ''}
      ${hasHeaderBackground ? `
      background: ${primaryColor};
      color: #fff;
      padding: 40px;
      margin: -40px -40px 30px -40px;
      ` : ''}
    }

    .name {
      font-size: 32px;
      font-weight: 700;
      color: ${hasHeaderBackground ? '#fff' : primaryColor};
      margin: 0 0 10px;
      padding: 0;
      line-height: 1.2;
    }

    .contact-info {
      font-size: 14px;
      color: ${hasHeaderBackground ? 'rgba(255, 255, 255, 0.9)' : '#666'};
      margin: 0;
      padding: 0;
      line-height: 1.5;
    }

    .resume-section {
      margin-bottom: 25px;
      page-break-inside: avoid;
    }

    .section-title {
      font-size: 18px;
      font-weight: 600;
      color: ${primaryColor};
      padding-bottom: 8px;
      margin-bottom: 15px;
      border-bottom: 2px solid ${primaryColor};
    }

    .section-content {
      line-height: 1.6;
      color: ${textColor};
    }

    .experience-item,
    .project-item,
    .honor-item,
    .work-item {
      margin-bottom: 20px;
    }

    .item-header {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      margin-bottom: 8px;
    }

    .item-title {
      font-size: 16px;
      font-weight: 600;
      margin: 0;
    }

    .item-period {
      font-size: 13px;
      color: #666;
    }

    .item-description,
    .item-meta {
      font-size: 14px;
      line-height: 1.6;
      color: ${textColor};
      margin: 0;
    }

    .education-item {
      margin-bottom: 15px;
    }

    .skills-container {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
    }

    .skills-list {
      flex-direction: column;
    }

    .skill-item {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .skills-list .skill-item {
      flex-direction: column;
      align-items: flex-start;
      gap: 4px;
    }

    .skill-name {
      font-weight: 500;
    }

    .skill-level {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .skill-bar {
      width: 100px;
      height: 8px;
      background: #e0e0e0;
      border-radius: 4px;
      overflow: hidden;
    }

    .skill-fill {
      height: 100%;
      background: ${primaryColor};
    }

    .skill-text {
      font-size: 12px;
      color: #666;
    }

    .item-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 8px;
    }

    .item-tags .tag {
      display: inline-block;
      padding: 2px 8px;
      background: ${secondaryColor};
      color: #fff;
      border-radius: 3px;
      font-size: 12px;
    }

    .work-link {
      display: inline-block;
      margin-top: 8px;
      color: ${secondaryColor};
      text-decoration: none;
    }

    @media print {
      body {
        background: #fff;
      }

      .resume-page {
        box-shadow: none;
        margin: 0;
        width: 100%;
      }

      .resume-header {
        -webkit-print-color-adjust: exact !important;
        print-color-adjust: exact !important;
      }
    }
  </style>
</head>
<body>
  <div class="resume-page">
    ${generateResumeContent()}
  </div>
  <script>
    window.onload = function() {
      setTimeout(function() {
        window.print();
      }, 100);
    };
  <\/script>
</body>
</html>`
  return html
}

const generateResumeContent = () => {
  const sectionOrder = currentTemplate.value.sectionOrder || ['header', 'summary', 'experience', 'education', 'projects', 'skills', 'personalSummary', 'honors', 'works']
  const content = resumeData.value.content
  let html = ''

  for (const sectionKey of sectionOrder) {
    switch (sectionKey) {
      case 'header':
        html += `
        <div class="resume-header">
          <h1 class="name">${content.name || '姓名'}</h1>
          <div class="contact-info">
            ${content.email || ''}${content.email && content.phone ? ' | ' : ''}${content.phone || ''}
          </div>
        </div>`
        break
      case 'summary':
        if (content.summary) {
          html += `
        <div class="resume-section">
          <h2 class="section-title">个人简介</h2>
          <p class="section-content">${content.summary}</p>
        </div>`
        }
        break
      case 'experience':
        if (content.experience?.length) {
          html += `
        <div class="resume-section">
          <h2 class="section-title">工作经历</h2>`
          content.experience.forEach(exp => {
            html += `
          <div class="experience-item">
            <div class="item-header">
              <h3 class="item-title">${exp.company} - ${exp.position}</h3>
              <span class="item-period">${exp.period}</span>
            </div>
            <p class="item-description">${exp.description}</p>
          </div>`
          })
          html += `</div>`
        }
        break
      case 'education':
        if (content.education?.length) {
          html += `
        <div class="resume-section">
          <h2 class="section-title">教育经历</h2>`
          content.education.forEach(edu => {
            html += `
          <div class="education-item">
            <div class="item-header">
              <h3 class="item-title">${edu.school} - ${edu.major}</h3>
              <span class="item-period">${edu.period}</span>
            </div>
            <p class="item-meta">${edu.degree}</p>
          </div>`
          })
          html += `</div>`
        }
        break
      case 'projects':
        if (content.projects?.length) {
          html += `
        <div class="resume-section">
          <h2 class="section-title">项目经历</h2>`
          content.projects.forEach(proj => {
            html += `
          <div class="project-item">
            <div class="item-header">
              <h3 class="item-title">${proj.name}</h3>
              <span class="item-period">${proj.period}</span>
            </div>
            <p class="item-description">${proj.description}</p>`
            if (proj.technologies?.length) {
              html += `
            <div class="item-tags">${proj.technologies.map(tech => `<span class="tag">${tech}</span>`).join('')}</div>`
            }
            html += `
          </div>`
          })
          html += `</div>`
        }
        break
      case 'skills':
        if (content.skills?.length) {
          html += `
        <div class="resume-section">
          <h2 class="section-title">专业技能</h2>
          <div class="skills-container">`
          content.skills.forEach(skill => {
            html += `
            <div class="skill-item">
              <div class="skill-name">${skill.name}</div>`
            if (skill.level) {
              html += `
              <div class="skill-level">
                <div class="skill-bar">
                  <div class="skill-fill" style="width: ${skill.level}%"></div>
                </div>
                <span class="skill-text">${skill.level}%</span>
              </div>`
            }
            html += `
            </div>`
          })
          html += `
          </div>
        </div>`
        }
        break
      case 'personalSummary':
        if (content.personalSummary) {
          html += `
        <div class="resume-section">
          <h2 class="section-title">个人总结</h2>
          <p class="section-content">${content.personalSummary}</p>
        </div>`
        }
        break
      case 'honors':
        if (content.honors?.length) {
          html += `
        <div class="resume-section">
          <h2 class="section-title">荣誉奖项</h2>`
          content.honors.forEach(honor => {
            html += `
          <div class="honor-item">
            <div class="item-header">
              <h3 class="item-title">${honor.name}</h3>
              <span class="item-period">${honor.date}</span>
            </div>`
            if (honor.description) {
              html += `<p class="item-description">${honor.description}</p>`
            }
            html += `
          </div>`
          })
          html += `</div>`
        }
        break
      case 'works':
        if (content.works?.length) {
          html += `
        <div class="resume-section">
          <h2 class="section-title">个人作品</h2>`
          content.works.forEach(work => {
            html += `
          <div class="work-item">
            <h3 class="item-title">${work.name}</h3>`
            if (work.description) {
              html += `<p class="item-description">${work.description}</p>`
            }
            if (work.url) {
              html += `<a class="work-link" href="${work.url}" target="_blank">${work.url}</a>`
            }
            html += `
          </div>`
          })
          html += `</div>`
        }
        break
    }
  }
  return html
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

    // 构建模板配置对象
    const layout = template.layout || 'classic'
    const themeConfig = typeof template.themeConfig === 'string'
      ? JSON.parse(template.themeConfig)
      : template.themeConfig
    const sectionConfig = typeof template.sectionConfig === 'string'
      ? JSON.parse(template.sectionConfig)
      : template.sectionConfig
    const sectionOrder = typeof template.sectionOrder === 'string'
      ? JSON.parse(template.sectionOrder)
      : template.sectionOrder

    currentTemplate.value = {
      layout,
      themeConfig,
      sectionConfig,
      sectionOrder
    }
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

const addProject = () => {
  if (!resumeData.value.content.projects) {
    resumeData.value.content.projects = []
  }
  resumeData.value.content.projects.push({
    name: '',
    period: '',
    description: '',
    technologiesString: ''
  })
}

const removeProject = (index) => {
  resumeData.value.content.projects.splice(index, 1)
}

const addSkill = () => {
  if (!resumeData.value.content.skills) {
    resumeData.value.content.skills = []
  }
  resumeData.value.content.skills.push({
    name: '',
    level: 80
  })
}

const removeSkill = (index) => {
  resumeData.value.content.skills.splice(index, 1)
}

const addHonor = () => {
  if (!resumeData.value.content.honors) {
    resumeData.value.content.honors = []
  }
  resumeData.value.content.honors.push({
    name: '',
    date: '',
    description: ''
  })
}

const removeHonor = (index) => {
  resumeData.value.content.honors.splice(index, 1)
}

const addWork = () => {
  if (!resumeData.value.content.works) {
    resumeData.value.content.works = []
  }
  resumeData.value.content.works.push({
    name: '',
    description: '',
    url: ''
  })
}

const removeWork = (index) => {
  resumeData.value.content.works.splice(index, 1)
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

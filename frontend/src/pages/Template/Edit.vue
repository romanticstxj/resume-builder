<template>
  <div class="template-editor">
    <div class="header">
      <h1>{{ isEdit ? '编辑模板' : '创建模板' }}</h1>
      <div class="actions">
        <t-button theme="default" @click="goBack">返回</t-button>
        <t-button theme="primary" @click="handleSave">保存</t-button>
      </div>
    </div>

    <div class="editor-container">
      <!-- 左侧：表单编辑 -->
      <div class="form-section">
        <t-card title="基本信息">
          <t-form ref="formRef" :data="form" :rules="rules" label-width="100px">
            <t-form-item label="模板名称" name="name">
              <t-input v-model="form.name" placeholder="请输入模板名称" />
            </t-form-item>
            <t-form-item label="分类" name="category">
              <t-select v-model="form.category" placeholder="请选择分类">
                <t-option value="技术" label="技术" />
                <t-option value="设计" label="设计" />
                <t-option value="产品" label="产品" />
                <t-option value="其他" label="其他" />
              </t-select>
            </t-form-item>
            <t-form-item label="布局类型" name="layout">
              <t-select v-model="form.layout" placeholder="请选择布局类型">
                <t-option value="classic" label="经典布局" />
                <t-option value="modern" label="现代布局" />
                <t-option value="minimalist" label="简约布局" />
                <t-option value="creative" label="创意布局" />
              </t-select>
            </t-form-item>
            <t-form-item label="描述" name="description">
              <t-textarea v-model="form.description" placeholder="请输入模板描述" :maxlength="500" />
            </t-form-item>
            <t-form-item label="预览图" name="previewUrl">
              <t-input v-model="form.previewUrl" placeholder="请输入预览图片URL" />
            </t-form-item>
            <t-form-item label="是否公开" name="isPublic">
              <t-switch v-model="form.isPublic" />
            </t-form-item>
          </t-form>
        </t-card>

        <t-card title="主题样式" style="margin-top: 16px">
          <t-form :data="form.themeConfig">
            <t-form-item label="主色调">
              <t-color-picker v-model="form.themeConfig.primaryColor" />
            </t-form-item>
            <t-form-item label="辅助色">
              <t-color-picker v-model="form.themeConfig.secondaryColor" />
            </t-form-item>
            <t-form-item label="文字颜色">
              <t-color-picker v-model="form.themeConfig.textColor" />
            </t-form-item>
            <t-form-item label="字体">
              <t-select v-model="form.themeConfig.fontFamily" placeholder="请选择字体">
                <t-option value="Arial, sans-serif" label="Arial" />
                <t-option value="'Microsoft YaHei', sans-serif" label="微软雅黑" />
                <t-option value="'SimSun', serif" label="宋体" />
                <t-option value="'KaiTi', serif" label="楷体" />
              </t-select>
            </t-form-item>
            <t-form-item label="字号">
              <t-slider v-model.number="form.themeConfig.fontSize" :min="12" :max="18" :marks="{12: '12', 14: '14', 16: '16', 18: '18'}" />
            </t-form-item>
          </t-form>
        </t-card>

        <t-card title="区块配置" style="margin-top: 16px">
          <t-form>
            <t-form-item label="区块顺序（拖拽排序）">
              <draggable v-model="sectionOrder" item-key="id" class="draggable-list" handle=".drag-handle">
                <template #item="{ element }">
                  <div class="draggable-item">
                    <span class="drag-handle">⋮⋮</span>
                    <span class="section-name">{{ element.label }}</span>
                    <t-switch v-model="form.sectionConfig[element.key].show" size="small" />
                  </div>
                </template>
              </draggable>
            </t-form-item>
            <t-divider>详细配置</t-divider>
            <t-form-item label="头部样式">
              <t-select v-model="form.sectionConfig.header.style" placeholder="请选择头部样式">
                <t-option value="left" label="左对齐" />
                <t-option value="center" label="居中" />
              </t-select>
            </t-form-item>
            <t-form-item label="头部背景">
              <t-switch v-model="form.sectionConfig.header.background" />
            </t-form-item>
            <t-form-item label="工作经历样式">
              <t-select v-model="form.sectionConfig.experience.style" placeholder="请选择样式">
                <t-option value="list" label="列表样式" />
                <t-option value="timeline" label="时间线样式" />
              </t-select>
            </t-form-item>
            <t-form-item label="教育经历样式">
              <t-select v-model="form.sectionConfig.education.style" placeholder="请选择样式">
                <t-option value="list" label="列表样式" />
              </t-select>
            </t-form-item>
            <t-form-item label="专业技能样式">
              <t-select v-model="form.sectionConfig.skills.style" placeholder="请选择样式">
                <t-option value="tags" label="标签样式" />
                <t-option value="list" label="列表样式" />
              </t-select>
            </t-form-item>
          </t-form>
        </t-card>

        <t-card title="示例内容（预览使用）" style="margin-top: 16px">
          <t-form>
            <t-form-item label="姓名">
              <t-input v-model="form.content.name" />
            </t-form-item>
            <t-form-item label="邮箱">
              <t-input v-model="form.content.email" />
            </t-form-item>
            <t-form-item label="电话">
              <t-input v-model="form.content.phone" />
            </t-form-item>
            <t-form-item label="个人简介">
              <t-textarea v-model="form.content.summary" :autosize="{ minRows: 3 }" />
            </t-form-item>
            <t-divider>工作经历示例</t-divider>
            <div v-for="(exp, index) in form.content.experience" :key="index" style="margin-bottom: 12px">
              <t-input v-model="exp.company" placeholder="公司名称" style="margin-bottom: 8px" />
              <t-input v-model="exp.position" placeholder="职位" style="margin-bottom: 8px" />
              <t-input v-model="exp.period" placeholder="时间段（如：2020-2023）" style="margin-bottom: 8px" />
              <t-textarea v-model="exp.description" placeholder="工作描述" :autosize="{ minRows: 2 }" />
            </div>
            <t-divider>教育经历示例</t-divider>
            <div v-for="(edu, index) in form.content.education" :key="index" style="margin-bottom: 12px">
              <t-input v-model="edu.school" placeholder="学校名称" style="margin-bottom: 8px" />
              <t-input v-model="edu.major" placeholder="专业" style="margin-bottom: 8px" />
              <t-input v-model="edu.degree" placeholder="学位" style="margin-bottom: 8px" />
              <t-input v-model="edu.period" placeholder="时间段（如：2015-2019）" />
            </div>
            <t-divider>项目经历示例</t-divider>
            <div v-for="(proj, index) in form.content.projects" :key="index" style="margin-bottom: 12px">
              <t-input v-model="proj.name" placeholder="项目名称" style="margin-bottom: 8px" />
              <t-input v-model="proj.period" placeholder="时间段" style="margin-bottom: 8px" />
              <t-textarea v-model="proj.description" placeholder="项目描述" :autosize="{ minRows: 2 }" />
              <t-input v-model="proj.technologiesString" placeholder="技术栈（逗号分隔）" />
            </div>
            <t-divider>专业技能示例</t-divider>
            <div v-for="(skill, index) in form.content.skills" :key="index" style="margin-bottom: 12px">
              <t-input v-model="skill.name" placeholder="技能名称" style="margin-bottom: 8px" />
              <t-slider v-model.number="skill.level" :min="0" :max="100" label="熟练度" />
            </div>
            <t-divider>个人总结示例</t-divider>
            <t-textarea v-model="form.content.personalSummary" :autosize="{ minRows: 3 }" placeholder="个人总结" />
            <t-divider>荣誉奖项示例</t-divider>
            <div v-for="(honor, index) in form.content.honors" :key="index" style="margin-bottom: 12px">
              <t-input v-model="honor.name" placeholder="奖项名称" style="margin-bottom: 8px" />
              <t-input v-model="honor.date" placeholder="获奖时间" style="margin-bottom: 8px" />
              <t-textarea v-model="honor.description" placeholder="奖项描述" :autosize="{ minRows: 2 }" />
            </div>
            <t-divider>个人作品示例</t-divider>
            <div v-for="(work, index) in form.content.works" :key="index" style="margin-bottom: 12px">
              <t-input v-model="work.name" placeholder="作品名称" style="margin-bottom: 8px" />
              <t-textarea v-model="work.description" placeholder="作品描述" :autosize="{ minRows: 2 }" style="margin-bottom: 8px" />
              <t-input v-model="work.url" placeholder="作品链接" />
            </div>
          </t-form>
        </t-card>
      </div>

      <!-- 右侧：实时预览 -->
      <div class="preview-section">
        <t-card title="实时预览" class="preview-card">
          <ResumeRenderer
            :resume-data="previewResumeData"
            :template-config="{
              layout: form.layout,
              themeConfig: form.themeConfig,
              sectionConfig: form.sectionConfig,
              sectionOrder: sectionOrder.map(s => s.key)
            }"
          />
        </t-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { getTemplateById, createTemplate, updateTemplate } from '@/api/template'
import ResumeRenderer from '@/components/ResumeRenderer.vue'
import draggable from 'vuedraggable'

const router = useRouter()
const route = useRoute()
const isEdit = computed(() => !!route.params.id)
const formRef = ref()

// 默认示例数据（用于新建模板或补充编辑时缺失的字段）
const defaultExampleContent = {
  name: '张三',
  email: 'zhangsan@example.com',
  phone: '13800138000',
  summary: '拥有5年软件开发经验，精通Java、Python，擅长高并发系统设计。具有良好的团队协作能力和项目管理经验。',
  experience: [
    {
      company: '某某科技有限公司',
      position: '高级软件工程师',
      period: '2021-2024',
      description: '负责核心业务系统的设计与开发，带领5人团队完成多个重要项目。'
    },
    {
      company: '某某互联网公司',
      position: '软件工程师',
      period: '2019-2021',
      description: '参与电商平台后端开发，优化系统性能提升30%。'
    }
  ],
  education: [
    {
      school: '某某大学',
      major: '计算机科学与技术',
      degree: '本科',
      period: '2015-2019'
    }
  ],
  projects: [
    {
      name: '电商平台重构',
      period: '2022-2023',
      description: '负责电商平台从单体架构到微服务架构的迁移，提升系统可用性至99.9%。',
      technologiesString: 'Java,Spring Cloud,MySQL,Redis'
    },
    {
      name: '在线教育系统',
      period: '2021-2022',
      description: '设计并开发了在线直播课程系统，支持万人同时在线学习。',
      technologiesString: 'Vue.js,Node.js,WebRTC,MongoDB'
    }
  ],
  skills: [
    { name: 'Java', level: 90 },
    { name: 'Spring Boot', level: 85 },
    { name: 'Python', level: 80 },
    { name: 'Vue.js', level: 85 },
    { name: 'MySQL', level: 80 },
    { name: 'Redis', level: 75 }
  ],
  personalSummary: '热爱技术，持续学习。具有良好的沟通能力和团队协作精神，能够快速适应新环境和新挑战。工作中注重代码质量和系统性能，善于从用户角度思考问题。',
  honors: [
    {
      name: '优秀员工',
      date: '2023年',
      description: '因在电商平台重构项目中表现突出，被评为年度优秀员工。'
    },
    {
      name: '技术竞赛一等奖',
      date: '2022年',
      description: '全国软件设计大赛一等奖。'
    }
  ],
  works: [
    {
      name: '开源项目 ResumeBuilder',
      description: '基于Vue和Spring Boot开发的简历生成系统，已获得200+ Star。',
      url: 'https://github.com/example/resume-builder'
    },
    {
      name: '技术博客',
      description: '分享Java后端开发和微服务架构相关技术文章。',
      url: 'https://blog.example.com'
    }
  ]
}

// 处理预览数据（将 technologiesString 转换为 technologies 数组）
const previewResumeData = computed(() => {
  const content = JSON.parse(JSON.stringify(form.value.content))
  
  // 处理项目经历的技术栈
  if (content.projects) {
    content.projects = content.projects.map(proj => ({
      ...proj,
      technologies: proj.technologiesString 
        ? proj.technologiesString.split(',').map(t => t.trim())
        : []
    }))
  }
  
  return { content, title: form.value.name }
})

// 区块顺序配置
const sectionOrder = ref([
  { id: 'header', key: 'header', label: '头部信息' },
  { id: 'summary', key: 'summary', label: '个人简介' },
  { id: 'experience', key: 'experience', label: '工作经历' },
  { id: 'education', key: 'education', label: '教育经历' },
  { id: 'projects', key: 'projects', label: '项目经历' },
  { id: 'skills', key: 'skills', label: '专业技能' },
  { id: 'personalSummary', key: 'personalSummary', label: '个人总结' },
  { id: 'honors', key: 'honors', label: '荣誉奖项' },
  { id: 'works', key: 'works', label: '个人作品' }
])

const form = ref({
  name: '',
  category: '',
  layout: 'classic',
  description: '',
  previewUrl: '',
  isPublic: true,
  themeConfig: {
    primaryColor: '#2c3e50',
    secondaryColor: '#3498db',
    textColor: '#333333',
    fontFamily: 'Arial, sans-serif',
    fontSize: 14
  },
  sectionConfig: {
    header: {
      show: true,
      style: 'left',
      background: false
    },
    summary: {
      show: true,
      style: 'plain'
    },
    experience: {
      show: true,
      style: 'list'
    },
    education: {
      show: true,
      style: 'list'
    },
    projects: {
      show: true,
      style: 'list'
    },
    skills: {
      show: true,
      style: 'tags'
    },
    personalSummary: {
      show: true,
      style: 'plain'
    },
    honors: {
      show: true,
      style: 'list'
    },
    works: {
      show: true,
      style: 'list'
    }
  },
  // 使用默认示例数据
  content: { ...defaultExampleContent }
})

const rules = {
  name: [{ required: true, message: '模板名称不能为空' }]
}

const loadTemplate = async () => {
  try {
    const template = await getTemplateById(route.params.id)

    form.value.name = template.name
    form.value.category = template.category
    form.value.description = template.description
    form.value.previewUrl = template.previewUrl
    form.value.isPublic = template.isPublic

    // 加载布局和配置
    if (template.layout) {
      form.value.layout = template.layout
    }

    try {
      const themeConfig = typeof template.themeConfig === 'string'
        ? JSON.parse(template.themeConfig)
        : template.themeConfig
      if (themeConfig) {
        form.value.themeConfig = { ...form.value.themeConfig, ...themeConfig }
      }
    } catch (e) {
      console.error('解析主题配置失败:', e)
    }

    try {
      const sectionConfig = typeof template.sectionConfig === 'string'
        ? JSON.parse(template.sectionConfig)
        : template.sectionConfig
      if (sectionConfig) {
        // 合并每个区块的配置
        Object.keys(sectionConfig).forEach(key => {
          if (form.value.sectionConfig[key]) {
            form.value.sectionConfig[key] = {
              ...form.value.sectionConfig[key],
              ...sectionConfig[key]
            }
          }
        })
      }
    } catch (e) {
      console.error('解析区块配置失败:', e)
    }

    // 加载区块顺序
    try {
      if (template.sectionOrder) {
        const order = typeof template.sectionOrder === 'string'
          ? JSON.parse(template.sectionOrder)
          : template.sectionOrder
        if (Array.isArray(order) && order.length > 0) {
          // 根据加载的顺序更新 sectionOrder 的顺序
          const newOrder = []
          order.forEach(key => {
            const section = sectionOrder.value.find(s => s.key === key)
            if (section) {
              newOrder.push(section)
            }
          })
          // 添加未被包含的区块
          sectionOrder.value.forEach(section => {
            if (!order.includes(section.key)) {
              newOrder.push(section)
            }
          })
          sectionOrder.value = newOrder
        }
      }
    } catch (e) {
      console.error('解析区块顺序失败:', e)
    }

    // 不再加载 content，因为示例数据不需要入库
    // form.value.content 始终使用默认示例数据，只用于预览
  } catch (error) {
    MessagePlugin.error('加载模板失败')
    console.error(error)
  }
}

const handleSave = async () => {
  const valid = await formRef.value.validate()
  if (valid !== true) return

  try {
    const data = {
      name: form.value.name,
      category: form.value.category,
      description: form.value.description,
      previewUrl: form.value.previewUrl,
      layout: form.value.layout,
      themeConfig: JSON.stringify(form.value.themeConfig),
      sectionConfig: JSON.stringify(form.value.sectionConfig),
      sectionOrder: JSON.stringify(sectionOrder.value.map(s => s.key)),
      isPublic: form.value.isPublic
      // 不再保存 content，示例数据只是预览用
    }

    if (isEdit.value) {
      await updateTemplate(route.params.id, data)
      MessagePlugin.success('更新成功')
    } else {
      await createTemplate(data)
      MessagePlugin.success('创建成功')
    }

    goBack()
  } catch (error) {
    MessagePlugin.error(isEdit.value ? '更新失败' : '创建失败')
    console.error(error)
  }
}

const goBack = () => {
  router.push('/template/list')
}

onMounted(() => {
  if (isEdit.value) {
    loadTemplate()
  }
})
</script>

<style scoped>
.template-editor {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: white;
  border-bottom: 1px solid #f0f0f0;
}

.actions {
  display: flex;
  gap: 12px;
}

.editor-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.form-section {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  border-right: 1px solid #f0f0f0;
}

.preview-section {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f5f5f5;
}

.preview-card {
  height: 100%;
  overflow: auto;
}

.preview-card :deep(.t-card__body) {
  height: 100%;
  overflow: auto;
}

/* 拖拽排序样式 */
.draggable-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.draggable-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 4px;
  cursor: move;
}

.draggable-item:hover {
  background: #e8e8e8;
}

.drag-handle {
  font-size: 20px;
  color: #999;
  cursor: move;
  user-select: none;
}

.section-name {
  flex: 1;
  font-weight: 500;
}
</style>

<template>
  <div class="resume-renderer" :style="rendererStyle">
    <div class="resume-page" :class="`layout-${layout}`">
      <!-- 根据区块顺序动态渲染 -->
      <template v-for="sectionKey in sectionOrder" :key="sectionKey">
        <!-- 头部区域 -->
        <div v-if="sectionKey === 'header' && showHeader" class="resume-header" :class="headerStyle">
          <div class="header-main">
            <div class="header-text">
              <h1 class="name">{{ resumeData.content.name || '姓名' }}</h1>
              <div v-if="resumeData.content.email || resumeData.content.phone || resumeData.content.city || resumeData.content.jobTarget" class="contact-info">
                <div v-if="resumeData.content.email || resumeData.content.phone">
                  <span v-if="resumeData.content.email">{{ resumeData.content.email }}</span>
                  <span v-if="resumeData.content.email && resumeData.content.phone"> | </span>
                  <span v-if="resumeData.content.phone">{{ resumeData.content.phone }}</span>
                </div>
                <div v-if="resumeData.content.city || resumeData.content.jobTarget">
                  <span v-if="resumeData.content.city">{{ resumeData.content.city }}</span>
                  <span v-if="resumeData.content.city && resumeData.content.jobTarget"> | </span>
                  <span v-if="resumeData.content.jobTarget">{{ resumeData.content.jobTarget }}</span>
                </div>
              </div>
            </div>
            <img v-if="resumeData.content.photo" :src="resumeData.content.photo" class="header-photo" />
          </div>
        </div>

        <!-- 个人简介 -->
        <div v-else-if="sectionKey === 'summary' && showSummary && resumeData.content.summary" class="resume-section">
          <h2 class="section-title" :style="sectionTitleStyle">{{ label('summary') }}</h2>
          <p class="section-content" v-html="resumeData.content.summary"></p>
        </div>

        <!-- 工作经历 -->
        <div v-else-if="sectionKey === 'experience' && showExperience && resumeData.content.experience?.length" class="resume-section">
          <h2 class="section-title" :style="sectionTitleStyle">{{ label('experience') }}</h2>
          <div
            v-for="(exp, index) in resumeData.content.experience"
            :key="index"
            class="experience-item"
            :class="experienceStyle"
          >
            <div class="item-header">
              <h3 class="item-title">{{ exp.company }}</h3>
              <span class="item-period">{{ exp.period }}</span>
            </div>
            <div v-if="exp.position" class="item-role-line">{{ exp.position }}</div>
            <p class="item-description" v-html="exp.description"></p>
          </div>
        </div>

        <!-- 教育经历 -->
        <div v-else-if="sectionKey === 'education' && showEducation && resumeData.content.education?.length" class="resume-section">
          <h2 class="section-title" :style="sectionTitleStyle">{{ label('education') }}</h2>
          <div
            v-for="(edu, index) in resumeData.content.education"
            :key="index"
            class="education-item"
            :class="educationStyle"
          >
            <div class="item-header">
              <h3 class="item-title">{{ edu.school }} - {{ edu.major }}</h3>
              <span class="item-period">{{ edu.period }}</span>
            </div>
            <p class="item-meta">{{ edu.degree }}</p>
          </div>
        </div>

        <!-- 项目经历 -->
        <div v-else-if="sectionKey === 'projects' && showProjects && resumeData.content.projects?.length" class="resume-section">
          <h2 class="section-title" :style="sectionTitleStyle">{{ label('projects') }}</h2>
          <div
            v-for="(project, index) in resumeData.content.projects"
            :key="index"
            class="project-item"
          >
            <div class="item-header">
              <h3 class="item-title">{{ project.name }}</h3>
              <span class="item-period">{{ project.period }}</span>
            </div>
            <div v-if="project.company || project.title" class="item-role-line">
              <span v-if="project.company">{{ project.company }}</span><span v-if="project.company && project.title"> · </span><span v-if="project.title">{{ project.title }}</span>
            </div>
            <p class="item-description" v-html="project.description"></p>
            <div v-if="project.technologies?.length" class="item-tags">
              <span v-for="tech in project.technologies" :key="tech" class="tag">{{ tech }}</span>
            </div>
          </div>
        </div>

        <!-- 专业技能 -->
        <div v-else-if="sectionKey === 'skills' && showSkills && (resumeData.content.skillsText || resumeData.content.skills?.length)" class="resume-section">
          <h2 class="section-title" :style="sectionTitleStyle">{{ label('skills') }}</h2>
          <div class="item-description" v-html="resumeData.content.skillsText || resumeData.content.skills?.map(s=>s.name).join(', ')"></div>
        </div>

        <!-- 荣誉证书 -->
        <div v-else-if="sectionKey === 'honors' && showHonors && resumeData.content.honors?.length" class="resume-section">
          <h2 class="section-title" :style="sectionTitleStyle">{{ label('honors') }}</h2>
          <div
            v-for="(honor, index) in resumeData.content.honors"
            :key="index"
            class="honor-item"
          >
            <div class="item-header">
              <h3 class="item-title">{{ honor.name }}</h3>
              <span class="item-period">{{ honor.date }}</span>
            </div>
            <p v-if="honor.description" class="item-description" v-html="honor.description"></p>
          </div>
        </div>

        <!-- 个人作品 -->
        <div v-else-if="sectionKey === 'works' && showWorks && resumeData.content.works?.length" class="resume-section">
          <h2 class="section-title" :style="sectionTitleStyle">{{ label('works') }}</h2>
          <div
            v-for="(work, index) in resumeData.content.works"
            :key="index"
            class="work-item"
          >
            <h3 class="item-title">{{ work.name }}</h3>
            <div v-if="work.role" class="item-role-line">{{ work.role }}</div>
            <p v-if="work.description" class="item-description" v-html="work.description"></p>
            <a v-if="work.url" :href="work.url" class="work-link" target="_blank">{{ work.url }}</a>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  resumeData: {
    type: Object,
    required: true
  },
  templateConfig: {
    type: Object,
    default: () => ({})
  }
})

// 解析配置
const layout = computed(() => props.templateConfig.layout || 'classic')

import { DEFAULT_SECTION_ORDER, getSectionLabel } from '@/config/sectionRegistry'

const lang = computed(() => props.resumeData.content?.language === 'en' ? 'en' : 'zh')
const label = (key) => getSectionLabel(key, lang.value)

// 使用模板配置的顺序或注册表默认顺序
const sectionOrder = computed(() => {
  try {
    const order = props.templateConfig.sectionOrder
    return Array.isArray(order) && order.length > 0 ? order : DEFAULT_SECTION_ORDER
  } catch {
    return DEFAULT_SECTION_ORDER
  }
})

const themeConfig = computed(() => {
  try {
    return typeof props.templateConfig.themeConfig === 'string'
      ? JSON.parse(props.templateConfig.themeConfig)
      : props.templateConfig.themeConfig || {}
  } catch {
    return {}
  }
})

const sectionConfig = computed(() => {
  try {
    return typeof props.templateConfig.sectionConfig === 'string'
      ? JSON.parse(props.templateConfig.sectionConfig)
      : props.templateConfig.sectionConfig || {}
  } catch {
    return {}
  }
})

// 渲染器样式
const rendererStyle = computed(() => {
  const fontSize = themeConfig.value.fontSize
  const fontSizePx = typeof fontSize === 'number' ? `${fontSize}px` : (fontSize || '14px')
  return {
    '--primary-color': themeConfig.value.primaryColor || '#2c3e50',
    '--secondary-color': themeConfig.value.secondaryColor || '#3498db',
    '--text-color': themeConfig.value.textColor || '#333333',
    '--font-family': themeConfig.value.fontFamily || 'Arial, sans-serif',
    '--font-size': fontSizePx
  }
})

const sectionTitleStyle = computed(() => ({
  borderColor: themeConfig.value.primaryColor || '#2c3e50'
}))

// 区块显示控制
const showHeader = computed(() => sectionConfig.value.header?.show !== false)
const headerStyle = computed(() => {
  const config = sectionConfig.value.header || {}
  return {
    'header-center': config.style === 'center',
    'header-background': config.background === true
  }
})

const showSummary = computed(() => sectionConfig.value.summary?.show !== false)

const showExperience = computed(() => sectionConfig.value.experience?.show !== false)
const experienceStyle = computed(() => ({
  'experience-timeline': sectionConfig.value.experience?.style === 'timeline',
  'experience-list': sectionConfig.value.experience?.style === 'list'
}))

const showEducation = computed(() => sectionConfig.value.education?.show !== false)
const educationStyle = computed(() => ({
  'education-list': sectionConfig.value.education?.style === 'list'
}))

const showProjects = computed(() => sectionConfig.value.projects?.show !== false)

const showSkills = computed(() => sectionConfig.value.skills?.show !== false)
const skillsStyle = computed(() => ({
  'skills-tags': sectionConfig.value.skills?.style === 'tags',
  'skills-list': sectionConfig.value.skills?.style === 'list'
}))

const showHonors = computed(() => sectionConfig.value.honors?.show !== false)

const showWorks = computed(() => sectionConfig.value.works?.show !== false)
</script>

<style scoped>
.resume-renderer {
  font-family: var(--font-family);
  font-size: var(--font-size);
  color: var(--text-color);
  background: #fff;
  padding: 20px;
}

.resume-page {
  max-width: 960px;
  margin: 0 auto;
  background: #fff;
  padding: 48px 56px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-height: 297mm;
  position: relative;
  overflow: hidden;
}

/* 背景水印 */
.resume-page::before {
  content: 'RESUME';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(-30deg);
  font-size: 120px;
  font-weight: 900;
  color: var(--primary-color);
  opacity: 0.03;
  pointer-events: none;
  white-space: nowrap;
  letter-spacing: 12px;
  z-index: 0;
}

/* 布局样式 */
.layout-classic {
  /* 经典布局：左对齐，简洁清晰 */
}

.layout-modern {
  /* 现代布局：更大标题，更多留白 */
}

.resume-header {
  margin-bottom: 30px;
}

.header-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-text { flex: 1; }

.header-photo {
  width: 80px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  margin-left: 24px;
  flex-shrink: 0;
  border: 1px solid #e0e0e0;
}

.header-center {
  text-align: center;
}

.header-background {
  background: var(--primary-color);
  color: #fff;
  padding: 30px;
  margin: -40px -40px 30px;
}

.name {
  font-size: 32px;
  font-weight: 700;
  color: var(--primary-color);
  margin: 0 0 10px;
}

.header-background .name {
  color: #fff;
}

.contact-info {
  font-size: 14px;
  color: #666;
}

.header-background .contact-info {
  color: rgba(255, 255, 255, 0.9);
}

.resume-section {
  margin-bottom: 25px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--primary-color);
  padding-bottom: 8px;
  margin-bottom: 15px;
  border-bottom: 2px solid var(--primary-color);
}

.section-content {
  line-height: 1.6;
  color: var(--text-color);
  white-space: pre-wrap;
}

/* 工作经历 */
.experience-item {
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

.item-role {
  font-size: 13px;
  font-weight: normal;
  color: #555;
}

.item-role-line {
  font-size: 13px;
  font-style: italic;
  color: #666;
  margin: 2px 0 4px;
}

.item-period {
  font-size: 13px;
  color: #666;
}

.item-description,
.item-meta {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-color) !important;
  margin: 0;
  white-space: pre-wrap;
}

/* Rich text content styles */
.item-description :deep(ul),
.section-content :deep(ul) {
  padding-left: 18px;
  list-style-type: disc;
  margin: 4px 0;
}

.item-description :deep(ol),
.section-content :deep(ol) {
  padding-left: 18px;
  list-style-type: decimal;
  margin: 4px 0;
}

.item-description :deep(li),
.section-content :deep(li) { margin: 2px 0; }

.item-description :deep(p),
.section-content :deep(p) { margin: 0 0 4px; }

/* Override any inline color styles from v-html content (e.g. from rich text editor or AI parse) */
.item-description :deep(*) {
  color: inherit !important;
}

/* Timeline 样式 */
.experience-timeline {
  position: relative;
  padding-left: 30px;
}

.experience-timeline::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 5px;
  bottom: 0;
  width: 2px;
  background: var(--secondary-color);
}

.experience-timeline .experience-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--primary-color);
  border: 2px solid #fff;
  box-shadow: 0 0 0 2px var(--primary-color);
}

/* 教育经历 */
.education-item {
  margin-bottom: 15px;
}

/* 项目经历 */
.project-item {
  margin-bottom: 20px;
}

.project-item .item-description {
  margin-top: 8px;
  line-height: 1.6;
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
  background: var(--secondary-color);
  color: #fff;
  border-radius: 3px;
  font-size: 12px;
}

/* 专业技能 */
.skills-tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  display: inline-block;
  padding: 3px 10px;
  background: color-mix(in srgb, var(--primary-color) 10%, white);
  color: var(--primary-color);
  border: 1px solid color-mix(in srgb, var(--primary-color) 30%, white);
  border-radius: 4px;
  font-size: 13px;
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
  background: var(--primary-color);
  transition: width 0.3s;
}

.skill-text {
  font-size: 12px;
  color: #666;
  min-width: 30px;
}

.skills-tags .skill-level {
  display: none;
}

/* 荣誉奖项 */
.honor-item {
  margin-bottom: 15px;
}

/* 个人作品 */
.work-item {
  margin-bottom: 15px;
}

.work-link {
  display: inline-block;
  margin-top: 8px;
  color: var(--secondary-color);
  text-decoration: none;
}

.work-link:hover {
  text-decoration: underline;
}

@media print {
  .resume-page {
    box-shadow: none;
    padding: 0;
    margin: 0;
  }
}
</style>

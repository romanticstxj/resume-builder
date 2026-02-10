<template>
  <div class="template-preview">
    <ResumeRenderer
      v-if="template"
      :resume-data="sampleResumeData"
      :template-config="templateConfig"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import ResumeRenderer from '@/components/ResumeRenderer.vue'

const props = defineProps({
  templateContent: {
    type: String,
    default: ''
  },
  template: {
    type: Object,
    default: () => null
  }
})

// 模板配置
const templateConfig = computed(() => {
  if (!props.template) return {}

  try {
    const layout = props.template.layout || 'classic'
    const themeConfig = typeof props.template.themeConfig === 'string'
      ? JSON.parse(props.template.themeConfig)
      : props.template.themeConfig
    const sectionConfig = typeof props.template.sectionConfig === 'string'
      ? JSON.parse(props.template.sectionConfig)
      : props.template.sectionConfig
    const sectionOrder = typeof props.template.sectionOrder === 'string'
      ? JSON.parse(props.template.sectionOrder)
      : props.template.sectionOrder

    return {
      layout,
      themeConfig,
      sectionConfig,
      sectionOrder
    }
  } catch (e) {
    console.error('解析模板配置失败:', e)
    return {}
  }
})

// 示例简历数据
const sampleResumeData = computed(() => ({
  content: {
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
        technologies: ['Java', 'Spring Cloud', 'MySQL', 'Redis']
      },
      {
        name: '在线教育系统',
        period: '2021-2022',
        description: '设计并开发了在线直播课程系统，支持万人同时在线学习。',
        technologies: ['Vue.js', 'Node.js', 'WebRTC', 'MongoDB']
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
  },
  title: '示例简历'
}))
</script>

<style scoped>
.template-preview {
  width: 100%;
  height: 100%;
  overflow: auto;
}

.template-preview :deep(.resume-renderer) {
  transform: scale(0.6);
  transform-origin: top left;
  width: 166.67%;
}
</style>

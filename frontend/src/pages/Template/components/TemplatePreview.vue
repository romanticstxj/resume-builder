<template>
  <div class="template-preview" :style="previewStyle">
    <div class="preview-container">
      <!-- 动态渲染模板内容 -->
      <div v-if="templateContent" class="template-content">
        <div
          v-for="section in sortedSections"
          :key="section.name"
          class="section"
          :style="sectionStyle(section)"
        >
          <h3 :style="sectionTitleStyle">{{ section.label }}</h3>
          <div class="section-fields">
            <div
              v-for="field in section.fields"
              :key="field.key"
              class="field"
            >
              <label>{{ field.label }}:</label>
              <span :class="`field-${field.type}`">
                {{ getFieldValue(field) }}
              </span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty">
        <span>暂无模板内容</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  templateContent: {
    type: String,
    default: ''
  }
})

const templateData = computed(() => {
  try {
    return props.templateContent ? JSON.parse(props.templateContent) : null
  } catch (e) {
    console.error('解析模板内容失败:', e)
    return null
  }
})

const sortedSections = computed(() => {
  if (!templateData.value?.sections) return []
  return [...templateData.value.sections].sort((a, b) => (a.order || 0) - (b.order || 0))
})

const previewStyle = computed(() => {
  const layout = templateData.value?.layout || {}
  const theme = templateData.value?.theme || {}

  return {
    padding: (layout.padding || 40) + 'px',
    backgroundColor: theme.backgroundColor || '#ffffff',
    fontSize: (theme.fontSize || 14) + 'px',
    lineHeight: theme.lineHeight || 1.6,
    fontFamily: theme.fontFamily || 'Arial, sans-serif',
    color: theme.textColor || '#333333'
  }
})

const sectionStyle = (section) => {
  const style = section.style || {}
  const spacing = templateData.value?.layout?.spacing || 20

  return {
    marginBottom: spacing + 'px',
    ...style
  }
}

const sectionTitleStyle = computed(() => {
  const theme = templateData.value?.theme || {}
  return {
    color: theme.primaryColor || '#1890ff',
    marginBottom: '12px'
  }
})

const getFieldValue = (field) => {
  const placeholders = {
    name: '张三',
    email: 'zhangsan@example.com',
    phone: '13800138000',
    position: '软件工程师',
    company: '某某科技有限公司',
    school: '某某大学',
    major: '计算机科学与技术',
    date: '2020-09 至今'
  }

  return placeholders[field.key] || field.placeholder || '示例内容'
}
</script>

<style scoped>
.template-preview {
  width: 100%;
  height: 100%;
  overflow: auto;
}

.preview-container {
  max-width: 800px;
  margin: 0 auto;
  background: white;
}

.template-content {
  min-height: 400px;
}

.section {
  padding: 16px 0;
}

.section h3 {
  margin: 0 0 12px 0;
  font-weight: 600;
}

.section-fields {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field {
  display: flex;
  gap: 8px;
}

.field label {
  font-weight: 500;
  min-width: 80px;
}

.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #999;
}
</style>

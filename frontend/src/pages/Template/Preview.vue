<template>
  <div class="template-preview-page">
    <div class="header">
      <t-button theme="default" variant="text" @click="goBack">
        <template #icon><chevron-left-icon /></template>
        返回
      </t-button>
      <h1>{{ template.name }}</h1>
      <t-space>
        <t-tag theme="success" v-if="template.isOfficial">官方模板</t-tag>
        <t-tag>{{ template.category }}</t-tag>
      </t-space>
    </div>

    <div class="content">
      <div class="info-section">
        <t-card>
          <div class="template-info">
            <div class="info-item">
              <span class="label">描述：</span>
              <span>{{ template.description || '暂无描述' }}</span>
            </div>
            <div class="info-item">
              <span class="label">使用次数：</span>
              <span>{{ template.usageCount }}</span>
            </div>
            <div class="info-item">
              <span class="label">创建时间：</span>
              <span>{{ formatDate(template.createdAt) }}</span>
            </div>
          </div>
        </t-card>

        <t-card title="模板结构">
          <div class="structure-info">
            <div class="structure-item">
              <span class="label">布局类型：</span>
              <span>{{ templateData?.layout?.type || 'vertical' }}</span>
            </div>
            <div class="structure-item">
              <span class="label">板块数量：</span>
              <span>{{ templateData?.sections?.length || 0 }}个</span>
            </div>
            <div v-if="templateData?.sections" class="sections-list">
              <div v-for="section in templateData.sections" :key="section.name" class="section-item">
                <t-icon name="check-circle" />
                <span>{{ section.label }}</span>
                <span v-if="section.fields">({{ section.fields.length }}个字段)</span>
              </div>
            </div>
          </div>
        </t-card>

        <t-card>
          <t-space size="large">
            <t-button theme="primary" size="large" @click="handleUseTemplate">
              使用此模板
            </t-button>
            <t-button theme="default" size="large" @click="handleEdit" v-if="canEdit">
              编辑模板
            </t-button>
          </t-space>
        </t-card>
      </div>

      <div class="preview-section">
        <t-card title="预览" class="preview-card">
          <template-preview :template-content="template.content" />
        </t-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { ChevronLeftIcon, CheckCircleFilledIcon } from 'tdesign-icons-vue-next'
import { getTemplateById } from '@/api/template'
import TemplatePreview from './components/TemplatePreview.vue'

const router = useRouter()
const template = ref({
  name: '',
  category: '',
  description: '',
  usageCount: 0,
  createdAt: '',
  content: '',
  isOfficial: false,
  createdBy: null
})

const templateData = computed(() => {
  try {
    return template.value.content ? JSON.parse(template.value.content) : null
  } catch (e) {
    return null
  }
})

const canEdit = computed(() => {
  return !template.value.isOfficial && template.value.createdBy === 1 // TODO: 替换为实际用户ID
})

const loadTemplate = async () => {
  try {
    const id = router.currentRoute.value.params.id
    const response = await getTemplateById(id)
    template.value = response.data
  } catch (error) {
    MessagePlugin.error('加载模板失败')
    console.error(error)
    goBack()
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const handleUseTemplate = () => {
  router.push(`/resumes/create?templateId=${template.value.id}`)
}

const handleEdit = () => {
  router.push(`/template/edit/${template.value.id}`)
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadTemplate()
})
</script>

<style scoped>
.template-preview-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-bottom: 1px solid #f0f0f0;
}

.header h1 {
  flex: 1;
  margin: 0;
}

.content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.info-section {
  width: 400px;
  padding: 20px;
  overflow-y: auto;
  border-right: 1px solid #f0f0f0;
}

.info-section .t-card {
  margin-bottom: 16px;
}

.template-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  gap: 8px;
}

.info-item .label {
  font-weight: 500;
  min-width: 80px;
}

.structure-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.structure-item {
  display: flex;
  gap: 8px;
}

.sections-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
}

.section-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.preview-section {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f5f5f5;
}

.preview-card {
  max-width: 1000px;
  margin: 0 auto;
}
</style>

<template>
  <div class="template-market">
    <div class="header">
      <h2>模板市场</h2>
      <p>选择您喜欢的模板开始制作简历</p>
    </div>

    <div class="filters">
      <t-radio-group v-model="selectedCategory" variant="default-filled" @change="fetchTemplates">
        <t-radio-button value="">全部</t-radio-button>
        <t-radio-button value="技术">技术</t-radio-button>
        <t-radio-button value="设计">设计</t-radio-button>
        <t-radio-button value="产品">产品</t-radio-button>
        <t-radio-button value="其他">其他</t-radio-button>
      </t-radio-group>
      
      <t-checkbox v-model="onlyOfficial" @change="fetchTemplates">仅显示官方模板</t-checkbox>
    </div>

    <div class="template-grid" v-loading="loading">
      <div
        v-for="template in templates"
        :key="template.id"
        class="template-card"
        @click="handleUseTemplate(template.id)"
      >
        <div class="template-preview">
          <div class="preview-placeholder">
            <t-icon name="file-copy" size="48px" />
            <span class="layout-tag">{{ getLayoutLabel(template.layout) }}</span>
          </div>
          <div class="template-tag" v-if="template.isOfficial">官方</div>
        </div>
        <div class="template-info">
          <h3>{{ template.name }}</h3>
          <p class="category">{{ template.category }}</p>
          <p class="description">{{ template.description }}</p>
          <div class="template-meta">
            <t-tag size="small">{{ template.usageCount }}次使用</t-tag>
          </div>
        </div>
      </div>

      <div v-if="templates.length === 0" class="empty-state">
        <t-icon name="file-copy" size="64px" />
        <p>暂无模板</p>
      </div>
    </div>

    <div class="pagination" v-if="total > pageSize">
      <t-pagination
        v-model="currentPage"
        :total="total"
        :page-size="pageSize"
        @change="fetchTemplates"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTemplateList } from '@/api/template'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()

const loading = ref(false)
const templates = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const selectedCategory = ref('')
const onlyOfficial = ref(false)

const fetchTemplates = async () => {
  loading.value = true
  try {
    const res = await getTemplateList({
      category: selectedCategory.value || undefined,
      isOfficial: onlyOfficial.value || undefined,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    // API interceptor 已经解包了 data，res 直接就是数组
    templates.value = Array.isArray(res) ? res : []
    total.value = templates.value.length
  } catch (error) {
    MessagePlugin.error('获取模板列表失败')
  } finally {
    loading.value = false
  }
}

const handleUseTemplate = (templateId) => {
  router.push(`/resumes/create?templateId=${templateId}`)
}

const getLayoutLabel = (layout) => {
  const labels = {
    'classic': '经典',
    'modern': '现代',
    'minimalist': '简约',
    'creative': '创意'
  }
  return labels[layout] || '经典'
}

onMounted(() => {
  fetchTemplates()
})
</script>

<style scoped>
.template-market {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.header {
  margin-bottom: 24px;
}

.header h2 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.header p {
  font-size: 14px;
  color: #666;
}

.filters {
  display: flex;
  gap: 24px;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.template-card {
  border: 2px solid var(--td-border-level-1-color);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
}

.template-card:hover {
  border-color: var(--td-brand-color);
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.template-preview {
  position: relative;
  height: 200px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-placeholder {
  color: #999;
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.layout-tag {
  position: absolute;
  bottom: 12px;
  left: 12px;
  background: rgba(255, 255, 255, 0.95);
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  color: var(--td-brand-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.template-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: var(--td-brand-color);
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
}

.template-info {
  padding: 16px;
}

.template-info h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.template-info .category {
  font-size: 13px;
  color: var(--td-brand-color);
  margin-bottom: 8px;
}

.template-info .description {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.template-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #999;
}

.empty-state p {
  margin: 16px 0;
  font-size: 16px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>

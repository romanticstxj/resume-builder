<template>
  <div class="template-list">
    <div class="header">
      <h1>模板管理</h1>
      <t-space>
        <template v-if="selectMode">
          <span class="select-hint">已选 {{ selectedIds.size }} 项</span>
          <t-button theme="danger" variant="outline" :disabled="selectedIds.size === 0" @click="handleBatchDelete">
            <template #icon><t-icon name="delete" /></template>
            删除所选
          </t-button>
          <t-button theme="default" @click="exitSelectMode">取消</t-button>
        </template>
        <template v-else>
          <t-button theme="default" variant="outline" @click="enterSelectMode">
            <template #icon><t-icon name="check-circle" /></template>
            批量删除
          </t-button>
          <t-button theme="primary" @click="handleCreate">
            <template #icon><add-icon /></template>
            新建模板
          </t-button>
        </template>
      </t-space>
    </div>

    <t-card class="filter-bar">
      <t-form :data="filterForm" layout="inline">
        <t-form-item label="分类">
          <t-select v-model="filterForm.category" placeholder="请选择分类" clearable style="width: 200px">
            <t-option value="专业" label="专业" />
            <t-option value="创意" label="创意" />
            <t-option value="简约" label="简约" />
          </t-select>
        </t-form-item>
        <t-form-item>
          <t-button theme="primary" @click="loadTemplates">查询</t-button>
          <t-button theme="default" @click="resetFilter">重置</t-button>
        </t-form-item>
      </t-form>
    </t-card>

    <t-loading :loading="loading">
      <t-row :gutter="16">
        <t-col :span="6" v-for="template in templates" :key="template.id">
          <t-card hover-shadow class="template-card" :class="{ selected: selectedIds.has(template.id) }" @click="selectMode ? toggleSelect(template.id) : handleView(template)">
            <div v-if="selectMode" class="select-checkbox">
              <t-checkbox :checked="selectedIds.has(template.id)" @click.stop="toggleSelect(template.id)" />
            </div>
            <div class="preview">
              <template-preview :template="template" :template-content="template.content" />
            </div>
            <div class="info">
              <div class="name">{{ template.name }}</div>
              <div class="category">{{ template.category }}</div>
              <div class="usage">使用次数: {{ template.usageCount }}</div>
              <div v-if="!selectMode" class="actions">
                <t-button size="small" theme="default" @click.stop="handleEdit(template)">编辑</t-button>
                <t-popconfirm content="确定删除该模板吗？" @confirm="handleDelete(template.id)">
                  <t-button size="small" theme="danger" @click.stop>删除</t-button>
                </t-popconfirm>
              </div>
            </div>
          </t-card>
        </t-col>
      </t-row>
    </t-loading>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import { getTemplateList, deleteTemplate } from '@/api/template'
import { AddIcon } from 'tdesign-icons-vue-next'
import TemplatePreview from './components/TemplatePreview.vue'

const router = useRouter()
const loading = ref(false)
const templates = ref([])
const filterForm = ref({ category: '' })

// 批量选择
const selectMode = ref(false)
const selectedIds = ref(new Set())

const enterSelectMode = () => {
  selectMode.value = true
  selectedIds.value = new Set()
}

const exitSelectMode = () => {
  selectMode.value = false
  selectedIds.value = new Set()
}

const toggleSelect = (id) => {
  const s = new Set(selectedIds.value)
  s.has(id) ? s.delete(id) : s.add(id)
  selectedIds.value = s
}

const handleBatchDelete = () => {
  if (selectedIds.value.size === 0) return
  const dialog = DialogPlugin.confirm({
    header: '确认批量删除',
    body: `确定要删除选中的 ${selectedIds.value.size} 个模板吗？此操作不可恢复。`,
    theme: 'warning',
    onConfirm: async () => {
      try {
        await Promise.all([...selectedIds.value].map(id => deleteTemplate(id)))
        MessagePlugin.success(`已删除 ${selectedIds.value.size} 个模板`)
        dialog.hide()
        exitSelectMode()
        loadTemplates()
      } catch (error) {
        MessagePlugin.error('部分删除失败，请重试')
      }
    }
  })
}

const loadTemplates = async () => {
  loading.value = true
  try {
    const params = filterForm.value.category ? { category: filterForm.value.category } : {}
    const response = await getTemplateList(params)
    // API interceptor 已经解包了 data，response 直接就是数组
    templates.value = Array.isArray(response) ? response : []
  } catch (error) {
    MessagePlugin.error('加载模板列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  filterForm.value.category = ''
  loadTemplates()
}

const handleCreate = () => {
  router.push('/template/create')
}

const handleView = (template) => {
  router.push(`/template/preview/${template.id}`)
}

const handleEdit = (template) => {
  router.push(`/template/edit/${template.id}`)
}

const handleDelete = async (id) => {
  try {
    await deleteTemplate(id)
    MessagePlugin.success('删除成功')
    loadTemplates()
  } catch (error) {
    MessagePlugin.error('删除失败')
    console.error(error)
  }
}

onMounted(() => {
  loadTemplates()
})
</script>

<style scoped>
.template-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-bar {
  margin-bottom: 20px;
}

.template-card {
  cursor: pointer;
  transition: transform 0.2s;
  position: relative;
}

.template-card:hover {
  transform: translateY(-4px);
}

.template-card.selected {
  outline: 2px solid var(--td-brand-color);
  background: #f0f7ff;
}

.select-checkbox {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 1;
}

.select-hint {
  font-size: 14px;
  color: #666;
  line-height: 32px;
}

.preview {
  height: 200px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  border-radius: 4px;
  overflow: hidden;
}

.info {
  padding: 12px 0 0;
}

.name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

.category {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.usage {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.actions {
  display: flex;
  gap: 8px;
}
</style>

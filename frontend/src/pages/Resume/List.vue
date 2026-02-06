<template>
  <div class="resume-list">
    <div class="header">
      <div class="header-left">
        <h2>我的简历</h2>
        <p>管理您的所有简历</p>
      </div>
      <t-button theme="primary" @click="handleCreate">
        <template #icon><t-icon name="add" /></template>
        新建简历
      </t-button>
    </div>

    <div class="resume-grid" v-loading="loading">
      <div
        v-for="resume in resumes"
        :key="resume.id"
        class="resume-card"
        @click="handleEdit(resume.id)"
      >
        <div class="card-header">
          <div class="status-tag" :class="resume.status">
            {{ resume.status === 'draft' ? '草稿' : '完成' }}
          </div>
          <t-dropdown :options="getCardOptions(resume)" @click="(e) => handleCardAction(e, resume.id)">
            <t-icon name="more" class="more-icon" />
          </t-dropdown>
        </div>
        <div class="card-body">
          <h3>{{ resume.title }}</h3>
          <p class="meta">
            <t-icon name="time" />
            {{ formatDate(resume.updatedAt) }}
          </p>
        </div>
        <div class="card-footer">
          <t-tag v-if="resume.isPrimary" theme="primary" size="small">主简历</t-tag>
          <t-tag v-else size="small">设为主简历</t-tag>
        </div>
      </div>

      <div v-if="resumes.length === 0" class="empty-state">
        <t-icon name="file-copy" size="64px" />
        <p>还没有简历</p>
        <t-button theme="primary" @click="handleCreate">创建第一个简历</t-button>
      </div>
    </div>

    <div class="pagination" v-if="total > pageSize">
      <t-pagination
        v-model="currentPage"
        :total="total"
        :page-size="pageSize"
        @change="fetchResumes"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getResumeList, deleteResume, copyResume } from '@/api/resume'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'

const router = useRouter()

const loading = ref(false)
const resumes = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const fetchResumes = async () => {
  loading.value = true
  try {
    const res = await getResumeList({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    console.log('简历列表响应:', res)
    // API interceptor 已经解包，res 是 Page 对象
    // 兼容处理：如果 res 直接是数组（某些接口），或者 res.content 存在
    if (Array.isArray(res)) {
      resumes.value = res
      total.value = res.length
    } else {
      resumes.value = res.content || []
      total.value = res.totalElements || 0
    }
  } catch (error) {
    MessagePlugin.error('获取简历列表失败')
    console.error('获取简历列表错误:', error)
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  router.push('/resumes/create')
}

const handleEdit = (id) => {
  router.push(`/resumes/${id}/edit`)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const getCardOptions = (resume) => [
  { content: '编辑', value: 'edit' },
  { content: '复制', value: 'copy' },
  { content: '删除', value: 'delete', theme: 'error' }
]

const handleCardAction = async ({ value }, id) => {
  switch (value) {
    case 'edit':
      handleEdit(id)
      break
    case 'copy':
      await handleCopy(id)
      break
    case 'delete':
      await handleDelete(id)
      break
  }
}

const handleCopy = async (id) => {
  try {
    await copyResume(id)
    MessagePlugin.success('复制成功')
    await fetchResumes()
  } catch (error) {
    MessagePlugin.error('复制失败')
  }
}

const handleDelete = async (id) => {
  const dialog = DialogPlugin.confirm({
    header: '确认删除',
    body: '确定要删除这个简历吗？此操作不可恢复。',
    theme: 'warning',
    onConfirm: async () => {
      try {
        await deleteResume(id)
        MessagePlugin.success('删除成功')
        await fetchResumes()
        dialog.hide()
      } catch (error) {
        MessagePlugin.error('删除失败')
      }
    }
  })
}

onMounted(() => {
  fetchResumes()
})
</script>

<style scoped>
.resume-list {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left h2 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.header-left p {
  font-size: 14px;
  color: #666;
}

.resume-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.resume-card {
  background: #f9f9f9;
  border: 2px solid transparent;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.resume-card:hover {
  border-color: var(--td-brand-color);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.status-tag {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.status-tag.draft {
  background: #e7e7e7;
  color: #666;
}

.status-tag.completed {
  background: #e8f5e9;
  color: #4caf50;
}

.more-icon {
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;
}

.more-icon:hover {
  background: rgba(0, 0, 0, 0.05);
}

.card-body h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-body .meta {
  font-size: 13px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-footer {
  margin-top: 16px;
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
  margin: 16px 0 24px;
  font-size: 16px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>

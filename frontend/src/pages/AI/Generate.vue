<template>
  <div class="ai-generate">
    <div class="header">
      <h2>AI 智能生成简历</h2>
      <p>输入职位信息,让AI帮您生成专业的简历内容</p>
    </div>

    <div class="content">
      <div class="left-panel">
        <t-form ref="formRef" :data="formData" :rules="rules" label-width="100px">
          <t-form-item label="目标职位" name="jobTitle">
            <t-input
              v-model="formData.jobTitle"
              placeholder="如:Java高级工程师"
              clearable
            />
          </t-form-item>

          <t-form-item label="目标公司" name="targetCompany">
            <t-input
              v-model="formData.targetCompany"
              placeholder="如:阿里巴巴"
              clearable
            />
          </t-form-item>

          <t-form-item label="工作年限" name="experience">
            <t-input
              v-model="formData.experience"
              placeholder="如:5年Java开发经验"
              clearable
            />
          </t-form-item>

          <t-form-item label="核心技能" name="skills">
            <t-textarea
              v-model="formData.skills"
              placeholder="请输入您掌握的核心技能,多个技能用逗号分隔"
              :autosize="{ minRows: 4 }"
            />
          </t-form-item>

          <t-form-item label="职位要求" name="requirements">
            <t-textarea
              v-model="formData.requirements"
              placeholder="请输入职位描述中的关键要求"
              :autosize="{ minRows: 4 }"
            />
          </t-form-item>

          <t-form-item>
            <t-button
              type="primary"
              theme="primary"
              size="large"
              :loading="generating"
              block
              @click="handleGenerate"
            >
              <template #icon><t-icon name="robot" /></template>
              开始生成
            </t-button>
          </t-form-item>
        </t-form>

        <t-alert
          theme="info"
          message="提示"
          description="AI生成功能正在开发中,敬请期待!当前您可以先使用编辑器手动创建简历。"
          :close="false"
        />
      </div>

      <div class="right-panel">
        <div class="preview-area">
          <div v-if="generatedContent" class="generated-content">
            <h3>生成结果</h3>
            <div class="content-section" v-for="(section, key) in generatedContent" :key="key">
              <h4>{{ getSectionTitle(key) }}</h4>
              <p>{{ section }}</p>
            </div>
            <t-button theme="primary" block @click="handleUseGenerated">
              使用生成的内容创建简历
            </t-button>
          </div>
          <div v-else class="placeholder">
            <t-icon name="robot" size="64px" />
            <p>在左侧填写信息后,点击"开始生成"</p>
            <p>AI将为您生成专业的简历内容</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'

const router = useRouter()

const formRef = ref()
const generating = ref(false)
const generatedContent = ref(null)

const formData = ref({
  jobTitle: '',
  targetCompany: '',
  experience: '',
  skills: '',
  requirements: ''
})

const rules = {
  jobTitle: [{ required: true, message: '请输入目标职位' }],
  targetCompany: [{ required: true, message: '请输入目标公司' }],
  experience: [{ required: true, message: '请输入工作年限' }],
  skills: [{ required: true, message: '请输入核心技能' }]
}

const getSectionTitle = (key) => {
  const titles = {
    summary: '个人简介',
    experience: '工作经历',
    skills: '技能清单',
    projects: '项目经验'
  }
  return titles[key] || key
}

const handleGenerate = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  generating.value = true
  try {
    MessagePlugin.info('AI生成功能正在开发中')
    // TODO: 调用AI生成接口
  } catch (error) {
    MessagePlugin.error('生成失败')
  } finally {
    generating.value = false
  }
}

const handleUseGenerated = () => {
  router.push({
    path: '/resumes/create',
    query: { content: JSON.stringify(generatedContent.value) }
  })
}
</script>

<style scoped>
.ai-generate {
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

.content {
  display: flex;
  gap: 24px;
}

.left-panel {
  flex: 1;
  max-width: 500px;
}

.right-panel {
  flex: 1;
}

.preview-area {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 24px;
  min-height: 500px;
}

.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 500px;
  color: #999;
}

.placeholder p {
  margin-top: 12px;
}

.generated-content h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}

.content-section {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.content-section h4 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--td-brand-color);
}

.content-section p {
  color: #333;
  line-height: 1.6;
}
</style>

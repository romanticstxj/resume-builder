import request from './index'

/**
 * 提交简历解析任务（异步）
 * @param {File} file - 简历文件（PDF/Word/Text）
 * @returns {Promise} 任务信息
 */
export function submitParseTask(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/ai/parse-resume',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取任务状态
 * @param {Number} taskId - 任务ID
 * @returns {Promise} 任务详情
 */
export function getTaskStatus(taskId) {
  return request({
    url: `/ai/tasks/${taskId}`,
    method: 'GET'
  })
}

/**
 * 获取任务列表
 * @param {Object} params - 查询参数 { page, size }
 * @returns {Promise} 任务列表
 */
export function getTaskList(params = {}) {
  return request({
    url: '/ai/tasks',
    method: 'GET',
    params: {
      page: params.page || 0,
      size: params.size || 10
    }
  })
}

/**
 * 同步解析简历文件（保留原有接口）
 * @param {File} file - 简历文件（PDF/Word/Text）
 * @returns {Promise} 解析结果
 */
export function parseResumeSync(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/ai/parse-resume-sync',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 从解析结果创建简历
 * @param {Object} parseResponse - AI解析的简历数据
 * @returns {Promise} 创建的简历
 */
export function createResumeFromParsed(parseResponse) {
  return request({
    url: '/resumes/from-parsed',
    method: 'POST',
    data: parseResponse
  })
}

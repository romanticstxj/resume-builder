import request from './index'

// 获取模板列表
export const getTemplateList = (params) => {
  return request({
    url: '/templates',
    method: 'get',
    params
  })
}

// 获取单个模板详情
export const getTemplateById = (id) => {
  return request({
    url: `/templates/${id}`,
    method: 'get'
  })
}

// 创建模板
export const createTemplate = (data) => {
  return request({
    url: '/templates',
    method: 'post',
    data
  })
}

// 更新模板
export const updateTemplate = (id, data) => {
  return request({
    url: `/templates/${id}`,
    method: 'put',
    data
  })
}

// 删除模板
export const deleteTemplate = (id) => {
  return request({
    url: `/templates/${id}`,
    method: 'delete'
  })
}


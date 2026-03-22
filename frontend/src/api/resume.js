import request from './index'

export const getResumeList = (params) => {
  return request({
    url: '/resumes',
    method: 'get',
    params
  })
}

export const getResumeById = (id) => {
  return request({
    url: `/resumes/${id}`,
    method: 'get'
  })
}

export const createResume = (data) => {
  return request({
    url: '/resumes',
    method: 'post',
    data
  })
}

export const updateResume = (id, data) => {
  return request({
    url: `/resumes/${id}`,
    method: 'put',
    data
  })
}

export const deleteResume = (id) => {
  return request({
    url: `/resumes/${id}`,
    method: 'delete'
  })
}

export const copyResume = (id) => {
  return request({
    url: `/resumes/${id}/copy`,
    method: 'post'
  })
}

export const previewResume = (id) => {
  return request({
    url: `/resumes/${id}/preview`,
    method: 'get',
    responseType: 'text'
  })
}

export const exportResumeWord = (id) => {
  return request({
    url: `/resumes/${id}/export/word`,
    method: 'get',
    responseType: 'blob'
  })
}

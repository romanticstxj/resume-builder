import request from './index'

export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export const register = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export const getGithubClientId = () => {
  return request({ url: '/auth/github/client-id', method: 'get' })
}

export const githubCallback = (code) => {
  return request({ url: `/auth/github/callback?code=${code}`, method: 'post' })
}

export const logoutApi = (refreshToken) => {
  return request({
    url: `/auth/logout${refreshToken ? `?refreshToken=${refreshToken}` : ''}`,
    method: 'post'
  })
}

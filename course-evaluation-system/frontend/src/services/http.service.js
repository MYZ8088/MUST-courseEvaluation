import axios from 'axios'
import store from '@/store'

// 创建axios实例
const apiClient = axios.create({
  baseURL: 'http://localhost:8088/api',
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 15000 // 增加超时时间
})

// 请求重试配置
const MAX_RETRIES = 3
const RETRY_DELAY = 1000

// 重试函数
const retryRequest = (config, retryCount = 0) => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      apiClient(config)
        .then(resolve)
        .catch((error) => {
          if (retryCount < MAX_RETRIES && error.code === 'ECONNABORTED') {
            console.log(`请求重试 ${retryCount + 1}/${MAX_RETRIES}`)
            retryRequest(config, retryCount + 1).then(resolve).catch(reject)
          } else {
            reject(error)
          }
        })
    }, RETRY_DELAY * retryCount)
  })
}

// 请求拦截器
apiClient.interceptors.request.use(
  config => {
    // 如果设置了Skip-Auth头，则跳过认证
    if (config.headers && config.headers['Skip-Auth']) {
      delete config.headers['Skip-Auth']
      return config
    }
    
    // 从本地存储获取token
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const userData = JSON.parse(userStr)
        if (userData && userData.token) {
          // 添加授权头
          config.headers.Authorization = `Bearer ${userData.token}`
        }
      } catch (e) {
        console.error('解析用户数据出错:', e)
      }
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  response => {
    return response
  },
  error => {
    // 如果是网络错误，尝试重试
    if (error.code === 'ECONNABORTED' && !error.config.__retryCount) {
      error.config.__retryCount = 0
      return retryRequest(error.config)
    }
    
    if (error.response) {
      // 检查是否是登录请求
      const isLoginRequest = error.config.url.includes('auth/login');
      
      // 定制化错误消息
      switch (error.response.status) {
        case 400:
          if (isLoginRequest) {
            // 登录请求的400状态码通常意味着用户名或密码错误
            error.message = '用户名或密码错误，请重新输入';
          } else {
            // 其他请求的400错误给出更友好的提示
            error.message = '提交的信息有误，请检查并重新填写';
          }
          break;
        case 401:
          error.message = '未登录或登录已过期，请重新登录';
          // 清除用户状态
          store.dispatch('auth/logout');
          // 如果不是登录页面，跳转到登录页
          if (window.location.pathname !== '/login') {
            window.location.href = '/login';
          }
          break;
        case 403:
          if (isLoginRequest) {
            error.message = '用户名或密码错误，请重新输入';
          } else {
            error.message = '您没有权限执行此操作';
          }
          break;
        case 404:
          if (isLoginRequest) {
            error.message = '用户不存在，请先注册账号';
          } else {
            error.message = '请求的资源不存在';
          }
          break;
          error.message = '请求过于频繁，请稍后再试';
          break;
        case 500:
          error.message = '服务器内部错误，请稍后再试';
          break;
        default:
          error.message = '网络错误，请稍后重试';
      }
    } else if (error.request) {
      // 请求已发送但未收到响应
      error.message = '服务器无响应，请检查网络连接';
    } else {
      // 请求配置有误
      error.message = '请求异常，请稍后重试';
    }
    
    return Promise.reject(error);
  }
)

export default apiClient 
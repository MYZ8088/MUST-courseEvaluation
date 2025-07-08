// 性能优化工具函数

/**
 * 防抖函数
 * @param {Function} func 要防抖的函数
 * @param {number} wait 等待时间
 * @param {boolean} immediate 是否立即执行
 */
export function debounce(func, wait, immediate = false) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      timeout = null
      if (!immediate) func.apply(this, args)
    }
    const callNow = immediate && !timeout
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
    if (callNow) func.apply(this, args)
  }
}

/**
 * 节流函数
 * @param {Function} func 要节流的函数
 * @param {number} limit 时间限制
 */
export function throttle(func, limit) {
  let inThrottle
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

/**
 * 图片懒加载
 * @param {string} selector 图片选择器
 */
export function lazyLoadImages(selector = 'img[data-src]') {
  const images = document.querySelectorAll(selector)
  
  const imageObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target
        img.src = img.dataset.src
        img.classList.remove('lazy')
        observer.unobserve(img)
      }
    })
  })
  
  images.forEach(img => imageObserver.observe(img))
}

/**
 * 虚拟滚动实现
 */
export class VirtualScroll {
  constructor(container, itemHeight, renderItem) {
    this.container = container
    this.itemHeight = itemHeight
    this.renderItem = renderItem
    this.scrollTop = 0
    this.containerHeight = container.clientHeight
    this.visibleCount = Math.ceil(this.containerHeight / itemHeight) + 2
    
    this.init()
  }
  
  init() {
    this.container.addEventListener('scroll', this.handleScroll.bind(this))
  }
  
  handleScroll() {
    this.scrollTop = this.container.scrollTop
    this.render()
  }
  
  render() {
    const startIndex = Math.floor(this.scrollTop / this.itemHeight)
    const endIndex = Math.min(startIndex + this.visibleCount, this.data.length)
    
    // 清空容器
    this.container.innerHTML = ''
    
    // 创建占位元素
    const spacerTop = document.createElement('div')
    spacerTop.style.height = `${startIndex * this.itemHeight}px`
    this.container.appendChild(spacerTop)
    
    // 渲染可见项
    for (let i = startIndex; i < endIndex; i++) {
      const item = this.renderItem(this.data[i], i)
      this.container.appendChild(item)
    }
    
    // 创建底部占位元素
    const spacerBottom = document.createElement('div')
    spacerBottom.style.height = `${(this.data.length - endIndex) * this.itemHeight}px`
    this.container.appendChild(spacerBottom)
  }
  
  setData(data) {
    this.data = data
    this.render()
  }
}

/**
 * 缓存管理器
 */
export class CacheManager {
  constructor(maxSize = 100, ttl = 5 * 60 * 1000) { // 默认5分钟过期
    this.cache = new Map()
    this.maxSize = maxSize
    this.ttl = ttl
  }
  
  set(key, value) {
    // 如果缓存已满，删除最旧的项
    if (this.cache.size >= this.maxSize) {
      const firstKey = this.cache.keys().next().value
      this.cache.delete(firstKey)
    }
    
    this.cache.set(key, {
      value,
      timestamp: Date.now()
    })
  }
  
  get(key) {
    const item = this.cache.get(key)
    if (!item) return null
    
    // 检查是否过期
    if (Date.now() - item.timestamp > this.ttl) {
      this.cache.delete(key)
      return null
    }
    
    return item.value
  }
  
  clear() {
    this.cache.clear()
  }
  
  delete(key) {
    this.cache.delete(key)
  }
}

// 创建全局缓存实例
export const globalCache = new CacheManager()

/**
 * 请求去重
 */
export class RequestDeduplicator {
  constructor() {
    this.pendingRequests = new Map()
  }
  
  async request(key, requestFn) {
    // 如果已有相同请求在进行中，返回该请求的Promise
    if (this.pendingRequests.has(key)) {
      return this.pendingRequests.get(key)
    }
    
    // 创建新请求
    const promise = requestFn().finally(() => {
      // 请求完成后清除
      this.pendingRequests.delete(key)
    })
    
    this.pendingRequests.set(key, promise)
    return promise
  }
}

// 创建全局请求去重实例
export const requestDeduplicator = new RequestDeduplicator()
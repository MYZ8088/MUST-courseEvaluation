<template>
  <div v-if="hasError" class="error-boundary">
    <div class="error-content">
      <div class="error-icon">
        <i class="fas fa-exclamation-triangle"></i>
      </div>
      <h3>出现了一些问题</h3>
      <p>{{ errorMessage }}</p>
      <div class="error-actions">
        <button @click="retry" class="btn-retry">重试</button>
        <button @click="goHome" class="btn-home">返回首页</button>
      </div>
    </div>
  </div>
  <slot v-else></slot>
</template>

<script>
export default {
  name: 'ErrorBoundary',
  data() {
    return {
      hasError: false,
      errorMessage: '页面加载失败，请稍后重试'
    }
  },
  methods: {
    handleError(error) {
      this.hasError = true
      this.errorMessage = error.message || '未知错误'
      console.error('ErrorBoundary caught an error:', error)
    },
    retry() {
      this.hasError = false
      this.$emit('retry')
    },
    goHome() {
      this.$router.push('/')
    }
  },
  errorCaptured(error, instance, info) {
    this.handleError(error)
    return false
  }
}
</script>

<style scoped>
.error-boundary {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 20px;
}

.error-content {
  text-align: center;
  max-width: 500px;
  padding: 40px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.error-icon {
  font-size: 48px;
  color: #ff6b6b;
  margin-bottom: 20px;
}

.error-content h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.error-content p {
  color: #666;
  margin-bottom: 30px;
  line-height: 1.5;
}

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.btn-retry, .btn-home {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn-retry {
  background-color: #0066cc;
  color: white;
}

.btn-retry:hover {
  background-color: #0055aa;
}

.btn-home {
  background-color: #f1f1f1;
  color: #333;
}

.btn-home:hover {
  background-color: #e0e0e0;
}
</style>
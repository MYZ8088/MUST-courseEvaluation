<template>
  <div class="login-container">
    <div class="login-form">
      <h1>课程评价系统 - 登录</h1>
      <div v-if="error" class="alert" :class="errorClass">
        {{ error }}
      </div>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input
            type="text"
            id="username"
            v-model="user.username"
            required
            class="form-control"
            placeholder="请输入用户名"
          />
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <div class="password-input-container">
            <input
              :type="showPassword ? 'text' : 'password'"
              id="password"
              v-model="user.password"
              required
              class="form-control"
              placeholder="请输入密码"
            />
            <button
              v-if="user.password"
              type="button"
              class="password-toggle-btn"
              @click="showPassword = !showPassword"
            >
              <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
                <path v-if="showPassword" d="M2 2l20 20" />
              </svg>
            </button>
          </div>
        </div>
        <div class="form-group">
          <button class="btn btn-primary btn-block" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm"></span>
            <span>登录</span>
          </button>
        </div>
        <div class="form-group">
          <div class="register-link">
            还没有账号？
            <router-link to="/register">立即注册</router-link>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'Login',
  data() {
    return {
      user: {
        username: '',
        password: ''
      },
      loading: false,
      error: null,
      isAccountDisabled: false,
      showPassword: false
    }
  },
  computed: {
    ...mapState({
      isLoggedIn: state => state.auth.status.loggedIn
    }),
    errorClass() {
      return this.isAccountDisabled ? 'alert-warning' : 'alert-danger';
    }
  },
  created() {
    // 如果用户已登录，重定向到首页
    if (this.isLoggedIn) {
      this.$router.push('/')
    }
  },
  methods: {
    handleLogin() {
      this.loading = true
      this.error = null
      this.isAccountDisabled = false
      
      this.$store.dispatch('auth/login', {
        username: this.user.username,
        password: this.user.password
      })
        .then(() => {
          // 如果存在重定向链接，则跳转到该页面，否则跳转到首页
          const redirectPath = this.$route.query.redirect || '/'
          this.$router.push(redirectPath)
        })
        .catch(error => {
          this.loading = false
          this.error = error.message || '登录失败，请检查您的凭据'
          
          // 判断是否是账户禁用错误
          if (error.isAccountDisabled) {
            this.isAccountDisabled = true
          }
        })
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f8f9fa;
}

.login-form {
  width: 100%;
  max-width: 450px;
  padding: 30px;
  background-color: #fff;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h1 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
  width: 100%;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-control {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  box-sizing: border-box;
}

.password-input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.password-toggle-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: #555;
  padding: 0;
  display: flex;
  align-items: center;
  z-index: 10;
  transition: color 0.2s ease;
}

.password-toggle-btn:hover {
  color: #4a6bff;
}

.password-toggle-btn svg {
  transition: all 0.2s ease;
}

.btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border-radius: 4px;
  cursor: pointer;
  box-sizing: border-box;
}

.btn-primary {
  background-color: #4a6bff;
  color: white;
  border: none;
}

.btn-primary:hover {
  background-color: #3955d9;
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.alert {
  padding: 12px;
  margin-bottom: 20px;
  border-radius: 4px;
}

.alert-danger {
  background-color: #ffeded;
  color: #d93025;
  border: 1px solid #f8d7da;
}

.alert-warning {
  background-color: #fff3e0;
  color: #e65100;
  border: 1px solid #ffccbc;
}

.register-link {
  text-align: center;
  margin-top: 15px;
}

.register-link a {
  color: #4a6bff;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style> 
<template>
  <div class="teacher-detail-container">
    <div class="page-header">
      <h1>教师详情</h1>
      <div class="header-actions">
        <router-link to="/teachers" class="btn-back">
          <i class="fas fa-arrow-left"></i> 返回教师列表
        </router-link>
      </div>
    </div>
    
    <div v-if="loading" class="loading">
      <p>加载中，请稍候...</p>
    </div>
    
    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="loadTeacher" class="btn-retry">重试</button>
    </div>
    
    <div v-else class="teacher-content">
      <!-- 教师基本信息 -->
      <div class="teacher-info-card">
        <div class="teacher-header">
          <h2 class="teacher-name">{{ teacher.name }}</h2>
          <div class="teacher-meta">
            <span class="teacher-title">{{ teacher.title || '未设置职称' }}</span>
            <span class="faculty-name">{{ teacher.facultyName || '未关联院系' }}</span>
          </div>
        </div>
        
        <div class="teacher-contact" v-if="teacher.email">
          <div class="info-item">
            <span class="label"><i class="fas fa-envelope"></i> 联系邮箱:</span>
            <span class="value">{{ teacher.email }}</span>
          </div>
        </div>
        
        <div class="teacher-bio" v-if="teacher.bio">
          <h3>个人简介</h3>
          <p>{{ teacher.bio }}</p>
        </div>
      </div>
      
      <!-- 教师课程列表 -->
      <div class="teacher-courses-section" v-if="teacherCourses.length > 0">
        <h3>授课课程</h3>
        <div class="courses-list">
          <div v-for="course in teacherCourses" :key="course.id" class="course-card">
            <router-link :to="{path: `/courses/${course.id}`, query: {from: 'teacher'}}" class="course-link">
              <span class="course-code">{{ course.code }}</span>
              <span class="course-name">{{ course.name }}</span>
              <span class="course-type" :class="course.type === 'COMPULSORY' ? 'compulsory' : 'elective'">
                {{ course.type === 'COMPULSORY' ? '必修课' : '选修课' }}
              </span>
            </router-link>
          </div>
        </div>
      </div>
      
      <!-- 教师评价统计 -->
      <div class="teacher-rating-section" v-if="ratings">
        <h3>评价统计</h3>
        <div class="ratings-overview">
          <div class="average-rating">
            <span class="rating-number">{{ ratings.averageRating ? ratings.averageRating.toFixed(1) : 'N/A' }}</span>
            <div class="stars">
              <i v-for="n in 5" :key="n"
                 :class="ratings.averageRating && n <= Math.round(ratings.averageRating) ? 'fas fa-star' : 'far fa-star'"></i>
            </div>
            <span class="rating-count">{{ ratings.totalReviews || 0 }}条评价</span>
          </div>
          
          <div class="rating-distribution">
            <div v-for="i in 5" :key="i" class="rating-bar">
              <span class="star-level">{{ 6 - i }}星</span>
              <div class="bar-container">
                <div class="bar" :style="{width: calculatePercentage(ratings.ratingDistribution[6 - i] || 0) + '%'}"></div>
              </div>
              <span class="count">{{ ratings.ratingDistribution[6 - i] || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 教师评价列表 -->
      <div class="teacher-reviews-section">
        <div class="reviews-header">
          <h3>学生评价</h3>
          <button v-if="isLoggedIn && teacherCourses.length > 0" @click="showReviewForm = true" class="btn-add-review">
            写评价
          </button>
        </div>
        
        <div v-if="!isLoggedIn" class="login-prompt">
          <p>请 <router-link to="/login">登录</router-link> 后参与教师评价</p>
        </div>
        
        <!-- 评价表单 -->
        <div v-if="showReviewForm" class="review-form-container">
          <form @submit.prevent="submitReview" class="review-form">
            <h4>写评价</h4>
            
            <div class="form-group">
              <label for="course-select">选择课程:</label>
              <select 
                id="course-select" 
                v-model="newReview.courseId"
                required
              >
                <option value="" disabled>请选择您上过的课程</option>
                <option v-for="course in teacherCourses" :key="course.id" :value="course.id">
                  {{ course.name }} ({{ course.code }})
                </option>
              </select>
            </div>
            
            <div class="form-group">
              <label>评分:</label>
              <div class="rating-input">
                <span 
                  v-for="star in 5" 
                  :key="star" 
                  @click="newReview.rating = star"
                  :class="['star', { active: star <= newReview.rating }]"
                >
                  <i class="fas fa-star"></i>
                </span>
              </div>
            </div>
            
            <div class="form-group">
              <label for="review-content">评价内容:</label>
              <textarea 
                id="review-content" 
                v-model="newReview.content"
                placeholder="分享你对这位老师的评价和建议..."
                rows="4"
                required
              ></textarea>
            </div>
            
            <div class="form-group">
              <label>
                <input type="checkbox" v-model="newReview.anonymous">
                匿名评价
              </label>
            </div>
            
            <div class="form-actions">
              <button type="button" @click="showReviewForm = false" class="btn-cancel">
                取消
              </button>
              <button type="submit" class="btn-submit" :disabled="reviewSubmitting">
                {{ reviewSubmitting ? '提交中...' : '提交评价' }}
              </button>
            </div>
          </form>
        </div>
        
        <!-- 评价列表 -->
        <div v-if="reviews.length === 0" class="no-reviews">
          <p>暂无评价</p>
        </div>
        
        <div v-else class="reviews-list">
          <div v-for="review in reviews" :key="review.id" class="review-card">
            <div class="review-header">
              <div class="review-author">
                {{ review.anonymous ? '匿名用户' : review.username }}
              </div>
              <div class="review-rating">
                <i v-for="n in 5" :key="n"
                   :class="n <= review.rating ? 'fas fa-star' : 'far fa-star'"></i>
              </div>
            </div>
            
            <div class="review-content">
              {{ review.content }}
            </div>
            
            <div class="review-footer">
              <div class="review-course">
                课程: <router-link :to="{path: `/courses/${review.courseId}`, query: {from: 'teacher'}}">{{ review.courseName }}</router-link>
              </div>
              <span class="review-date">{{ formatDate(review.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import teacherService from '@/services/teacher.service'
import courseService from '@/services/course.service'
import reviewService from '@/services/review.service'
import contentFilterService from '@/services/content-filter.service'
import { mapGetters } from 'vuex'

export default {
  name: 'TeacherDetail',
  data() {
    return {
      loading: true,
      error: null,
      teacher: {},
      teacherCourses: [],
      ratings: null,
      reviews: [],
      showReviewForm: false,
      reviewSubmitting: false,
      newReview: {
        courseId: '',
        rating: 0,
        content: '',
        anonymous: false
      }
    }
  },
  computed: {
    ...mapGetters({
      isLoggedIn: 'auth/isLoggedIn',
      currentUser: 'auth/currentUser'
    })
  },
  methods: {
    loadTeacher() {
      this.loading = true
      this.error = null
      
      const teacherId = this.$route.params.id
      
      // 获取教师信息
      teacherService.getTeacher(teacherId)
        .then(response => {
          this.teacher = response.data
          
          // 获取教师课程
          return courseService.getCoursesByTeacher(teacherId)
        })
        .then(response => {
          this.teacherCourses = response.data
          
          // 获取教师评价统计
          return reviewService.getTeacherReviews(teacherId)
        })
        .then(response => {
          this.reviews = response.data
          
          // 获取评分统计
          return reviewService.getTeacherRatings(teacherId)
        })
        .then(response => {
          this.ratings = response.data
          this.loading = false
        })
        .catch(error => {
          console.error('加载教师信息失败:', error)
          this.error = '获取教师信息失败，请稍后再试'
          this.loading = false
        })
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      })
    },
    
    calculatePercentage(count) {
      if (!this.ratings || !this.ratings.totalReviews || this.ratings.totalReviews === 0) {
        return 0
      }
      return (count / this.ratings.totalReviews) * 100
    },
    
    submitReview() {
      if (this.newReview.rating === 0) {
        this.$notify({
          type: 'error',
          title: '错误',
          message: '请选择评分'
        })
        return
      }
      
      if (!this.newReview.courseId) {
        this.$notify({
          type: 'error',
          title: '错误',
          message: '请选择课程'
        })
        return
      }
      
      if (!this.newReview.content.trim()) {
        this.$notify({
          type: 'error',
          title: '错误',
          message: '请填写评价内容'
        })
        return
      }
      
      // 检查是否包含侮辱性言论
      if (contentFilterService.containsSensitiveContent(this.newReview.content)) {
        const sensitiveWords = contentFilterService.getSensitiveWordsInContent(this.newReview.content)
        this.$notify({
          type: 'error',
          title: '错误',
          message: '您的评价包含不适当的言论，请用客观理性的言论评价。请修改后再提交。'
        })
        return
      }
      
      this.reviewSubmitting = true
      
      // 准备提交的评价数据
      const reviewData = {
        ...this.newReview,
        userId: this.currentUser.id
      }
      
      reviewService.createReview(reviewData)
        .then(response => {
          // 重置表单
          this.newReview = {
            courseId: '',
            rating: 0,
            content: '',
            anonymous: false
          }
          this.showReviewForm = false
          this.$notify({
            type: 'success',
            title: '成功',
            message: '评价发布成功'
          })
          this.reviewSubmitting = false
          
          // 如果不是匿名评价且是自己的评价，添加到当前列表(显示为待审核)
          if (!reviewData.anonymous) {
            this.reviews.unshift(response.data)
          }
        })
        .catch(error => {
          console.error('提交评价失败:', error)
          this.reviewSubmitting = false
          
          if (error.response && error.response.data && error.response.data.message) {
            this.$notify({
              type: 'error',
              title: '错误',
              message: '提交失败: ' + error.response.data.message
            })
          } else {
            this.$notify({
              type: 'error',
              title: '错误',
              message: '评价提交失败，请稍后再试'
            })
          }
        })
    }
  },
  created() {
    this.loadTeacher()
  },
  watch: {
    '$route.params.id'(newValue) {
      if (newValue) {
        this.loadTeacher()
      }
    }
  }
}
</script>

<style scoped>
.teacher-detail-container {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

h1 {
  margin: 0;
  color: #333;
}

.btn-back {
  display: inline-block;
  background-color: #0066cc;
  color: white;
  padding: 8px 16px;
  border-radius: 4px;
  text-decoration: none;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background-color: #0055aa;
}

.loading, .error {
  text-align: center;
  padding: 50px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.btn-retry {
  background-color: #0066cc;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  margin-top: 10px;
  cursor: pointer;
}

.teacher-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* 教师信息卡片样式 */
.teacher-info-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.teacher-header {
  margin-bottom: 20px;
}

.teacher-name {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 1.8rem;
}

.teacher-meta {
  display: flex;
  gap: 15px;
  align-items: center;
  color: #666;
}

.teacher-title {
  font-weight: 500;
}

.teacher-contact {
  margin-bottom: 20px;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
}

.info-item .label {
  width: 100px;
  color: #666;
}

.info-item .value {
  color: #333;
}

.teacher-bio h3 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #333;
  font-size: 1.2rem;
}

.teacher-bio p {
  color: #333;
  line-height: 1.6;
}

/* 课程列表样式 */
.teacher-courses-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.teacher-courses-section h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
}

.courses-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.course-card {
  border: 1px solid #eee;
  border-radius: 4px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.course-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.course-link {
  display: block;
  padding: 15px;
  color: inherit;
  text-decoration: none;
}

.course-code {
  display: block;
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 5px;
}

.course-name {
  display: block;
  font-weight: 600;
  margin-bottom: 10px;
}

.course-type {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: white;
}

.course-type.compulsory {
  background-color: #dc3545;
}

.course-type.elective {
  background-color: #28a745;
}

/* 评价统计样式 */
.teacher-rating-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.teacher-rating-section h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
}

.ratings-overview {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
}

.average-rating {
  flex: 1;
  min-width: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.rating-number {
  font-size: 48px;
  font-weight: bold;
  color: #333;
  display: block;
}

.stars {
  color: #ffc107;
  margin: 10px 0;
  font-size: 24px;
}

.rating-count {
  color: #777;
  font-size: 14px;
}

.rating-distribution {
  flex: 2;
  padding-left: 30px;
}

.rating-bar {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.star-level {
  width: 40px;
  text-align: right;
  margin-right: 10px;
  font-size: 14px;
  color: #555;
}

.bar-container {
  flex: 1;
  height: 15px;
  background-color: #f1f1f1;
  border-radius: 4px;
  overflow: hidden;
  margin: 0 10px;
}

.bar {
  height: 100%;
  background-color: #ffc107;
}

.count {
  width: 30px;
  font-size: 14px;
  color: #555;
}

/* 评价部分样式 */
.teacher-reviews-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.teacher-reviews-section h3 {
  margin-top: 0;
  margin-bottom: 0;
  color: #333;
}

.btn-add-review {
  background-color: #0066cc;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-add-review:hover {
  background-color: #0055aa;
}

.login-prompt {
  text-align: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

.login-prompt a {
  color: #0066cc;
  text-decoration: none;
  font-weight: bold;
}

.review-form-container {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.review-form h4 {
  margin-top: 0;
  margin-bottom: 15px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: white;
}

.rating-input {
  display: flex;
}

.star {
  font-size: 24px;
  color: #ddd;
  margin-right: 5px;
  cursor: pointer;
  transition: color 0.2s;
}

.star.active, .star:hover {
  color: #ffc107;
}

textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.btn-cancel {
  background-color: #f1f1f1;
  color: #333;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  margin-right: 10px;
  cursor: pointer;
}

.btn-submit {
  background-color: #0066cc;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-submit:disabled {
  background-color: #99c2e8;
  cursor: not-allowed;
}

.no-reviews {
  text-align: center;
  padding: 30px;
  color: #666;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.reviews-list {
  margin-top: 20px;
}

.review-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  background-color: #fff;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.review-author {
  font-weight: 500;
}

.review-rating {
  color: #ffc107;
}

.review-content {
  color: #333;
  margin-bottom: 15px;
  line-height: 1.5;
}

.review-footer {
  display: flex;
  justify-content: space-between;
  color: #777;
  font-size: 14px;
}

.review-course a {
  color: #0066cc;
  text-decoration: none;
}

.review-course a:hover {
  text-decoration: underline;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .ratings-overview {
    flex-direction: column;
  }
  
  .rating-distribution {
    padding-left: 0;
    margin-top: 20px;
  }
  
  .review-footer {
    flex-direction: column;
    gap: 5px;
  }
}
</style> 
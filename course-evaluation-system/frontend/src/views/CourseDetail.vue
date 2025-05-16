<template>
  <div class="course-detail-container">
    <div class="page-header">
      <h1>课程详情</h1>
      <div class="header-actions">
        <!-- 如果是从老师详情页过来，则提供返回老师详情页的选项 -->
        <template v-if="fromTeacher && course.teacherId">
          <router-link :to="`/teachers/${course.teacherId}`" class="btn-back">
            <i class="fas fa-arrow-left"></i> 返回教师主页
          </router-link>
        </template>
        <!-- 如果是从管理页面过来，则提供返回课程管理页面的选项 -->
        <template v-else-if="fromAdmin">
          <router-link to="/admin/courses" class="btn-back">
            <i class="fas fa-arrow-left"></i> 返回课程管理
          </router-link>
        </template>
        <template v-else>
          <router-link to="/courses" class="btn-back">
            <i class="fas fa-arrow-left"></i> 返回课程列表
          </router-link>
        </template>
      </div>
    </div>
    
    <div v-if="loading" class="loading">
      <p>加载中，请稍候...</p>
    </div>
    
    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="loadCourse" class="btn-retry">重试</button>
    </div>
    
    <div v-else class="course-content">
      <!-- 课程基本信息 -->
      <div class="course-header-card">
        <div class="course-header">
          <div class="course-title-section">
            <span class="course-code">{{ course.code }}</span>
            <h2>{{ course.name }}</h2>
          </div>
          <div class="course-meta">
            <span :class="['course-type', course.type === 'COMPULSORY' ? 'compulsory' : 'elective']">
              {{ course.type === 'COMPULSORY' ? '必修课' : '选修课' }}
            </span>
            <span class="course-faculty">{{ course.facultyName }}</span>
            <span class="course-credits">学分: {{ course.credits }}</span>
          </div>
        </div>
        
        <div class="course-info">
          <div class="course-description">
            <h3>课程简介</h3>
            <p>{{ course.description || '暂无课程简介' }}</p>
          </div>
          
          <div class="course-teacher" v-if="course.teacherName">
            <h3>授课教师</h3>
            <router-link :to="`/teachers/${course.teacherId}`" class="teacher-link">
              {{ course.teacherName }}
            </router-link>
          </div>
          
          <div class="course-assessment" v-if="course.assessmentCriteria">
            <h3>考核标准</h3>
            <p>{{ course.assessmentCriteria }}</p>
          </div>
        </div>
      </div>
      
      <!-- 课程评价统计 -->
      <div class="course-rating-section" v-if="ratings">
        <h3>评价统计</h3>
        <div class="ratings-overview">
          <div class="average-rating">
            <span class="rating-number">{{ ratings.averageRating ? ratings.averageRating.toFixed(1) : 'N/A' }}</span>
            <div class="stars">
              <i v-for="n in 5" :key="n"
                 :class="ratings.averageRating && n <= Math.round(Number(ratings.averageRating)) ? 'fas fa-star' : 'far fa-star'"></i>
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
      
      <!-- 课程评价列表 -->
      <div class="course-reviews-section">
        <div class="reviews-header">
          <h3>学生评价</h3>
          <button v-if="isLoggedIn" @click="showReviewForm = true" class="btn-add-review">
            写评价
          </button>
        </div>
        
        <div v-if="!isLoggedIn" class="login-prompt">
          <p>请 <router-link to="/login">登录</router-link> 后参与课程评价</p>
        </div>
        
        <!-- 评价表单 -->
        <div v-if="showReviewForm" class="review-form-container">
          <form @submit.prevent="submitReview" class="review-form">
            <h4>写评价</h4>
            
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
                placeholder="分享你对这门课程的体验和想法..."
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
          <div v-for="review in reviews" :key="review.id" :class="['review-card', { 'pinned-review': review.pinned }, { 'admin-review': review.isAdminReview && !review.anonymous }]">
            <div class="review-header">
              <div class="review-author">
                <span>{{ review.anonymous ? '匿名用户' : review.username }}</span>
                <span v-if="review.isAdminReview && !review.anonymous" class="admin-tag">管理员</span>
                <span v-if="review.pinned" class="pin-tag"><i class="fas fa-thumbtack"></i> 置顶</span>
              </div>
              <div class="review-rating">
                <i v-for="n in 5" :key="n"
                   :class="n <= Number(review.rating) ? 'fas fa-star' : 'far fa-star'"></i>
              </div>
            </div>
            
            <div class="review-content">
              {{ review.content }}
            </div>
            
            <div class="review-footer">
              <span class="review-date">{{ formatDate(review.createdAt) }}</span>
              <div class="review-actions">
                <!-- 只有管理员可以置顶自己的评论 -->
                <template v-if="isAdmin && !review.pinned">
                  <button @click="pinReview(review)" class="btn-pin">
                    <i class="fas fa-thumbtack"></i> 置顶
                  </button>
                </template>
                <template v-if="isAdmin && review.pinned">
                  <button @click="unpinReview(review)" class="btn-unpin">
                    <i class="fas fa-thumbtack"></i> 取消置顶
                  </button>
                </template>
                <button v-if="isCurrentUserReview(review) || isAdmin" @click="deleteReview(review.id)" class="btn-delete">
                  <i class="fas fa-trash"></i> 删除
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import courseService from '@/services/course.service'
import reviewService from '@/services/review.service'
import contentFilterService from '@/services/content-filter.service'
import { mapGetters } from 'vuex'

export default {
  name: 'CourseDetail',
  data() {
    return {
      loading: true,
      reviewsLoading: false,
      reviewSubmitting: false,
      error: null,
      course: {},
      ratings: null,
      reviews: [],
      showReviewForm: false,
      fromTeacher: false, // 是否从教师页面跳转过来
      fromAdmin: false, // 是否从管理页面跳转过来
      newReview: {
        rating: 0,
        content: '',
        anonymous: false
      }
    }
  },
  computed: {
    ...mapGetters({
      isLoggedIn: 'auth/isLoggedIn',
      currentUser: 'auth/currentUser',
      isAdmin: 'auth/isAdmin'
    })
  },
  created() {
    // 检查是否从教师页面跳转过来
    this.fromTeacher = this.$route.query.from === 'teacher'
    // 检查是否从管理页面跳转过来
    this.fromAdmin = this.$route.query.from === 'admin'
    this.loadCourse()
  },
  methods: {
    loadCourse() {
      this.loading = true
      this.error = null
      const courseId = this.$route.params.id
      
      courseService.getCourseById(courseId)
        .then(response => {
          this.course = response.data
          this.loading = false
          
          // 加载评价统计和评价列表
          this.loadRatings()
          this.loadReviews()
        })
        .catch(error => {
          console.error('获取课程详情失败:', error)
          this.error = '获取课程详情失败，请稍后重试'
          this.loading = false
        })
    },
    
    loadRatings() {
      courseService.getCourseRatings(this.$route.params.id)
        .then(response => {
          this.ratings = response.data
          
          // 确保评分数据为数字类型
          if (this.ratings && this.ratings.averageRating) {
            this.ratings.averageRating = Number(this.ratings.averageRating);
          }
          
          // 确保评分分布数据为数字类型
          if (this.ratings && this.ratings.ratingDistribution) {
            for (let key in this.ratings.ratingDistribution) {
              this.ratings.ratingDistribution[key] = Number(this.ratings.ratingDistribution[key]);
            }
          }
          
          console.log('课程评分统计数据:', this.ratings);
        })
        .catch(error => {
          console.error('获取评价统计失败:', error)
        })
    },
    
    loadReviews() {
      this.reviewsLoading = true
      reviewService.getCourseReviews(this.$route.params.id)
        .then(response => {
          // 获取评论列表
          this.reviews = response.data;
          
          // 确保评分数据为数字类型
          this.reviews = this.reviews.map(review => ({
            ...review,
            rating: Number(review.rating)
          }));
          
          // 添加日志记录用于调试星级问题
          console.log('课程详情页评价列表数据:', this.reviews);
          this.reviews.forEach(review => {
            console.log(`课程详情页评价ID ${review.id} 的评分: ${review.rating}，类型: ${typeof review.rating}`);
          });
          
          // 对评论进行排序，置顶的评论排在前面，然后按创建时间降序排序
          this.reviews.sort((a, b) => {
            if (a.pinned && !b.pinned) {
              return -1;
            } else if (!a.pinned && b.pinned) {
              return 1;
            } else {
              return new Date(b.createdAt) - new Date(a.createdAt);
            }
          });
          
          this.reviewsLoading = false;
        })
        .catch(error => {
          console.error('获取评价列表失败:', error)
          this.reviewsLoading = false
        })
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
      
      // 只提交必要的字段，不包含置顶状态
      const reviewData = {
        rating: this.newReview.rating,
        content: this.newReview.content,
        anonymous: this.newReview.anonymous,
        courseId: this.course.id,
        userId: this.currentUser.id
      }
      
      reviewService.createReview(reviewData)
        .then(() => {
          // 重置表单
          this.newReview = {
            rating: 0,
            content: '',
            anonymous: false
          }
          
          // 关闭表单
          this.showReviewForm = false
          
          // 重新加载评价和统计
          this.loadRatings()
          this.loadReviews()
          
          this.reviewSubmitting = false
          
          this.$notify({
            type: 'success',
            title: '成功',
            message: '评价发布成功'
          })
        })
        .catch(error => {
          console.error('提交评价失败:', error)
          this.$notify({
            type: 'error',
            title: '错误',
            message: '提交评价失败: ' + (error.response?.data?.message || error.message)
          })
          this.reviewSubmitting = false
        })
    },
    
    calculatePercentage(count) {
      if (!this.ratings || !this.ratings.totalReviews || this.ratings.totalReviews === 0) {
        return 0
      }
      
      return (count / this.ratings.totalReviews) * 100
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    },
    
    deleteReview(reviewId) {
      if (!confirm('确定要删除这条评价吗？此操作不可恢复。')) {
        return
      }
      
      reviewService.deleteReview(reviewId)
        .then(() => {
          // 从列表中移除已删除的评价
          this.reviews = this.reviews.filter(review => review.id !== reviewId)
          // 重新加载评价统计
          this.loadRatings()
          this.$notify({
            type: 'success',
            title: '成功',
            message: '评价已删除'
          })
        })
        .catch(error => {
          console.error('删除评价失败:', error)
          this.$notify({
            type: 'error',
            title: '错误',
            message: '删除评价失败: ' + (error.response?.data?.message || error.message)
          })
        })
    },
    
    isCurrentUserReview(review) {
      return this.isLoggedIn && this.currentUser && review.userId === this.currentUser.id
    },
    
    pinReview(review) {
      reviewService.pinReview(review.id)
        .then(response => {
          // 仅更新本地状态，无需重新加载所有评论
          const index = this.reviews.findIndex(r => r.id === review.id);
          if (index !== -1) {
            this.reviews[index].pinned = true;
            // 重新排序评论，将置顶评论放在前面
            this.sortReviews();
          }
          this.$notify({
            type: 'success',
            title: '成功',
            message: '评价已置顶'
          })
        })
        .catch(error => {
          console.error("置顶评论失败:", error);
          this.$notify({
            type: "error",
            title: "错误",
            message: "置顶评论失败"
          });
        });
    },

    unpinReview(review) {
      reviewService.unpinReview(review.id)
        .then(response => {
          // 仅更新本地状态，无需重新加载所有评论
          const index = this.reviews.findIndex(r => r.id === review.id);
          if (index !== -1) {
            this.reviews[index].pinned = false;
            // 重新排序评论，将置顶评论放在前面
            this.sortReviews();
          }
          this.$notify({
            type: 'success',
            title: '成功',
            message: '已取消置顶'
          })
        })
        .catch(error => {
          console.error("取消置顶评论失败:", error);
          this.$notify({
            type: "error",
            title: "错误",
            message: "取消置顶评论失败"
          });
        });
    },

    // 添加新方法用于排序评论
    sortReviews() {
      // 对评论进行排序：置顶评论在前，其次按创建时间降序排列
      this.reviews.sort((a, b) => {
        if (a.pinned && !b.pinned) return -1;
        if (!a.pinned && b.pinned) return 1;
        
        // 如果置顶状态相同，则按创建时间降序排列
        const dateA = new Date(a.createdAt);
        const dateB = new Date(b.createdAt);
        return dateB - dateA;
      });
    }
  }
}
</script>

<style scoped>
.course-detail-container {
  padding: 30px;
  max-width: 1000px;
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
  padding: 50px 0;
  color: #666;
}

.btn-retry {
  background-color: #0066cc;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  margin-top: 15px;
  cursor: pointer;
}

.course-header-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.course-header {
  padding: 20px;
  background-color: #0066cc;
  color: white;
}

.course-code {
  display: inline-block;
  background-color: rgba(255, 255, 255, 0.2);
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 14px;
  margin-bottom: 8px;
}

.course-title-section h2 {
  margin: 0;
  font-size: 24px;
}

.course-meta {
  margin-top: 15px;
  display: flex;
  align-items: center;
}

.course-type {
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 14px;
  margin-right: 15px;
}

.course-type.compulsory {
  background-color: #e53935;
  color: white;
}

.course-type.elective {
  background-color: #43a047;
  color: white;
}

.course-faculty {
  font-size: 14px;
}

.course-credits {
  font-size: 14px;
  margin-left: 15px;
}

.course-info {
  padding: 20px;
}

.course-info h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 18px;
}

.course-description, .course-teacher, .course-assessment {
  margin-bottom: 20px;
}

.course-description p, .course-assessment p {
  margin: 0;
  line-height: 1.6;
  color: #555;
}

.teacher-link {
  color: #0066cc;
  text-decoration: none;
}

.teacher-link:hover {
  text-decoration: underline;
}

.course-rating-section, .course-reviews-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.course-rating-section h3, .course-reviews-section h3 {
  margin-top: 0;
  color: #333;
}

.ratings-overview {
  display: flex;
  margin-top: 20px;
}

.average-rating {
  flex: 1;
  text-align: center;
  padding-right: 30px;
  border-right: 1px solid #eee;
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

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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
  line-height: 1.6;
  color: #555;
  margin-bottom: 10px;
}

.review-footer {
  color: #999;
  font-size: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-actions {
  display: flex;
}

.btn-delete {
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-delete:hover {
  background-color: #d32f2f;
}

.pinned-review {
  border-left: 4px solid #ffc107;
  background-color: #fff9e6;
}

.pin-tag {
  display: inline-block;
  margin-left: 8px;
  color: #ffc107;
  font-size: 0.9em;
  font-weight: bold;
}

.btn-pin, .btn-unpin {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-right: 5px;
}

.btn-unpin {
  background-color: #ff9800;
}

.admin-tag {
  display: inline-block;
  margin-left: 5px;
  background-color: #1976d2;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.admin-review {
  border-left: 4px solid #1976d2;
  background-color: #f0f7ff;
}
</style> 
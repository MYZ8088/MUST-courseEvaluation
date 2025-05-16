import http from './http.service'

class ReviewService {
  // 获取所有评价（管理员用）
  getAllReviews(params = {}) {
    return http.get('reviews', { params })
  }
  
  // 获取课程评价
  getCourseReviews(courseId) {
    return http.get(`reviews/course/${courseId}`)
      .catch(error => {
        if (error.response && error.response.status === 401) {
          console.log('课程评价获取失败，尝试作为公共API重试')
          return http.get(`reviews/course/${courseId}`, { 
            headers: { 'Skip-Auth': true }
          })
        }
        return Promise.reject(error)
      })
  }
  
  // 获取教师评价
  getTeacherReviews(teacherId) {
    return http.get(`reviews/teacher/${teacherId}`)
      .catch(error => {
        if (error.response && error.response.status === 401) {
          console.log('教师评价获取失败，尝试作为公共API重试')
          return http.get(`reviews/teacher/${teacherId}`, { 
            headers: { 'Skip-Auth': true }
          })
        }
        return Promise.reject(error)
      })
  }
  
  // 获取用户评价
  getUserReviews(userId) {
    return http.get(`reviews/user/${userId}`)
  }
  
  // 创建评价
  createReview(reviewData) {
    return http.post('reviews', reviewData)
      .catch(error => {
        if (error.response && error.response.status === 403) {
          // 用户被禁言的情况
          return Promise.reject({
            ...error,
            isBanned: true,
            message: '您的账号已被禁言，无法发表评论'
          })
        }
        return Promise.reject(error)
      })
  }
  
  // 更新评价
  updateReview(id, reviewData) {
    return http.put(`reviews/${id}`, reviewData)
  }
  
  // 删除评价
  deleteReview(id) {
    // 从store获取当前用户ID
    const userStr = localStorage.getItem('user')
    const userId = userStr ? JSON.parse(userStr).id : null
    
    if (!userId) {
      return Promise.reject(new Error('未登录用户'))
    }
    
    return http.delete(`reviews/${id}?reviewOwnerId=${userId}`)
  }
  
  // 置顶评价（管理员用）
  pinReview(id) {
    return http.patch(`reviews/${id}/pin`)
  }
  
  // 取消置顶评价（管理员用）
  unpinReview(id) {
    return http.patch(`reviews/${id}/unpin`)
  }
  
  // 获取课程评分统计
  getCourseRatings(courseId) {
    return http.get(`reviews/course/${courseId}/ratings`)
      .catch(error => {
        if (error.response && error.response.status === 401) {
          console.log('课程评分获取失败，尝试作为公共API重试')
          return http.get(`reviews/course/${courseId}/ratings`, { 
            headers: { 'Skip-Auth': true }
          })
        }
        return Promise.reject(error)
      })
  }
  
  // 获取教师评分统计
  getTeacherRatings(teacherId) {
    return http.get(`reviews/teacher/${teacherId}/ratings`)
      .catch(error => {
        if (error.response && error.response.status === 401) {
          console.log('教师评分获取失败，尝试作为公共API重试')
          return http.get(`reviews/teacher/${teacherId}/ratings`, { 
            headers: { 'Skip-Auth': true }
          })
        }
        return Promise.reject(error)
      })
  }
  
  // 管理员删除评价（无需reviewOwnerId）
  adminDeleteReview(id) {
    return http.delete(`reviews/${id}/admin`)
  }
  
  // 批量删除评价（管理员用）
  batchDeleteReviews(ids) {
    return http.post('reviews/batch-delete', { ids })
  }
}

export default new ReviewService() 
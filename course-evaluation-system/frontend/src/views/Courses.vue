<template>
  <div class="courses-container">
    <div class="page-header">
      <h1>课程列表</h1>
      <router-link to="/" class="btn-back">
        <i class="fas fa-home"></i> 返回首页
      </router-link>
    </div>
    
    <!-- 搜索和过滤 -->
    <div class="filters">
      <div class="search-box">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索课程名称..." 
          @input="filterCourses"
        />
      </div>
      
      <div class="faculty-filter">
        <select v-model="selectedFaculty" @change="filterCourses">
          <option :value="null">所有院系</option>
          <option v-for="faculty in faculties" :key="faculty.id" :value="faculty.id">
            {{ faculty.name }}
          </option>
        </select>
      </div>
      
      <div class="type-filter">
        <select v-model="selectedType" @change="filterCourses">
          <option :value="null">所有类型</option>
          <option value="COMPULSORY">必修课</option>
          <option value="ELECTIVE">选修课</option>
        </select>
      </div>
    </div>
    
    <!-- 课程列表 -->
    <div class="courses-list" v-if="!loading">
      <div v-if="filteredCourses.length === 0" class="no-results">
        <p>没有找到符合条件的课程</p>
      </div>
      
      <div v-else class="courses-grid">
        <div 
          v-for="course in filteredCourses" 
          :key="course.id" 
          class="course-card"
          @click="viewCourseDetails(course.id)"
        >
          <div class="course-header">
            <span class="course-code">{{ course.code }}</span>
            <span :class="['course-type', course.type === 'COMPULSORY' ? 'compulsory' : 'elective']">
              {{ course.type === 'COMPULSORY' ? '必修' : '选修' }}
            </span>
          </div>
          <div class="course-info">
            <h3>{{ course.name }}</h3>
            <p class="course-faculty">{{ course.facultyName }}</p>
            <p class="course-teacher" v-if="course.teacherName">
              授课教师: {{ course.teacherName }}
            </p>
            <p class="course-credits">
              学分: {{ course.credits }}
            </p>
            <p class="course-rating" v-if="course.averageRating">
              <span class="stars">
                <i v-for="n in 5" :key="n" class="fas" 
                  :class="n <= Math.round(course.averageRating) ? 'fa-star' : 'fa-star-o'"></i>
              </span>
              <span class="rating-value">{{ course.averageRating.toFixed(1) }}</span>
              <span class="review-count">({{ course.reviewCount }}条评价)</span>
            </p>
            <p class="course-description" v-if="course.description">
              {{ truncate(course.description, 100) }}
            </p>
          </div>
        </div>
      </div>
    </div>
    
    <div v-else class="loading">
      <p>加载中，请稍候...</p>
    </div>
  </div>
</template>

<script>
import courseService from '@/services/course.service';
import facultyService from '@/services/faculty.service';

export default {
  name: 'Courses',
  data() {
    return {
      loading: true,
      courses: [],
      faculties: [],
      searchQuery: '',
      selectedFaculty: null,
      selectedType: null,
      filteredCourses: []
    }
  },
  created() {
    this.loadCourses();
    this.loadFaculties();
  },
  methods: {
    loadCourses() {
      this.loading = true;
      courseService.getAllCourses()
        .then(response => {
          this.courses = response.data;
          this.filteredCourses = [...this.courses];
          this.loading = false;
        })
        .catch(error => {
          console.error('获取课程列表失败:', error);
          this.loading = false;
        });
    },
    
    loadFaculties() {
      facultyService.getFaculties()
        .then(response => {
          this.faculties = response.data;
        })
        .catch(error => {
          console.error('获取院系列表失败:', error);
        });
    },
    
    filterCourses() {
      this.filteredCourses = this.courses.filter(course => {
        // 过滤院系
        const facultyMatch = !this.selectedFaculty || course.facultyId === this.selectedFaculty;
        
        // 过滤课程类型
        const typeMatch = !this.selectedType || course.type === this.selectedType;
        
        // 过滤搜索词
        const searchMatch = !this.searchQuery || 
          course.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
          course.code.toLowerCase().includes(this.searchQuery.toLowerCase());
          
        return facultyMatch && typeMatch && searchMatch;
      });
    },
    
    viewCourseDetails(courseId) {
      this.$router.push(`/courses/${courseId}`);
    },
    
    truncate(text, length) {
      if (!text) return '';
      return text.length > length ? text.substring(0, length) + '...' : text;
    }
  }
}
</script>

<style scoped>
.courses-container {
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

.filters {
  display: flex;
  margin-bottom: 20px;
  gap: 15px;
  width: 100%;
}

.search-box {
  flex: 2; /* 搜索框占2/4的宽度 */
}

.faculty-filter, .type-filter {
  flex: 1; /* 院系和类型过滤器各占1/4的宽度 */
}

.search-box input,
.faculty-filter select, 
.type-filter select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.courses-list {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.course-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
  background-color: #fff;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.course-header {
  display: flex;
  justify-content: space-between;
  padding: 10px 15px;
  background-color: #f9f9f9;
  border-bottom: 1px solid #eee;
}

.course-code {
  font-weight: bold;
  color: #555;
}

.course-type {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  color: white;
}

.course-type.compulsory {
  background-color: #e53935;
}

.course-type.elective {
  background-color: #43a047;
}

.course-info {
  padding: 15px;
}

.course-info h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
}

.course-faculty {
  color: #0066cc;
  margin: 0 0 8px 0;
  font-size: 14px;
}

.course-teacher {
  color: #555;
  margin: 0 0 8px 0;
  font-size: 14px;
}

.course-credits {
  color: #555;
  margin: 0 0 8px 0;
  font-size: 14px;
}

.course-rating {
  display: flex;
  align-items: center;
  margin: 0 0 8px 0;
}

.stars {
  color: #ffc107;
  margin-right: 5px;
}

.rating-value {
  font-weight: bold;
  margin-right: 5px;
}

.review-count {
  color: #777;
  font-size: 12px;
}

.course-description {
  color: #666;
  font-size: 14px;
  line-height: 1.4;
  margin: 10px 0 0 0;
}

.no-results, .loading {
  text-align: center;
  padding: 30px;
  color: #666;
}
</style> 
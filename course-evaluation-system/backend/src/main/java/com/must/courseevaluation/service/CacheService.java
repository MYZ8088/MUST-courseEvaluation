package com.must.courseevaluation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String COURSE_CACHE_PREFIX = "course:";
    private static final String TEACHER_CACHE_PREFIX = "teacher:";
    private static final String RATING_CACHE_PREFIX = "rating:";
    private static final String FACULTY_CACHE_PREFIX = "faculty:";
    
    // 缓存过期时间（小时）
    private static final int CACHE_EXPIRE_HOURS = 2;
    
    /**
     * 缓存课程信息
     */
    public void cacheCourse(Long courseId, Object courseData) {
        String key = COURSE_CACHE_PREFIX + courseId;
        redisTemplate.opsForValue().set(key, courseData, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
    }
    
    /**
     * 获取缓存的课程信息
     */
    public Object getCachedCourse(Long courseId) {
        String key = COURSE_CACHE_PREFIX + courseId;
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 缓存教师信息
     */
    public void cacheTeacher(Long teacherId, Object teacherData) {
        String key = TEACHER_CACHE_PREFIX + teacherId;
        redisTemplate.opsForValue().set(key, teacherData, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
    }
    
    /**
     * 获取缓存的教师信息
     */
    public Object getCachedTeacher(Long teacherId) {
        String key = TEACHER_CACHE_PREFIX + teacherId;
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 缓存评分统计
     */
    public void cacheRating(String type, Long id, Object ratingData) {
        String key = RATING_CACHE_PREFIX + type + ":" + id;
        redisTemplate.opsForValue().set(key, ratingData, 30, TimeUnit.MINUTES); // 评分缓存30分钟
    }
    
    /**
     * 获取缓存的评分统计
     */
    public Object getCachedRating(String type, Long id) {
        String key = RATING_CACHE_PREFIX + type + ":" + id;
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 缓存院系列表
     */
    public void cacheFaculties(Object facultiesData) {
        String key = FACULTY_CACHE_PREFIX + "all";
        redisTemplate.opsForValue().set(key, facultiesData, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
    }
    
    /**
     * 获取缓存的院系列表
     */
    public Object getCachedFaculties() {
        String key = FACULTY_CACHE_PREFIX + "all";
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 清除相关缓存
     */
    public void evictCourseCache(Long courseId) {
        String key = COURSE_CACHE_PREFIX + courseId;
        redisTemplate.delete(key);
        
        // 同时清除相关的评分缓存
        String ratingKey = RATING_CACHE_PREFIX + "course:" + courseId;
        redisTemplate.delete(ratingKey);
    }
    
    /**
     * 清除教师相关缓存
     */
    public void evictTeacherCache(Long teacherId) {
        String key = TEACHER_CACHE_PREFIX + teacherId;
        redisTemplate.delete(key);
        
        // 同时清除相关的评分缓存
        String ratingKey = RATING_CACHE_PREFIX + "teacher:" + teacherId;
        redisTemplate.delete(ratingKey);
    }
    
    /**
     * 清除所有院系缓存
     */
    public void evictFacultiesCache() {
        String key = FACULTY_CACHE_PREFIX + "all";
        redisTemplate.delete(key);
    }
}
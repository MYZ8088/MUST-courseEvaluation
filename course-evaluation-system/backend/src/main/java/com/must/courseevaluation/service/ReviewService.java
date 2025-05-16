package com.must.courseevaluation.service;

import com.must.courseevaluation.dto.ReviewDto;
import com.must.courseevaluation.model.Review;

import java.util.List;
import java.util.Map;

public interface ReviewService {
    List<ReviewDto> getAllReviews();
    ReviewDto getReviewById(Long id);
    List<ReviewDto> getReviewsByCourse(Long courseId);
    List<ReviewDto> getReviewsByTeacher(Long teacherId);
    List<ReviewDto> getReviewsByUser(Long userId);
    ReviewDto createReview(ReviewDto reviewDto);
    ReviewDto updateReview(ReviewDto reviewDto);
    ReviewDto updateReview(Long id, ReviewDto reviewDto);
    void deleteReview(Long id);
    Map<String, Object> getCourseRatings(Long courseId);
    Map<String, Object> getTeacherRatings(Long teacherId);
    
    // 新增的置顶和取消置顶方法
    ReviewDto pinReview(Long id);
    ReviewDto unpinReview(Long id);
    
    // 保留这些方法但更改实现，维持向后兼容
    List<ReviewDto> getReviewsByCourse(Long courseId, boolean approvedOnly);
    List<ReviewDto> getReviewsByTeacher(Long teacherId, boolean approvedOnly);
    List<ReviewDto> getReviewsByStatus(Review.ReviewStatus status);
    ReviewDto updateReviewStatus(Long id, Review.ReviewStatus status);
    void approveReview(Long id);
    void rejectReview(Long id);
} 
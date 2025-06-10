package com.gandalp.gandalp.notice.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.notice.entity.Notice;
import com.gandalp.gandalp.notice.entity.NoticeCategory;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {


	List<Notice> findAllByCategoryAndCreatedAtBetweenAndDepartment(NoticeCategory category, LocalDateTime start, LocalDateTime end, Department department);

	List<Notice> findAllByCategoryAndCreatedAtBefore(NoticeCategory category, LocalDateTime limit);

	List<Notice> findAllByDepartmentAndCategory(Department department, NoticeCategory category);

	boolean existsByContentAndCategoryAndDepartment(String content, NoticeCategory category, Department department);

}

package com.kustacks.kuring.notice.domain;

import com.kustacks.kuring.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeQueryRepository {

    List<Notice> findByCategory(Category category);

    List<Notice> findBySubjectContainingOrCategoryNameContaining(String subject, String categoryName);

    default Map<String, Notice> findByCategoryMap(Category category) {
        return findByCategory(category).stream().collect(Collectors.toMap(Notice::getArticleId, v -> v));
    }
}

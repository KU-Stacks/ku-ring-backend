package com.kustacks.kuring.notice.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeQueryRepository {

    List<Notice> findByCategoryName(CategoryName categoryName);
}

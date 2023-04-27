package com.kustacks.kuring.notice.domain;

import com.kustacks.kuring.category.domain.Category;
import com.kustacks.kuring.category.domain.CategoryName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "article_id", length = 15, nullable = false)
    private String articleId;

    @Column(name = "posted_dt", length = 32, nullable = false)
    private String postedDate;

    @Column(name = "updated_dt", length = 32)
    private String updatedDate;

    @Column(name = "subject", length = 128, nullable = false)
    private String subject;

    @Column(name = "important")
    private Boolean important = false;

    @Embedded
    private Url url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_name", nullable = false)
    private Category category;

    public Notice(String articleId, String postedDate, String updatedDate, String subject, Category category, Boolean important, String fullUrl) {
        this.articleId = articleId;
        this.postedDate = postedDate;
        this.updatedDate = updatedDate;
        this.subject = subject;
        this.category = category;
        this.important = important;
        this.url = new Url(fullUrl);
    }

    public boolean isSameCategoryName(CategoryName categoryName) {
        return this.category.isSameName(categoryName);
    }

    public void setPostedDate(String postedDate) {
        this.postedDate = postedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notice notice = (Notice) o;
        return Objects.equals(getId(), notice.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}


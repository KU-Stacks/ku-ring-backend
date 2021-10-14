package com.kustacks.kuring.domain.category;

import com.kustacks.kuring.domain.notice.Notice;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "category")
public class Category {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", unique = true, nullable = false)
//    private Long id;

    @Id
    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @OneToMany(mappedBy = "category")
    @Fetch(FetchMode.JOIN) // TODO: 확인 필요
    private List<Notice> noticeList = new ArrayList<>();

    @Builder
    public Category(String name) {
        this.name = name;
    }
}

package com.kustacks.kuring.category.domain;

import lombok.Getter;

@Getter
public enum CategoryName {

    BACHELOR("bachelor", "bch", "학사"),
    SCHOLARSHIP("scholarship", "sch", "장학"),
    EMPLOYMENT("employment", "emp", "취창업"),
    NATIONAL("national", "nat", "국제"),
    STUDENT("student", "stu", "학생"),
    INDUSTRY_UNIVERSITY("industry_university", "ind", "산학"),
    NORMAL("normal", "nor", "일반"),
    LIBRARY("library", "lib", "도서관"),
    DEPARTMENT("department", "dep", "학과");

    private String name;
    private String shortName;
    private String korName;

    CategoryName(String name, String shortName, String korName) {
        this.name = name;
        this.shortName = shortName;
        this.korName = korName;
    }

    public boolean isSameName(String name) {
        return this.name.equals(name);
    }

    public boolean isSameShortName(String shortName) {
        return this.shortName.equals(shortName);
    }

    public boolean isSameKorName(String name) {
        return this.korName.equals(name);
    }
}

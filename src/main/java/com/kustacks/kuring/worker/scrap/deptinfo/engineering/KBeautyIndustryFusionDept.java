package com.kustacks.kuring.worker.scrap.deptinfo.engineering;

import com.kustacks.kuring.worker.DepartmentName;
import com.kustacks.kuring.worker.scrap.deptinfo.NoticeScrapInfo;
import com.kustacks.kuring.worker.scrap.deptinfo.RegisterDepartmentMap;
import com.kustacks.kuring.worker.scrap.deptinfo.StaffScrapInfo;

import java.util.Collections;
import java.util.List;

@RegisterDepartmentMap(key = DepartmentName.KBEAUTY)
public class KBeautyIndustryFusionDept extends EngineeringCollege {
    
    public KBeautyIndustryFusionDept() {
        super();
        List<String> professorForumIds = List.of("17275625");
        List<String> forumIds = List.of("17258184");
        List<String> boardSeqs = Collections.emptyList();
        List<String> menuSeqs = Collections.emptyList();

        this.staffScrapInfo = new StaffScrapInfo(professorForumIds);
        this.noticeScrapInfo = new NoticeScrapInfo(forumIds, "KBEAUTY", boardSeqs, menuSeqs);
        this.code = "127432";
        this.deptName = "K뷰티산업융합학과";
    }
}

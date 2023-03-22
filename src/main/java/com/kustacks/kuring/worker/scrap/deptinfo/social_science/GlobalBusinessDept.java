package com.kustacks.kuring.worker.scrap.deptinfo.social_science;

import com.kustacks.kuring.worker.DepartmentName;
import com.kustacks.kuring.worker.scrap.deptinfo.NoticeScrapInfo;
import com.kustacks.kuring.worker.scrap.deptinfo.RegisterDepartmentMap;
import com.kustacks.kuring.worker.scrap.deptinfo.StaffScrapInfo;

import java.util.Collections;
import java.util.List;

@RegisterDepartmentMap(key = DepartmentName.GLOBAL_BUSI)
public class GlobalBusinessDept extends SocialSciencesCollege {

    public GlobalBusinessDept() {
        super();
        List<String> professorForumIds = List.of("7516");
        List<String> forumIds = Collections.emptyList();
        List<String> boardSeqs = List.of("1002");
        List<String> menuSeqs = List.of("7026");

        this.staffScrapInfo = new StaffScrapInfo(professorForumIds);
        this.noticeScrapInfo = new NoticeScrapInfo(forumIds, "ITRADE", boardSeqs, menuSeqs);
        this.code = "127126";
        this.deptName = DepartmentName.GLOBAL_BUSI.getKorName();
    }
}

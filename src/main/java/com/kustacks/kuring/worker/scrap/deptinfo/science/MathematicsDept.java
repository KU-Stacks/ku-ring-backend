package com.kustacks.kuring.worker.scrap.deptinfo.science;

import com.kustacks.kuring.worker.DepartmentName;
import com.kustacks.kuring.worker.scrap.deptinfo.NoticeScrapInfo;
import com.kustacks.kuring.worker.scrap.deptinfo.RegisterDepartmentMap;
import com.kustacks.kuring.worker.scrap.deptinfo.StaffScrapInfo;

import java.util.Collections;
import java.util.List;

@RegisterDepartmentMap(key = DepartmentName.MATH)
public class MathematicsDept extends ScienceCollege {

    public MathematicsDept() {
        super();
        List<String> professorForumIds = List.of("8663");
        List<String> forumIds = List.of("8652");
        List<String> boardSeqs = Collections.emptyList();
        List<String> menuSeqs = Collections.emptyList();

        this.staffScrapInfo = new StaffScrapInfo(professorForumIds);
        this.noticeScrapInfo = new NoticeScrapInfo(forumIds, "MATH", boardSeqs, menuSeqs);
        this.code = "121260";
        this.deptName = DepartmentName.MATH.getKorName();
    }
}

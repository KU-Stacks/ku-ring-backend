package com.kustacks.kuring.worker.update.staff;

import com.kustacks.kuring.common.dto.StaffDto;
import com.kustacks.kuring.staff.domain.Staff;
import com.kustacks.kuring.staff.domain.StaffRepository;
import com.kustacks.kuring.common.error.InternalLogicException;
import com.kustacks.kuring.worker.update.Updater;
import com.kustacks.kuring.worker.scrap.StaffScraper;
import com.kustacks.kuring.worker.update.staff.deptinfo.DeptInfo;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StaffUpdater implements Updater {

    private final StaffRepository staffRepository;

    private final StaffScraper staffScraper;
    private final List<DeptInfo> deptInfos;

    private final int STAFF_UPDATE_RETRY_PERIOD = 1000 * 60; // 1분후에 실패한 크론잡 재시도

    public StaffUpdater(StaffRepository staffRepository,
                        StaffScraper staffScraper,
                        List<DeptInfo> deptInfos) {

        this.staffRepository = staffRepository;

        this.staffScraper = staffScraper;
        this.deptInfos = deptInfos;
    }

    @Override
    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.DAYS)
    public void update() {

        log.info("========== 교직원 업데이트 시작 ==========");

        // www.konkuk.ac.kr 에서 스크래핑하므로, kuis 로그인 필요 없음

        /*
           각 학과별 url로 스크래핑, 교수진 데이터 수집

           스크래핑 실패한 학과들을 재시도하기 위해 호출된 경우
           values에 StaffDeptInfo 전체 값이 아닌, 매개변수로 들어온 값을 전달한다.
         */

        Map<String, StaffDto> kuStaffDTOMap = new HashMap<>();
        List<String> successDeptNames = new LinkedList<>();
        for (DeptInfo deptInfo : deptInfos) {
            try {
                scrapDeptAndConvertToMap(kuStaffDTOMap, deptInfo);
                successDeptNames.add(deptInfo.getDeptName());
            } catch(InternalLogicException e) {
                log.error("[ScraperException] 스크래핑 문제 발생. 문제가 발생한 학과 = {}", deptInfo.getDeptName());
                Sentry.captureException(e);
            }
        }

        compareAndUpdateDB(kuStaffDTOMap, successDeptNames);

        log.info("========== 교직원 업데이트 종료 ==========");
    }

    private void scrapDeptAndConvertToMap(Map<String, StaffDto> kuStaffDTOMap, DeptInfo deptInfo) throws InternalLogicException {

        List<StaffDto> scrapedStaffDtoList = staffScraper.scrap(deptInfo);

        for (StaffDto staffDTO : scrapedStaffDtoList) {
            StaffDto mapStaffDto = kuStaffDTOMap.get(staffDTO.getEmail());
            if(mapStaffDto == null) {
                kuStaffDTOMap.put(staffDTO.getEmail(), staffDTO);
            } else {
                mapStaffDto.setDeptName(mapStaffDto.getDeptName() + ", " + staffDTO.getDeptName());
            }
        }
    }

    private void compareAndUpdateDB(Map<String, StaffDto> kuStaffDTOMap, List<String> successDeptNames) {

        // 스크래핑으로 수집한 교직원 정보와 비교
        // 달라진 정보가 있거나, 새로운 교직원 정보이면 db에 추가할 리스트에 저장
        // db에는 있으나 스크래핑한 리스트에 없는 교직원이라면, 삭제할 리스트에 저장

        // db에 저장되어있는 교직원 정보 조회
        Map<String, Staff> dbStaffMap = staffRepository.findByDeptContainingMap(successDeptNames);
        List<Staff> toBeUpdateStaffs = new LinkedList<>();
        Iterator<StaffDto> kuStaffDTOIterator = kuStaffDTOMap.values().iterator();
        while(kuStaffDTOIterator.hasNext()) {
            StaffDto staffDTO = kuStaffDTOIterator.next();

            Staff staff = dbStaffMap.get(staffDTO.getEmail());
            if(staff != null) {
                StaffDto dbStaffDto = StaffDto.entityToDto(staff);

                if(!staffDTO.equals(dbStaffDto)) {
                    updateStaffEntity(staffDTO, staff);
                    toBeUpdateStaffs.add(staff);
                }

                dbStaffMap.remove(staffDTO.getEmail());
                kuStaffDTOIterator.remove();
            }
        }

        log.info("=== 삭제할 교직원 리스트 ===");
        for (String key : dbStaffMap.keySet()) {
            log.info("{} {} {}", dbStaffMap.get(key).getCollege(), dbStaffMap.get(key).getDept(), dbStaffMap.get(key).getName());
        }
        log.info("=== 업데이트할 교직원 리스트 ===");
        for (Staff toBeUpdateStaff : toBeUpdateStaffs) {
            log.info("{} {} {}", toBeUpdateStaff.getCollege(), toBeUpdateStaff.getDept(), toBeUpdateStaff.getName());
        }
        log.info("=== 추가할 교직원 리스트 ===");
        for (String key : kuStaffDTOMap.keySet()) {
            log.info("{} {} {}", kuStaffDTOMap.get(key).getCollegeName(), kuStaffDTOMap.get(key).getDeptName(), kuStaffDTOMap.get(key).getName());
        }

        staffRepository.deleteAll(dbStaffMap.values());
        staffRepository.saveAll(kuStaffDTOMap.values().stream().map(StaffDto::toEntity).collect(Collectors.toList()));
        staffRepository.saveAll(toBeUpdateStaffs);
    }

    private void updateStaffEntity(StaffDto staffDTO, Staff staff) {
        staff.setName(staffDTO.getName());
        staff.setMajor(staffDTO.getMajor());
        staff.setLab(staffDTO.getLab());
        staff.setPhone(staffDTO.getPhone());
        staff.setEmail(staffDTO.getEmail());
        staff.setDept(staffDTO.getDeptName());
        staff.setCollege(staffDTO.getCollegeName());
    }
}
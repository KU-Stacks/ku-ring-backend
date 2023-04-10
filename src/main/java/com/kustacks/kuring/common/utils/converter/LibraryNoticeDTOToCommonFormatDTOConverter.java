package com.kustacks.kuring.common.utils.converter;

import com.kustacks.kuring.worker.update.notice.dto.response.CommonNoticeFormatDto;
import com.kustacks.kuring.worker.update.notice.dto.response.LibraryNoticeDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LibraryNoticeDTOToCommonFormatDTOConverter implements DTOConverter {

    @Value("${notice.library-base-url}")
    private String libraryBaseUrl;

    @Override
    public Object convert(Object target) {

        LibraryNoticeDTO libraryNoticeDTO = (LibraryNoticeDTO) target;
        return CommonNoticeFormatDto.builder()
                .articleId(libraryNoticeDTO.getId())
                .postedDate(libraryNoticeDTO.getDateCreated())
                .updatedDate(libraryNoticeDTO.getLastUpdated())
                .subject(libraryNoticeDTO.getTitle())
                .fullUrl(libraryBaseUrl + "/" + libraryNoticeDTO.getId())
                .build();
    }
}

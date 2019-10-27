package org.sterl.jmsui.bl.common.api;

import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor
public class SimplePageable {
    
    /**
     * Returns the offset to be taken according to the underlying page and page size.
     */
    private long offset;
    /**
     * Returns the page to be returned.
     */
    private int pageNumber;

    public SimplePageable(Pageable in) {
        this.offset = in.getOffset();
        this.pageNumber = in.getPageNumber();
    }
}

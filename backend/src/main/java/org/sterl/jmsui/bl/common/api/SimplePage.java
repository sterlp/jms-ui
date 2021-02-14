package org.sterl.jmsui.bl.common.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Goal is to have a simple Spring Page implementation which is compatible to the
 * JSON structure.
 * 
 * @author sterlp
 * @param <T> inner Type
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor
public class SimplePage<T> {
    public static <T> SimplePage<T> of(List<T> items) {
        SimplePage<T> result = new SimplePage<>();
        result.content = items;
        result.number = 0;
        result.numberOfElements = items.size();
        result.size = items.size();
        result.totalElements = items.size();
        return result;
    }

    private List<T> content = new ArrayList<>();
    /**
     * Returns the number of the current {@link Slice}. Is always non-negative.
     */
    private int number;
    /**
     * Returns the number of elements currently on this {@link Slice}.
     */
    private int numberOfElements;
    /**
     * Returns the size of the {@link Slice}.
     */
    private int size;
    /**
     * Returns the total amount of elements.
     */
    private long totalElements;
    /**
     * Returns the {@link Pageable} that's been used to request the current {@link Slice}.
     */
    private SimplePageable pageable;
    
    public SimplePage(Page<T> in) {
        this.content = in.getContent();
        this.number = in.getNumber();
        this.numberOfElements = in.getNumberOfElements();
        this.size = in.getSize();
        this.totalElements = in.getTotalElements();
        if (null != in.getPageable() && in.getPageable().isPaged()) {
            this.pageable = new SimplePageable(in.getPageable());
        }
    }
}

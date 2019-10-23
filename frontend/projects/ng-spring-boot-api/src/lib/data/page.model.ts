export interface PageInfo {
    /**
     * Returns the offset to be taken according to the underlying page and page size.
     */
    offset: number;
    /**
     * Returns the page to be returned.
     */
    pageNumber: number;
}
export const EMPTY_PAGE: Page<any> = {
    content: [],
    number: 0,
    numberOfElements: 0,
    size: 0,
    totalElements: 0,
    pageable: {offset: 0, pageNumber: 0}
};
/**
 * Simple Spring Page which wraps the content during a pagining request/ reponse.
 *
 * A page is a sublist of a list of objects. It allows gain information about
 * the position of it in the containing entire list.
 */
export interface Page<T> {
    /** The content data */
    content: Array<T>;
    /**
     * Returns the number of the current {@link Slice}. Is always non-negative.
     */
    number: number;
    /**
     * Returns the number of elements currently on this {@link Slice}.
     */
    numberOfElements: number;
    /**
     * Returns the size of the {@link Slice}.
     */
    size: number;
    /**
     * Returns the total amount of elements.
     */
    totalElements: number;
    /**
     * Returns the {@link Pageable} that's been used to request the current {@link Slice}.
     */
    pageable: PageInfo;
}

package com.atlassian.plugin.connect.jira.util;

import com.atlassian.jira.util.Page;
import com.atlassian.jira.util.PageRequest;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.util.List;

// TODO: remove this once it lands in the JIRA API
public class Pages {
    /**
     * Creates a page given a list of values consisting the page, a total count of all values and a page request
     *
     * @param values values that appear on the page
     * @param totalCount total count of all values
     * @param pageRequest a page request used to create this page
     * @param <T> type of entities in the list
     * @return the requested page
     *
     * @throws IllegalArgumentException if arguments are inconsistent
     */
    public static <T> Page<T> page(final Iterable<T> values, final long totalCount, final PageRequest pageRequest) {
        ImmutableList<T> valuesList = ImmutableList.copyOf(values);

        boolean isLast = pageRequest.getStart() + valuesList.size() == totalCount;

        if (!isLast && pageRequest.getLimit() != valuesList.size() || valuesList.size() > totalCount) {
            throw new IllegalArgumentException("inconsistent arguments");
        }

        return PageImpl.<T>builder()
                .setIsLast(isLast)
                .setSize(valuesList.size())
                .setStart(pageRequest.getStart())
                .setTotal(totalCount)
                .setValues(valuesList)
                .build();
    }

    private static final class PageImpl<T> implements Page<T> {
        private final long start;
        private final Long total;
        private final int size;
        private final boolean isLast;
        private final List<T> values;

        private PageImpl(long start, Long total, int size, boolean isLast, List<T> values) {
            this.start = start;
            this.total = total;
            this.size = size;
            this.isLast = isLast;
            this.values = values;
        }

        public long getStart() {
            return start;
        }

        public Long getTotal() {
            return total;
        }

        public int getSize() {
            return size;
        }

        public boolean isLast() {
            return isLast;
        }

        public List<T> getValues() {
            return values;
        }

        public static <T> Builder<T> builder() {
            return new Builder<>();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            PageImpl that = (PageImpl) o;

            return Objects.equal(this.start, that.start) &&
                    Objects.equal(this.total, that.total) &&
                    Objects.equal(this.size, that.size) &&
                    Objects.equal(this.isLast, that.isLast) &&
                    Objects.equal(this.values, that.values);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(start, total, size, isLast, values);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("start", start)
                    .add("total", total)
                    .add("size", size)
                    .add("hasNext", isLast)
                    .add("values", values)
                    .toString();
        }

        public static final class Builder<T> {
            private long start;
            private Long total;
            private int size;
            private boolean isLast;
            private List<T> values = ImmutableList.of();

            private Builder() {
            }

            public Builder<T> setStart(long start) {
                this.start = start;
                return this;
            }

            public Builder<T> setTotal(Long total) {
                this.total = total;
                return this;
            }

            public Builder<T> setSize(int size) {
                this.size = size;
                return this;
            }

            public Builder<T> setIsLast(boolean isLast) {
                this.isLast = isLast;
                return this;
            }

            public Builder<T> setValues(List<T> values) {
                this.values = values;
                return this;
            }

            public PageImpl<T> build() {
                return new PageImpl<>(start, total, size, isLast, values);
            }
        }
    }
}

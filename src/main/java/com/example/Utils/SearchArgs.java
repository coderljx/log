package com.example.Utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchArgs {
    private final Logger mylog = LoggerFactory.getLogger(SearchArgs.class);
    public Integer per_page;
    public Integer curr_page;
    public String search;
    public SearchArgs.Filters filters;
    public List<SearchArgs.Order> order;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filters {
        private List<ArgsItem> rules;
        public String search;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArgsItem {
        private String type;
        private List<Condition> children;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Condition {
        public String field;
        public String operator;
        public String value;
        public List<String> values;
        public String type;
        public List<Condition> children;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {
        private String field;
        private String order_type;
    }

    @Data
    public static class ESArgs {
        private MatchQueryBuilder matchQueryBuilder;
        private RangeQueryBuilder rangeQueryBuilder;
        private Sort sort;
    }


}


package com.example.Utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        public String field(){
            return "field";
        }
        public String operator(){
            return "operator";
        }
        public String value(){
            return "value";
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {
        private String field;
        private String order_type;
    }


}


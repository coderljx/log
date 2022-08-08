package com.example.Utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchArgsMap {

    private Integer per_page;
    private Integer curr_page;
    private String search;
    private Map<String,Object> filters;
    private Map<String,Object> order1;

    private SearchArgs.ArgsItem argsItem = new SearchArgs.ArgsItem();
    private SearchArgs.Order order = new SearchArgs.Order();

    public SearchArgsMap(Map<String,Object> filters,Map<String,Object> order) {
        this.filters = filters;
        this.order1 = order;
    }


    /**
     * 将前端传入的map转换成bean
     * @return
     * @throws Exception
     */
    public boolean MapTpArgsItem() throws Exception{
        if (this.filters == null) return false;

        Map<String, Object> filters = this.getFilters();
        if (filters.get("rules") != null) {
            List<Map<String,Object>> rules = (List<Map<String, Object>>) filters.get("rules");

            for (Map<String, Object> rule : rules) {
                argsItem.setType((String) rule.get("type"));
                List<Map<String,Object>> children = (List<Map<String, Object>>) rule.get("children");

                for (int c = 0; c < children.size(); c++) {
                    // children中还有
                    if (children.get(c).get("children") != null && children.get(c).get("type") != null) {
                        List<Map<String,Object>> children3 = (List<Map<String, Object>>)children.get(c).get("children");
                        argsItem.setChildren(this.GetConditionFormMaps(children3));
                        return true;
                    }else {
                        argsItem.setChildren(this.GetConditionFormMaps(children));
                        return true;
                    }

                }
            }
        }
        return false;
    }


    /**
     * 解析map中的参数，设置order排序
     */
    public <T> boolean MapToOrder(Class<T> cls){
        if (this.order == null) return false;

        Map<String, Object> order = this.order1;
        if (order.get("field") == null || order.get("order_type") == null) return false;

        String field = (String) order.get("field");
        String type = (String) order.get("order_type");
        if (!field.equals("") && !type.equals("")){
            if (Maputil.MapExistsBean(field, cls)){
                this.order.setField(field);
                this.order.setOrder_type(type);
                return true;
            }
        }
        return false;
    }


    /**
     * 获取Condition，从map中获取
     */
    private List<SearchArgs.Condition> GetConditionFormMaps (List<Map<String,Object>> content) throws Exception {
        List<SearchArgs.Condition> list = new ArrayList<>();
        for (Map<String, Object> stringObjectMap : content) {
            SearchArgs.Condition t = new SearchArgs.Condition();
            String names[] = Maputil.BeanKeys(t.getClass());
            for (String name : names) {
                Field field = t.getClass().getField(name);
                if (field.getType().isInstance(stringObjectMap.get(name))){
                    if(name.equals(field.getName())){
                        field.set(t, stringObjectMap.get(name));
                    }
                }
            }
            list.add(t);
        }
        return list;
    }



}

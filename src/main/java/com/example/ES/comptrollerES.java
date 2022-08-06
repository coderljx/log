package com.example.ES;

import com.example.Pojo.comptroller;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface comptrollerES extends ElasticsearchRepository<comptroller,Long> {

    List<comptroller> findAllBy(PageRequest request);

    List<comptroller> findByAuditcontent(String message, PageRequest request);
    List<comptroller> findByAppname(String Moudel,PageRequest request);
    List<comptroller> findByRecorddate(long date, PageRequest request);
    List<comptroller> findByRecorddateAfter(long date, PageRequest request);
    List<comptroller> findByRecorddateBefore(long date, PageRequest request);
    List<comptroller> findByRecorddateBetween(long start,long end , PageRequest request);





}

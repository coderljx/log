package com.example.ES;

import com.example.Pojo.Log;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogES extends ElasticsearchRepository<Log,Long> {

    List<Log> findAllBy(PageRequest request);

    List<Log> findByRecorddate(long date, PageRequest request);
    List<Log> findByRecorddateAfter(long date, PageRequest request);
    List<Log> findByRecorddateBefore(long date, PageRequest request);
    List<Log> findByRecorddateBetween(long start,long end, PageRequest request);

}

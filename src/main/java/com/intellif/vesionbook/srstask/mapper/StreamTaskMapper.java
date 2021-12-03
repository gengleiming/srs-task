package com.intellif.vesionbook.srstask.mapper;

import com.intellif.vesionbook.srstask.model.dto.StreamTaskDto;
import com.intellif.vesionbook.srstask.model.entity.StreamTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface StreamTaskMapper {

    List<StreamTask> selectByParam(StreamTaskDto streamTaskDto);
}
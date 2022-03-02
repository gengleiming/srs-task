package com.intellif.vesionbook.srstask.model.vo.req;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GetOssUrlRspVo {
    private String ossUrl;
    private List<String> ossUrlList;
}

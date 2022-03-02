package com.intellif.vesionbook.srstask.model.vo.req;

import lombok.Data;

import java.util.List;

@Data
public class GetOssUrlReqVo {
    private String objectName;
    private List<String> objectNameList;
}

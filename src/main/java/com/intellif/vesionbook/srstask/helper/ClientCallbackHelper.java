package com.intellif.vesionbook.srstask.helper;

import com.intellif.vesionbook.srstask.enums.ReturnCodeEnum;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import com.intellif.vesionbook.srstask.model.vo.req.RecorderFileClientCallbackVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Component
@Slf4j
public class ClientCallbackHelper {

    @Resource
    RestTemplate restTemplate;

    public BaseResponseVo<String> recorderFileCallbackRequest(String url, RecorderFileClientCallbackVo vo) {
        HttpEntity<RecorderFileClientCallbackVo> request = new HttpEntity<>(vo);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request , String.class );
        if(response.getStatusCode() == HttpStatus.OK) {
            return BaseResponseVo.ok();
        }
        log.error("callback error. url: {}, vo: {}, status code: {}, response: {}",
                url, vo, response.getStatusCode(), response.getBody());
        return BaseResponseVo.error(ReturnCodeEnum.ERROR_VIDEO_RECORDER_CALLBACK_REQUEST_ERROR);

    }


}

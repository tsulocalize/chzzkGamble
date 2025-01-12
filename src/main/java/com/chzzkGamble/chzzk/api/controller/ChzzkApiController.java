package com.chzzkGamble.chzzk.api.controller;

import com.chzzkGamble.chzzk.api.ChzzkApiService;
import com.chzzkGamble.chzzk.dto.VideoSettingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChzzkApiController {

    private final ChzzkApiService chzzkApiService;

    @GetMapping("/video-setting")
    public ResponseEntity<VideoSettingResponse> getVideoSetting(@RequestParam("channelId") String channelId) {
        VideoSettingResponse videoSetting = chzzkApiService.getVideoSetting(channelId);

        return ResponseEntity.ok(videoSetting);
    }
}

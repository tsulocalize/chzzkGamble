package com.chzzkGamble.videodonation.controller;

import com.chzzkGamble.videodonation.domain.VideoDonation;
import com.chzzkGamble.videodonation.dto.Criteria;
import com.chzzkGamble.videodonation.dto.VideoDonationRankingResponses;
import com.chzzkGamble.videodonation.dto.VideoDonationResponse;
import com.chzzkGamble.videodonation.dto.VideoDonationResponses;
import com.chzzkGamble.videodonation.service.VideoDonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/video-donations")
@RequiredArgsConstructor
public class VideoDonationController {

    private final VideoDonationService videoDonationService;

    @GetMapping
    public ResponseEntity<List<VideoDonationResponse>> readVideoDonations(@RequestParam String channelName) {
        List<VideoDonation> recentlyVideoDonation = videoDonationService.getRecentlyVideoDonation(channelName);

        return ResponseEntity.ok(recentlyVideoDonation.stream()
                .map(VideoDonationResponse::from)
                .toList());
    }

    @GetMapping("/v1")
    public ResponseEntity<VideoDonationResponses> readVideoDonationsV1(@RequestParam String channelName) {
        List<VideoDonation> recentlyVideoDonation = videoDonationService.getRecentlyVideoDonation(channelName);

        return ResponseEntity.ok(VideoDonationResponses.from(recentlyVideoDonation));
    }

    @GetMapping("/ranking")
    public ResponseEntity<?> getRankByCriteria(@RequestParam Criteria criteria) {
        VideoDonationRankingResponses rankings = videoDonationService.getRankingByCriteria(criteria);

        return ResponseEntity.ok(rankings);
    }
}

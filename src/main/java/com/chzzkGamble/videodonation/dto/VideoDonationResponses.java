package com.chzzkGamble.videodonation.dto;

import com.chzzkGamble.videodonation.domain.VideoDonation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class VideoDonationResponses {
    private List<VideoDonationResponse> general;
    private List<VideoDonationResponse> highlighter;

    public static VideoDonationResponses from(List<VideoDonation> videoDonations) {
        List<VideoDonationResponse> general = videoDonations.stream()
                .filter(videoDonation -> !videoDonation.isHighlighter())
                .map(VideoDonationResponse::from)
                .toList();
        List<VideoDonationResponse> highlighter = videoDonations.stream()
                .filter(VideoDonation::isHighlighter)
                .map(VideoDonationResponse::from)
                .toList();

        return new VideoDonationResponses(general, highlighter);
    }
}

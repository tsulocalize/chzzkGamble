package com.chzzkGamble.chzzk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VideoSettingResponse {
    private final boolean donationActive;
    private final int payAmountPerSecond;
    private final int minCurrencyPayAmount;
    private final int maxDurationLength;
    private final boolean isYoutubeVideoAllow;
    private final boolean isChzzkClipAllow;
    private final boolean isAllowForSubscriber;
}

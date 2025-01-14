package com.chzzkGamble.videodonation.service;

import com.chzzkGamble.chzzk.dto.DonationMessage;
import com.chzzkGamble.event.DonationEvent;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoDonationHandler {

    private final VideoDonationService videoDonationService;

    @Async
    @EventListener(DonationEvent.class)
    public CompletableFuture<Boolean> handleVideoDonation(DonationEvent donationEvent) {
        DonationMessage donationMessage = (DonationMessage) donationEvent.getSource();
        if (!donationMessage.isVideoDonation()) {
            return CompletableFuture.completedFuture(false);
        }
        videoDonationService.save(donationMessage.getChannelName(), donationMessage.getCheese(),
                donationMessage.getMsg(), donationMessage.isHighlighter());
        return CompletableFuture.completedFuture(true);
    }
}

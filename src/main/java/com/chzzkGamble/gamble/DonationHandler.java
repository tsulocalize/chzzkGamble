package com.chzzkGamble.gamble;

import com.chzzkGamble.chzzk.dto.DonationMessage;
import com.chzzkGamble.event.DonationEvent;
import com.chzzkGamble.gamble.roulette.service.RouletteService;
import com.chzzkGamble.utils.StringParser;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DonationHandler {

    private static final char LEFT_DELIMITER = '<';
    private static final char RIGHT_DELIMITER = '>';

    private final RouletteService rouletteService;

    @Async
    @EventListener(DonationEvent.class)
    public CompletableFuture<Boolean> voteGamble(DonationEvent donationEvent) {
        DonationMessage donationMessage = (DonationMessage) donationEvent.getSource();
        String elementName = getElementName(donationMessage.getMsg());
        if (elementName == null) {
            return CompletableFuture.completedFuture(false); //투표용 도네가 아니다.
        }
        rouletteService.vote(donationMessage.getChannelName(), elementName, donationMessage.getCheese());
        return CompletableFuture.completedFuture(true);
    }

    private String getElementName(String msg) {
        try {
            return StringParser.parseFromTo(msg, LEFT_DELIMITER, RIGHT_DELIMITER);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

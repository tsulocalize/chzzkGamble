package com.chzzkGamble.support;

import com.chzzkGamble.chzzk.dto.DonationMessage;
import com.chzzkGamble.event.DonationEvent;
import org.springframework.web.socket.TextMessage;

public class StubDonationEvent extends DonationEvent {

    private StubDonationEvent(String channelName, String msg, int cheese, String type) {
        super(new DonationMessage(channelName, new TextMessage(
                "{\n" +
                        "  \"bdy\": [\n" +
                        "    {\n" +
                        "      \"msg\": \"" + msg + "\",\n" +
                        "      \"extras\": \"payAmount:" + cheese + ",donationType:" + type + "\",\n" +
                        "      \"profile\": null\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"
        )));
    }

    public static StubDonationEvent ofChat(String channelName, String msg, int cheese){
        return new StubDonationEvent(channelName,msg,cheese,"CHAT");
    }

    public static StubDonationEvent ofVideo(String channelName, String msg, int cheese){
        return new StubDonationEvent(channelName,msg,cheese,"VIDEO");
    }
}

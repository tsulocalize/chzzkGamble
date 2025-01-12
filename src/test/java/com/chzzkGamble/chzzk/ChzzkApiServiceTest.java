package com.chzzkGamble.chzzk;

import com.chzzkGamble.chzzk.api.ChzzkApiService;
import com.chzzkGamble.exception.ChzzkException;
import com.chzzkGamble.exception.ChzzkExceptionCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChzzkApiServiceTest {

    private final String DdahyoniChannelId = "0dad8baf12a436f722faa8e5001c5011";

    @Autowired
    ChzzkApiService chzzkApiService;

    @Test
    @DisplayName("채널 정보를 가져올 수 있다.")
    void getChannelInfo() {
        String channelName = chzzkApiService.getChannelInfo("따효니").getChannelId();
        assertThat(channelName).isEqualTo(DdahyoniChannelId);
    }

    @Test
    @DisplayName("채널 정보를 가져올 수 없다: 이름을 잘못된 경우")
    void getChannelInfo_InvalidChannelId() {
        assertThatThrownBy(() -> chzzkApiService.getChannelInfo("ANDOSNO239r9777"))
                .isInstanceOf(ChzzkException.class)
                .hasMessage(ChzzkExceptionCode.CHANNEL_INFO_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("채팅 정보를 가져올 수 있다.")
    void getChatInfo() {
        assertThatCode(() -> chzzkApiService.getChatInfo("따효니"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("채팅 정보를 가져올 수 없다 : 이름이 잘못된 경우")
    void getChatInfo_InvalidChannelId_Exception() {
        assertThatThrownBy(() -> chzzkApiService.getChatInfo("f13i0hfe0ind777"))
                .isInstanceOf(ChzzkException.class)
                .hasMessage(ChzzkExceptionCode.CHANNEL_INFO_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("영상 도네이션 세팅 정보를 가져올 수 있다.")
    void getVideoSetting() {
        // when
        assertThatCode(() -> chzzkApiService.getVideoSetting(DdahyoniChannelId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("영상 도네이션 세팅 정보를 가져올 수 없다 : 채널 id가 잘못된 경우")
    void getVideoSetting_InvalidChannelId_Exception() {
        // when
        assertThatThrownBy(() -> chzzkApiService.getVideoSetting("1"))
                .isInstanceOf(ChzzkException.class)
                .hasMessage(ChzzkExceptionCode.CHANNEL_ID_INVALID.getMessage());
    }
}

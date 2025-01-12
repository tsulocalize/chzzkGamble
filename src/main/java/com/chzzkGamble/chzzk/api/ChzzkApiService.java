package com.chzzkGamble.chzzk.api;

import com.chzzkGamble.chzzk.dto.ChannelInfoApiResponse;
import com.chzzkGamble.chzzk.dto.ChatInfoApiResponse;
import com.chzzkGamble.chzzk.dto.VideoSettingResponse;
import com.chzzkGamble.exception.ChzzkException;
import com.chzzkGamble.exception.ChzzkExceptionCode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

@Service
@RequiredArgsConstructor
public class ChzzkApiService {

    private static final Gson gson = new Gson();

    private final ChzzkRestClient restClient;

    public ChatInfoApiResponse getChatInfo(String channelName) {
        ChannelInfoApiResponse response = getChannelInfo(channelName);

        String url = "https://api.chzzk.naver.com/polling/v3/channels/" + response.getChannelId() + "/live-status";
        String jsonString = restClient.get(url);

        JsonObject content = JsonParser.parseString(jsonString)
                .getAsJsonObject()
                .getAsJsonObject("content");

        return gson.fromJson(content, ChatInfoApiResponse.class);
    }

    public ChannelInfoApiResponse getChannelInfo(String channelName) {
        String url = "https://api.chzzk.naver.com/service/v1/search/channels?keyword=" + channelName;
        String jsonString = restClient.get(url);

        JsonArray data = JsonParser.parseString(jsonString)
                .getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("data");

        JsonObject channelInfo = data.asList()
                .stream()
                .map(je -> je.getAsJsonObject().getAsJsonObject("channel"))
                .filter(jsonObject -> {
                    String responseChannelName = jsonObject.getAsJsonPrimitive("channelName").getAsString();
                    return responseChannelName.equals(channelName);
                })
                .findAny()
                .orElseThrow(() -> new ChzzkException(ChzzkExceptionCode.CHANNEL_INFO_NOT_FOUND));

        return gson.fromJson(channelInfo, ChannelInfoApiResponse.class);
    }

    public String getChatAccessToken(String chatChannelId) {
        String url = "https://comm-api.game.naver.com/nng_main/v1/chats/access-token?channelId=" + chatChannelId + "&chatType=STREAMING";
        String jsonString;
        try {
            jsonString = restClient.get(url);
        } catch (HttpServerErrorException.InternalServerError internalServerError) {
            throw new ChzzkException(ChzzkExceptionCode.CHAT_ACCESS_ERROR);
        }

        JsonObject content = JsonParser.parseString(jsonString)
                .getAsJsonObject()
                .getAsJsonObject("content");

        return content.get("accessToken").getAsString();
    }

    public VideoSettingResponse getVideoSetting(String channelId) {
        String url = "https://api.chzzk.naver.com/service/v1/channels/" + channelId + "/donations/video-setting";
        String jsonString;
        try {
            jsonString = restClient.get(url);
            System.out.println("jsonString = " + jsonString);
        } catch (HttpServerErrorException.InternalServerError internalServerError) {
            throw new ChzzkException(ChzzkExceptionCode.CHANNEL_SETTING_FETCH_ERROR);
        }

        JsonObject videoSetting = JsonParser.parseString(jsonString)
                .getAsJsonObject()
                .getAsJsonObject("content");
        if (videoSetting.get("payAmountPerSecond").getAsInt() == 0) {
            throw new ChzzkException(ChzzkExceptionCode.CHANNEL_ID_INVALID);
        }

        return gson.fromJson(videoSetting, VideoSettingResponse.class);
    }
}

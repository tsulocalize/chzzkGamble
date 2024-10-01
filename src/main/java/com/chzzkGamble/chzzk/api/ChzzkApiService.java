package com.chzzkGamble.chzzk.api;

import com.chzzkGamble.chzzk.dto.ChannelInfoApiResponse;
import com.chzzkGamble.chzzk.dto.ChatInfoApiResponse;
import com.chzzkGamble.exception.ChzzkException;
import com.chzzkGamble.exception.ChzzkExceptionCode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

@Service
public class ChzzkApiService {

    private static final Gson gson = new Gson();

    private final ChzzkRestClient restClient;

    public ChzzkApiService(ChzzkRestClient restClient) {
        this.restClient = restClient;
    }

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

        if (data.isEmpty()) {
            throw new ChzzkException(ChzzkExceptionCode.CHANNEL_INFO_NOT_FOUND);
        }

        return gson.fromJson(data.get(0)
                .getAsJsonObject()
                .getAsJsonObject("channel"), ChannelInfoApiResponse.class);
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
}

package com.chzzkGamble.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import com.chzzkGamble.exception.handler.ChzOnMeExceptionCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ChzzkExceptionCode implements ChzOnMeExceptionCode {

    // api 1_xxx
    CHANNEL_ID_INVALID(BAD_REQUEST, 1_001, "유효하지 않은 채널 ID 입니다."),
    STREAM_URI_INVALID(BAD_REQUEST, 1_002, "잘못된 채널 주소 입니다."),
    CHANNEL_INFO_NOT_FOUND(BAD_REQUEST, 1_003, "채널 정보를 찾아오지 못했습니다."),
    CHANNEL_NAME_INVALID(BAD_REQUEST, 1_006, "유효하지 않은 채널명입니다."),
    CHANNEL_LIVE_CLOSED(BAD_REQUEST, 1_007, "현재 방송중이 아닌 채널입니다."),
    CHANNEL_SETTING_FETCH_ERROR(INTERNAL_SERVER_ERROR, 1_008, "영상 도네이션 세팅을 가져오는 데 실패했습니다."),

    // chat 2_xxx
    CHAT_IS_CONNECTED(BAD_REQUEST, 2_001, "채팅방과 이미 연결되어 있습니다."),
    CHAT_IS_DISCONNECTED(BAD_REQUEST, 2_002, "채팅방과 이미 연결이 끊겨있습니다."),
    CHAT_CONNECTION_ERROR(INTERNAL_SERVER_ERROR, 2_003, "채팅방 연결에 실패했습니다"),
    CHAT_DISCONNECTION_ERROR(INTERNAL_SERVER_ERROR, 2_004, "채팅방 연결 종료에 실패했습니다."),
    CHAT_CONNECTION_LIMIT(SERVICE_UNAVAILABLE, 2_005, "채팅방 최대 연결 수를 초과했습니다."),
    CHAT_RECONNECTION_ERROR(INTERNAL_SERVER_ERROR, 2_006, "채팅방 재연결에 실패했습니다."),
    CHAT_IS_CONNECTING(BAD_REQUEST, 2_007, "채팅방에 연결중입니다."),
    CHAT_ACCESS_ERROR(INTERNAL_SERVER_ERROR, 2_008, "채팅방에 연결할 수 없습니다."),
    CHAT_FROM_OTHERS(BAD_REQUEST, 2_009, "다른 인스턴스에 의해 연결된 채팅입니다."),

    // json 3_xxx
    JSON_CONVERT(INTERNAL_SERVER_ERROR, 3_001, "JSON 변환 중 오류가 생겼습니다."),
    JSON_PARSING(INTERNAL_SERVER_ERROR, 3_002, "JSON 파싱 중 오류가 생겼습니다."),
    ;
    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    ChzzkExceptionCode(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}

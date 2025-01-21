package com.chzzkGamble.exception;

import com.chzzkGamble.exception.handler.ChzOnMeExceptionCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GambleExceptionCode implements ChzOnMeExceptionCode {

    // roulette 1_xxx
    ROULETTE_NOT_FOUND(HttpStatus.NOT_FOUND,1_001, "룰렛을 찾을 수 없습니다."),
    CHAT_NOT_CONNECTED(HttpStatus.BAD_REQUEST,1_002, "현재 연결 중이 아닌 채널입니다."),

    // rouletteElement 2_xxx
    ROULETTE_ELEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, 2_001, "룰렛 요소를 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    GambleExceptionCode(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}

package com.chzzkGamble.videodonation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class VideoDonation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String channelName;

    private int cheese;

    private String videoId;

    private String videoName;

    private boolean isHighlighter;

    @CreatedDate
    private LocalDateTime createdAt;

    public VideoDonation(String channelName, int cheese, String videoId, String videoName) {
        this(channelName, cheese, videoId, videoName, false);
    }

    public VideoDonation(String channelName, int cheese, String videoId, String videoName, boolean isHighlighter) {
        this.channelName = channelName;
        this.cheese = cheese;
        this.videoId = videoId;
        this.videoName = videoName;
        this.isHighlighter = isHighlighter;
    }
}

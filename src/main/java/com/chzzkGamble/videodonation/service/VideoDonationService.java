package com.chzzkGamble.videodonation.service;

import com.chzzkGamble.chzzk.chat.domain.Chat;
import com.chzzkGamble.chzzk.chat.repository.ChatRepository;
import com.chzzkGamble.utils.RankAssigner;
import com.chzzkGamble.utils.Ranking;
import com.chzzkGamble.videodonation.domain.VideoDonation;
import com.chzzkGamble.videodonation.dto.Criteria;
import com.chzzkGamble.videodonation.dto.VideoDonationRankingResponse;
import com.chzzkGamble.videodonation.dto.VideoDonationRankingResponses;
import com.chzzkGamble.videodonation.repository.VideoDonationRepository;
import com.chzzkGamble.videodonation.youtube.YoutubeClient;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VideoDonationService {

    private final YoutubeClient youtubeClient;
    private final VideoDonationRepository videoDonationRepository;
    private final ChatRepository chatRepository;
    private final Clock clock;

    @Transactional
    public void save(String channelName, int cheese, String msg, boolean isHighlighter) {
        String videoId = youtubeClient.getVideoIdByTitleOrNull(msg);
        videoDonationRepository.save(new VideoDonation(channelName, cheese, videoId, msg, isHighlighter));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "videoDonation", key = "#channelName", cacheManager = "recentlyCacheManager")
    public List<VideoDonation> getRecentlyVideoDonation(String channelName) {
        Chat chat = chatRepository.findByChannelNameAndOpenedIsTrue(channelName)
                .orElseThrow(() -> new IllegalStateException("최근 연결된 채팅방을 찾을 수 없습니다."));

        LocalDateTime recent = chat.getCreatedAt();

        return videoDonationRepository.findByChannelNameAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(channelName,
                recent);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "videoDonation", key = "#criteria", cacheManager = "rankingCacheManager")
    public VideoDonationRankingResponses getRankingByCriteria(Criteria criteria) {
        LocalDateTime onTime = LocalDateTime.now(clock)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        List<Ranking<VideoDonationRankingResponse>> ranking = getRanking(criteria, onTime);

        return new VideoDonationRankingResponses(ranking, onTime);
    }

    private List<Ranking<VideoDonationRankingResponse>> getRanking(Criteria criteria, LocalDateTime onTime) {
        PageRequest pageable = PageRequest.of(0, 10);
        if (criteria == Criteria.CHEESE) {
            List<VideoDonationRankingResponse> rankingByCheese = videoDonationRepository.findRankingByCheese(onTime.minusWeeks(1), onTime, pageable);
            return RankAssigner.assignRanking(rankingByCheese, VideoDonationRankingResponse::getCheese);
        }
        if (criteria == Criteria.COUNT) {
            List<VideoDonationRankingResponse> rankingByCount = videoDonationRepository.findRankingByCount(onTime.minusWeeks(1), onTime, pageable);
            return RankAssigner.assignRanking(rankingByCount, VideoDonationRankingResponse::getCount);
        }
        if (criteria == Criteria.COMBINED) {
            List<VideoDonationRankingResponse> videoDonations = videoDonationRepository.findVideoDonations(onTime.minusWeeks(1), onTime);
            return getCombinedRanking(videoDonations).subList(0, Math.min(videoDonations.size(), 10));

        }

        throw new IllegalArgumentException();
    }

    private List<Ranking<VideoDonationRankingResponse>> getCombinedRanking(List<VideoDonationRankingResponse> videoDonations) {
        VideoDonationRankingResponses rankingsByCheese = new VideoDonationRankingResponses(
                RankAssigner.assignRanking(videoDonations, VideoDonationRankingResponse::getCheese), null);
        VideoDonationRankingResponses rankingsByCount = new VideoDonationRankingResponses(
                RankAssigner.assignRanking(videoDonations, VideoDonationRankingResponse::getCount), null);

        return RankAssigner.assignRanking(videoDonations, vd -> -(rankingsByCheese.getRank(vd) + rankingsByCount.getRank(vd)));
    }
}

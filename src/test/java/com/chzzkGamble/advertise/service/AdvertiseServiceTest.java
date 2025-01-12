package com.chzzkGamble.advertise.service;

import com.chzzkGamble.advertise.domain.Advertise;
import com.chzzkGamble.advertise.domain.AdvertiseMap;
import com.chzzkGamble.advertise.dto.ApprovalAdvertiseResponse;
import com.chzzkGamble.advertise.dto.NotApprovalAdvertiseResponse;
import com.chzzkGamble.advertise.repository.AdvertiseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql("classpath:advertise.sql")
public class AdvertiseServiceTest {

    private static final Clock after10Days;
    static {
        long epochSecond = Clock.system(ZoneId.of("Asia/Seoul")).instant().getEpochSecond();
        epochSecond += 10 * 24 * 60 * 60L + 100; // 10 days + error bound
        after10Days = Clock.fixed(Instant.ofEpochSecond(epochSecond), ZoneId.of("Asia/Seoul"));
    }

    @Autowired
    AdvertiseRepository advertiseRepository;

    @Autowired
    AdvertiseService advertiseService;

    @SpyBean
    Clock clock;

    @BeforeEach
    void setUp() {
        advertiseService.updateAdvertiseMap();
    }

    @Test
    @DisplayName("광고 목록을 업데이트할 수 있다.")
    void updateAdvertiseMap() {
        // given
        Advertise advertise = new Advertise("따효니", "image1", 1000L, 10);
        advertise.approval(clock);
        advertiseRepository.save(advertise);

        // when
        advertiseService.updateAdvertiseMap();

        // then
        List<ApprovalAdvertiseResponse> approvalAdvertise = advertiseService.getApprovalAdvertise();
        assertThat(approvalAdvertise.stream()
                .anyMatch(approvalAdvertiseResponse -> approvalAdvertiseResponse.name().equals("따효니")))
                .isTrue();
    }

    @Test
    @DisplayName("광고기한이 지난 광고는 업데이트되지 않는다.")
    void updateAdvertiseMap_after10Days() {
        // given
        Advertise advertise = new Advertise("따효니", "image1", 1000L, 10);
        advertise.approval(clock);
        advertiseRepository.save(advertise);
        doReturn(Instant.now(after10Days))
                .when(clock)
                .instant();

        // when
        advertiseService.updateAdvertiseMap();

        // then
        assertThat(advertiseService.getAdvertise().getName()).isEqualTo(AdvertiseMap.DEFAULT_ADVERTISE_NAME);
    }

    @Test
    @DisplayName("일정 주기로 광고 목록이 업데이트 된다.") // 3 seconds in test profile
    void updateAdvertiseMap_scheduled() throws InterruptedException {
        // given
        assertThat(advertiseService.getAdvertiseProbabilities()).isEmpty();

        // when
        Advertise advertise = new Advertise("따효니", "image1", 1000L, 10);
        advertise.approval(clock);
        advertiseRepository.save(advertise);
        Thread.sleep(4 * 1000L);

        // then
        assertThat(advertiseService.getAdvertiseProbabilities()).hasSize(1);
    }

    @Test
    @DisplayName("승인된 광고들만 AdvertiseMap에 저장된다.")
    void approvalAdvertise() {
        // given
        Advertise advertise1 = new Advertise("따효니1", "image1", 1000L, 10);
        advertise1.approval(clock);
        advertiseRepository.save(advertise1);
        Advertise advertise2 = new Advertise("따효니2", "image2", 1000L, 10);
        advertiseRepository.save(advertise2);

        // when
        advertiseService.updateAdvertiseMap();

        // then
        assertThat(advertiseService.getAdvertiseProbabilities()).hasSize(1);
    }

    @Test
    @DisplayName("승인된 광고들만 AdvertiseMap에 저장된다.")
    void getAdvertise() {
        // given
        Advertise advertise1 = new Advertise("따효니1", "image1", 1000L, 10);
        Advertise savedAdvertise1 = advertiseRepository.save(advertise1);
        Advertise advertise2 = new Advertise("따효니2", "image2", 1000L, 10);
        Advertise savedAdvertise2 = advertiseRepository.save(advertise2);
        Advertise advertise3 = new Advertise("따효니2", "image2", 1000L, 10);
        advertiseRepository.save(advertise3);

        advertiseService.updateAdvertiseMap();
        List<ApprovalAdvertiseResponse> approvalAdvertise1 = advertiseService.getApprovalAdvertise();
        List<NotApprovalAdvertiseResponse> notApprovalAdvertise1 = advertiseService.getNotApprovalAdvertise();

        assertThat(approvalAdvertise1).hasSize(0);
        assertThat(notApprovalAdvertise1).hasSize(3);

        advertiseService.approvalAdvertise(savedAdvertise1.getId());
        advertiseService.approvalAdvertise(savedAdvertise2.getId());
        advertiseService.updateAdvertiseMap();
        List<ApprovalAdvertiseResponse> approvalAdvertise2 = advertiseService.getApprovalAdvertise();
        List<NotApprovalAdvertiseResponse> notApprovalAdvertise2 = advertiseService.getNotApprovalAdvertise();

        assertThat(approvalAdvertise2).hasSize(2);
        assertThat(notApprovalAdvertise2).hasSize(1);
    }

    @Test
    @DisplayName("승인 취소된 광고는 비승인광고에 포함되지 않는다.")
    void rejectionAdvertise() {
        // given
        Advertise advertise1 = new Advertise("따효니1", "image1", 1000L, 10);
        Advertise savedAdvertise1 = advertiseRepository.save(advertise1);
        Advertise advertise2 = new Advertise("따효니2", "image2", 1000L, 10);
        Advertise savedAdvertise2 = advertiseRepository.save(advertise2);
        Advertise advertise3 = new Advertise("따효니2", "image2", 1000L, 10);
        advertiseRepository.save(advertise3);

        advertiseService.approvalAdvertise(savedAdvertise1.getId());
        advertiseService.approvalAdvertise(savedAdvertise2.getId());
        advertiseService.updateAdvertiseMap();
        List<ApprovalAdvertiseResponse> approvalAdvertise1 = advertiseService.getApprovalAdvertise();
        List<NotApprovalAdvertiseResponse> notApprovalAdvertise1 = advertiseService.getNotApprovalAdvertise();

        assertThat(approvalAdvertise1).hasSize(2);
        assertThat(notApprovalAdvertise1).hasSize(1);

        advertiseService.rejectionAdvertise(savedAdvertise1.getId());
        advertiseService.updateAdvertiseMap();
        List<ApprovalAdvertiseResponse> approvalAdvertise2 = advertiseService.getApprovalAdvertise();
        List<NotApprovalAdvertiseResponse> notApprovalAdvertise2 = advertiseService.getNotApprovalAdvertise();

        assertThat(approvalAdvertise2).hasSize(1);
        assertThat(notApprovalAdvertise2).hasSize(1);
    }
}

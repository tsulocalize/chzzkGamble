package com.chzzkGamble.gamble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.chzzkGamble.chzzk.chat.service.ChzzkChatService;
import com.chzzkGamble.gamble.roulette.domain.Roulette;
import com.chzzkGamble.gamble.roulette.domain.RouletteElement;
import com.chzzkGamble.gamble.roulette.repository.RouletteElementRepository;
import com.chzzkGamble.gamble.roulette.repository.RouletteRepository;
import com.chzzkGamble.gamble.roulette.service.RouletteService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class RouletteServiceTest {

    private static final String CHANNEL_NAME = "ch_name";
    private static final int ROULETTE_UNIT = 1000;

    private static final Clock after1Day;
    static {
        long epochSecond = Clock.system(ZoneId.of("Asia/Seoul")).instant().getEpochSecond();
        epochSecond += 24 * 60 * 60L - 10000; // 1 day - error bound
        after1Day = Clock.fixed(Instant.ofEpochSecond(epochSecond), ZoneId.of("Asia/Seoul"));
    }

    @Autowired
    RouletteService rouletteService;
    @Autowired
    RouletteElementRepository rouletteElementRepository;
    @SpyBean
    private Clock clock;
    @MockBean
    private ChzzkChatService chzzkChatService;
    @Autowired
    private RouletteRepository rouletteRepository;

    @BeforeEach
    void setUp() {
        when(chzzkChatService.isConnected(any()))
                .thenReturn(true);
    }

    @Test
    @DisplayName("룰렛을 생성할 수 있다.")
    void createRoulette() {
        // given & when & then
        assertThatCode(() -> rouletteService.createRoulette(CHANNEL_NAME, ROULETTE_UNIT))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("룰렛 단위 0 이하로 생성하면 예외가 발생한다.")
    void createRoulette_el0_Exception() {
        // given & when & then
        assertThatThrownBy(() -> rouletteService.createRoulette(CHANNEL_NAME, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("룰렛을 불러올 수 있다.")
    void readRoulette() {
        // given
        Roulette roulette = rouletteService.createRoulette(CHANNEL_NAME, ROULETTE_UNIT);

        // when & then
        assertThatCode(() -> rouletteService.readRoulette(roulette.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 룰렛을 불러올 수 없다.")
    void readRoulette_invalidId_exception() {
        // given & when & then
        assertThatThrownBy(() -> rouletteService.readRoulette(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("룰렛 요소 목록을 불러올 수 있다.")
    void readElements() {
        // given
        Roulette roulette = rouletteService.createRoulette(CHANNEL_NAME, ROULETTE_UNIT);
        RouletteElement element1 = rouletteElementRepository.save(new RouletteElement("요소1", 0, roulette));
        RouletteElement element2 = rouletteElementRepository.save(new RouletteElement("요소2", 0, roulette));

        // when & then
        assertThat(rouletteService.readRouletteElements(roulette.getId()))
                .containsExactlyInAnyOrder(element1, element2);
    }

    @Test
    @DisplayName("룰렛 요소에 투표할 수 있다.")
    void vote() {
        // given
        Roulette roulette = rouletteService.createRoulette(CHANNEL_NAME, ROULETTE_UNIT);
        RouletteElement element = rouletteElementRepository.save(new RouletteElement("요소", 0, roulette));
        // when
        rouletteService.startVote(roulette.getId());
        rouletteService.vote(CHANNEL_NAME, "요소", 3000);

        // then
        RouletteElement votedElement = rouletteElementRepository.findById(element.getId()).orElseThrow();
        assertThat(votedElement.getCheese()).isEqualTo(3000);
    }

    @Test
    @DisplayName("룰렛에 없는 요소에 투표할 수 있다.")
    void vote_notInRoulette() {
        // given
        Roulette roulette = rouletteService.createRoulette(CHANNEL_NAME, ROULETTE_UNIT);
        // when
        rouletteService.startVote(roulette.getId());
        rouletteService.vote(CHANNEL_NAME, "요소", 3000);

        // then
        List<RouletteElement> votedElements = rouletteElementRepository.findByRouletteId(roulette.getId());
        assertThat(votedElements).hasSize(1);
        assertThat(votedElements.get(0).getName()).isEqualTo("요소");
    }

    @Test
    @DisplayName("이미 존재하는 요소에 투표하면 count가 증가한다.")
    void vote_alreadyExists() {
        // given
        Roulette roulette = rouletteService.createRoulette(CHANNEL_NAME, ROULETTE_UNIT);
        // when
        rouletteService.startVote(roulette.getId());
        rouletteService.vote(CHANNEL_NAME, "요소", 3_000);
        rouletteService.vote(CHANNEL_NAME, "요소", 4_000);

        // then
        List<RouletteElement> votedElements = rouletteElementRepository.findByRouletteId(roulette.getId());

        assertThat(votedElements).hasSize(1);
        assertThat(votedElements.get(0).getCheese()).isEqualTo(7000);
    }

    @Test
    @DisplayName("룰렛 요소에 음수 개수만큼 투표할 수 없다.")
    void vote_minusVote_Exception() {
        // given
        Roulette roulette = rouletteService.createRoulette(CHANNEL_NAME, ROULETTE_UNIT);
        rouletteElementRepository.save(new RouletteElement("요소", 0, roulette));
        // when & then
        rouletteService.startVote(roulette.getId());
        assertThatThrownBy(() -> rouletteService.vote(CHANNEL_NAME, "요소", -1_000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("룰렛 요소 투표에 동시성 제어가 되어있다.")
    void vote_concurrencyControl() throws InterruptedException {
        // given
        Roulette roulette = rouletteService.createRoulette(CHANNEL_NAME, ROULETTE_UNIT);
        rouletteService.startVote(roulette.getId());
        RouletteElement element = rouletteElementRepository.save(new RouletteElement("요소", 0, roulette));

        // when
        int threadsCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadsCount);
        for (int i = 0; i < threadsCount; i++) {
            executorService.submit(() ->
                    rouletteService.vote(CHANNEL_NAME, "요소", 1_000)
            );
        }
        executorService.shutdown();
        Thread.sleep(1000L);

        // then
        RouletteElement votedElement = rouletteElementRepository.findById(element.getId()).orElseThrow();
        assertThat(votedElement.getCheese()).isEqualTo(1_000 * threadsCount);
    }

    @Test
    @DisplayName("해당 채널에 투표중인 룰렛이 있는지 확인할 수 있다.")
    void hasVotingRoulette() {
        // given
        Roulette roulette = rouletteService.createRoulette(CHANNEL_NAME, ROULETTE_UNIT);

        // when & then
        assertThat(rouletteService.hasVotingRoulette(CHANNEL_NAME)).isFalse();
        rouletteService.startVote(roulette.getId());
        assertThat(rouletteService.hasVotingRoulette(CHANNEL_NAME)).isTrue();
    }

    @Test
    @DisplayName("룰렛 단위를 업데이트할 수 있다.")
    void updateRouletteUnit() {
        // given
        Roulette roulette = rouletteService.createRoulette(CHANNEL_NAME, ROULETTE_UNIT);

        // when
        rouletteService.updateRouletteUnit(roulette.getId(), 2000);

        //then
        Roulette actual = rouletteService.readRoulette(roulette.getId());
        assertThat(actual.getRouletteUnit()).isEqualTo(2000);
    }

    @Test
    @DisplayName("업데이트되는 룰렛 단위가 0이하면 예외가 발생한다.")
    void updateRouletteUnit_el0_Exception() {
        // given
        Roulette roulette = rouletteService.createRoulette(CHANNEL_NAME, ROULETTE_UNIT);

        // when & then
        assertThatThrownBy(() -> rouletteService.updateRouletteUnit(roulette.getId(), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("룰렛 투표를 멈출 수 있다.")
    void endVote(){
        // given
        Roulette roulette = rouletteService.createRoulette(CHANNEL_NAME, ROULETTE_UNIT);
        roulette.startVote();

        // when
        Roulette actual = rouletteService.endVote(roulette.getId());

        // then
        assertThat(actual.isVoting()).isFalse();
    }

    @Test
    @DisplayName("생성한 지 24시간이 지난 룰렛은 투표중일 수 없다")
    void endExpiredRouletteVoting() throws InterruptedException {
        // given
        Roulette roulette = new Roulette("ch_name", 1000);
        roulette.startVote();
        rouletteRepository.save(roulette);

        doReturn(Instant.now(after1Day))
                .when(clock)
                .instant();

        // when
        Thread.sleep(2000L);
        
        // then
        assertThat(rouletteService.hasVotingRoulette("ch_name"))
                .isFalse();
    }
}

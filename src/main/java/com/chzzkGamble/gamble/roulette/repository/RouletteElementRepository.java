package com.chzzkGamble.gamble.roulette.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.chzzkGamble.gamble.roulette.domain.RouletteElement;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface RouletteElementRepository extends JpaRepository<RouletteElement, Long> {

    List<RouletteElement> findByRouletteId(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RouletteElement> findByNameAndRouletteId(String name, UUID rouletteId);
}

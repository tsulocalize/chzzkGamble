package com.chzzkGamble.gamble.roulette.repository;

import com.chzzkGamble.gamble.roulette.domain.Roulette;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RouletteRepository extends JpaRepository<Roulette, UUID> {

    List<Roulette> findByChannelNameAndCreatedAtIsAfter(String channelName, LocalDateTime dateTime);

    Optional<Roulette> findByIdAndCreatedAtAfter(UUID id, LocalDateTime dateTime);

    List<Roulette> findByVotingIsTrueAndCreatedAtAfter(LocalDateTime dateTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r "
            + "FROM Roulette AS r "
            + "WHERE r.id = :id AND r.createdAt > :dateTime ")
    Optional<Roulette> findByIdAndCreatedAtAfterWithLock(@Param("id") UUID id, @Param("dateTime")LocalDateTime dateTime);

    List<Roulette> findByChannelNameAndVotingIsTrue(String channelName);

    boolean existsByChannelNameAndVotingIsTrue(String channelName);
}

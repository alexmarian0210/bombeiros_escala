package com.example.bombeiros.repository;

import com.example.bombeiros.model.Participacao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ParticipacaoRepository
        extends JpaRepository<Participacao, Integer> {

    @Query("""
        SELECT p
        FROM Participacao p
        WHERE p.bombeiro.id = :bombeiroId
          AND p.escalaPlantao.id = :escalaId
          AND p.statusReserva = 'CONFIRMADA'
    """)
    Optional<Participacao> findReservaAtiva(
            @Param("bombeiroId") Integer bombeiroId,
            @Param("escalaId") Integer escalaId);

    @EntityGraph(attributePaths = {
            "escalaPlantao",
            "bombeiro"
    })
    @Query("""
        SELECT p
        FROM Participacao p
        WHERE p.bombeiro.id = :bombeiroId
          AND p.statusReserva = 'CONFIRMADA'
        ORDER BY p.escalaPlantao.data ASC
    """)
    List<Participacao> findReservasAtivasDoBombeiro(
            @Param("bombeiroId") Integer bombeiroId);

    @EntityGraph(attributePaths = {
            "bombeiro"
    })
    @Query("""
        SELECT p
        FROM Participacao p
        WHERE p.escalaPlantao.id = :escalaId
          AND p.statusReserva = 'CONFIRMADA'
    """)
    List<Participacao> findParticipacoesConfirmadas(
            @Param("escalaId") Integer escalaId);

    // === Novos métodos para validação das regras de negócio ===

    @EntityGraph(attributePaths = {"bombeiro"})
    @Query("""
        SELECT p
        FROM Participacao p
        WHERE p.escalaPlantao.id = :escalaId
    """)
    List<Participacao> findByEscalaId(@Param("escalaId") Integer escalaId);

    @Query("""
        SELECT COUNT(p) > 0
        FROM Participacao p
        WHERE p.bombeiro.id = :bombeiroId
          AND p.escalaPlantao.data = :data
          AND (p.statusReserva IS NULL OR UPPER(p.statusReserva) <> UPPER(:statusIgnorado))
    """)
    boolean existsByBombeiroIdAndEscalaDataAndStatusReservaNot(
            @Param("bombeiroId") Integer bombeiroId,
            @Param("data") LocalDate data,
            @Param("statusIgnorado") String statusIgnorado);

    @Query("""
        SELECT COUNT(p) > 0
        FROM Participacao p
        WHERE p.bombeiro.id = :bombeiroId
          AND p.escalaPlantao.data = :data
          AND UPPER(p.escalaPlantao.turno) = UPPER(:turno)
          AND (p.statusReserva IS NULL OR UPPER(p.statusReserva) <> UPPER(:statusIgnorado))
    """)
    boolean existsByBombeiroIdAndEscalaDataAndEscalaTurnoAndStatusReservaNot(
            @Param("bombeiroId") Integer bombeiroId,
            @Param("data") LocalDate data,
            @Param("turno") String turno,
            @Param("statusIgnorado") String statusIgnorado);
}

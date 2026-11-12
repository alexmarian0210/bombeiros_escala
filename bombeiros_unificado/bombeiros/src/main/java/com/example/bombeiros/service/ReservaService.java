
package com.example.bombeiros.service;

import com.example.bombeiros.model.Bombeiro;
import com.example.bombeiros.model.EscalaPlantao;
import com.example.bombeiros.model.Participacao;
import com.example.bombeiros.repository.BombeiroRepository;
import com.example.bombeiros.repository.EscalaPlantaoRepository;
import com.example.bombeiros.repository.ParticipacaoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ReservaService {

    private static final ZoneId FUSO =
            ZoneId.of("America/Sao_Paulo");

    private static final int LIMITE_CHEFES = 1;
    private static final int LIMITE_VOLUNTARIOS = 3;

    @Autowired
    private ParticipacaoRepository participacaoRepository;

    @Autowired
    private EscalaPlantaoRepository escalaRepository;

    @Autowired
    private BombeiroRepository bombeiroRepository;

    private LocalDateTime agora() {
        return LocalDateTime.now(FUSO);
    }

    private List<Participacao> participacoesConfirmadas(
            EscalaPlantao escala) {

        if (escala.getParticipacoes() == null) {
            return List.of();
        }

        return escala.getParticipacoes().stream()
                .filter(p -> "CONFIRMADA".equalsIgnoreCase(
                        p.getStatusReserva()))
                .toList();
    }

    public long contarChefes(EscalaPlantao escala) {
        return participacoesConfirmadas(escala).stream()
                .filter(p -> p.getBombeiro() != null)
                .filter(p -> "CHEFE_EQUIPE".equalsIgnoreCase(
                        p.getBombeiro().getPerfil()))
                .count();
    }

    public long contarVoluntarios(EscalaPlantao escala) {
        return participacoesConfirmadas(escala).stream()
                .filter(p -> p.getBombeiro() != null)
                .filter(p -> "VOLUNTARIO".equalsIgnoreCase(
                        p.getBombeiro().getTipo()))
                .filter(p -> !"CHEFE_EQUIPE".equalsIgnoreCase(
                        p.getBombeiro().getPerfil()))
                .count();
    }

    public int calcularVagasChefe(EscalaPlantao escala) {
        return Math.max(0,
                LIMITE_CHEFES - (int) contarChefes(escala));
    }

    public int calcularVagasVoluntarios(EscalaPlantao escala) {
        return Math.max(0,
                LIMITE_VOLUNTARIOS - (int) contarVoluntarios(escala));
    }

    public int calcularVagasDisponiveis(EscalaPlantao escala) {
        return calcularVagasChefe(escala)
                + calcularVagasVoluntarios(escala);
    }

    public boolean estaCompleto(EscalaPlantao escala) {
        return contarChefes(escala) >= LIMITE_CHEFES
                && contarVoluntarios(escala) >= LIMITE_VOLUNTARIOS;
    }

    public String obterStatusComposicao(EscalaPlantao escala) {
        return estaCompleto(escala) ? "COMPLETO" : "INCOMPLETO";
    }

    public boolean possuiVagaParaBombeiro(
            EscalaPlantao escala, Bombeiro bombeiro) {

        if ("CHEFE_EQUIPE".equalsIgnoreCase(bombeiro.getPerfil())) {
            return calcularVagasChefe(escala) > 0;
        }

        if ("VOLUNTARIO".equalsIgnoreCase(bombeiro.getTipo())) {
            return calcularVagasVoluntarios(escala) > 0;
        }

        return false;
    }

    public LocalDateTime obterDataHoraInicioPlantao(
            EscalaPlantao escala) {

        if (escala.getData() == null || escala.getTurno() == null) {
            throw new IllegalArgumentException(
                    "O plantão precisa ter data e turno cadastrados.");
        }

        LocalTime hora = switch (
                escala.getTurno().trim().toUpperCase()) {

            case "MANHA", "MANHÃ" -> LocalTime.of(7, 0);
            case "TARDE" -> LocalTime.of(13, 0);
            case "NOITE" -> LocalTime.of(19, 0);
            default -> throw new IllegalArgumentException(
                    "Turno inválido: " + escala.getTurno());
        };

        return LocalDateTime.of(escala.getData(), hora);
    }

    @Transactional
    public void realizarReserva(
            Integer escalaId,
            Integer bombeiroId,
            String observacoes) throws Exception {

        EscalaPlantao escala = escalaRepository.findById(escalaId)
                .orElseThrow(() ->
                        new Exception("Plantão não encontrado."));

        Bombeiro bombeiro = bombeiroRepository.findById(bombeiroId)
                .orElseThrow(() ->
                        new Exception("Bombeiro não encontrado."));

        if (!agora().isBefore(obterDataHoraInicioPlantao(escala))) {
            throw new Exception(
                    "Não é possível reservar um plantão que já começou.");
        }

        if (participacaoRepository
                .findReservaAtiva(bombeiroId, escalaId).isPresent()) {
            throw new Exception(
                    "Você já possui uma reserva confirmada neste plantão.");
        }

        if (!possuiVagaParaBombeiro(escala, bombeiro)) {
            if ("CHEFE_EQUIPE".equalsIgnoreCase(bombeiro.getPerfil())) {
                throw new Exception(
                        "A vaga de chefe de equipe já está preenchida.");
            }

            if ("VOLUNTARIO".equalsIgnoreCase(bombeiro.getTipo())) {
                throw new Exception(
                        "As três vagas de voluntários já estão preenchidas.");
            }

            throw new Exception(
                    "Seu cadastro não corresponde a uma vaga deste plantão. É necessário ser chefe de equipe ou voluntário.");
        }

        Participacao participacao = new Participacao();

        participacao.setEscalaPlantao(escala);
        participacao.setBombeiro(bombeiro);
        participacao.setObservacoes(observacoes);
        participacao.setStatusReserva("CONFIRMADA");
        participacao.setStatusPresenca(true);
        participacao.setDataHoraReserva(agora());
        participacao.setDataHoraCancelamento(null);

        participacaoRepository.save(participacao);
    }

    @Transactional
    public void cancelarReserva(
            Integer participacaoId,
            Integer bombeiroId) throws Exception {

        Participacao participacao =
                participacaoRepository.findById(participacaoId)
                        .orElseThrow(() ->
                                new Exception("Reserva não localizada."));

        if (!participacao.getBombeiro().getId().equals(bombeiroId)) {
            throw new Exception(
                    "Você não pode cancelar a reserva de outro bombeiro.");
        }

        if (!"CONFIRMADA".equalsIgnoreCase(
                participacao.getStatusReserva())) {
            throw new Exception("Esta reserva já foi cancelada.");
        }

        LocalDateTime inicioPlantao =
                obterDataHoraInicioPlantao(
                        participacao.getEscalaPlantao());

        if (!agora().isBefore(inicioPlantao.minusHours(24))) {
            throw new Exception(
                    "O cancelamento só pode ser feito com pelo menos 24 horas de antecedência.");
        }

        participacao.setStatusReserva("CANCELADA");
        participacao.setStatusPresenca(false);
        participacao.setDataHoraCancelamento(agora());

        participacaoRepository.save(participacao);
    }
}

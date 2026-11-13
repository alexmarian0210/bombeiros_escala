package com.example.bombeiros.service;

import com.example.bombeiros.model.Bombeiro;
import com.example.bombeiros.model.EscalaPlantao;
import com.example.bombeiros.model.Participacao;
import com.example.bombeiros.repository.ParticipacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EscalaValidacaoService {

    @Autowired
    private ParticipacaoRepository participacaoRepository;

    public void validarEntradaBombeiro(Bombeiro bombeiro, EscalaPlantao escalaAlvo) {
        List<Participacao> ativasNoPlantao = buscarParticipacoesAtivas(escalaAlvo.getId());

        boolean jaEscalado = ativasNoPlantao.stream()
                .anyMatch(p -> p.getBombeiro().getId().equals(bombeiro.getId()));
        if (jaEscalado) {
            throw new IllegalArgumentException("Este bombeiro já está escalado neste plantão.");
        }

        if ("ESTAGIARIO".equalsIgnoreCase(bombeiro.getTipo())) {
            long qtdEstagiarios = ativasNoPlantao.stream()
                    .filter(p -> "ESTAGIARIO".equalsIgnoreCase(p.getBombeiro().getTipo()))
                    .count();
            if (qtdEstagiarios >= 2) {
                throw new IllegalArgumentException("Limite atingido: máximo de 2 estagiários por plantão.");
            }
        }

        if ("CHEFE_EQUIPE".equalsIgnoreCase(bombeiro.getPerfil())) {
            long qtdChefes = ativasNoPlantao.stream()
                    .filter(p -> "CHEFE_EQUIPE".equalsIgnoreCase(p.getBombeiro().getPerfil()))
                    .count();
            if (qtdChefes >= 1) {
                throw new IllegalArgumentException("Já existe 1 Chefe de Equipe escalado neste plantão.");
            }
        }

        if ("CLT".equalsIgnoreCase(bombeiro.getTipo())) {
            validarEscalaClt12x36(bombeiro, escalaAlvo);
        }

        validarPlantaoSeguido(bombeiro, escalaAlvo);
    }

    public void validarFechamentoPlantao(Integer escalaId) {
        List<Participacao> ativas = buscarParticipacoesAtivas(escalaId);
        List<Bombeiro> equipe = ativas.stream()
                .map(Participacao::getBombeiro)
                .collect(Collectors.toList());

        List<String> erros = new ArrayList<>();

        long qtdComCnh = equipe.stream().filter(Bombeiro::possuiCnh).count();
        if (qtdComCnh < 2) {
            erros.add("É obrigatório ter pelo menos 2 bombeiros com CNH (atual: " + qtdComCnh + ").");
        }

        long qtdChefes = equipe.stream()
                .filter(b -> "CHEFE_EQUIPE".equalsIgnoreCase(b.getPerfil()))
                .count();
        if (qtdChefes != 1) {
            erros.add("O plantão deve ter exatamente 1 Chefe de Equipe (atual: " + qtdChefes + ").");
        }

        long qtdVoluntarios = equipe.stream()
                .filter(b -> "VOLUNTARIO".equalsIgnoreCase(b.getTipo()))
                .count();
        if (qtdVoluntarios < 3) {
            erros.add("É obrigatório ter no mínimo 3 Voluntários (atual: " + qtdVoluntarios + ").");
        }

        long qtdEstagiarios = equipe.stream()
                .filter(b -> "ESTAGIARIO".equalsIgnoreCase(b.getTipo()))
                .count();
        if (qtdEstagiarios > 2) {
            erros.add("Permitido no máximo 2 Estagiários por plantão (atual: " + qtdEstagiarios + ").");
        }

        if (!erros.isEmpty()) {
            throw new IllegalStateException("Não é possível fechar o plantão:\n- " + String.join("\n- ", erros));
        }
    }

    private void validarEscalaClt12x36(Bombeiro clt, EscalaPlantao escala) {
        int diaDoMes = escala.getData().getDayOfMonth();
        boolean diaPar = (diaDoMes % 2 == 0);

        if ("PAR".equalsIgnoreCase(clt.getTurma12x36()) && !diaPar) {
            throw new IllegalArgumentException("Bombeiro CLT da turma PAR só pode trabalhar em dias pares (escala 12/36).");
        }
        if ("IMPAR".equalsIgnoreCase(clt.getTurma12x36()) && diaPar) {
            throw new IllegalArgumentException("Bombeiro CLT da turma ÍMPAR só pode trabalhar em dias ímpares (escala 12/36).");
        }

        boolean jaTrabalhaNoDia = participacaoRepository.existsByBombeiroIdAndEscalaDataAndStatusReservaNot(
                clt.getId(), escala.getData(), "CANCELADA");
        if (jaTrabalhaNoDia) {
            throw new IllegalArgumentException("Bombeiro CLT já está escalado em outro turno nesta data (limite 12h).");
        }
    }

    private void validarPlantaoSeguido(Bombeiro bombeiro, EscalaPlantao escalaAlvo) {
        LocalDate data = escalaAlvo.getData();
        String turno = escalaAlvo.getTurno().toUpperCase();

        if ("MANHA".equals(turno) || "MANHÃ".equals(turno)) {
            verificarConflito(bombeiro.getId(), data.minusDays(1), "NOITE", "trabalhou na NOITE anterior");
            verificarConflito(bombeiro.getId(), data, "TARDE", "está escalado na TARDE seguinte");
        } else if ("TARDE".equals(turno)) {
            verificarConflito(bombeiro.getId(), data, "MANHA", "trabalhou na MANHÃ deste dia");
            verificarConflito(bombeiro.getId(), data, "NOITE", "está escalado na NOITE seguinte");
        } else if ("NOITE".equals(turno)) {
            verificarConflito(bombeiro.getId(), data, "TARDE", "trabalhou na TARDE deste dia");
            verificarConflito(bombeiro.getId(), data.plusDays(1), "MANHA", "está escalado na MANHÃ do dia seguinte");
        }
    }

    private void verificarConflito(Integer bombeiroId, LocalDate data, String turno, String motivo) {
        boolean conflito = participacaoRepository
                .existsByBombeiroIdAndEscalaDataAndEscalaTurnoAndStatusReservaNot(
                        bombeiroId, data, turno, "CANCELADA");
        if (conflito) {
            throw new IllegalArgumentException("Escala seguida proibida: o bombeiro já " + motivo + ".");
        }
    }

    private List<Participacao> buscarParticipacoesAtivas(Integer escalaId) {
        return participacaoRepository.findByEscalaId(escalaId).stream()
                .filter(p -> !"CANCELADA".equalsIgnoreCase(p.getStatusReserva()))
                .collect(Collectors.toList());
    }
}


package com.example.bombeiros.service;

import com.example.bombeiros.model.Bombeiro;
import com.example.bombeiros.model.EscalaPlantao;
import com.example.bombeiros.model.Notificacao;
import com.example.bombeiros.repository.BombeiroRepository;
import com.example.bombeiros.repository.EscalaPlantaoRepository;
import com.example.bombeiros.repository.NotificacaoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private BombeiroRepository bombeiroRepository;

    @Autowired
    private EscalaPlantaoRepository escalaRepository;

    // Listar todas as notificações de um bombeiro
    @Transactional(readOnly = true)
    public List<Notificacao> listarNotificacoes(Integer bombeiroId) {
        return notificacaoRepository
                .findByBombeiroIdOrderByDataEnvioDesc(bombeiroId);
    }

    // Listar somente notificações não lidas
    @Transactional(readOnly = true)
    public List<Notificacao> listarNaoLidas(Integer bombeiroId) {
        return notificacaoRepository
                .findByBombeiroIdAndStatusIgnoreCaseOrderByDataEnvioDesc(
                        bombeiroId, "NAO_LIDA");
    }

    // Contar notificações não lidas
    @Transactional(readOnly = true)
    public long contarNaoLidas(Integer bombeiroId) {
        return notificacaoRepository
                .countByBombeiroIdAndStatusIgnoreCase(
                        bombeiroId, "NAO_LIDA");
    }

    // Criar uma notificação para um bombeiro
    @Transactional
    public void criarNotificacao(
            Integer bombeiroId,
            Integer escalaId,
            String tipo,
            String mensagem) {

        Bombeiro bombeiro = bombeiroRepository.findById(bombeiroId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Bombeiro não encontrado."));

        Notificacao notificacao = new Notificacao();

        notificacao.setBombeiro(bombeiro);
        notificacao.setTipo(tipo);
        notificacao.setMensagem(mensagem);
        notificacao.setStatus("NAO_LIDA");

        // O plantão é opcional
        if (escalaId != null) {
            EscalaPlantao escala = escalaRepository.findById(escalaId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Plantão não encontrado."));

            notificacao.setEscalaPlantao(escala);
        }

        notificacaoRepository.save(notificacao);
    }

    // Marcar uma notificação específica como lida
    @Transactional
    public void marcarComoLida(
            Integer notificacaoId,
            Integer bombeiroId) {

        Notificacao notificacao = notificacaoRepository
                .findById(notificacaoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Notificação não encontrada."));

        // Impede que um usuário altere a notificação de outro
        if (!notificacao.getBombeiro().getId().equals(bombeiroId)) {
            throw new IllegalArgumentException(
                    "Você não tem permissão para alterar esta notificação.");
        }

        notificacao.setStatus("LIDA");

        notificacaoRepository.save(notificacao);
    }

    // Marcar todas as notificações como lidas
    @Transactional
    public void marcarTodasComoLidas(Integer bombeiroId) {

        List<Notificacao> notificacoes =
                notificacaoRepository
                        .findByBombeiroIdAndStatusIgnoreCaseOrderByDataEnvioDesc(
                                bombeiroId, "NAO_LIDA");

        for (Notificacao notificacao : notificacoes) {
            notificacao.setStatus("LIDA");
        }

        notificacaoRepository.saveAll(notificacoes);
    }

    // Criar notificações para vários bombeiros
@Transactional
public void notificarVarios(
        List<Bombeiro> bombeiros,
        Integer escalaId,
        String tipo,
        String mensagem) {

    for (Bombeiro bombeiro : bombeiros) {

        criarNotificacao(
                bombeiro.getId(),
                escalaId,
                tipo,
                mensagem
        );
    }
}
}

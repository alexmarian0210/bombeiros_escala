
package com.example.bombeiros.repository;

import com.example.bombeiros.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

    // Buscar todas as notificações de um bombeiro
    List<Notificacao> findByBombeiroIdOrderByDataEnvioDesc(Integer bombeiroId);

    // Buscar notificações não lidas
    List<Notificacao> findByBombeiroIdAndStatusIgnoreCaseOrderByDataEnvioDesc(
            Integer bombeiroId, String status);

    // Contar notificações não lidas
    long countByBombeiroIdAndStatusIgnoreCase(
            Integer bombeiroId, String status);

}

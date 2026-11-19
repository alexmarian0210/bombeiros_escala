
package com.example.bombeiros.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Plantão relacionado à notificação
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escala_id")
    private EscalaPlantao escalaPlantao;

    // Bombeiro que receberá a notificação
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bombeiro_id", nullable = false)
    private Bombeiro bombeiro;

    private String mensagem;

    // Categoria do alerta
    private String tipo;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    // NAO_LIDA ou LIDA
    private String status = "NAO_LIDA";

    @PrePersist
    public void antesDeSalvar() {
        if (dataEnvio == null) {
            dataEnvio = LocalDateTime.now();
        }

        if (status == null) {
            status = "NAO_LIDA";
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EscalaPlantao getEscalaPlantao() {
        return escalaPlantao;
    }

    public void setEscalaPlantao(EscalaPlantao escalaPlantao) {
        this.escalaPlantao = escalaPlantao;
    }

    public Bombeiro getBombeiro() {
        return bombeiro;
    }

    public void setBombeiro(Bombeiro bombeiro) {
        this.bombeiro = bombeiro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

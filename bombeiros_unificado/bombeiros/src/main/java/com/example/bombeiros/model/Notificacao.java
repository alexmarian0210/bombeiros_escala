package com.example.bombeiros.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Alterado para Integer

    @ManyToOne
    @JoinColumn(name = "escala_id")
    private EscalaPlantao escalaPlantao;

    private String mensagem;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    private String status;

    // Getters e Setters
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

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
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
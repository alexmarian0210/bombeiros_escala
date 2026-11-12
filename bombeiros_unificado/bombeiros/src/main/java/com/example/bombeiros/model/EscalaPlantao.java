package com.example.bombeiros.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "escala_plantao")
public class EscalaPlantao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Alterado para Integer

    private LocalDate data;
    private String turno;

    @OneToMany(mappedBy = "escalaPlantao")
    private List<Participacao> participacoes;

    @OneToMany(mappedBy = "escalaPlantao")
    private List<Notificacao> notificacoes;

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public List<Participacao> getParticipacoes() {
        return participacoes;
    }

    public void setParticipacoes(List<Participacao> participacoes) {
        this.participacoes = participacoes;
    }

    public List<Notificacao> getNotificacoes() {
        return notificacoes;
    }

    public void setNotificacoes(List<Notificacao> notificacoes) {
        this.notificacoes = notificacoes;
    }
}
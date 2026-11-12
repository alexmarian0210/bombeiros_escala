
package com.example.bombeiros.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "participacao")
public class Participacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escala_id")
    private EscalaPlantao escalaPlantao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bombeiro_id")
    private Bombeiro bombeiro;

    @Column(name = "status_presenca")
    private Boolean statusPresenca = true;

    private String observacoes;

    @Column(name = "status_reserva")
    private String statusReserva = "CONFIRMADA";

    @Column(name = "data_hora_reserva")
    private LocalDateTime dataHoraReserva;

    @Column(name = "data_hora_cancelamento")
    private LocalDateTime dataHoraCancelamento;

    @PrePersist
    public void antesDeSalvar() {
        if (statusReserva == null) {
            statusReserva = "CONFIRMADA";
        }

        if (statusPresenca == null) {
            statusPresenca = true;
        }

        if (dataHoraReserva == null) {
            dataHoraReserva = LocalDateTime.now();
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

    public Boolean getStatusPresenca() {
        return statusPresenca;
    }

    public void setStatusPresenca(Boolean statusPresenca) {
        this.statusPresenca = statusPresenca;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getStatusReserva() {
        return statusReserva;
    }

    public void setStatusReserva(String statusReserva) {
        this.statusReserva = statusReserva;
    }

    public LocalDateTime getDataHoraReserva() {
        return dataHoraReserva;
    }

    public void setDataHoraReserva(LocalDateTime dataHoraReserva) {
        this.dataHoraReserva = dataHoraReserva;
    }

    public LocalDateTime getDataHoraCancelamento() {
        return dataHoraCancelamento;
    }

    public void setDataHoraCancelamento(LocalDateTime dataHoraCancelamento) {
        this.dataHoraCancelamento = dataHoraCancelamento;
    }
}

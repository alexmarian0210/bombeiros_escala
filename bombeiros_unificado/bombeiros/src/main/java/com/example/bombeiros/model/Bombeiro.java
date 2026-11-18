
package com.example.bombeiros.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "bombeiro")
public class Bombeiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;
    private String tipo;
    private String email;

    @JsonIgnore
    @Column(name = "senha_hash")
    private String senhaHash;

    private String perfil;

    @Column(name = "categoria_cnh")
    private String categoriaCnh;

    @Column(name = "curso_socorrista")
    private Boolean cursoSocorrista = false;

    private String habilidades;

    @Column(name = "turma_12x36")
    private String turma12x36;

    @JsonIgnore
    @OneToMany(mappedBy = "bombeiro")
    private List<Participacao> participacoes;

    // Verifica se possui CNH
    public boolean possuiCnh() {
        return this.categoriaCnh != null
                && !this.categoriaCnh.trim().isEmpty();
    }

    // ID
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // Nome
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // Tipo
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    // E-mail
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Senha
    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    // Perfil
    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    // CNH
    public String getCategoriaCnh() {
        return categoriaCnh;
    }

    public void setCategoriaCnh(String categoriaCnh) {
        this.categoriaCnh = categoriaCnh;
    }

    // Curso de socorrista
    public Boolean getCursoSocorrista() {
        return cursoSocorrista;
    }

    public void setCursoSocorrista(Boolean cursoSocorrista) {
        this.cursoSocorrista = cursoSocorrista;
    }

    // Habilidades
    public String getHabilidades() {
        return habilidades;
    }

    public void setHabilidades(String habilidades) {
        this.habilidades = habilidades;
    }

    // Turma 12x36
    public String getTurma12x36() {
        return turma12x36;
    }

    public void setTurma12x36(String turma12x36) {
        this.turma12x36 = turma12x36;
    }

    // Participações
    public List<Participacao> getParticipacoes() {
        return participacoes;
    }

    public void setParticipacoes(List<Participacao> participacoes) {
        this.participacoes = participacoes;
    }
}

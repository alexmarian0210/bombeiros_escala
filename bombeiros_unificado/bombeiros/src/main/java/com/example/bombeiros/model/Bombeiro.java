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
    private Boolean cursoSocorrista;
    
    private String habilidades;

    @Column(name = "turma_12x36")
    private String turma12x36;

    @JsonIgnore
    @OneToMany(mappedBy = "bombeiro")
    private List<Participacao> participacoes;

    public boolean possuiCnh() {
        return this.categoriaCnh != null && !this.categoriaCnh.trim().isEmpty();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    
    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public String getCategoriaCnh() { return categoriaCnh; }
    public void setCategoriaCnh(String categoriaCnh) { this.categoriaCnh = categoriaCnh; }

    public Boolean getCursoSocorrista() { return cursoSocorrista; }
    public void setCursoSocorrista(Boolean cursoSocorrista) { this.cursoSocorrista = cursoSocorrista; }

    public String getHabilidades() { return habilidades; }
    public void setHabilidades(String habilidades) { this.habilidades = habilidades; }

    public String getTurma12x36() { return turma12x36; }
    public void setTurma12x36(String turma12x36) { this.turma12x36 = turma12x36; }

    public List<Participacao> getParticipacoes() { return participacoes; }
    public void setParticipacoes(List<Participacao> participacoes) { this.participacoes = participacoes; }
}

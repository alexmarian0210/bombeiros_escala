package com.example.bombeiros.model;

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
    
    @Column(name = "senha_hash")
    private String senhaHash;
    
    private String perfil;

    // Novos campos adicionados
    @Column(name = "categoria_cnh")
    private String categoriaCnh;
    
    @Column(name = "curso_socorrista")
    private Boolean cursoSocorrista;
    
    private String habilidades;

    @OneToMany(mappedBy = "bombeiro")
    private List<Participacao> participacoes;

    // Getters e Setters
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

    public List<Participacao> getParticipacoes() { return participacoes; }
    public void setParticipacoes(List<Participacao> participacoes) { this.participacoes = participacoes; }
}
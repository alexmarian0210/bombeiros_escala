package com.example.bombeiros.controller;

import com.example.bombeiros.model.Bombeiro;
import com.example.bombeiros.repository.BombeiroRepository;
import com.example.bombeiros.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/bombeiros")
public class BombeiroAdminController {

    @Autowired
    private BombeiroRepository bombeiroRepository;

    @Autowired
    private PasswordService passwordService;

    @GetMapping
    public String paginaAdminBombeiros() {
        return "admin-bombeiros";
    }

    @GetMapping("/api")
    @ResponseBody
    public List<Bombeiro> listarComFiltros(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String perfil,
            @RequestParam(required = false) String possuiCnh) {

        return bombeiroRepository.findAll().stream()
                .filter(b -> nome == null || nome.isBlank() ||
                        b.getNome().toLowerCase().contains(nome.toLowerCase()) ||
                        b.getEmail().toLowerCase().contains(nome.toLowerCase()))
                .filter(b -> tipo == null || tipo.isBlank() ||
                        tipo.equalsIgnoreCase(b.getTipo()))
                .filter(b -> perfil == null || perfil.isBlank() ||
                        perfil.equalsIgnoreCase(b.getPerfil()))
                .filter(b -> {
                    if (possuiCnh == null || possuiCnh.isBlank()) return true;
                    if ("SIM".equalsIgnoreCase(possuiCnh)) return b.possuiCnh();
                    if ("NAO".equalsIgnoreCase(possuiCnh)) return !b.possuiCnh();
                    return true;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Bombeiro> buscarPorId(@PathVariable Integer id) {
        return bombeiroRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> criar(@RequestBody Bombeiro novo) {
        try {
            novo.setSenhaHash(passwordService.encode("123456"));
            if (!"CLT".equalsIgnoreCase(novo.getTipo())) {
                novo.setTurma12x36(null);
            }
            Bombeiro salvo = bombeiroRepository.save(novo);
            return ResponseEntity.ok(salvo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao cadastrar: verifique se o e-mail já está em uso.");
        }
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> atualizar(@PathVariable Integer id, @RequestBody Bombeiro dados) {
        return bombeiroRepository.findById(id).map(existente -> {
            existente.setNome(dados.getNome());
            existente.setEmail(dados.getEmail());
            existente.setTipo(dados.getTipo());
            existente.setPerfil(dados.getPerfil());
            existente.setCategoriaCnh(dados.getCategoriaCnh() != null && dados.getCategoriaCnh().isBlank() ? null : dados.getCategoriaCnh());
            existente.setHabilidades(dados.getHabilidades());
            existente.setCursoSocorrista(dados.getCursoSocorrista());
            existente.setTurma12x36("CLT".equalsIgnoreCase(dados.getTipo()) ? dados.getTurma12x36() : null);

            bombeiroRepository.save(existente);
            return ResponseEntity.ok(existente);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> excluir(@PathVariable Integer id) {
        if (!bombeiroRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        bombeiroRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

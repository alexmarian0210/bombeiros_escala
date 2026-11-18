
package com.example.bombeiros.controller;

import com.example.bombeiros.model.Bombeiro;
import com.example.bombeiros.repository.BombeiroRepository;
import com.example.bombeiros.service.PasswordService;

import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/bombeiros")
public class AdminBombeiroRestController {

    private final BombeiroRepository repository;
    private final PasswordService passwordService;

    public AdminBombeiroRestController(
            BombeiroRepository repository,
            PasswordService passwordService) {
        this.repository = repository;
        this.passwordService = passwordService;
    }

    public record BombeiroDTO(
            Integer id,
            String nome,
            String tipo,
            String email,
            String senha,
            String perfil,
            String categoriaCnh,
            String habilidades,
            Boolean cursoSocorrista
    ) {}

    private boolean ehAdmin(HttpSession session) {
        Object perfil = session.getAttribute("usuarioLogadoPerfil");

        return perfil != null &&
                "ADMINISTRADOR".equalsIgnoreCase(perfil.toString());
    }

    private ResponseEntity<?> acessoNegado() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "mensagem",
                        "Acesso permitido somente ao administrador."
                ));
    }

    private BombeiroDTO converter(Bombeiro b) {
        return new BombeiroDTO(
                b.getId(),
                b.getNome(),
                b.getTipo(),
                b.getEmail(),
                null,
                b.getPerfil(),
                b.getCategoriaCnh(),
                b.getHabilidades(),
                b.getCursoSocorrista()
        );
    }

    @GetMapping
    public ResponseEntity<?> listar(HttpSession session) {
        if (!ehAdmin(session)) {
            return acessoNegado();
        }

        List<BombeiroDTO> lista = repository.findAll()
                .stream()
                .map(this::converter)
                .toList();

        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(
            @RequestBody BombeiroDTO dados,
            HttpSession session) {

        if (!ehAdmin(session)) {
            return acessoNegado();
        }

        if (dados.nome() == null || dados.nome().isBlank()
                || dados.email() == null || dados.email().isBlank()
                || dados.senha() == null || dados.senha().isBlank()
                || dados.tipo() == null || dados.perfil() == null) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensagem",
                            "Preencha nome, e-mail, senha, tipo e perfil."
                    ));
        }

        String tipo = dados.tipo().trim().toUpperCase();
        String perfil = dados.perfil().trim().toUpperCase();

        if (!List.of("CLT", "VOLUNTARIO", "ESTAGIARIO").contains(tipo)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensagem", "Tipo inválido."));
        }

        if (!List.of("ADMINISTRADOR", "CHEFE_EQUIPE", "BOMBEIRO")
                .contains(perfil)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensagem", "Perfil inválido."));
        }

        if (repository.findByEmail(dados.email().trim()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "mensagem",
                            "Este e-mail já está cadastrado."
                    ));
        }

        if ("ADMINISTRADOR".equals(perfil) &&
                repository.findAll().stream()
                        .anyMatch(b -> "ADMINISTRADOR".equalsIgnoreCase(
                                b.getPerfil()))) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "mensagem",
                            "Já existe um administrador cadastrado."
                    ));
        }

        Bombeiro b = new Bombeiro();

        b.setNome(dados.nome().trim());
        b.setEmail(dados.email().trim());
        b.setTipo(tipo);
        b.setPerfil(perfil);
        b.setSenhaHash(passwordService.encode(dados.senha()));
        b.setCategoriaCnh(dados.categoriaCnh());
        b.setHabilidades(dados.habilidades());

        // Salva se possui curso de socorrista
        b.setCursoSocorrista(
                dados.cursoSocorrista() != null
                        ? dados.cursoSocorrista()
                        : false
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(converter(repository.save(b)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(
            @PathVariable Integer id,
            @RequestBody BombeiroDTO dados,
            HttpSession session) {

        if (!ehAdmin(session)) {
            return acessoNegado();
        }

        Bombeiro b = repository.findById(id).orElse(null);

        if (b == null) {
            return ResponseEntity.notFound().build();
        }

        if (dados.nome() == null || dados.nome().isBlank()
                || dados.email() == null || dados.email().isBlank()
                || dados.tipo() == null || dados.perfil() == null) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensagem",
                            "Preencha nome, e-mail, tipo e perfil."
                    ));
        }

        var outro = repository.findByEmail(dados.email().trim());

        if (outro.isPresent() &&
                !outro.get().getId().equals(id)) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "mensagem",
                            "Este e-mail já pertence a outro bombeiro."
                    ));
        }

        String tipo = dados.tipo().trim().toUpperCase();
        String perfil = dados.perfil().trim().toUpperCase();

        if (!List.of("CLT", "VOLUNTARIO", "ESTAGIARIO").contains(tipo)
                || !List.of("ADMINISTRADOR", "CHEFE_EQUIPE", "BOMBEIRO")
                .contains(perfil)) {

            return ResponseEntity.badRequest()
                    .body(Map.of("mensagem", "Tipo ou perfil inválido."));
        }

        if ("ADMINISTRADOR".equals(perfil) &&
                repository.findAll().stream()
                        .anyMatch(x -> !x.getId().equals(id)
                                && "ADMINISTRADOR".equalsIgnoreCase(
                                        x.getPerfil()))) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "mensagem",
                            "Já existe outro administrador."
                    ));
        }

        b.setNome(dados.nome().trim());
        b.setEmail(dados.email().trim());
        b.setTipo(tipo);
        b.setPerfil(perfil);
        b.setCategoriaCnh(dados.categoriaCnh());
        b.setHabilidades(dados.habilidades());

        // Atualiza o curso de socorrista
        if (dados.cursoSocorrista() != null) {
            b.setCursoSocorrista(dados.cursoSocorrista());
        }

        // Só altera a senha se uma nova senha for informada
        if (dados.senha() != null && !dados.senha().isBlank()) {
            b.setSenhaHash(passwordService.encode(dados.senha()));
        }

        return ResponseEntity.ok(converter(repository.save(b)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
            @PathVariable Integer id,
            HttpSession session) {

        if (!ehAdmin(session)) {
            return acessoNegado();
        }

        Bombeiro b = repository.findById(id).orElse(null);

        if (b == null) {
            return ResponseEntity.notFound().build();
        }

        if ("ADMINISTRADOR".equalsIgnoreCase(b.getPerfil())) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensagem",
                            "O administrador não pode ser excluído por esta tela."
                    ));
        }

        try {
            repository.delete(b);

            return ResponseEntity.ok(
                    Map.of(
                            "mensagem",
                            "Bombeiro excluído com sucesso."
                    )
            );

        } catch (DataIntegrityViolationException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "mensagem",
                            "Não é possível excluir este bombeiro porque existem participações ou registros vinculados a ele."
                    ));
        }
    }
}

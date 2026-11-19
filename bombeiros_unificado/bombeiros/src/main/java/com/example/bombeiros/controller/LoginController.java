package com.example.bombeiros.controller;

import com.example.bombeiros.model.Bombeiro;
import com.example.bombeiros.repository.BombeiroRepository;
import com.example.bombeiros.service.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    private final BombeiroRepository bombeiroRepository;
    private final PasswordService passwordService;

    public LoginController(
            BombeiroRepository bombeiroRepository,
            PasswordService passwordService) {
        this.bombeiroRepository = bombeiroRepository;
        this.passwordService = passwordService;
    }

    // Abre a tela de login na raiz e em /login
    @GetMapping({"/", "/login"})
    public String exibirLogin(HttpSession session) {
        if (session.getAttribute("usuarioLogadoId") != null) {
            return "redirect:/home";
        }
        return "Login";
    }

    // Recebe o formulário POST tanto de "/" quanto de "/login"
    @PostMapping({"/", "/login"})
    public String processarLogin(
            @RequestParam("email") String email,
            @RequestParam("senha") String senha,
            HttpServletRequest request,
            Model model) {

        String emailInformado = email == null ? "" : email.trim();

        if (emailInformado.isBlank() || senha == null || senha.isEmpty()) {
            model.addAttribute("erro", "Informe o e-mail e a senha.");
            model.addAttribute("emailDigitado", emailInformado);
            return "Login";
        }

        Optional<Bombeiro> bombeiroOpt =
                bombeiroRepository.findByEmail(emailInformado);

        if (bombeiroOpt.isPresent()) {
            Bombeiro bombeiro = bombeiroOpt.get();
            String senhaCadastrada = bombeiro.getSenhaHash();

            boolean senhaValida = false;

            if (senhaCadastrada != null) {
                if (senhaCadastrada.startsWith("pbkdf2$")) {
                    senhaValida = passwordService.matches(senha, senhaCadastrada);
                } else if (senhaCadastrada.equals(senha)) {
                    senhaValida = true;
                    bombeiro.setSenhaHash(passwordService.encode(senha));
                    bombeiroRepository.save(bombeiro);
                }
            }

            if (senhaValida) {
                HttpSession sessaoAntiga = request.getSession(false);
                if (sessaoAntiga != null) {
                    sessaoAntiga.invalidate();
                }

                HttpSession novaSessao = request.getSession(true);
                novaSessao.setAttribute("usuarioLogadoId", bombeiro.getId());
                novaSessao.setAttribute("usuarioLogadoNome", bombeiro.getNome());
                novaSessao.setAttribute("usuarioLogadoPerfil", bombeiro.getPerfil());

                return "redirect:/home";
            }
        }

        model.addAttribute("erro", "E-mail ou senha incorretos.");
        model.addAttribute("emailDigitado", emailInformado);
        return "Login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

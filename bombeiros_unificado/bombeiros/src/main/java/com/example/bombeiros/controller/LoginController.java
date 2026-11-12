package com.example.bombeiros.controller;

import com.example.bombeiros.model.Bombeiro;
import com.example.bombeiros.repository.BombeiroRepository;
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

    public LoginController(BombeiroRepository bombeiroRepository) {
        this.bombeiroRepository = bombeiroRepository;
    }

    // Abre a tela de login na raiz do sistema.
    @GetMapping("/")
    public String raiz(HttpSession session) {
        if (session.getAttribute("usuarioLogadoId") != null) {
            return "redirect:/home";
        }
        return "Login";
    }

    @GetMapping("/login")
    public String exibirLogin(HttpSession session) {
        if (session.getAttribute("usuarioLogadoId") != null) {
            return "redirect:/home";
        }
        return "Login";
    }

    // Login com comparação direta da senha armazenada no banco.
    // Não utiliza BCrypt, hash ou Spring Security.
    @PostMapping("/")
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

            // O campo senhaHash representa aqui a coluna senha_hash,
            // mas o conteúdo é texto simples, conforme solicitado.
            String senhaCadastrada = bombeiro.getSenhaHash();

            if (senhaCadastrada != null && senhaCadastrada.equals(senha)) {
                // Cria uma nova sessão após autenticar.
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

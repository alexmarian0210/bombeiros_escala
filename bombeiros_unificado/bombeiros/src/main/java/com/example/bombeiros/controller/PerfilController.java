package com.example.bombeiros.controller;

import com.example.bombeiros.model.Bombeiro;
import com.example.bombeiros.repository.BombeiroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

@Controller
public class PerfilController {

    @Autowired
    private BombeiroRepository bombeiroRepository;

    @GetMapping("/perfil")
    public String exibirPerfil(HttpSession session, Model model) {
        // Vai buscar o ID do utilizador que guardámos na sessão no momento do login
        Integer usuarioId = (Integer) session.getAttribute("usuarioLogadoId");

        // Se não estiver logado, manda para o login
        if (usuarioId == null) {
            return "redirect:/login";
        }

        // Procura os dados completos do bombeiro na base de dados
        Optional<Bombeiro> bombeiroOpt = bombeiroRepository.findById(usuarioId);
        
        if (bombeiroOpt.isPresent()) {
            model.addAttribute("bombeiro", bombeiroOpt.get());
            return "Perfil"; // Aponta para o ficheiro Perfil.html
        } else {
            return "redirect:/login";
        }
    }
}
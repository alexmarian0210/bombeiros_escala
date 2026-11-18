
package com.example.bombeiros.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminBombeiroPageController {

    @GetMapping({"/admin", "/admin/bombeiros"})
    public String abrirTela(HttpSession session) {

        // Recupera o perfil do usuário conectado
        Object perfil = session.getAttribute("usuarioLogadoPerfil");

        // Verifica se é administrador
        if (perfil == null ||
                !"ADMINISTRADOR".equalsIgnoreCase(perfil.toString())) {

            return "redirect:/login";
        }

        // Abre Admin.html
        return "Admin";
    }
}

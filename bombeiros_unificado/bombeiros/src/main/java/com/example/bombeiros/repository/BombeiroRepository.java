package com.example.bombeiros.repository;

import com.example.bombeiros.model.Bombeiro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BombeiroRepository extends JpaRepository<Bombeiro, Integer> {
    
    // Método para buscar o usuário pelo e-mail
    Optional<Bombeiro> findByEmail(String email);
}
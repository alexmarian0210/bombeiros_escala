
package com.example.bombeiros.repository;

import com.example.bombeiros.model.EscalaPlantao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalaPlantaoRepository
        extends JpaRepository<EscalaPlantao, Integer> {

    @Query("""
        SELECT e
        FROM EscalaPlantao e
        ORDER BY
            e.data ASC,
            CASE
                WHEN e.turno = 'MANHA' THEN 1
                WHEN e.turno = 'TARDE' THEN 2
                WHEN e.turno = 'NOITE' THEN 3
                ELSE 4
            END ASC
    """)
    List<EscalaPlantao> buscarTodasOrdenadas();
}

package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseConocimiento {
    private Set<Hecho> hechos = new HashSet<>();
    private Set<Regla> reglas = new HashSet<>();
    private Set<Set<String>> clausulasDisyuntivas = new HashSet<>();

    public void agregarHecho(Hecho hecho) {
        this.hechos.add(hecho);
    }

    public void agregarRegla(Regla regla) {
        this.reglas.add(regla);
    }

    public void agregarClausulasDisyuntivas(Set<Set<String>> clausulas) {
        this.clausulasDisyuntivas.addAll(clausulas);
    }
}

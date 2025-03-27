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
public class BaseConocimiento {
    private Set<Hecho> hechos;
    private Set<Regla> reglas;

    public BaseConocimiento() {
        this.hechos = new HashSet<>();
        this.reglas = new HashSet<>();
    }

    public void agregarHecho(Hecho hecho) {
        this.hechos.add(hecho);
    }
    public void agregarRegla(Regla regla) {
        this.reglas.add(regla);
    }
}

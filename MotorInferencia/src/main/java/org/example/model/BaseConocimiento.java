package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseConocimiento {
    private Set<Hecho> hechos;
    private Set<Regla> reglas;

    public void agregarHecho(Hecho hecho) {
        hechos.add(hecho);
    }
    public void agregarRegla(Regla regla) {
        reglas.add(regla);
    }
}

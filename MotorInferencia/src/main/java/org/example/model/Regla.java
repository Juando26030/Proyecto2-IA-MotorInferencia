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
public class Regla {
    private Set<Hecho> premisas; // Condiciones
    private Hecho conclusion; // Resultado

    @Override
    public String toString() {
        return premisas + " → " + conclusion;
    }
}
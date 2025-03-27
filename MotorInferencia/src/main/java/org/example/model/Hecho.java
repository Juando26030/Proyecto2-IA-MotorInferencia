package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Hecho {
    private String predicado;
    private String sujeto;
    private String objeto; // Puede ser null si es unario

    public Hecho(String predicado, String sujeto) {
        this(predicado, sujeto, null);
    }

    @Override
    public String toString() {
        return objeto == null ? predicado + "(" + sujeto + ")" : predicado + "(" + sujeto + ", " + objeto + ")";
    }
}

package modelo;

// Ação guardada na pilha de histórico (PUSH ou POP de um ingrediente)
public class Acao {

    public enum Tipo {
        PUSH, POP
    }

    private final Tipo tipo;
    private final Ingrediente ingrediente;

    public Acao( Tipo tipo, Ingrediente ingrediente ) {
        this.tipo = tipo;
        this.ingrediente = ingrediente;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Ingrediente getIngrediente() {
        return ingrediente;
    }

    @Override
    public String toString() {
        return tipo + " " + ingrediente.getNome();
    }

}

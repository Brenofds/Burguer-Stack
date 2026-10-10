package estruturas;

// base da Pilha e da Fila
public class No<T> {

    T valor;
    No<T> proximo;

    No( T valor ) {
        this.valor = valor;
        this.proximo = null;
    }

}

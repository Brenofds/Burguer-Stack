package estruturas;

// Usada para a fila de clientes
public class Fila<T> {

    private No<T> inicio; // frente da fila (próximo a sair)
    private No<T> fim;    // último que entrou
    private int tamanho;

    public Fila() {
        inicio = null;
        fim = null;
        tamanho = 0;
    }

    // entra no fim da fila. O(1) por causa do ponteiro fim
    public void enqueue( T valor ) {
        No<T> novo = new No<>( valor );
        if ( isEmpty() ) {
            inicio = novo;
        } else {
            fim.proximo = novo;
        }
        fim = novo;
        tamanho++;
    }

    // sai da frente da fila. O(1)
    public T dequeue() {
        if ( isEmpty() ) {
            throw new IllegalStateException( "Fila vazia" );
        }
        T valor = inicio.valor;
        inicio = inicio.proximo;
        if ( inicio == null ) { // fila ficou vazia: fim também vira null
            fim = null;
        }
        tamanho--;
        return valor;
    }

    // olha quem está na frente, sem remover
    public T peek() {
        if ( isEmpty() ) {
            throw new IllegalStateException( "Fila vazia" );
        }
        return inicio.valor;
    }

    public boolean isEmpty() {
        return inicio == null;
    }

    public int size() {
        return tamanho;
    }

    public void limpar() {
        inicio = null;
        fim = null;
        tamanho = 0;
    }

    // copia os elementos num vetor, da frente (0) até o fim.
    public Object[] toArray() {
        Object[] vetor = new Object[tamanho];
        No<T> atual = inicio;
        for ( int i = 0; i < tamanho; i++ ) {
            vetor[i] = atual.valor;
            atual = atual.proximo;
        }
        return vetor;
    }

}

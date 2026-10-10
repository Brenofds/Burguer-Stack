package estruturas;

// Usada na bandeja e no histórico do desfazer
public class Pilha<T> {

    private No<T> topo; // último elemento colocado
    private int tamanho;

    public Pilha() {
        topo = null;
        tamanho = 0;
    }

    // coloca no topo: o novo nó aponta para o antigo topo. O(1)
    public void push( T valor ) {
        No<T> novo = new No<>( valor );
        novo.proximo = topo;
        topo = novo;
        tamanho++;
    }

    // tira do topo e devolve o valor. O(1)
    public T pop() {
        if ( isEmpty() ) {
            throw new IllegalStateException( "Pilha vazia" );
        }
        T valor = topo.valor;
        topo = topo.proximo;
        tamanho--;
        return valor;
    }

    // só olha o topo, sem remover
    public T peek() {
        if ( isEmpty() ) {
            throw new IllegalStateException( "Pilha vazia" );
        }
        return topo.valor;
    }

    public boolean isEmpty() {
        return topo == null;
    }

    public int size() {
        return tamanho;
    }

    public void limpar() {
        topo = null;
        tamanho = 0;
    }

    // copia os elementos num vetor, da base (0) até o topo.
    public Object[] toArray() {
        Object[] vetor = new Object[tamanho];
        No<T> atual = topo;
        for ( int i = tamanho - 1; i >= 0; i-- ) {
            vetor[i] = atual.valor;
            atual = atual.proximo;
        }
        return vetor;
    }

}

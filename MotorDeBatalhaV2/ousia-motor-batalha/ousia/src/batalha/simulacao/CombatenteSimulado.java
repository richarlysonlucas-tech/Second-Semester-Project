package batalha.simulacao;

import batalha.contratos.Combatente;

/** STUB: personagem falso só para testes. Será substituído pelo módulo real de personagens. */
public class CombatenteSimulado implements Combatente {

    private final String nome;
    private int pv;

    public CombatenteSimulado(String nome, int pv) {
        this.nome = nome;
        this.pv = pv;
    }

    @Override public String getNome() { return nome; }

    @Override public boolean estaVivo() { return pv > 0; }

    public int getPv() { return pv; }

    public void receberDano(int dano) { pv = Math.max(0, pv - dano); }

    public void curar(int valor) { pv += valor; }
}

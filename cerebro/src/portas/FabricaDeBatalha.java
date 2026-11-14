package portas;

import estado.DadosDoJogo;

/**
 * Monta a batalha para um encontro. É aqui que o jogo decide quem é o herói (a partir dos dados salvos)
 * e quem é o inimigo (a partir do encontro). Pode ser escrita como lambda.
 */
public interface FabricaDeBatalha {
    BatalhaEmCurso criar(DadosDoJogo heroi, EncontroDetectado encontro);
}

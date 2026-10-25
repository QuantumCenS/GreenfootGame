/**
 * Estados da máquina de estados dos inimigos (InimigoCosmico e subclasses).
 * Cada subclasse decide o que faz em cada estado em executarComportamento().
 *
 * Conceitos de POO: enum (conjunto fechado de constantes com tipo próprio, mais seguro do que inteiros).
 */
public enum EstadoInimigo
{
    /** Percorre a rota pré-definida. */
    PATRULHA,
    /** Viu ou ouviu algo: pára e olha (dá tempo ao jogador para se esconder). */
    SUSPEITO,
    /** Vai até ao ponto de uma distração e procura à volta. */
    INVESTIGAR,
    /** Persegue o agente (a criatura: persegue o som). */
    ALERTA,
    /** Volta à rota de patrulha. */
    REGRESSAR,
    /** Atingido por um frasco: parado durante uns segundos. */
    ATORDOADO,
    /** Derrubado pelo Zig: inconsciente, sai do jogo. */
    NEUTRALIZADO
}

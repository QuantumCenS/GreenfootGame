/**
 * Interface dos inimigos que o Zig consegue derrubar com um golpe furtivo (não letal).
 * A CriaturaAbissal NÃO a implementa: é grande demais e tem de ser evitada ou distraída.
 *
 * Implementada por: CultistaPatrulheiro (e, por herança, CultistaVigia, CultistaInvocador e SumoSacerdote).
 * Conceitos de POO: interface; o Zig testa "instanceof Neutralizavel" para saber que inimigos
 * pode neutralizar, sem depender das classes concretas.
 */
public interface Neutralizavel
{
    /**
     * Verifica se o agente está em posição de neutralizar este inimigo.
     * Contrato: verdadeiro se o agente está colado às costas do inimigo (ou se este está atordoado).
     *
     * @param agente Agente que tenta o golpe (ex.: o Zig, ao carregar em E)
     * @return true se o golpe é possível neste momento; false caso contrário
     */
    boolean podeSerNeutralizadoPor(AgenteInvestigador agente);

    /**
     * Derruba o inimigo (fica inconsciente e sai do jogo).
     *
     * @param agente Agente que deu o golpe (ex.: o Zig)
     */
    void neutralizar(AgenteInvestigador agente);
}

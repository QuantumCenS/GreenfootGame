/**
 * Interface dos sistemas ligados a um TerminalHacking: câmaras, barreiras laser, cofres.
 * Quando o Zag resolve o puzzle do terminal, todos os alvos ligados são desativados.
 *
 * Implementada por: CamaraVigilancia, BarreiraSeguranca e Cofre.
 * Conceitos de POO: interface e polimorfismo (o terminal guarda uma lista de Hackeavel e
 * desativa-os a todos da mesma forma, cada um reagindo à sua maneira).
 */
public interface Hackeavel
{
    /**
     * Desativa o sistema depois de um hacking bem-sucedido.
     * Contrato: a desativação é permanente (a câmara desliga-se, a barreira desaparece,
     * o cofre abre e não volta a fechar).
     */
    void desativarPorHacking();

    /**
     * Indica se o sistema já foi desativado por hacking.
     *
     * @return true se desativarPorHacking() já foi chamado com efeito; false caso contrário
     */
    boolean isHackeado();
}

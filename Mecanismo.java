/**
 * Interface dos mecanismos acionados por placas de pressão (portões, selos).
 *
 * Implementada por: Portao (e, por herança, PortaoSelado e Cofre).
 * Conceitos de POO: interface (a PlacaPressao só conhece Mecanismo, não a classe concreta,
 * o que reduz o acoplamento).
 */
public interface Mecanismo
{
    /**
     * Começa a abrir o mecanismo. Chamado pela PlacaPressao quando passa a estar premida.
     */
    void abrir();

    /**
     * Começa a fechar o mecanismo. Chamado pela PlacaPressao quando deixa de estar premida.
     * As implementações podem ignorar o pedido (ex.: um selo já quebrado não volta a fechar).
     */
    void fechar();

    /**
     * Indica se o mecanismo está totalmente aberto.
     *
     * @return true se estiver completamente aberto; false se estiver fechado ou a meio do movimento
     */
    boolean isAberto();
}

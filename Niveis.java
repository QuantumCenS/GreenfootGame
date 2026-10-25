/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * "Fábrica" dos níveis: sabe quantos níveis há e cria o nível n com um estado de missão.
 * Assim o menu, o sistema de gravação e os ecrãs de transição não precisam de conhecer
 * cada classe Cenario1..Cenario5 (baixo acoplamento).
 *
 * Classe utilitária final (não estende nada além de Object e não pode ser instanciada).
 * Conceitos de POO: construtor privado, métodos estáticos e polimorfismo (criar() devolve
 * o tipo abstrato Cenario, seja qual for o nível concreto).
 */
public final class Niveis
{
    /** Número total de níveis do jogo (5). */
    public static final int TOTAL = 5;

    /** Construtor privado: classe só com métodos estáticos, não se criam objetos. */
    private Niveis()
    {
    }

    /**
     * Cria o nível pedido com o estado de missão indicado.
     *
     * @param numero Número do nível, de 1 a 5 (ex.: 2 para o Templo Subterrâneo)
     * @param estado Estado da missão com que o nível começa (ex.: o estado vindo do nível anterior)
     * @return o nível pedido, ou null se o número não existir
     */
    public static Cenario criar(int numero, EstadoMissao estado)
    {
        switch (numero) {
            case 1:  return new Cenario1(estado);
            case 2:  return new Cenario2(estado);
            case 3:  return new Cenario3(estado);
            case 4:  return new Cenario4(estado);
            case 5:  return new Cenario5(estado);
            default: return null;
        }
    }

    /**
     * Verifica se existe um nível com este número.
     *
     * @param numero Número do nível (ex.: getNumeroNivel() + 1 para saber se há nível seguinte)
     * @return true se estiver entre 1 e TOTAL; false caso contrário
     */
    public static boolean existe(int numero)
    {
        return numero >= 1 && numero <= TOTAL;
    }

    /**
     * Devolve o nome curto do nível, para menus.
     *
     * @param numero Número do nível (ex.: 3)
     * @return nome do nível (ex.: "A Biblioteca Proibida"), ou "?" se não existir
     */
    public static String getNome(int numero)
    {
        switch (numero) {
            case 1:  return "Docas de Zanarkin";
            case 2:  return "O Templo Subterrâneo";
            case 3:  return "A Biblioteca Proibida";
            case 4:  return "O Observatório do Pico Negro";
            case 5:  return "O Coração da Fossa";
            default: return "?";
        }
    }
}

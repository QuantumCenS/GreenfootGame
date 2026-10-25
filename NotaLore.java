import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Nota escrita que conta um pedaço da história. Opcional, mas vale pontos (+100):
 * ao ser apanhada mostra o texto no ecrã e soma à pontuação.
 *
 * Hierarquia: NotaLore estende ItemRecolhivel, que estende ElementoInterativo, que estende Actor
 * (3 níveis além de Actor).
 * Conceitos de POO: herança, implementação do método abstrato aoSerRecolhido (overriding),
 * encapsulamento (id privado e final, só com getter).
 */
public class NotaLore extends ItemRecolhivel
{
    /** Identificador da nota em ArquivoLore (ex.: "N1-3"). */
    private final String id;

    /**
     * Cria uma nota com o ícone "nota.png" e brilho amarelado.
     *
     * @param id identificador da nota em ArquivoLore (ex.: "N1-3")
     * @throws IllegalArgumentException se não existir nenhuma nota com esse id
     */
    public NotaLore(String id)
    {
        super("nota.png", new Color(255, 230, 150));
        if (!ArquivoLore.existeNota(id)) {
            throw new IllegalArgumentException("Nota inexistente: " + id);
        }
        this.id = id;
    }

    /**
     * Implementa ItemRecolhivel.aoSerRecolhido: pede ao Cenario para registar a nota e mostrar o texto.
     *
     * @param agente Agente que apanhou a nota (ex.: o Zag)
     * @param cenario Nível atual (ex.: um Cenario1)
     */
    @Override
    protected void aoSerRecolhido(AgenteInvestigador agente, Cenario cenario)
    {
        cenario.recolherNota(this);
    }

    /**
     * Devolve o identificador da nota.
     *
     * @return id em ArquivoLore (ex.: "N1-3")
     */
    public String getId()
    {
        return id;
    }
}

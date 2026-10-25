import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Classe base ABSTRATA dos elementos do cenário com que os agentes interagem
 * (plataformas, zonas de sombra, projéteis de distração...).
 * Hierarquia: Actor -> ElementoInterativo -> Plataforma -> PlataformaMovel -> RochaRunica
 *
 * Fornece os limites do elemento (esquerda, direita, topo, base), usados nas colisões.
 *
 * Subclasses diretas: Plataforma, Projectil, ItemRecolhivel, CamaraVigilancia, TerminalHacking,
 * PlacaPressao, SinoAncestral, ZonaSombra, ZonaExtracao e Dica.
 * Conceitos de POO: classe abstrata, herança (todas as subclasses reutilizam os métodos de limites)
 * e overriding de act() nas subclasses com comportamento.
 */
public abstract class ElementoInterativo extends Actor
{
    /** Construtor vazio; a imagem é definida por cada subclasse. */
    public ElementoInterativo()
    {
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo de execução. Redefine Actor.act() sem fazer nada:
     * as subclasses com comportamento redefinem-no (overriding).
     */
    public void act()
    {
        // As subclasses com comportamento redefinem este método.
    }

    /**
     * Devolve a largura do elemento (a largura da imagem atual).
     *
     * @return largura em píxeis (ex.: 96 numa plataforma de madeira típica)
     */
    public int getLargura()
    {
        return getImage().getWidth();
    }

    /**
     * Devolve a altura do elemento (a altura da imagem atual).
     *
     * @return altura em píxeis (ex.: 18 numa plataforma de madeira)
     */
    public int getAltura()
    {
        return getImage().getHeight();
    }

    /**
     * Devolve a coordenada x do lado esquerdo do elemento.
     *
     * @return x do limite esquerdo, em píxeis do ecrã
     */
    public int getEsquerda()
    {
        return getX() - getLargura() / 2;
    }

    /**
     * Devolve a coordenada x do lado direito do elemento.
     *
     * @return x do limite direito, em píxeis do ecrã
     */
    public int getDireita()
    {
        return getEsquerda() + getLargura();
    }

    /**
     * Devolve a coordenada y do topo do elemento (onde as entidades se apoiam).
     *
     * @return y do limite superior, em píxeis
     */
    public int getTopo()
    {
        return getY() - getAltura() / 2;
    }

    /**
     * Devolve a coordenada y da base do elemento.
     *
     * @return y do limite inferior, em píxeis
     */
    public int getBase()
    {
        return getTopo() + getAltura();
    }
}

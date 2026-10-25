import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Plataforma que oscila entre a posição onde foi colocada e (origem + alcance).
 * O movimento é suave (abranda nas pontas) e a trajetória é configurável:
 * horizontal (alcanceY = 0), vertical (alcanceX = 0) ou diagonal.
 * Os agentes que estão em cima são transportados (ver EntidadeAnimada.seguirPlataformaApoio).
 *
 * Hierarquia: PlataformaMovel estende Plataforma, que estende ElementoInterativo, que estende Actor
 * (3 níveis além de Actor); subclasse: RochaRunica.
 * Conceitos de POO: herança, implementação de interface (Deslocavel), overriding (act, addedToWorld,
 * isMovel, getDeltaX, getDeltaY), overloading de construtores (3 versões), método calcularProgresso()
 * pensado para ser redefinido (a RochaRunica redefine-o).
 */
public class PlataformaMovel extends Plataforma implements Deslocavel
{
    /** Ciclos para ir e voltar, quando não se indica outro valor (240). */
    private static final int PERIODO_PADRAO = 240;   // frames para ir e voltar

    /** Deslocamento horizontal máximo em píxeis; negativo = para a esquerda (ex.: 96). */
    private final int alcanceX;
    /** Deslocamento vertical máximo em píxeis; negativo = para cima (ex.: -300). */
    private final int alcanceY;
    /** Ciclos que demora a ir e voltar (mínimo 2; ex.: 260). */
    private final int periodo;
    /** Coordenada x de origem (corrigida quando a câmara se move). */
    private int xOrigem;
    /** Coordenada y de origem. */
    private int yOrigem;
    /** Posição atual no período, de 0 a periodo - 1. */
    private int fase;
    /** Deslocamento horizontal no último ciclo, em píxeis (para transportar quem está em cima). */
    private int deltaX;
    /** Deslocamento vertical no último ciclo, em píxeis. */
    private int deltaY;
    /** true se a oscilação está a correr. */
    private boolean ativa;

    /** Elevador de madeira que sobe 150 px (para criar pelo menu do Greenfoot). */
    public PlataformaMovel()
    {
        this(96, 0, -150);
    }

    /**
     * Plataforma de madeira com período padrão de 240 ciclos (overloading).
     *
     * @param largura Largura em píxeis (ex.: 96)
     * @param alcanceX Deslocamento horizontal máximo em píxeis (ex.: 0)
     * @param alcanceY Deslocamento vertical máximo em píxeis, negativo = para cima (ex.: -150)
     */
    public PlataformaMovel(int largura, int alcanceX, int alcanceY)
    {
        this(largura, TipoPlataforma.MADEIRA, alcanceX, alcanceY, PERIODO_PADRAO);
    }

    /**
     * Plataforma móvel com todos os parâmetros.
     *
     * @param largura  Largura em píxeis (ex.: 96)
     * @param tipo     Textura da plataforma (ex.: TipoPlataforma.MADEIRA, a jangada do Cenario1)
     * @param alcanceX deslocamento horizontal máximo (negativo = para a esquerda) (ex.: 96 no Cenario1)
     * @param alcanceY deslocamento vertical máximo (negativo = para cima) (ex.: -300 no elevador do Cenario4)
     * @param periodo  frames que demora a ir e voltar, mínimo 2 (ex.: 260)
     */
    public PlataformaMovel(int largura, TipoPlataforma tipo, int alcanceX, int alcanceY, int periodo)
    {
        super(largura, tipo);
        this.alcanceX = alcanceX;
        this.alcanceY = alcanceY;
        this.periodo = Math.max(2, periodo);
        this.ativa = true;
    }

    /**
     * Guarda a origem da oscilação quando é colocada no mundo (overriding de Actor.addedToWorld).
     *
     * @param world Mundo onde foi colocada (ex.: um Cenario1)
     */
    @Override
    protected void addedToWorld(World world)
    {
        super.addedToWorld(world);
        xOrigem = getX();
        yOrigem = getY();
    }

    /**
     * A câmara avançou: a origem da oscilação também recua no ecrã (implementa Deslocavel).
     *
     * @param dx Quanto a câmara avançou para a direita, em píxeis (ex.: 10)
     */
    @Override
    public void deslocarReferencias(int dx)
    {
        xOrigem -= dx;
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine ElementoInterativo.act): move-se para a posição
     * correspondente ao progresso e regista o deslocamento.
     */
    @Override
    public void act()
    {
        int xAntes = getX();
        int yAntes = getY();
        double progresso = calcularProgresso();
        setLocation(xOrigem + (int) Math.round(alcanceX * progresso),
                    yOrigem + (int) Math.round(alcanceY * progresso));
        deltaX = getX() - xAntes;
        deltaY = getY() - yAntes;
    }

    /**
     * Progresso entre 0 (origem) e 1 (destino). Aqui segue uma curva cosseno (oscilação suave).
     * As subclasses podem redefinir (a RochaRunica só avança com os dois agentes em cima).
     *
     * @return valor entre 0.0 (na origem) e 1.0 (no destino)
     */
    protected double calcularProgresso()
    {
        if (ativa) {
            fase = (fase + 1) % periodo;
        }
        return (1 - Math.cos(2 * Math.PI * fase / periodo)) / 2;
    }

    /**
     * Redefine Plataforma.isMovel: esta plataforma move-se.
     *
     * @return true
     */
    @Override
    public boolean isMovel()
    {
        return true;
    }

    /**
     * Redefine Plataforma.getDeltaX: deslocamento horizontal no último ciclo.
     *
     * @return deslocamento em píxeis
     */
    @Override
    public int getDeltaX()
    {
        return deltaX;
    }

    /**
     * Redefine Plataforma.getDeltaY: deslocamento vertical no último ciclo.
     *
     * @return deslocamento em píxeis
     */
    @Override
    public int getDeltaY()
    {
        return deltaY;
    }

    /**
     * Pausa ou retoma a oscilação. Atualmente não é chamado no projeto (não existem alavancas).
     *
     * @param ativa true para oscilar, false para parar onde está (ex.: false)
     */
    public void setAtiva(boolean ativa)
    {
        this.ativa = ativa;
    }

    /**
     * Indica se a oscilação está a correr.
     *
     * @return true se estiver ativa
     */
    public boolean isAtiva()
    {
        return ativa;
    }

    /**
     * Devolve o alcance horizontal.
     *
     * @return deslocamento horizontal máximo em píxeis (ex.: 96)
     */
    public int getAlcanceX() { return alcanceX; }

    /**
     * Devolve o alcance vertical.
     *
     * @return deslocamento vertical máximo em píxeis (ex.: -300)
     */
    public int getAlcanceY() { return alcanceY; }
}

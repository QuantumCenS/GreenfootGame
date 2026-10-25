import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Frasco de éter (distração do Zig). Parte-se ao primeiro impacto com um estrondo
 * que se ouve a 340 px: os cultistas vão investigar e as crias correm para o som.
 * Se acertar em cheio num inimigo, deixa-o atordoado (o Zig pode então neutralizá-lo).
 *
 * Hierarquia: FrascoEter estende ProjectilDistracao, que estende Projectil, que estende
 * ElementoInterativo, que estende Actor (4 níveis além de Actor).
 * Conceitos de POO: herança, implementação dos métodos abstratos, overriding dos hooks
 * (aoBaterNaParede, aoAtingirSino) e de act() com super.act(), overloading de construtores.
 */
public class FrascoEter extends ProjectilDistracao
{
    /** Ciclos de atordoamento de um inimigo atingido em cheio (110). */
    private static final int FRAMES_ATORDOADO = 110;

    /** Frasco lançado para a direita (para criar pelo menu do Greenfoot). */
    public FrascoEter()
    {
        this(1);
    }

    /**
     * Cria o frasco com a imagem "frasco_eter.png", sem limite de duração.
     *
     * @param direcao Sentido do lançamento: 1 direita, -1 esquerda (ex.: getDirecao() do Zig)
     */
    public FrascoEter(int direcao)
    {
        super(direcao, 0);
        setImage("frasco_eter.png");
    }

    /**
     * Redefine Projectil.act (chamado pelo Greenfoot em cada ciclo): executa o voo herdado e
     * roda a imagem 18 graus por ciclo.
     */
    @Override
    public void act()
    {
        super.act();
        if (getWorld() != null) {
            setRotation(getRotation() + 18);   // rodopia no ar
        }
    }

    /**
     * Atordoa o inimigo atingido durante 110 ciclos e parte-se.
     *
     * @param inimigo Inimigo atingido (ex.: o SumoSacerdote, cuja aura assim se quebra)
     */
    @Override
    protected void aoAtingirInimigo(InimigoCosmico inimigo)
    {
        inimigo.atordoar(FRAMES_ATORDOADO);
        partir(getX(), getY());
    }

    /**
     * Parte-se ao cair no chão.
     *
     * @param x Coordenada x onde caiu, em píxeis (ex.: getX())
     * @param yChao Coordenada y do topo da plataforma, em píxeis (ex.: 520)
     */
    @Override
    protected void aoAterrar(int x, int yChao)
    {
        partir(x, yChao - 4);
    }

    /** Redefine o hook de ProjectilDistracao: parte-se ao bater numa parede. */
    @Override
    protected void aoBaterNaParede()
    {
        partir(getX(), getY());
    }

    /**
     * Redefine o hook de ProjectilDistracao: parte-se ao acertar num sino (que já tocou).
     *
     * @param sino Sino atingido
     */
    @Override
    protected void aoAtingirSino(SinoAncestral sino)
    {
        partir(getX(), getY());
    }

    /**
     * Parte o frasco: partículas de vidro, ruído TipoRuido.FRASCO, som e remoção.
     *
     * @param x Coordenada x do impacto, em píxeis (ex.: getX())
     * @param y Coordenada y do impacto, em píxeis (ex.: 516, 4 píxeis acima do chão)
     */
    private void partir(int x, int y)
    {
        if (getWorld() == null) {
            return;
        }
        getWorld().addObject(new EfeitoParticulas(new Color(150, 220, 255)), x, y);
        fazerRuido(x, y, TipoRuido.FRASCO);
        Audio.tocar("frasco.wav");
        remover();
    }
}

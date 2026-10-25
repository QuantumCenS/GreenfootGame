import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Projétil NÃO LETAL lançado pelos agentes para distrair (requisito: sem armas).
 * Voa em arco; ao tocar num inimigo, num sino ou no chão chama os métodos abstratos
 * aoAtingirInimigo() / aoAterrar(), que cada tipo de distração define (polimorfismo):
 *  - FrascoEter (Zig): parte-se com estrondo; atordoa quem atinge;
 *  - AmuletoLuminoso (Zag): fica no chão a brilhar e atrai quem está perto.
 *
 * Hierarquia: ProjectilDistracao estende Projectil, que estende ElementoInterativo, que estende Actor
 * (3 níveis além de Actor); subclasses: FrascoEter e AmuletoLuminoso.
 * Conceitos de POO: classe abstrata, implementação de verificarColisoes (overriding), métodos
 * abstratos e hooks com comportamento por omissão (aoBaterNaParede, aoAtingirSino).
 */
public abstract class ProjectilDistracao extends Projectil
{
    /** Velocidade horizontal do lançamento, em píxeis por ciclo (7,5). */
    protected static final double VELOCIDADE_LANCAMENTO_X = 7.5;
    /** Velocidade vertical do lançamento, em píxeis por ciclo (-7,5, para cima). */
    protected static final double VELOCIDADE_LANCAMENTO_Y = -7.5;
    /** Gravidade aplicada às distrações, em píxeis por ciclo (0,45). */
    protected static final double GRAVIDADE = 0.45;

    /**
     * Cria a distração com o arco de lançamento padrão.
     *
     * @param direcao Sentido do lançamento: 1 direita, -1 esquerda (ex.: getDirecao() do agente)
     * @param duracaoMaxima Duração máxima em ciclos, 0 = sem limite (ex.: 0)
     */
    protected ProjectilDistracao(int direcao, int duracaoMaxima)
    {
        super(direcao * VELOCIDADE_LANCAMENTO_X, VELOCIDADE_LANCAMENTO_Y, GRAVIDADE, duracaoMaxima);
    }

    /**
     * Implementa Projectil.verificarColisoes, por esta ordem: inimigo ativo (aoAtingirInimigo),
     * sino (toca-o e chama aoAtingirSino), chão atravessado (aoAterrar) e parede (aoBaterNaParede).
     */
    @Override
    protected void verificarColisoes()
    {
        for (InimigoCosmico inimigo : getWorld().getObjects(InimigoCosmico.class)) {
            if (!inimigo.isNeutralizado() && dentroDe(inimigo)) {
                aoAtingirInimigo(inimigo);
                return;
            }
        }
        SinoAncestral sino = (SinoAncestral) getOneIntersectingObject(SinoAncestral.class);
        if (sino != null) {
            sino.tocar();
            aoAtingirSino(sino);
            return;
        }
        Plataforma chao = chaoAtravessado();
        if (chao != null) {
            aoAterrar(getX(), chao.getTopo());
            return;
        }
        if (paredeNoPonto() != null) {
            aoBaterNaParede();
        }
    }

    /**
     * Verifica se a posição do projétil está dentro da hitbox de uma entidade.
     *
     * @param e Entidade a testar (ex.: um CultistaPatrulheiro)
     * @return true se o ponto estiver dentro da hitbox
     */
    private boolean dentroDe(EntidadeAnimada e)
    {
        return getX() >= e.getEsquerdaColisao() && getX() <= e.getDireitaColisao()
            && getY() >= e.getTopoColisao() && getY() <= e.getBaseColisao();
    }

    /**
     * Emite o ruído desta distração no ponto indicado (se estiver num Cenario).
     *
     * @param x Coordenada x do ruído, em píxeis (ex.: getX())
     * @param y Coordenada y do ruído, em píxeis (ex.: getY())
     * @param tipo Tipo de ruído (ex.: TipoRuido.FRASCO)
     */
    protected void fazerRuido(int x, int y, TipoRuido tipo)
    {
        if (getCenario() != null) {
            getCenario().emitirRuido(x, y, tipo);
        }
    }

    /**
     * Contrato: atingiu um inimigo em cheio.
     *
     * @param inimigo Inimigo atingido (ex.: uma CriaturaAbissal)
     */
    protected abstract void aoAtingirInimigo(InimigoCosmico inimigo);

    /**
     * Contrato: caiu no topo de uma plataforma.
     *
     * @param x Coordenada x onde caiu, em píxeis (ex.: getX())
     * @param yChao Coordenada y do topo da plataforma, em píxeis (ex.: 520)
     */
    protected abstract void aoAterrar(int x, int yChao);

    /** Bateu numa parede. Por omissão cai a direito (overriding no frasco: parte-se). */
    protected void aoBaterNaParede()
    {
        setVelocidadeX(0);
    }

    /**
     * Acertou num sino. Por omissão cai a direito (overriding no frasco: parte-se).
     *
     * @param sino Sino atingido
     */
    protected void aoAtingirSino(SinoAncestral sino)
    {
        setVelocidadeX(0);
    }
}

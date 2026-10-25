import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Prof. Abel Corvo, Sumo-Sacerdote da Ordem Abissal (mini-chefe do nível 5).
 *  - É um CultistaInvocador mais perigoso: vê mais longe e invoca orbes mais depressa.
 *  - Está protegido por uma aura: o Zig só o consegue neutralizar quando estiver
 *    ATORDOADO (atingido em cheio por um frasco de éter). Cooperação: um distrai/atordoa,
 *    o outro derruba.
 *
 * Hierarquia: ... -> CultistaPatrulheiro -> CultistaInvocador -> SumoSacerdote (5 níveis além de Actor)
 * (SumoSacerdote estende CultistaInvocador, que estende CultistaPatrulheiro, que estende InimigoCosmico,
 * que estende EntidadeAnimada, que estende Actor).
 * Conceitos de POO: herança em 5 níveis, overriding com reutilização de super (podeSerNeutralizadoPor,
 * neutralizar, atordoar) e de um hook (getRecargaInvocacao), overloading de construtores.
 */
public class SumoSacerdote extends CultistaInvocador
{
    /** Ciclos entre orbes, menos do que os 85 do invocador normal (55). */
    private static final int RECARGA = 55;
    /** Pontos extra ao neutralizá-lo, além dos 25 normais (200). */
    private static final int PONTOS_BONUS = 200;

    /** Sumo-Sacerdote com rota de 120 px (para criar pelo menu do Greenfoot). */
    public SumoSacerdote()
    {
        this(120);
    }

    /**
     * Sumo-Sacerdote com imagens "sacerdote", cone inclinado 10 graus e alcance de 340 px.
     *
     * @param limite Comprimento da rota para a direita, em píxeis (ex.: 180, no Cenario5)
     */
    public SumoSacerdote(int limite)
    {
        super(limite, 10, "sacerdote", 340);
    }

    /**
     * Redefine o hook de CultistaInvocador: invoca orbes mais depressa.
     *
     * @return 55 ciclos
     */
    @Override
    protected int getRecargaInvocacao()
    {
        return RECARGA;
    }

    /**
     * Overriding: só pode ser neutralizado quando atordoado (a aura protege-o de golpes normais)
     * e se as condições de CultistaPatrulheiro também se cumprirem.
     *
     * @param agente Agente que tenta o golpe (ex.: o Zig)
     * @return true se estiver atordoado e o agente estiver em posição
     */
    @Override
    public boolean podeSerNeutralizadoPor(AgenteInvestigador agente)
    {
        return getEstado() == EstadoInimigo.ATORDOADO && super.podeSerNeutralizadoPor(agente);
    }

    /**
     * Redefine neutralizar: além do comportamento herdado, dá 200 pontos de bónus e mostra uma mensagem.
     *
     * @param agente Agente que deu o golpe (ex.: o Zig)
     */
    @Override
    public void neutralizar(AgenteInvestigador agente)
    {
        super.neutralizar(agente);
        if (getCenario() != null) {
            getCenario().getEstado().adicionarPontos(PONTOS_BONUS);
            getCenario().mostrarMensagem("O Sumo-Sacerdote caiu! +" + (PONTOS_BONUS + EstadoMissao.PONTOS_POR_NEUTRALIZACAO),
                                         new Color(255, 120, 120), 150);
        }
    }

    /**
     * Quando um frasco o atordoa, mostra que a aura quebrou (janela para o golpe do Zig).
     * Redefine InimigoCosmico.atordoar, chamando primeiro a versão herdada.
     *
     * @param frames Duração do atordoamento em ciclos (ex.: 110 com um frasco de éter)
     */
    @Override
    public void atordoar(int frames)
    {
        super.atordoar(frames);
        mostrarBalao("A aura quebrou!", new Color(150, 30, 40), frames);
    }
}

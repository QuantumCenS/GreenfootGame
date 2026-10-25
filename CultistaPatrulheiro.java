import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Cultista da Ordem Abissal que patrulha uma rota com uma lanterna de visão cónica.
 *
 * Máquina de estados:
 *   PATRULHA  --vê um agente-->  SUSPEITO ("?", ~0,5 s para o agente se esconder)
 *   SUSPEITO  --continua a ver-->  ALERTA ("!", persegue e chama os outros cultistas)
 *   ALERTA    --perde o rasto 2,5 s-->  INVESTIGAR o último sítio onde o viu
 *   ruído subtil (passos)      -> vira-se e fica SUSPEITO
 *   distração (frasco, amuleto, sino, alarme) -> INVESTIGAR o local
 *   INVESTIGAR --nada encontrado-->  REGRESSAR à rota  -->  PATRULHA
 *
 * Pode ser NEUTRALIZADO pelo Zig por trás (interface Neutralizavel) — o requisito
 * "inimigos derrotados pelo jogador" sem usar armas.
 *
 * Hierarquia: CultistaPatrulheiro estende InimigoCosmico, que estende EntidadeAnimada, que estende Actor
 * (3 níveis além de Actor); subclasses: CultistaVigia e CultistaInvocador.
 * Conceitos de POO: herança, overriding (executarComportamento, reagirADistracao, atualizarAnimacao,
 * act, addedToWorld, getLarguraColisao...), overloading de construtores (4 versões encadeadas),
 * implementação de interface (Neutralizavel), composição (CampoVisao, ConeVisao, Animacao).
 */
public class CultistaPatrulheiro extends InimigoCosmico implements Neutralizavel
{
    /** Velocidade em patrulha e ao regressar, em píxeis por ciclo (1). */
    protected static final int VELOCIDADE_PATRULHA = 1;
    /** Velocidade ao investigar uma distração, em píxeis por ciclo (2). */
    protected static final int VELOCIDADE_INVESTIGAR = 2;
    /** Velocidade a perseguir um agente, em píxeis por ciclo (3). */
    protected static final int VELOCIDADE_PERSEGUIR = 3;
    /** Ciclos em SUSPEITO antes de dar o alarme se continuar a ver o agente (28, cerca de 0,5 s). */
    private static final int TEMPO_REACAO = 28;
    /** Ciclos sem ver o agente até desistir da perseguição (150, cerca de 2,5 s). */
    private static final int TEMPO_PERDER_RASTO = 150;
    /** Ciclos a procurar no local de uma distração antes de regressar (200). */
    private static final int TEMPO_PROCURAR = 200;
    /** Ciclos a olhar em SUSPEITO antes de regressar à rota (70). */
    private static final int TEMPO_OLHAR = 70;
    /** Distância horizontal máxima para o golpe do Zig, em píxeis (58). */
    private static final int ALCANCE_GOLPE = 58;
    /** Largura da hitbox do cultista, em píxeis (26). */
    private static final int LARGURA_COLISAO = 26;
    /** Altura da hitbox do cultista, em píxeis (76). */
    private static final int ALTURA_COLISAO = 76;
    /** Distância dos olhos abaixo do topo da hitbox, em píxeis (16). */
    private static final int ALTURA_OLHOS = 16;   // abaixo do topo da hitbox

    /** Campo de visão cónico (alcance e abertura), usado para detetar agentes. */
    private final CampoVisao visao;
    /** Graus que o cone aponta para baixo (ex.: 35 numa varanda). */
    private final int inclinacaoVisao;
    /** Animação a andar. */
    private final Animacao animAndar;
    /** Imagem parado. */
    private final Animacao animParado;
    /** Imagem em alerta. */
    private final Animacao animAlerta;
    /** Imagem atordoado. */
    private final Animacao animAtordoado;
    /** Imagem neutralizado (caído). */
    private final Animacao animNeutralizado;
    /** Desenho do cone de visão no mundo (criado ao entrar no mundo). */
    private ConeVisao cone;
    /** Balão "[E]" que indica ao Zig que pode dar o golpe, ou null. */
    private Balao balaoGolpe;
    /** Ciclos que faltam, em SUSPEITO, para dar o alarme. */
    private int framesSuspeita;
    /** Ciclos seguidos em ALERTA sem ver nenhum agente. */
    private int framesSemVer;
    /** Velocidade usada no estado INVESTIGAR (2 normal, 3 se o ruído for urgente). */
    private int velocidadeInvestigacao;

    /** Cultista com rota de 200 px (para criar pelo menu do Greenfoot). */
    public CultistaPatrulheiro()
    {
        this(200);
    }

    /**
     * Cultista com o cone de visão na horizontal (overloading).
     *
     * @param limite Comprimento da rota para a direita, em píxeis (ex.: 250, no Cenario1)
     */
    public CultistaPatrulheiro(int limite)
    {
        this(limite, 0);
    }

    /**
     * Cultista com imagens "cultista", alcance de visão 240 px e semiângulo 24 graus.
     *
     * @param limite          comprimento da rota (para a direita), em píxeis (ex.: 250)
     * @param inclinacaoVisao quantos graus o cone aponta para baixo (útil em varandas) (ex.: 0)
     */
    public CultistaPatrulheiro(int limite, int inclinacaoVisao)
    {
        this(limite, inclinacaoVisao, "cultista", 240, 24);
    }

    /**
     * Construtor completo, usado pelas subclasses para mudar imagens e visão.
     *
     * @param limite Comprimento da rota para a direita, em píxeis (ex.: 0 no CultistaVigia)
     * @param inclinacaoVisao Graus que o cone aponta para baixo (ex.: 6 no CultistaVigia)
     * @param prefixo Prefixo dos ficheiros de imagem (ex.: "vigia" ou "invocador")
     * @param alcanceVisao Alcance do cone de visão, em píxeis (ex.: 260 no CultistaVigia)
     * @param semiAngulo Metade da abertura do cone, em graus (ex.: 24)
     */
    protected CultistaPatrulheiro(int limite, int inclinacaoVisao, String prefixo, int alcanceVisao, int semiAngulo)
    {
        super(limite, VELOCIDADE_PATRULHA);
        this.inclinacaoVisao = inclinacaoVisao;
        this.visao = new CampoVisao(alcanceVisao, semiAngulo);
        this.animAndar = carregarAnimacao(prefixo + "_andar", 4, 9);
        this.animParado = carregarAnimacao(prefixo + "_parado", 1, 1);
        this.animAlerta = carregarAnimacao(prefixo + "_alerta", 1, 1);
        this.animAtordoado = carregarAnimacao(prefixo + "_atordoado", 1, 1);
        this.animNeutralizado = carregarAnimacao(prefixo + "_neutralizado", 1, 1);
        setImage(animParado.getFrameAtual(true));
    }

    /**
     * Redefine EntidadeAnimada.addedToWorld: além de guardar a posição inicial, cria o ConeVisao
     * que desenha a lanterna (só na primeira vez).
     *
     * @param mundo Mundo onde o cultista foi colocado (ex.: um Cenario1)
     */
    @Override
    protected void addedToWorld(World mundo)
    {
        super.addedToWorld(mundo);
        if (cone == null) {
            cone = new ConeVisao(this);
            mundo.addObject(cone, getX(), getY());
        }
    }

    // ------------------------------------------------------------------ comportamento (overriding)

    /**
     * Implementa o método abstrato de InimigoCosmico: a máquina de estados do cultista
     * (ver diagrama no comentário da classe).
     */
    @Override
    protected void executarComportamento()
    {
        AgenteInvestigador visto = procurarAgenteVisivel();
        switch (getEstado()) {
            case PATRULHA:
                if (visto != null) {
                    suspeitar(visto, TEMPO_REACAO);
                } else {
                    comportamentoPatrulha();
                }
                break;
            case SUSPEITO:
                if (visto != null) {
                    virarPara(visto.getX());
                    framesSuspeita--;
                    if (framesSuspeita <= 0) {
                        alertar(visto);
                    }
                } else if (getTemporizador() > TEMPO_OLHAR) {
                    mudarEstado(EstadoInimigo.REGRESSAR);
                }
                break;
            case INVESTIGAR:
                if (visto != null) {
                    suspeitar(visto, TEMPO_REACAO / 2);
                } else if (irPara(getPontoAlvoX(), velocidadeInvestigacao)) {
                    olharEmVolta();
                    if (getTemporizador() > TEMPO_PROCURAR) {
                        mostrarBalao("...", new Color(90, 90, 110), 60);
                        mudarEstado(EstadoInimigo.REGRESSAR);
                    }
                }
                break;
            case ALERTA:
                if (visto != null) {
                    framesSemVer = 0;
                    setPontoAlvoX(visto.getX());
                } else {
                    framesSemVer++;
                    if (framesSemVer > TEMPO_PERDER_RASTO) {
                        mostrarBalao("?", new Color(200, 160, 40), 60);
                        investigar(getPontoAlvoX(), VELOCIDADE_INVESTIGAR);
                        break;
                    }
                }
                comportamentoAlerta(visto);
                break;
            case REGRESSAR:
                if (visto != null) {
                    suspeitar(visto, TEMPO_REACAO);
                } else if (irPara(getXinicial(), VELOCIDADE_PATRULHA)) {
                    mudarEstado(EstadoInimigo.PATRULHA);
                }
                break;
            default:
                break;
        }
    }

    /** O que faz em PATRULHA: percorre a rota (o CultistaVigia redefine: fica parado a vigiar). */
    protected void comportamentoPatrulha()
    {
        patrulhar(VELOCIDADE_PATRULHA);
    }

    /**
     * O que faz em ALERTA: persegue o agente (o Invocador redefine para lançar feitiços).
     *
     * @param visto Agente visível neste ciclo, ou null se o perdeu de vista (ex.: o Zag)
     */
    protected void comportamentoAlerta(AgenteInvestigador visto)
    {
        irPara(getPontoAlvoX(), VELOCIDADE_PERSEGUIR);
    }

    /**
     * Reação a ruídos (overriding): passos só o fazem olhar; distrações fazem-no investigar.
     * Em ALERTA ignora ruídos; alarmes e gritos fazem-no investigar a correr.
     *
     * @param x Coordenada x da origem do ruído (ex.: a posição do sino que tocou)
     * @param y Coordenada y da origem do ruído (ex.: a base do sino)
     * @param tipo Tipo de ruído (ex.: TipoRuido.SINO)
     */
    @Override
    public void reagirADistracao(int x, int y, TipoRuido tipo)
    {
        if (getEstado() == EstadoInimigo.ALERTA) {
            return;
        }
        if (tipo.isSubtil()) {
            if (getEstado() != EstadoInimigo.SUSPEITO) {
                virarPara(x);
                framesSuspeita = TEMPO_REACAO;
                mudarEstado(EstadoInimigo.SUSPEITO);
                mostrarBalao("?", new Color(200, 160, 40), 50);
            }
            return;
        }
        boolean urgente = tipo == TipoRuido.ALARME || tipo == TipoRuido.GRITO;
        if (getEstado() != EstadoInimigo.INVESTIGAR) {
            mostrarBalao(urgente ? "!?" : "?", new Color(200, 160, 40), 50);
        }
        investigar(x, urgente ? VELOCIDADE_PERSEGUIR : VELOCIDADE_INVESTIGAR);
    }

    /**
     * Passa a INVESTIGAR o ponto indicado, com a velocidade indicada.
     *
     * @param x Coordenada x a investigar (ex.: getPontoAlvoX(), o último sítio onde viu o agente)
     * @param velocidade Velocidade em píxeis por ciclo (ex.: 2, ou 3 se for urgente)
     */
    private void investigar(int x, int velocidade)
    {
        setPontoAlvoX(x);
        velocidadeInvestigacao = velocidade;
        mudarEstado(EstadoInimigo.INVESTIGAR);
        setTemporizador(0);
    }

    /**
     * Passa a SUSPEITO: vira-se para o agente e mostra "?".
     *
     * @param agente Agente avistado (ex.: o Zig)
     * @param tempoReacao Ciclos até dar o alarme se continuar a vê-lo (ex.: 28, ou 14 se já estava a investigar)
     */
    private void suspeitar(AgenteInvestigador agente, int tempoReacao)
    {
        virarPara(agente.getX());
        framesSuspeita = tempoReacao;
        mudarEstado(EstadoInimigo.SUSPEITO);
        mostrarBalao("?", new Color(200, 160, 40), 40);
    }

    /**
     * Passa a ALERTA: persegue o agente, mostra "!", toca o som de alerta e grita
     * (TipoRuido.GRITO) para chamar os outros cultistas.
     *
     * @param agente Agente detetado (ex.: o Zag)
     */
    private void alertar(AgenteInvestigador agente)
    {
        setPontoAlvoX(agente.getX());
        framesSemVer = 0;
        mudarEstado(EstadoInimigo.ALERTA);
        mostrarBalao("!", new Color(200, 30, 40), 60);
        Audio.tocar("alerta.wav");
        if (getCenario() != null) {
            getCenario().emitirRuido(getX(), getY(), TipoRuido.GRITO);
        }
    }

    /** Vira-se para o outro lado a cada 45 ciclos (procura à volta do ponto investigado). */
    private void olharEmVolta()
    {
        if (getTemporizador() % 45 == 0) {
            setViradoParaDireita(!isViradoParaDireita());
        }
    }

    /**
     * Redefine o hook de InimigoCosmico: depois de tocar num agente, entra em ALERTA.
     *
     * @param agente Agente que foi tocado (ex.: o Zig)
     */
    @Override
    protected void aoTocarAgente(AgenteInvestigador agente)
    {
        if (getEstado() != EstadoInimigo.ALERTA) {
            alertar(agente);
        }
    }

    //  visão

    /**
     * Procura o primeiro agente visível dentro do cone.
     *
     * @return agente visível, ou null se não vir nenhum
     */
    protected AgenteInvestigador procurarAgenteVisivel()
    {
        List<AgenteInvestigador> agentes = getWorld().getObjects(AgenteInvestigador.class);
        for (AgenteInvestigador agente : agentes) {
            if (visao.ve(getWorld(), getOlhoX(), getOlhoY(), getDirecaoVisao(), agente)) {
                return agente;
            }
        }
        return null;
    }

    /**
     * Devolve a coordenada x dos olhos (origem do cone), 6 píxeis à frente do centro.
     *
     * @return x dos olhos, em píxeis do ecrã
     */
    public int getOlhoX()
    {
        return getX() + getDirecao() * 6;
    }

    /**
     * Devolve a coordenada y dos olhos (origem do cone).
     *
     * @return y dos olhos, em píxeis
     */
    public int getOlhoY()
    {
        return getTopoColisao() + ALTURA_OLHOS;
    }

    /**
     * Devolve a direção do cone em graus: em frente, um pouco inclinado para baixo.
     *
     * @return ângulo em graus (0 = direita, 90 = baixo, 180 = esquerda); ex.: 35 ou 145 com inclinação 35
     */
    public double getDirecaoVisao()
    {
        return isViradoParaDireita() ? inclinacaoVisao : 180 - inclinacaoVisao;
    }

    /**
     * Devolve o campo de visão (usado pelo ConeVisao para o desenhar).
     *
     * @return campo de visão do cultista
     */
    public CampoVisao getCampoVisao()
    {
        return visao;
    }

    /**
     * Indica se o cone está ativo: só se mostra/funciona enquanto o cultista está ativo.
     *
     * @return true se não estiver atordoado nem neutralizado
     */
    public boolean isVisaoAtiva()
    {
        return getEstado() != EstadoInimigo.ATORDOADO && getEstado() != EstadoInimigo.NEUTRALIZADO;
    }

    //  Neutralizavel

    /**
     * Implementa Neutralizavel: pode ser neutralizado por trás, ao alcance do braço (58 px) e à mesma
     * altura (20 px) — ou atordoado (de qualquer lado). Nunca em ALERTA, a não ser atordoado.
     *
     * @param agente Agente que tenta o golpe (ex.: o Zig)
     * @return true se o golpe é possível neste momento
     */
    @Override
    public boolean podeSerNeutralizadoPor(AgenteInvestigador agente)
    {
        if (!agente.podeNeutralizarInimigos() || isNeutralizado() || agente.getWorld() != getWorld()) {
            return false;
        }
        int dx = agente.getX() - getX();
        if (Math.abs(dx) > ALCANCE_GOLPE || Math.abs(agente.getBaseColisao() - getBaseColisao()) > 20) {
            return false;
        }
        if (getEstado() == EstadoInimigo.ATORDOADO) {
            return true;
        }
        boolean porTras = isViradoParaDireita() ? dx < -4 : dx > 4;
        return porTras && getEstado() != EstadoInimigo.ALERTA;
    }

    /**
     * Implementa Neutralizavel: derruba o cultista e regista a neutralização no Cenario (+25 pontos).
     *
     * @param agente Agente que deu o golpe (ex.: o Zig)
     */
    @Override
    public void neutralizar(AgenteInvestigador agente)
    {
        esconderBalaoGolpe();
        ficarNeutralizado();
        if (getCenario() != null) {
            getCenario().registarNeutralizacao(this);
        }
    }

    /** Redefine o hook de InimigoCosmico: retira o balão do golpe e o cone de visão. */
    @Override
    protected void aoSerNeutralizado()
    {
        esconderBalaoGolpe();
        if (cone != null && cone.getWorld() != null) {
            cone.getWorld().removeObject(cone);
        }
    }

    //  act extra: dica do golpe

    /**
     * Redefine InimigoCosmico.act (chamado pelo Greenfoot em cada ciclo): executa o ciclo
     * da superclasse e depois atualiza a dica do golpe.
     */
    @Override
    public void act()
    {
        super.act();
        if (getWorld() != null && !isNeutralizado()) {
            atualizarDicaGolpe();
        }
    }

    /** Mostra "[E]" por cima do cultista quando o Zig está em posição de o neutralizar. */
    private void atualizarDicaGolpe()
    {
        AgenteInvestigador emPosicao = null;
        for (AgenteInvestigador a : getWorld().getObjects(AgenteInvestigador.class)) {
            if (podeSerNeutralizadoPor(a)) {
                emPosicao = a;
            }
        }
        if (emPosicao != null && balaoGolpe == null) {
            balaoGolpe = new Balao(this, "[" + emPosicao.getNomeTeclaAcao() + "]", new Color(30, 120, 60), Balao.PERMANENTE, 36);
            getWorld().addObject(balaoGolpe, getX(), getY());
        } else if (emPosicao == null) {
            esconderBalaoGolpe();
        }
    }

    /** Remove o balão da dica do golpe, se existir. */
    private void esconderBalaoGolpe()
    {
        if (balaoGolpe != null && balaoGolpe.getWorld() != null) {
            balaoGolpe.getWorld().removeObject(balaoGolpe);
        }
        balaoGolpe = null;
    }

    // animação e hitbox

    /** Implementa o método abstrato de InimigoCosmico: escolhe a imagem conforme o estado. */
    @Override
    protected void atualizarAnimacao()
    {
        switch (getEstado()) {
            case NEUTRALIZADO:
                setImage(new GreenfootImage(animNeutralizado.getFrameAtual(isViradoParaDireita())));
                break;
            case ATORDOADO:
                mostrar(animAtordoado);
                break;
            case ALERTA:
                mostrar(isNoChao() && getTemporizador() % 30 < 15 ? animAlerta : animAndar);
                break;
            case SUSPEITO:
                mostrar(animParado);
                break;
            default:
                mostrar(isEmPausa() || (getEstado() == EstadoInimigo.INVESTIGAR
                        && Math.abs(getPontoAlvoX() - getX()) <= VELOCIDADE_INVESTIGAR) ? animParado : animAndar);
                break;
        }
    }

    /**
     * Mostra uma imagem diretamente. Pensado para a pose de invocação do CultistaInvocador,
     * mas atualmente não é chamado (o Invocador usa mostrar(animInvocar)).
     *
     * @param imagem Imagem a mostrar (ex.: um frame de "invocador_invocar")
     */
    protected void mostrarImagem(GreenfootImage imagem)
    {
        setImage(imagem);
    }

    /**
     * Redefine a largura da hitbox (mais estreita do que a imagem).
     *
     * @return 26 píxeis
     */
    @Override
    public int getLarguraColisao()
    {
        return LARGURA_COLISAO;
    }

    /**
     * Redefine a altura da hitbox.
     *
     * @return 76 píxeis
     */
    @Override
    public int getAlturaColisao()
    {
        return ALTURA_COLISAO;
    }

    /**
     * Devolve a cor do cone conforme o estado (amarelo calmo, laranja desconfiado, vermelho alerta),
     * sempre semitransparente.
     *
     * @return cor de preenchimento do cone (ex.: new Color(255, 40, 40, 70) em ALERTA)
     */
    public Color getCorCone()
    {
        switch (getEstado()) {
            case ALERTA:   return new Color(255, 40, 40, 70);
            case SUSPEITO:
            case INVESTIGAR: return new Color(255, 170, 40, 55);
            default:       return new Color(255, 240, 170, 38);
        }
    }
}

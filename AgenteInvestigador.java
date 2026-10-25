import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Classe base ABSTRATA dos agentes controlados pelos jogadores.
 * Hierarquia: Actor -> EntidadeAnimada -> AgenteInvestigador -> Zig / Zag  (3 níveis além de Actor).
 *
 * Implementa tudo o que é comum aos dois espiões:
 *  - movimento com colisões; o agente fica sempre dentro do ecrã (co-op com scroll);
 *  - salto com "coyote time", buffer de salto e altura variável;
 *  - saltos no ar (Zig) e planar (Zag), ativados por cada subclasse (polimorfismo);
 *  - furtividade: agachado anda a metade da velocidade e NÃO faz barulho;
 *    correr e aterrar de um salto fazem ruído que os inimigos ouvem;
 *  - ação (E / ENTER): primeiro tenta a especialidade do agente (neutralizar / hackear),
 *    se não houver nada para isso lança uma distração (frasco / amuleto) — template method;
 *  - empurrar objetos pesados (CaixaPesada só se move com os DOIS a empurrar);
 *  - estado "ocupado" (a hackear, a golpear) em que não responde ao teclado;
 *  - queda: reaparece na última posição segura; dano com empurrão e invulnerabilidade.
 *
 * Conceitos de POO: classe abstrata com métodos abstratos (contrato para Zig e Zag), herança
 * (2 níveis além de Actor, 3 nas subclasses), overriding (act, addedToWorld, sofrerDano,
 * limitarPosicao, getLarguraColisao...), overloading de construtores, encapsulamento
 * (atributos privados) e composição (ControlosJogador, Animacao).
 */
public abstract class AgenteInvestigador extends EntidadeAnimada
{
    /** Velocidade vertical do salto, em píxeis por ciclo (-17). */
    private static final int FORCA_SALTO = -17;          // ~136 px de altura máxima
    /** Velocidade vertical do salto aéreo (salto duplo), em píxeis por ciclo (-14). */
    private static final int FORCA_SALTO_AEREO = -14;    // salto duplo, ~91 px extra
    /** Velocidade vertical a que o salto é cortado quando se larga a tecla (-5). */
    private static final int CORTE_SALTO = -5;
    /** Impulso vertical ao sofrer dano, em píxeis por ciclo (-8). */
    private static final int FORCA_EMPURRAO = -8;
    /** Velocidade horizontal do empurrão ao sofrer dano, em píxeis por ciclo (5). */
    private static final int VELOCIDADE_EMPURRAO = 5;
    /** Duração do empurrão, em ciclos (12). */
    private static final int FRAMES_EMPURRAO = 12;
    /** Ciclos em que ainda se pode saltar depois de sair de uma plataforma ("coyote time", 6). */
    private static final int FRAMES_COYOTE = 6;
    /** Ciclos em que um salto premido antes de aterrar fica guardado (6). */
    private static final int FRAMES_BUFFER_SALTO = 6;
    /** Duração da invulnerabilidade depois de sofrer dano ou cair, em ciclos (90, cerca de 1,5 s). */
    private static final int FRAMES_INVULNERAVEL = 90;
    /** Velocidade máxima de queda a planar, em píxeis por ciclo (2). */
    private static final int QUEDA_MAXIMA_PLANAR = 2;
    /** Largura da hitbox do agente, em píxeis (24). */
    private static final int LARGURA_COLISAO = 24;
    /** Altura da hitbox de pé, em píxeis (78). */
    private static final int ALTURA_COLISAO = 78;
    /** Altura da hitbox agachado, em píxeis (58). */
    private static final int ALTURA_COLISAO_AGACHADO = 58;
    /** Ciclos de espera entre dois lançamentos de distração (70). */
    private static final int RECARGA_DISTRACAO = 70;     // frames entre lançamentos
    /** Ciclos entre dois ruídos de passos ao correr (14). */
    private static final int INTERVALO_PASSOS = 14;
    /** Velocidade de queda a partir da qual aterrar faz barulho, em píxeis por ciclo (9). */
    private static final int QUEDA_RUIDOSA = 9;          // velocidade de queda que faz barulho ao aterrar

    /** Teclas que controlam este agente. */
    private final ControlosJogador controlos;
    /** Animação de cada estado (só os estados com imagens carregadas). */
    private final Map<EstadoAgente, Animacao> animacoes;
    /** Estado de animação atual. */
    private EstadoAgente estado;
    /** Imagem transparente usada para o efeito de piscar durante a invulnerabilidade. */
    private GreenfootImage imagemInvisivel;

    /** Saltos no ar que ainda pode dar antes de voltar a tocar no chão. */
    private int saltosAereosRestantes;
    /** Ciclos de "coyote time" restantes. */
    private int framesCoyote;
    /** Ciclos restantes do salto guardado no buffer. */
    private int framesBufferSalto;
    /** Estado da tecla de salto no ciclo anterior (para detetar o momento em que é premida). */
    private boolean saltoPremidoAntes;
    /** Estado da tecla de ação no ciclo anterior; começa a true para obrigar a largar a tecla do menu. */
    private boolean acaoPremidaAntes = true;   // obriga a largar a tecla do menu primeiro
    /** true se está agachado (modo furtivo). */
    private boolean agachado;
    /** true se está a planar neste ciclo. */
    private boolean aPlanar;

    /** Coordenada x da última posição segura (onde reaparece se cair). */
    private int xSeguro;
    /** Coordenada y da última posição segura. */
    private int ySeguro;
    /** Ciclos de invulnerabilidade restantes. */
    private int framesInvulneravel;
    /** Ciclos de empurrão restantes. */
    private int framesEmpurrao;
    /** Sentido do empurrão: 1 direita, -1 esquerda. */
    private int direcaoEmpurrao;

    /** Ciclos que faltam para poder lançar outra distração. */
    private int cooldownDistracao;
    /** Contador de ciclos para o ruído dos passos. */
    private int framesPassos;
    /** Animação a mostrar enquanto está ocupado, ou null se estiver livre. */
    private EstadoAgente estadoOcupado;   // null = livre
    /** Ciclos de ocupação restantes; -1 significa até alguém chamar libertar(). */
    private int framesOcupado;            // -1 = até alguém chamar libertar()

    /**
     * Agente com velocidade padrão (overloading). Ver também AgenteInvestigador(int, ControlosJogador).
     *
     * @param controlos Esquema de teclas do agente (ex.: ControlosJogador.wasd())
     */
    public AgenteInvestigador(ControlosJogador controlos)
    {
        this(VELOCIDADE_PADRAO, controlos);
    }

    /**
     * Agente com velocidade horizontal e esquema de teclas próprios.
     *
     * @param velocidade Velocidade horizontal em píxeis por ciclo (ex.: 4)
     * @param controlos Esquema de teclas do agente (ex.: ControlosJogador.setas() no Zag)
     * @throws IllegalArgumentException se controlos for null
     */
    public AgenteInvestigador(int velocidade, ControlosJogador controlos)
    {
        super(velocidade);
        if (controlos == null) {
            throw new IllegalArgumentException("O agente precisa de controlos");
        }
        this.controlos = controlos;
        this.animacoes = new EnumMap<EstadoAgente, Animacao>(EstadoAgente.class);
        this.estado = EstadoAgente.PARADO;
    }

    //métodos abstratos

    /**
     * Contrato: quantos saltos extra pode dar no ar (1 = salto duplo).
     *
     * @return número de saltos aéreos (1 no Zig, 0 no Zag)
     */
    protected abstract int getSaltosAereos();

    /**
     * Contrato: indica se manter o salto premido durante a queda o faz planar.
     *
     * @return true no Zag; false no Zig
     */
    protected abstract boolean podePlanar();

    /**
     * Contrato: nome de código do agente, usado nas mensagens do ecrã.
     *
     * @return "Zig" ou "Zag"
     */
    public abstract String getNomeCodigo();

    /**
     * Contrato: o Zag (ocultismo e tecnologia) consegue hackear terminais.
     *
     * @return true se o agente sabe hackear
     */
    public abstract boolean podeHackear();

    /**
     * Contrato: o Zig (assalto tático) consegue neutralizar cultistas por trás.
     *
     * @return true se o agente sabe neutralizar inimigos
     */
    public abstract boolean podeNeutralizarInimigos();

    /**
     * Contrato: tenta usar a especialidade do agente (neutralizar / hackear).
     * @return true se a usou; false para lançar uma distração em vez disso
     */
    protected abstract boolean usarEspecialidade();

    /**
     * Contrato: cria o projétil de distração próprio deste agente (frasco ou amuleto).
     *
     * @param direcao Sentido do lançamento: 1 direita, -1 esquerda (ex.: getDirecao())
     * @return novo projétil, ainda fora do mundo
     */
    protected abstract ProjectilDistracao criarDistracao(int direcao);

    //  animações

    /**
     * Carrega todas as animações obrigatórias (as não opcionais): prefixo_parado_0.png,
     * prefixo_correr_0.png, ... e mostra o primeiro frame parado.
     *
     * @param prefixo Prefixo dos ficheiros de imagem do agente (ex.: "zig")
     */
    protected final void carregarAnimacoes(String prefixo)
    {
        for (EstadoAgente e : EstadoAgente.values()) {
            if (!e.isOpcional()) {
                carregarAnimacao(prefixo, e);
            }
        }
        GreenfootImage primeiro = animacoes.get(EstadoAgente.PARADO).getFrameAtual(true);
        imagemInvisivel = new GreenfootImage(primeiro.getWidth(), primeiro.getHeight());
        setImage(primeiro);
    }

    /**
     * Carrega a animação de um único estado. Usado por carregarAnimacoes() e pelas subclasses
     * para os estados opcionais (não é overloading: o nome é diferente de carregarAnimacoes).
     *
     * @param prefixo Prefixo dos ficheiros de imagem do agente (ex.: "zag")
     * @param e Estado a carregar (ex.: EstadoAgente.PLANAR)
     */
    protected final void carregarAnimacao(String prefixo, EstadoAgente e)
    {
        animacoes.put(e, new Animacao(prefixo + "_" + e.getNomeFicheiro(), e.getNumeroFrames(), e.getAtrasoFrames()));
    }

    /**
     * Devolve a animação de um estado, ou a de PARADO se esse estado não tiver imagens.
     *
     * @param e Estado pretendido (ex.: EstadoAgente.CORRER)
     * @return animação a usar
     */
    private Animacao getAnimacao(EstadoAgente e)
    {
        Animacao a = animacoes.get(e);
        if (a == null) {
            a = animacoes.get(EstadoAgente.PARADO);
        }
        return a;
    }

    // ciclo principal

    /**
     * Redefine EntidadeAnimada.addedToWorld: além de guardar a posição inicial,
     * marca a posição de entrada como a primeira posição segura.
     *
     * @param world Mundo onde o agente foi colocado (ex.: um Cenario1)
     */
    @Override
    protected void addedToWorld(World world)
    {
        super.addedToWorld(world);
        guardarPosicaoSegura();
    }

    /**
     * Redefine EntidadeAnimada.deslocarReferencias: corrige também a posição segura.
     *
     * @param dx Quanto a câmara avançou para a direita, em píxeis (ex.: 10)
     */
    @Override
    public void deslocarReferencias(int dx)
    {
        super.deslocarReferencias(dx);
        xSeguro -= dx;
    }

    /**
     * Ciclo de jogo do agente, chamado pelo Greenfoot em cada ciclo de execução
     * (redefine EntidadeAnimada.act). A ordem dos passos importa para colisões estáveis:
     * plataforma de apoio, movimento horizontal, salto, gravidade, ruídos, queda, ação e animação.
     */
    @Override
    public void act()
    {
        if (getWorld() == null) {
            return;
        }
        seguirPlataformaApoio();
        atualizarOcupado();

        int direcao = isOcupado() ? 0 : controlos.getDirecaoHorizontal();
        atualizarAgachamento();
        if (framesEmpurrao > 0) {
            framesEmpurrao--;
            moverComColisoes(direcaoEmpurrao * VELOCIDADE_EMPURRAO);
        } else {
            if (direcao != 0) {
                setViradoParaDireita(direcao > 0);
            }
            moverComColisoes(direcao * getVelocidadeAtual());
        }

        boolean estavaNoChao = isNoChao();
        int velocidadeQueda = getVelocidadeVertical();
        processarSalto();
        aplicarFisicaVertical();
        emitirRuidos(direcao, estavaNoChao, velocidadeQueda);
        if (getWorld() == null) {
            return;
        }
        verificarQueda();
        if (isNoChao() && !getPlataformaApoio().isMovel()) {
            guardarPosicaoSegura();
        }
        processarAcao();

        if (framesInvulneravel > 0) {
            framesInvulneravel--;
        }
        if (cooldownDistracao > 0) {
            cooldownDistracao--;
        }
        atualizarEstado(framesEmpurrao > 0 ? 0 : direcao);
        atualizarImagem();
    }

    /** Desconta um ciclo ao tempo de ocupação e liberta o agente quando chega a zero. */
    private void atualizarOcupado()
    {
        if (estadoOcupado != null && framesOcupado > 0) {
            framesOcupado--;
            if (framesOcupado == 0) {
                estadoOcupado = null;
            }
        }
    }

    /** Agachar só no chão; ao largar a tecla só se levanta se houver espaço por cima. */
    private void atualizarAgachamento()
    {
        if (!isOcupado() && controlos.agacharPremido() && isNoChao()) {
            agachado = true;
        } else if (agachado && podeLevantar()) {
            agachado = false;
        }
    }

    /**
     * Verifica se há espaço para se pôr de pé (testa a hitbox de pé e repõe o estado agachado).
     *
     * @return true se a hitbox de pé não intersetar nenhuma plataforma sólida
     */
    private boolean podeLevantar()
    {
        agachado = false;
        boolean livre = !tocaPlataformaSolida();
        agachado = true;
        return livre;
    }

    /**
     * Calcula a velocidade horizontal atual (metade, no mínimo 1, se estiver agachado).
     *
     * @return velocidade em píxeis por ciclo (ex.: 4 de pé, 2 agachado)
     */
    private int getVelocidadeAtual()
    {
        int v = getVelocidadeHorizontal();
        return agachado ? Math.max(1, v / 2) : v;
    }

    /**
     * Trata a tecla de salto: coyote time, buffer, salto aéreo, descer por plataformas
     * atravessáveis (agachado + salto), altura variável e planar.
     */
    private void processarSalto()
    {
        boolean saltoAgora = !isOcupado() && controlos.saltoPremido();
        boolean saltoNovo = saltoAgora && !saltoPremidoAntes;
        saltoPremidoAntes = saltoAgora;

        if (isNoChao()) {
            framesCoyote = FRAMES_COYOTE;
            saltosAereosRestantes = getSaltosAereos();
        } else if (framesCoyote > 0) {
            framesCoyote--;
        }
        if (saltoNovo) {
            framesBufferSalto = FRAMES_BUFFER_SALTO;
        } else if (framesBufferSalto > 0) {
            framesBufferSalto--;
        }

        if (framesBufferSalto > 0) {
            if (agachado && isNoChao() && atravessarPlataformaAtual()) {
                framesBufferSalto = 0;
            } else if (framesCoyote > 0) {
                saltar(FORCA_SALTO);
                Audio.tocar("salto.wav");
                framesCoyote = 0;
                framesBufferSalto = 0;
            } else if (saltoNovo && saltosAereosRestantes > 0) {
                saltar(FORCA_SALTO_AEREO);
                Audio.tocar("salto.wav");
                saltosAereosRestantes--;
                framesBufferSalto = 0;
            }
        }

        if (!saltoAgora && framesEmpurrao == 0 && getVelocidadeVertical() < CORTE_SALTO) {
            setVelocidadeVertical(CORTE_SALTO);
        }
        aPlanar = podePlanar() && saltoAgora && !isNoChao() && getVelocidadeVertical() >= 0;
    }

    /**
     * Emite os ruídos do agente: correr e aterrar com força fazem barulho; agachado ou a planar é silencioso.
     *
     * @param direcao Direção do movimento neste ciclo: -1, 0 ou 1 (ex.: 1 a correr para a direita)
     * @param estavaNoChao true se estava apoiado antes de aplicar a gravidade neste ciclo
     * @param velocidadeQueda Velocidade vertical antes de aterrar, em píxeis por ciclo (ex.: 12 numa queda alta)
     */
    private void emitirRuidos(int direcao, boolean estavaNoChao, int velocidadeQueda)
    {
        Cenario cenario = getCenario();
        if (cenario == null) {
            return;
        }
        if (isNoChao() && direcao != 0 && !agachado) {
            framesPassos++;
            if (framesPassos >= INTERVALO_PASSOS) {
                framesPassos = 0;
                cenario.emitirRuido(getX(), getBaseColisao(), TipoRuido.PASSOS);
            }
        } else {
            framesPassos = INTERVALO_PASSOS / 2;
        }
        if (!estavaNoChao && isNoChao() && velocidadeQueda >= QUEDA_RUIDOSA && !agachado) {
            cenario.emitirRuido(getX(), getBaseColisao(), TipoRuido.ATERRAGEM);
        }
    }

    /** Executa a ação apenas no ciclo em que a tecla de ação é premida (e se o agente estiver livre). */
    private void processarAcao()
    {
        boolean acaoAgora = controlos.acaoPremida();
        if (acaoAgora && !acaoPremidaAntes && !isOcupado()) {
            executarAcao();
        }
        acaoPremidaAntes = acaoAgora;
    }

    /**
     * Ação da tecla E / ENTER (template method): a especialidade tem prioridade,
     * caso contrário lança uma distração. Os passos variáveis (usarEspecialidade e
     * criarDistracao) são definidos por Zig e Zag.
     */
    protected void executarAcao()
    {
        if (!usarEspecialidade()) {
            lancarDistracao();
        }
    }

    /** Lança o projétil de distração na direção em que está virado (com recarga de 70 ciclos). */
    protected void lancarDistracao()
    {
        if (cooldownDistracao > 0 || getWorld() == null) {
            return;
        }
        int direcao = getDirecao();
        ProjectilDistracao projectil = criarDistracao(direcao);
        getWorld().addObject(projectil, getX() + direcao * 18, getTopoColisao() + 18);
        cooldownDistracao = RECARGA_DISTRACAO;
        Audio.tocar("lancar.wav");
        ocupar(EstadoAgente.LANCAR, 10);
    }

    /** Caiu para fora do ecrã: volta à última posição segura (ou para junto do parceiro) e perde sanidade. */
    private void verificarQueda()
    {
        if (!caiuDoMundo()) {
            return;
        }
        int x = xSeguro;
        int y = ySeguro;
        int margem = 30;
        if (x < margem || x > getWorld().getWidth() - margem) {
            AgenteInvestigador parceiro = getParceiro();
            if (parceiro != null) {
                x = parceiro.getX();
                y = parceiro.getY();
            } else {
                x = Math.max(margem, Math.min(getWorld().getWidth() - margem, x));
            }
        }
        setLocation(x, y);
        setVelocidadeVertical(0);
        framesEmpurrao = 0;
        framesInvulneravel = FRAMES_INVULNERAVEL;
        libertar();
        if (getCenario() != null) {
            getCenario().registarQueda(this);
        }
    }

    /**
     * Devolve o outro agente do nível.
     *
     * @return o parceiro (o Zag para o Zig e vice-versa), ou null se não existir
     */
    public AgenteInvestigador getParceiro()
    {
        if (getWorld() == null) {
            return null;
        }
        List<AgenteInvestigador> agentes = getWorld().getObjects(AgenteInvestigador.class);
        for (AgenteInvestigador a : agentes) {
            if (a != this) {
                return a;
            }
        }
        return null;
    }

    /** Guarda a posição atual como última posição segura. */
    private void guardarPosicaoSegura()
    {
        xSeguro = getX();
        ySeguro = getY();
    }

    /**
     * Escolhe o estado de animação a partir da situação do agente e reinicia a animação se mudar.
     *
     * @param direcao Direção do movimento neste ciclo: -1, 0 ou 1 (ex.: 0 parado)
     */
    private void atualizarEstado(int direcao)
    {
        EstadoAgente novo;
        if (estadoOcupado != null) {
            novo = estadoOcupado;
        } else if (!isNoChao()) {
            if (aPlanar && animacoes.containsKey(EstadoAgente.PLANAR)) {
                novo = EstadoAgente.PLANAR;
            } else {
                novo = getVelocidadeVertical() < 0 ? EstadoAgente.SALTAR : EstadoAgente.CAIR;
            }
        } else if (agachado) {
            novo = direcao != 0 ? EstadoAgente.FURTIVO : EstadoAgente.AGACHADO;
        } else {
            novo = direcao != 0 ? EstadoAgente.CORRER : EstadoAgente.PARADO;
        }
        if (novo != estado) {
            estado = novo;
            getAnimacao(estado).reiniciar();
        }
    }

    /** Mostra o frame seguinte da animação; pisca (imagem invisível) enquanto está invulnerável. */
    private void atualizarImagem()
    {
        GreenfootImage frame = getAnimacao(estado).avancar(isViradoParaDireita());
        boolean piscar = framesInvulneravel > 0 && (framesInvulneravel / 4) % 2 == 0;
        setImage(piscar ? imagemInvisivel : frame);
    }

    //  estado ocupado

    /**
     * Fica ocupado (sem responder ao teclado) durante alguns ciclos, com a animação indicada.
     *
     * @param animacao Estado de animação a mostrar enquanto está ocupado (ex.: EstadoAgente.LANCAR)
     * @param frames Duração em ciclos; -1 para ficar ocupado até libertar() (ex.: 10 ao lançar, 24 no golpe do Zig)
     */
    protected void ocupar(EstadoAgente animacao, int frames)
    {
        estadoOcupado = animacao;
        framesOcupado = frames;
    }

    /**
     * Fica ocupado até alguém chamar libertar() (ex.: o terminal de hacking).
     *
     * @param animacao Estado de animação a mostrar (ex.: EstadoAgente.HACKEAR)
     */
    public void ocuparIndefinidamente(EstadoAgente animacao)
    {
        ocupar(animacao, -1);
    }

    /** Volta a responder ao teclado. */
    public void libertar()
    {
        estadoOcupado = null;
        framesOcupado = 0;
    }

    /**
     * Indica se o agente está ocupado (não responde ao teclado).
     *
     * @return true se estiver a lançar, a golpear ou a hackear
     */
    public boolean isOcupado()
    {
        return estadoOcupado != null;
    }

    //  overriding de EntidadeAnimada

    /**
     * Redefine a largura da hitbox: mais estreita do que a imagem.
     *
     * @return 24 píxeis
     */
    @Override
    public int getLarguraColisao()
    {
        return LARGURA_COLISAO;
    }

    /**
     * Redefine a altura da hitbox: mais baixa quando está agachado.
     *
     * @return 58 píxeis agachado, 78 de pé
     */
    @Override
    public int getAlturaColisao()
    {
        return agachado ? ALTURA_COLISAO_AGACHADO : ALTURA_COLISAO;
    }

    /**
     * Redefine a velocidade máxima de queda: muito menor a planar.
     *
     * @return 2 píxeis por ciclo a planar; o valor da superclasse (14) nos outros casos
     */
    @Override
    protected int getVelocidadeQuedaMaxima()
    {
        return aPlanar ? QUEDA_MAXIMA_PLANAR : super.getVelocidadeQuedaMaxima();
    }

    /**
     * Redefine EntidadeAnimada.limitarPosicao: os agentes ficam sempre dentro do ecrã
     * (os dois têm de estar visíveis) e dentro dos limites do nível.
     */
    @Override
    protected void limitarPosicao()
    {
        int esquerda = 0;
        int direita = getWorld().getWidth();
        if (getCenario() != null) {
            esquerda = Math.max(esquerda, getCenario().getLimiteEsquerdo());
            direita = Math.min(direita, getCenario().getLimiteDireito());
        }
        limitarEntre(esquerda, direita);
    }

    /**
     * Redefine o hook de EntidadeAnimada: ao bater numa CaixaPesada enquanto anda no chão,
     * regista que a está a empurrar.
     *
     * @param plataforma Plataforma contra a qual bateu (ex.: o caixote do Cenario1)
     * @param direcao Sentido do movimento: 1 direita, -1 esquerda (ex.: 1)
     */
    @Override
    protected void aoColidirComPlataforma(Plataforma plataforma, int direcao)
    {
        if (plataforma instanceof CaixaPesada && isNoChao()) {
            ((CaixaPesada) plataforma).registarEmpurrao(this, direcao);
        }
    }

    /**
     * Recebe dano (redefine EntidadeAnimada.sofrerDano): é empurrado, salta para trás e fica
     * invulnerável durante 90 ciclos (cerca de 1,5 s). Interrompe o que estava a fazer
     * (ex.: hacking). Avisa o Cenario (sanidade). Ignorado se já estiver invulnerável.
     *
     * @param direcao Sentido do empurrão: -1 esquerda, 1 direita, 0 para o lado oposto ao que está virado
     *                (ex.: 1 quando um orbe vindo da esquerda o atinge)
     */
    @Override
    public void sofrerDano(int direcao)
    {
        if (isInvulneravel() || getWorld() == null) {
            return;
        }
        if (direcao == 0) {
            direcao = isViradoParaDireita() ? -1 : 1;
        }
        direcaoEmpurrao = direcao > 0 ? 1 : -1;
        framesEmpurrao = FRAMES_EMPURRAO;
        framesInvulneravel = FRAMES_INVULNERAVEL;
        agachado = false;
        libertar();
        saltar(FORCA_EMPURRAO);
        if (getCenario() != null) {
            getCenario().registarDano(this);
        }
    }

    //  consultas públicas

    /**
     * Indica se o agente está em modo furtivo. Agachado = modo furtivo (anda devagar e não faz barulho).
     *
     * @return true se estiver agachado
     */
    public boolean isFurtivo()
    {
        return agachado;
    }

    /**
     * Verifica se o agente está escondido: furtivo, com a posição x dentro da parte útil de uma
     * ZonaSombra e os pés dentro da zona. Os inimigos e as câmaras não o veem.
     *
     * @return true se estiver escondido numa sombra
     */
    public boolean isEscondido()
    {
        if (!agachado || getWorld() == null) {
            return false;
        }
        for (ZonaSombra sombra : getWorld().getObjects(ZonaSombra.class)) {
            if (sombra.cobre(getX()) && getBaseColisao() > sombra.getTopo() && getBaseColisao() <= sombra.getBase() + 4) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indica se o agente está invulnerável (depois de sofrer dano ou cair).
     *
     * @return true enquanto houver ciclos de invulnerabilidade
     */
    public boolean isInvulneravel()
    {
        return framesInvulneravel > 0;
    }

    /**
     * Indica se o agente está a planar no ar.
     *
     * @return true se estiver a planar e não estiver no chão
     */
    public boolean isAPlanar()
    {
        return aPlanar && !isNoChao();
    }

    /**
     * Devolve o estado de animação atual.
     *
     * @return estado atual (ex.: EstadoAgente.CORRER)
     */
    public EstadoAgente getEstado()
    {
        return estado;
    }

    /**
     * Devolve o esquema de teclas do agente (o PainelHacking usa-o para ler as teclas do Zag).
     *
     * @return controlos do agente
     */
    public ControlosJogador getControlos()
    {
        return controlos;
    }

    /**
     * Devolve o nome da tecla de ação para mostrar em dicas.
     *
     * @return nome da tecla pronto a mostrar (ex.: "E", "ENTER")
     */
    public String getNomeTeclaAcao()
    {
        return ControlosJogador.nomeTecla(controlos.getTeclaAcao());
    }

    /**
     * Devolve os ciclos que faltam para poder lançar outra distração.
     *
     * @return ciclos de recarga restantes (0 se já puder lançar)
     */
    public int getCooldownDistracao()
    {
        return cooldownDistracao;
    }
}

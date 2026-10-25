import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Classe base ABSTRATA de todos os inimigos (Membro 2).
 * Hierarquia: Actor -> EntidadeAnimada -> InimigoCosmico -> CultistaPatrulheiro -> CultistaInvocador
 *                                                        -> CriaturaAbissal
 *
 * O ciclo act() é um "template method" igual para todos os inimigos:
 *   seguir plataforma -> (atordoado? esperar) -> executarComportamento() -> gravidade ->
 *   contacto com os agentes -> animação.
 * Cada subclasse redefine (overriding):
 *   - executarComportamento(): o que faz em cada estado (patrulhar, perseguir, ...);
 *   - reagirADistracao(x, y, tipo): como reage a ruídos e distrações;
 *   - atualizarAnimacao(): que imagens mostra.
 *
 * Rota pré-definida: anda entre a posição inicial e (posição inicial + limite), parando
 * um pouco em cada ponta. Nunca cai de plataformas: vira-se na borda.
 * Contacto com um agente causa dano (o agente perde sanidade e é empurrado).
 *
 * Conceitos de POO: classe abstrata com métodos abstratos, template method (act), herança
 * (InimigoCosmico estende EntidadeAnimada, que estende Actor), overriding (act, sofrerDano,
 * deslocarReferencias) e métodos hook (causaDanoPorContacto, aoTocarAgente, aoSerNeutralizado),
 * encapsulamento (atributos privados com getters/setters protegidos).
 */
public abstract class InimigoCosmico extends EntidadeAnimada
{
    /** Ciclos de pausa em cada ponta da rota de patrulha (50). */
    private static final int PAUSA_NAS_PONTAS = 50;
    /** Ciclos que um inimigo neutralizado demora a desaparecer (120). */
    private static final int FRAMES_DESVANECER = 120;

    /** Comprimento da rota de patrulha para a direita da posição inicial, em píxeis (ex.: 250). */
    private final int limitePatrulha;
    /** Estado atual da máquina de estados. */
    private EstadoInimigo estado;
    /** Ciclos desde que entrou no estado atual. */
    private int temporizador;          // frames desde que entrou no estado atual
    /** Coordenada x para onde vai quando investiga ou persegue. */
    private int pontoAlvoX;            // para onde vai quando investiga/persegue
    /** Ciclos de atordoamento restantes. */
    private int framesAtordoado;
    /** Ciclos de pausa restantes numa ponta da rota. */
    private int framesPausa;
    /** Sentido atual da patrulha: 1 direita, -1 esquerda. */
    private int direcaoPatrulha;
    /** Balão de estado mostrado por cima da cabeça ("?", "!"...), ou null. */
    private Balao balaoEstado;

    /**
     * Cria um inimigo em PATRULHA, virado para a direita.
     *
     * @param limite     comprimento da rota de patrulha para a direita da posição inicial, em píxeis;
     *                   valores negativos passam a 0 (ex.: 250 no primeiro cultista do Cenario1)
     * @param velocidade velocidade de patrulha (píxeis por frame) (ex.: 1)
     */
    public InimigoCosmico(int limite, int velocidade)
    {
        super(velocidade);
        this.limitePatrulha = Math.max(0, limite);
        this.estado = EstadoInimigo.PATRULHA;
        this.direcaoPatrulha = 1;
    }

    //  métodos abstratos

    /** Contrato: comportamento específico de cada tipo de inimigo, chamado uma vez por ciclo (exceto atordoado). */
    protected abstract void executarComportamento();

    /**
     * Contrato: reação a um ruído ou distração no ponto (x, y).
     * Polimorfismo: o cultista vai investigar, a criatura cega ataca o som...
     *
     * @param x Coordenada x da origem do ruído, em píxeis (ex.: a posição de um frasco partido)
     * @param y Coordenada y da origem do ruído, em píxeis (ex.: 516)
     * @param tipo Tipo de ruído ouvido (ex.: TipoRuido.FRASCO)
     */
    public abstract void reagirADistracao(int x, int y, TipoRuido tipo);

    /** Contrato: escolhe e mostra a imagem certa para o estado atual. */
    protected abstract void atualizarAnimacao();

    //  ciclo principal (template)

    /**
     * Ciclo do inimigo, chamado pelo Greenfoot em cada ciclo de execução (redefine EntidadeAnimada.act).
     * É o template method comum a todos os inimigos: neutralizado desvanece; atordoado espera;
     * caso contrário executa o comportamento da subclasse; depois gravidade, contacto e animação.
     */
    @Override
    public void act()
    {
        if (getWorld() == null) {
            return;
        }
        if (estado == EstadoInimigo.NEUTRALIZADO) {
            desvanecer();
            return;
        }
        temporizador++;
        seguirPlataformaApoio();
        if (estado == EstadoInimigo.ATORDOADO) {
            framesAtordoado--;
            if (framesAtordoado <= 0) {
                mudarEstado(EstadoInimigo.INVESTIGAR);
                setPontoAlvoX(getX());
            }
        } else {
            executarComportamento();
        }
        aplicarFisicaVertical();
        if (caiuDoMundo()) {
            removerDoMundo();
            return;
        }
        verificarContactoComAgentes();
        atualizarAnimacao();
    }

    /**
     * Chamado pelo Cenario quando há um ruído ao alcance (ignorado se estiver fora de combate).
     * É final: as subclasses redefinem reagirADistracao, não este método.
     *
     * @param x Coordenada x da origem do ruído (ex.: getX() do agente que correu)
     * @param y Coordenada y da origem do ruído (ex.: getBaseColisao() do agente)
     * @param tipo Tipo de ruído (ex.: TipoRuido.PASSOS)
     */
    public final void ouvirRuido(int x, int y, TipoRuido tipo)
    {
        if (estado != EstadoInimigo.NEUTRALIZADO && estado != EstadoInimigo.ATORDOADO) {
            reagirADistracao(x, y, tipo);
        }
    }

    /**
     * Fica atordoado (parado) durante alguns ciclos, por exemplo ao ser atingido em cheio por um frasco.
     * Mostra o balão "@#!".
     *
     * @param frames Duração do atordoamento em ciclos (ex.: 110 com um frasco de éter, 60 ao sofrer dano)
     */
    public void atordoar(int frames)
    {
        if (estado == EstadoInimigo.NEUTRALIZADO) {
            return;
        }
        framesAtordoado = Math.max(framesAtordoado, frames);
        mudarEstado(EstadoInimigo.ATORDOADO);
        mostrarBalao("@#!", new Color(120, 90, 200), frames);
    }

    /**
     * Inimigos não perdem vida: um "dano" deixa-os apenas atordoados durante 60 ciclos
     * (overriding de EntidadeAnimada.sofrerDano).
     *
     * @param direcao Sentido do golpe; ignorado pelos inimigos (ex.: 1)
     */
    @Override
    public void sofrerDano(int direcao)
    {
        atordoar(60);
    }

    //  movimento reutilizável

    /**
     * Anda um passo na direção indicada, sem cair de plataformas.
     *
     * @param direcao Sentido: 1 direita, -1 esquerda, 0 não anda (ex.: 1)
     * @param velocidade Píxeis a andar neste ciclo (ex.: 1 em patrulha, 3 a perseguir)
     * @return false se não conseguiu (borda ou parede)
     */
    protected boolean andar(int direcao, int velocidade)
    {
        if (direcao == 0) {
            return true;
        }
        setViradoParaDireita(direcao > 0);
        if (!isNoChao()) {
            return true;
        }
        int pontaPe = direcao > 0 ? getDireitaColisao() + velocidade : getEsquerdaColisao() - velocidade;
        if (!haChaoEm(pontaPe)) {
            return false;
        }
        return !moverComColisoes(direcao * velocidade);
    }

    /**
     * Patrulha a rota pré-definida, com pausa de 50 ciclos em cada ponta (ou ao encontrar uma borda/parede).
     *
     * @param velocidade Velocidade de patrulha em píxeis por ciclo (ex.: 1)
     */
    protected void patrulhar(int velocidade)
    {
        if (framesPausa > 0) {
            framesPausa--;
            if (framesPausa == 0) {
                direcaoPatrulha = -direcaoPatrulha;
            }
            return;
        }
        int inicio = getXinicial();
        int fim = getXinicial() + limitePatrulha;
        boolean chegouAoFim = (direcaoPatrulha > 0 && getX() >= fim) || (direcaoPatrulha < 0 && getX() <= inicio);
        if (chegouAoFim || !andar(direcaoPatrulha, velocidade)) {
            framesPausa = PAUSA_NAS_PONTAS;
        }
    }

    /**
     * Anda em direção a x.
     *
     * @param x Coordenada x de destino, em píxeis (ex.: getPontoAlvoX() ou getXinicial())
     * @param velocidade Píxeis por ciclo (ex.: 3 quando a criatura caça um som)
     * @return true quando chegou (ou não consegue avançar mais)
     */
    protected boolean irPara(int x, int velocidade)
    {
        int distancia = x - getX();
        if (Math.abs(distancia) <= velocidade) {
            return true;
        }
        return !andar(distancia > 0 ? 1 : -1, velocidade);
    }

    /** Causa dano por contacto a qualquer agente que esteja a tocar e não esteja invulnerável. */
    private void verificarContactoComAgentes()
    {
        if (!causaDanoPorContacto()) {
            return;
        }
        List<AgenteInvestigador> agentes = getWorld().getObjects(AgenteInvestigador.class);
        for (AgenteInvestigador agente : agentes) {
            if (tocaEm(agente) && !agente.isInvulneravel()) {
                agente.sofrerDano(agente.getX() < getX() ? -1 : 1);
                aoTocarAgente(agente);
            }
        }
    }

    /**
     * Hook: só causa dano se estiver ativo (overriding possível).
     *
     * @return true se não estiver atordoado nem neutralizado
     */
    protected boolean causaDanoPorContacto()
    {
        return estado != EstadoInimigo.ATORDOADO && estado != EstadoInimigo.NEUTRALIZADO;
    }

    /**
     * Hook: o que fazer depois de ferir um agente (por omissão nada; o cultista entra em ALERTA).
     *
     * @param agente Agente que foi ferido (ex.: o Zig)
     */
    protected void aoTocarAgente(AgenteInvestigador agente)
    {
    }

    //  estado e balões

    /**
     * Muda de estado e reinicia o temporizador (não faz nada se já estiver nesse estado).
     *
     * @param novo Novo estado (ex.: EstadoInimigo.INVESTIGAR)
     */
    protected void mudarEstado(EstadoInimigo novo)
    {
        if (novo != estado) {
            estado = novo;
            temporizador = 0;
        }
    }

    /**
     * Mostra um balão por cima da cabeça (substitui o anterior).
     *
     * @param texto Texto do balão (ex.: "?")
     * @param cor Cor do texto (ex.: new Color(200, 160, 40) para suspeita)
     * @param duracao Duração em ciclos (ex.: 50)
     */
    protected void mostrarBalao(String texto, Color cor, int duracao)
    {
        esconderBalao();
        if (getWorld() != null) {
            balaoEstado = new Balao(this, texto, cor, duracao);
            getWorld().addObject(balaoEstado, getX(), getY());
        }
    }

    /** Remove o balão de estado, se existir. */
    protected void esconderBalao()
    {
        if (balaoEstado != null && balaoEstado.getWorld() != null) {
            balaoEstado.getWorld().removeObject(balaoEstado);
        }
        balaoEstado = null;
    }

    /** Passa a NEUTRALIZADO: deixa de agir, mostra "z Z z" e desaparece aos poucos. */
    protected void ficarNeutralizado()
    {
        mudarEstado(EstadoInimigo.NEUTRALIZADO);
        mostrarBalao("z Z z", new Color(60, 60, 90), FRAMES_DESVANECER);
        aoSerNeutralizado();
        atualizarAnimacao();
    }

    /** Hook: limpeza extra quando é neutralizado ou removido (ex.: retirar o cone de visão). */
    protected void aoSerNeutralizado()
    {
    }

    /** Torna a imagem cada vez mais transparente e remove o inimigo ao fim de 120 ciclos. */
    private void desvanecer()
    {
        temporizador++;
        int alfa = 255 - temporizador * 255 / FRAMES_DESVANECER;
        if (alfa <= 0) {
            removerDoMundo();
        } else {
            getImage().setTransparency(alfa);
        }
    }

    /** Sai do mundo, levando consigo os efeitos associados (balão, cone de visão). */
    protected void removerDoMundo()
    {
        esconderBalao();
        aoSerNeutralizado();
        if (getWorld() != null) {
            getWorld().removeObject(this);
        }
    }

    /**
     * Redefine EntidadeAnimada.deslocarReferencias: corrige também o ponto alvo.
     *
     * @param dx Quanto a câmara avançou para a direita, em píxeis (ex.: 10)
     */
    @Override
    public void deslocarReferencias(int dx)
    {
        super.deslocarReferencias(dx);
        pontoAlvoX -= dx;
    }

    /**
     * Carrega n frames prefixo_0.png ... (ajuda para as subclasses).
     *
     * @param prefixo Início do nome dos ficheiros (ex.: "criatura_rastejar")
     * @param frames Número de frames (ex.: 4)
     * @param atraso Ciclos que cada frame fica visível (ex.: 8)
     * @return nova animação
     */
    protected static Animacao carregarAnimacao(String prefixo, int frames, int atraso)
    {
        return new Animacao(prefixo, frames, atraso);
    }

    /**
     * Mostra o frame seguinte da animação, virado para o lado certo.
     *
     * @param animacao Animação a avançar (ex.: animAndar de um cultista)
     */
    protected void mostrar(Animacao animacao)
    {
        setImage(animacao.avancar(isViradoParaDireita()));
    }

    // getters / setters

    /**
     * Devolve o estado atual.
     *
     * @return estado da máquina de estados (ex.: EstadoInimigo.PATRULHA)
     */
    public EstadoInimigo getEstado()
    {
        return estado;
    }

    /**
     * Indica se o inimigo foi neutralizado.
     *
     * @return true se estiver no estado NEUTRALIZADO
     */
    public boolean isNeutralizado()
    {
        return estado == EstadoInimigo.NEUTRALIZADO;
    }

    /**
     * Devolve os ciclos passados no estado atual.
     *
     * @return valor do temporizador em ciclos
     */
    protected int getTemporizador()
    {
        return temporizador;
    }

    /**
     * Define o temporizador do estado atual (valores negativos passam a 0).
     *
     * @param frames Novo valor em ciclos (ex.: 0)
     */
    protected void setTemporizador(int frames)
    {
        temporizador = Math.max(0, frames);
    }

    /**
     * Devolve a coordenada x para onde o inimigo vai quando investiga ou persegue.
     *
     * @return x do ponto alvo, em píxeis do ecrã
     */
    public int getPontoAlvoX()
    {
        return pontoAlvoX;
    }

    /**
     * Define a coordenada x para onde o inimigo deve ir.
     *
     * @param x Coordenada x alvo, em píxeis (ex.: visto.getX(), a posição do agente avistado)
     */
    protected void setPontoAlvoX(int x)
    {
        pontoAlvoX = x;
    }

    /**
     * Devolve o comprimento da rota de patrulha.
     *
     * @return limite em píxeis (ex.: 250)
     */
    public int getLimitePatrulha()
    {
        return limitePatrulha;
    }

    /**
     * Indica se está parado numa ponta da rota.
     *
     * @return true durante a pausa de patrulha
     */
    protected boolean isEmPausa()
    {
        return framesPausa > 0;
    }
}

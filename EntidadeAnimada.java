import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;
import java.util.List;

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Classe base ABSTRATA de tudo o que se move no jogo (agentes e inimigos).
 * Nível 1 da hierarquia além de Actor:  Actor -> EntidadeAnimada -> AgenteInvestigador / InimigoCosmico.
 *
 * Responsabilidades (reutilizadas pelas subclasses, sem duplicar código):
 *  - velocidade horizontal/vertical e direção para onde está virada;
 *  - caixa de colisão (hitbox) e colisões precisas com Plataformas (chão, paredes e teto);
 *  - gravidade, transporte por plataformas móveis e limites do nível;
 *  - acompanhar o scroll da câmara (interface Deslocavel).
 *
 * Conceitos de POO: classe abstrata, implementação de interface (Deslocavel), encapsulamento
 * (atributos privados com getters/setters validados), overloading (construtores e sofrerDano),
 * overriding (addedToWorld, e métodos "hook" que as subclasses redefinem).
 */
public abstract class EntidadeAnimada extends Actor implements Deslocavel
{
    /** Velocidade horizontal usada pelo construtor sem argumentos, em píxeis por ciclo (4). */
    protected static final int VELOCIDADE_PADRAO = 4;
    /** Aceleração da gravidade, em píxeis por ciclo somados à velocidade vertical (1). */
    private static final int GRAVIDADE = 1;
    /** Velocidade máxima de queda, em píxeis por ciclo (14). */
    private static final int VELOCIDADE_QUEDA_MAXIMA = 14;
    /** Limite superior aceite por setVelocidadeHorizontal, em píxeis por ciclo (20). */
    private static final int VELOCIDADE_HORIZONTAL_MAXIMA = 20;
    /** Ciclos durante os quais a plataforma atravessada é ignorada ao descer por ela (12). */
    private static final int FRAMES_ATRAVESSAR = 12;

    // Encapsulamento: todos os atributos são privados, acesso só por getters/setters validados.
    /** Velocidade horizontal em píxeis por ciclo (ex.: 4). */
    private int velocidadeHorizontal;
    /** Velocidade vertical em píxeis por ciclo; negativa a subir, positiva a cair. */
    private int velocidadeVertical;
    /** Coordenada x onde foi colocada no mundo (corrigida quando a câmara se move). */
    private int xInicial;
    /** Coordenada y onde foi colocada no mundo. */
    private int yInicial;
    /** true se está virada para a direita; false se para a esquerda. */
    private boolean viradoParaDireita;
    /** true se no último ciclo ficou apoiada numa plataforma. */
    private boolean noChao;
    /** Plataforma onde está apoiada, ou null se estiver no ar. */
    private Plataforma plataformaApoio;
    /** Plataforma atravessável pela qual está a descer (ignorada durante alguns ciclos). */
    private Plataforma plataformaIgnorada;
    /** Ciclos que faltam para voltar a considerar a plataformaIgnorada. */
    private int framesIgnorar;

    /**
     * Construtor por omissão (overloading): usa a velocidade padrão (4 píxeis por ciclo).
     * Ver também EntidadeAnimada(int velocidadeHorizontal).
     */
    public EntidadeAnimada()
    {
        this(VELOCIDADE_PADRAO);
    }

    /**
     * Construtor com velocidade horizontal (overloading). Começa parada na vertical e virada para a direita.
     *
     * @param velocidadeHorizontal Velocidade horizontal em píxeis por ciclo, limitada a 0..20
     *                             (ex.: 4 nos agentes, 1 na patrulha de um cultista)
     */
    public EntidadeAnimada(int velocidadeHorizontal)
    {
        setVelocidadeHorizontal(velocidadeHorizontal);
        this.velocidadeVertical = 0;
        this.viradoParaDireita = true;
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo de execução. Redefine Actor.act() sem fazer nada:
     * cada subclasse define o seu comportamento (overriding).
     */
    public void act()
    {
        // Cada subclasse define o seu comportamento (overriding).
    }

    /**
     * Guarda a posição inicial quando a entidade é colocada no mundo (overriding de Actor.addedToWorld).
     *
     * @param world Mundo onde a entidade foi colocada (ex.: um Cenario1)
     */
    @Override
    protected void addedToWorld(World world)
    {
        this.xInicial = getX();
        this.yInicial = getY();
    }

    /**
     * A câmara moveu-se dx píxeis: as coordenadas guardadas também têm de se mover.
     * Implementa Deslocavel.deslocarReferencias (corrige xInicial).
     *
     * @param dx Quanto a câmara avançou para a direita, em píxeis (ex.: 10)
     */
    @Override
    public void deslocarReferencias(int dx)
    {
        xInicial -= dx;
    }

    // movimento simples

    /**
     * Move a entidade sem verificar colisões.
     * @param direcaoX -1 esquerda, 0 parado, 1 direita (multiplicado pela velocidade) (ex.: 1)
     * @param deltaY   deslocamento vertical em píxeis (ex.: 0)
     */
    public void mover(int direcaoX, int deltaY)
    {
        if (direcaoX != 0) {
            viradoParaDireita = direcaoX > 0;
        }
        setLocation(getX() + direcaoX * velocidadeHorizontal, getY() + deltaY);
    }

    /**
     * Nome original mantido por compatibilidade com o código antigo do grupo: chama mover().
     * Atualmente não é chamado em nenhuma classe do projeto.
     *
     * @param direcaoX -1 esquerda, 0 parado, 1 direita (ex.: 1)
     * @param deltaY Deslocamento vertical em píxeis (ex.: 0)
     */
    public void moving(int direcaoX, int deltaY)
    {
        mover(direcaoX, deltaY);
    }

    /**
     * Vira a entidade para o ponto x indicado (não altera a posição).
     *
     * @param x Coordenada x para onde olhar, em píxeis do ecrã (ex.: agente.getX() de um agente avistado)
     */
    public void virarPara(int x)
    {
        if (x != getX()) {
            viradoParaDireita = x > getX();
        }
    }

    //caixa de colisão

    /**
     * Devolve a largura da hitbox. Por omissão é a largura da imagem; as subclasses podem reduzir.
     *
     * @return largura da caixa de colisão em píxeis
     */
    public int getLarguraColisao()
    {
        return getImage().getWidth();
    }

    /**
     * Devolve a altura da hitbox. Por omissão é a altura da imagem (os agentes reduzem ao agachar).
     *
     * @return altura da caixa de colisão em píxeis
     */
    public int getAlturaColisao()
    {
        return getImage().getHeight();
    }

    /**
     * Devolve a linha dos pés: a parte de baixo da imagem.
     *
     * @return coordenada y da base da hitbox, em píxeis
     */
    public int getBaseColisao()
    {
        return getY() + getImage().getHeight() / 2;
    }

    /**
     * Devolve o topo da hitbox (a base menos a altura de colisão).
     *
     * @return coordenada y do topo da hitbox, em píxeis
     */
    public int getTopoColisao()
    {
        return getBaseColisao() - getAlturaColisao();
    }

    /**
     * Devolve o lado esquerdo da hitbox.
     *
     * @return coordenada x do lado esquerdo da hitbox, em píxeis
     */
    public int getEsquerdaColisao()
    {
        return getX() - getLarguraColisao() / 2;
    }

    /**
     * Devolve o lado direito da hitbox.
     *
     * @return coordenada x do lado direito da hitbox, em píxeis
     */
    public int getDireitaColisao()
    {
        return getEsquerdaColisao() + getLarguraColisao();
    }

    /**
     * Devolve o ponto central (vertical) da hitbox (útil para a visão dos inimigos).
     *
     * @return coordenada y do centro da hitbox, em píxeis
     */
    public int getCentroY()
    {
        return (getTopoColisao() + getBaseColisao()) / 2;
    }

    /**
     * Verifica se as hitboxes das duas entidades se sobrepõem.
     *
     * @param outra Entidade com que testar a colisão (ex.: um AgenteInvestigador tocado por um inimigo)
     * @return true se as caixas de colisão se intersetam; false caso contrário
     */
    public boolean tocaEm(EntidadeAnimada outra)
    {
        return getEsquerdaColisao() < outra.getDireitaColisao() && outra.getEsquerdaColisao() < getDireitaColisao()
            && getTopoColisao() < outra.getBaseColisao() && outra.getTopoColisao() < getBaseColisao();
    }

    /**
     * Verifica se a hitbox se sobrepõe à plataforma no eixo horizontal.
     *
     * @param p Plataforma a testar (ex.: uma parede de METAL)
     * @return true se os intervalos em x se intersetam
     */
    private boolean sobrepoeNaHorizontal(Plataforma p)
    {
        return getEsquerdaColisao() < p.getDireita() && getDireitaColisao() > p.getEsquerda();
    }

    /**
     * Verifica se a hitbox se sobrepõe à plataforma no eixo vertical.
     *
     * @param p Plataforma a testar (ex.: uma parede de METAL)
     * @return true se os intervalos em y se intersetam
     */
    private boolean sobrepoeNaVertical(Plataforma p)
    {
        return getBaseColisao() > p.getTopo() && getTopoColisao() < p.getBase();
    }

    /**
     * Verifica se a hitbox atual interseta alguma plataforma sólida
     * (usado, por exemplo, para saber se um agente agachado se pode levantar).
     *
     * @return true se houver sobreposição com uma plataforma sólida; false caso contrário
     */
    protected boolean tocaPlataformaSolida()
    {
        for (Plataforma p : getPlataformas()) {
            if (p.isSolida() && sobrepoeNaHorizontal(p) && sobrepoeNaVertical(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Devolve as plataformas que contam para colisões neste momento (ignora, por exemplo,
     * portões totalmente abertos e plataformas frágeis desfeitas).
     *
     * @return lista nova com as plataformas colidíveis do mundo
     */
    protected List<Plataforma> getPlataformas()
    {
        List<Plataforma> todas = getWorld().getObjects(Plataforma.class);
        List<Plataforma> ativas = new ArrayList<Plataforma>(todas.size());
        for (Plataforma p : todas) {
            if (p.isColidivel()) {
                ativas.add(p);
            }
        }
        return ativas;
    }

    /**
     * Verifica se há chão (topo de alguma plataforma) exatamente debaixo do ponto x, à altura dos pés
     * (tolerância de 4 píxeis). Usado pelos inimigos para não caírem das bordas.
     *
     * @param x Coordenada x a testar, em píxeis (ex.: getDireitaColisao() + velocidade, a ponta do pé)
     * @return true se houver uma plataforma por baixo desse ponto; false caso contrário
     */
    protected boolean haChaoEm(int x)
    {
        int base = getBaseColisao();
        for (Plataforma p : getPlataformas()) {
            if (x >= p.getEsquerda() && x <= p.getDireita() && Math.abs(p.getTopo() - base) <= 4) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ física com colisões

    /** Se esteve apoiada numa plataforma móvel, acompanha o deslocamento dessa plataforma no último ciclo. */
    protected void seguirPlataformaApoio()
    {
        if (plataformaApoio != null && plataformaApoio.getWorld() != null && plataformaApoio.isMovel()) {
            setLocation(getX() + plataformaApoio.getDeltaX(), getY() + plataformaApoio.getDeltaY());
        }
    }

    /**
     * Move na horizontal e resolve colisões com paredes (plataformas sólidas) e com os limites do nível.
     * @param dx deslocamento em píxeis (positivo = direita) (ex.: 4 a correr, 2 agachado, 5 a ser empurrado)
     * @return true se foi travada por alguma parede ou pelos limites
     */
    protected boolean moverComColisoes(int dx)
    {
        boolean bloqueado = false;
        setLocation(getX() + dx, getY());
        for (Plataforma p : getPlataformas()) {
            if (p.isSolida() && sobrepoeNaHorizontal(p) && sobrepoeNaVertical(p)) {
                if (getX() <= p.getX()) {
                    setLocation(getX() - (getDireitaColisao() - p.getEsquerda()), getY());
                } else {
                    setLocation(getX() + (p.getDireita() - getEsquerdaColisao()), getY());
                }
                if (dx != 0) {
                    bloqueado = true;
                    aoColidirComPlataforma(p, dx > 0 ? 1 : -1);
                }
            }
        }
        int xAntes = getX();
        limitarPosicao();
        return bloqueado || getX() != xAntes;
    }

    /**
     * Hook (overriding): chamado quando a entidade bate numa parede ao andar. Por omissão não faz nada;
     * o AgenteInvestigador redefine-o para empurrar caixas pesadas.
     *
     * @param plataforma Plataforma sólida contra a qual bateu (ex.: uma CaixaPesada)
     * @param direcao Sentido do movimento: 1 para a direita, -1 para a esquerda (ex.: 1)
     */
    protected void aoColidirComPlataforma(Plataforma plataforma, int direcao)
    {
    }

    /**
     * Impede que a hitbox saia dos limites do nível. Num Cenario com scroll os limites
     * mudam com a câmara; os agentes redefinem este método para ficarem dentro do ecrã.
     */
    protected void limitarPosicao()
    {
        int esquerda = 0;
        int direita = getWorld().getWidth();
        if (getWorld() instanceof Cenario) {
            Cenario cenario = (Cenario) getWorld();
            esquerda = cenario.getLimiteEsquerdo();
            direita = cenario.getLimiteDireito();
        }
        limitarEntre(esquerda, direita);
    }

    /**
     * Mantém a hitbox entre as duas coordenadas x indicadas. É o passo comum usado por
     * limitarPosicao() e pela sua versão redefinida em AgenteInvestigador
     * (não é overloading: os nomes são diferentes).
     *
     * @param esquerda Coordenada x mínima do lado esquerdo da hitbox, em píxeis (ex.: 0)
     * @param direita Coordenada x máxima do lado direito da hitbox, em píxeis (ex.: 1024, a largura do ecrã)
     */
    protected void limitarEntre(int esquerda, int direita)
    {
        if (getEsquerdaColisao() < esquerda) {
            setLocation(getX() + (esquerda - getEsquerdaColisao()), getY());
        }
        if (getDireitaColisao() > direita) {
            setLocation(getX() - (getDireitaColisao() - direita), getY());
        }
    }

    /**
     * Aplica a gravidade e resolve colisões verticais:
     *  - a cair: aterra no topo de qualquer plataforma (sólida ou atravessável);
     *  - a subir: bate com a cabeça apenas em plataformas sólidas.
     */
    protected void aplicarFisicaVertical()
    {
        int baseAnterior = getBaseColisao();
        int topoAnterior = getTopoColisao();

        velocidadeVertical = Math.min(velocidadeVertical + getGravidade(), getVelocidadeQuedaMaxima());
        setLocation(getX(), getY() + velocidadeVertical);

        noChao = false;
        plataformaApoio = null;
        if (framesIgnorar > 0) {
            framesIgnorar--;
        }

        if (velocidadeVertical >= 0) {
            Plataforma chao = procurarChao(baseAnterior);
            if (chao != null) {
                setLocation(getX(), getY() - (getBaseColisao() - chao.getTopo()));
                velocidadeVertical = 0;
                noChao = true;
                plataformaApoio = chao;
            }
        } else {
            Plataforma teto = procurarTeto(topoAnterior);
            if (teto != null) {
                setLocation(getX(), getY() + (teto.getBase() - getTopoColisao()));
                velocidadeVertical = 0;
            }
        }
    }

    /**
     * Procura a plataforma mais alta cujo topo foi atravessado pelos pés neste ciclo.
     *
     * @param baseAnterior Coordenada y dos pés antes do movimento vertical deste ciclo
     *                     (ex.: 520, o chão do Cenario1)
     * @return plataforma onde aterrar, ou null se não atravessou nenhum topo
     */
    private Plataforma procurarChao(int baseAnterior)
    {
        Plataforma melhor = null;
        for (Plataforma p : getPlataformas()) {
            if (p == plataformaIgnorada && framesIgnorar > 0) {
                continue;
            }
            int tolerancia = Math.abs(p.getDeltaY()) + 1;
            boolean atravessouTopo = baseAnterior <= p.getTopo() + tolerancia && getBaseColisao() >= p.getTopo();
            if (atravessouTopo && sobrepoeNaHorizontal(p)) {
                if (melhor == null || p.getTopo() < melhor.getTopo()) {
                    melhor = p;
                }
            }
        }
        return melhor;
    }

    /**
     * Procura a plataforma sólida mais baixa contra a qual a cabeça bateu neste ciclo.
     *
     * @param topoAnterior Coordenada y do topo da hitbox antes do movimento vertical deste ciclo
     *                     (ex.: 442 para um agente de pé no chão a y 520)
     * @return plataforma que serve de teto, ou null se não bateu em nenhuma
     */
    private Plataforma procurarTeto(int topoAnterior)
    {
        Plataforma melhor = null;
        for (Plataforma p : getPlataformas()) {
            if (!p.isSolida()) {
                continue;
            }
            boolean atravessouBase = topoAnterior >= p.getBase() - 1 && getTopoColisao() < p.getBase();
            if (atravessouBase && sobrepoeNaHorizontal(p)) {
                if (melhor == null || p.getBase() > melhor.getBase()) {
                    melhor = p;
                }
            }
        }
        return melhor;
    }

    /**
     * Dá um impulso vertical e deixa de estar apoiada.
     *
     * @param forca Velocidade vertical inicial em píxeis por ciclo, negativa = para cima
     *              (ex.: -17 no salto normal, -14 no salto aéreo, -8 ao ser empurrado)
     */
    protected void saltar(int forca)
    {
        velocidadeVertical = forca;
        noChao = false;
        plataformaApoio = null;
    }

    /**
     * Deixa-se cair através da plataforma atravessável onde está (ex.: tábuas de madeira).
     * @return true se conseguiu (plataformas sólidas não deixam)
     */
    protected boolean atravessarPlataformaAtual()
    {
        if (plataformaApoio == null || plataformaApoio.isSolida()) {
            return false;
        }
        plataformaIgnorada = plataformaApoio;
        framesIgnorar = FRAMES_ATRAVESSAR;
        noChao = false;
        plataformaApoio = null;
        return true;
    }

    /**
     * Verifica se caiu para baixo do ecrã (mar ou abismo).
     *
     * @return true se o topo da hitbox já passou o fundo do mundo
     */
    protected boolean caiuDoMundo()
    {
        return getTopoColisao() > getWorld().getHeight();
    }

    /**
     * Devolve o Cenario onde está.
     *
     * @return o mundo atual convertido em Cenario, ou null se estiver noutro tipo de mundo
     */
    protected Cenario getCenario()
    {
        return (getWorld() instanceof Cenario) ? (Cenario) getWorld() : null;
    }

    //dano (polimorfismo)

    /**
     * Sofre dano sem direção definida. Versão sem parâmetros (overloading):
     * equivale a sofrerDano(0). Ver também sofrerDano(int direcaoEmpurrao).
     */
    public void sofrerDano()
    {
        sofrerDano(0);
    }

    /**
     * Sofre dano vindo de uma direção (overloading). Por omissão não faz nada:
     * cada subclasse decide como reage (overriding em AgenteInvestigador e InimigoCosmico).
     * @param direcaoEmpurrao -1 empurra para a esquerda, 1 para a direita, 0 automático
     *                        (ex.: -1 quando um inimigo toca num agente que está à sua esquerda)
     */
    public void sofrerDano(int direcaoEmpurrao)
    {
    }

    // getters / setters

    /**
     * Hook (overriding): permite a uma subclasse ter gravidade diferente.
     *
     * @return aceleração vertical em píxeis por ciclo (1 por omissão)
     */
    protected int getGravidade()
    {
        return GRAVIDADE;
    }

    /**
     * Hook (overriding): o Zag reduz este valor quando plana.
     *
     * @return velocidade máxima de queda em píxeis por ciclo (14 por omissão)
     */
    protected int getVelocidadeQuedaMaxima()
    {
        return VELOCIDADE_QUEDA_MAXIMA;
    }

    /**
     * Devolve a velocidade horizontal.
     *
     * @return velocidade em píxeis por ciclo (ex.: 4)
     */
    public int getVelocidadeHorizontal()
    {
        return velocidadeHorizontal;
    }

    /**
     * Define a velocidade horizontal. Validação: a velocidade fica sempre entre 0 e o máximo permitido (20).
     *
     * @param velocidade Nova velocidade em píxeis por ciclo (ex.: 4)
     */
    public void setVelocidadeHorizontal(int velocidade)
    {
        this.velocidadeHorizontal = Math.max(0, Math.min(VELOCIDADE_HORIZONTAL_MAXIMA, velocidade));
    }

    /**
     * Devolve a velocidade vertical.
     *
     * @return velocidade em píxeis por ciclo; negativa a subir, positiva a cair
     */
    public int getVelocidadeVertical()
    {
        return velocidadeVertical;
    }

    /**
     * Define a velocidade vertical, limitada entre -28 e 14 píxeis por ciclo.
     *
     * @param velocidade Nova velocidade vertical (ex.: -5 para cortar um salto, 0 ao reaparecer)
     */
    protected void setVelocidadeVertical(int velocidade)
    {
        this.velocidadeVertical = Math.max(-VELOCIDADE_QUEDA_MAXIMA * 2, Math.min(VELOCIDADE_QUEDA_MAXIMA, velocidade));
    }

    /**
     * Indica se está apoiada numa plataforma.
     *
     * @return true se no último ciclo aterrou ou ficou em cima de uma plataforma
     */
    public boolean isNoChao()
    {
        return noChao;
    }

    /**
     * Devolve a plataforma onde está apoiada.
     *
     * @return plataforma de apoio, ou null se estiver no ar
     */
    public Plataforma getPlataformaApoio()
    {
        return plataformaApoio;
    }

    /**
     * Indica para que lado está virada.
     *
     * @return true se estiver virada para a direita; false se para a esquerda
     */
    public boolean isViradoParaDireita()
    {
        return viradoParaDireita;
    }

    /**
     * Define para que lado está virada.
     *
     * @param direita true para virar à direita, false à esquerda (ex.: true quando a tecla da direita está premida)
     */
    protected void setViradoParaDireita(boolean direita)
    {
        this.viradoParaDireita = direita;
    }

    /**
     * Devolve a direção em que está virada como número.
     *
     * @return 1 se virada para a direita, -1 se para a esquerda
     */
    public int getDirecao()
    {
        return viradoParaDireita ? 1 : -1;
    }

    /**
     * Devolve a coordenada x onde foi colocada (corrigida pelo scroll da câmara).
     *
     * @return x inicial em píxeis do ecrã (início da rota de patrulha nos inimigos)
     */
    public int getXinicial()
    {
        return xInicial;
    }

    /**
     * Devolve a coordenada y onde foi colocada.
     *
     * @return y inicial em píxeis
     */
    public int getYinicial()
    {
        return yInicial;
    }
}

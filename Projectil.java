import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Classe base ABSTRATA de todos os projéteis (dos agentes e dos inimigos).
 * Guarda a posição em double para trajetórias suaves, aplica gravidade opcional e,
 * em cada frame, chama verificarColisoes() — que cada subclasse define (template method).
 *
 * Hierarquia: Actor -> ElementoInterativo -> Projectil -> ProjectilDistracao -> FrascoEter / AmuletoLuminoso
 *                                                      -> ProjectilSombrio
 *
 * Conceitos de POO: classe abstrata com método abstrato, template method (act), métodos hook
 * (comportamentoPousado, aoExpirar), implementação de interface (Deslocavel), encapsulamento
 * (posição e velocidade privadas, com getters e setter protegido).
 */
public abstract class Projectil extends ElementoInterativo implements Deslocavel
{
    /** Distância fora dos limites a partir da qual o projétil é removido, em píxeis (80). */
    private static final int MARGEM_FORA = 80;

    /** Posição x exata, em píxeis. */
    private double x;
    /** Posição y exata, em píxeis. */
    private double y;
    /** Velocidade horizontal, em píxeis por ciclo. */
    private double vx;
    /** Velocidade vertical, em píxeis por ciclo (positiva = para baixo). */
    private double vy;
    /** Gravidade somada a vy em cada ciclo, em píxeis por ciclo (ex.: 0.45; 0 = linha reta). */
    private final double gravidade;
    /** Duração máxima em ciclos; 0 = sem limite. */
    private final int duracaoMaxima;
    /** Ciclos desde que foi criado. */
    private int idade;
    /** true enquanto voa; false depois de pousar. */
    private boolean emVoo;

    /**
     * Cria um projétil em voo.
     *
     * @param vx Velocidade horizontal inicial, em píxeis por ciclo (ex.: 7.5 numa distração lançada para a direita)
     * @param vy Velocidade vertical inicial, em píxeis por ciclo, negativa = para cima (ex.: -7.5)
     * @param gravidade Gravidade por ciclo (ex.: 0.45 nas distrações, 0 nos orbes)
     * @param duracaoMaxima Duração máxima em ciclos, 0 = sem limite (ex.: 200 nos orbes)
     */
    protected Projectil(double vx, double vy, double gravidade, int duracaoMaxima)
    {
        this.vx = vx;
        this.vy = vy;
        this.gravidade = gravidade;
        this.duracaoMaxima = duracaoMaxima;
        this.emVoo = true;
    }

    /**
     * Inicializa a posição exata quando o projétil é colocado no mundo (overriding de Actor.addedToWorld).
     *
     * @param mundo Mundo onde foi colocado (ex.: um Cenario1)
     */
    @Override
    protected void addedToWorld(World mundo)
    {
        x = getX();
        y = getY();
    }

    /**
     * Implementa Deslocavel: corrige a posição x exata quando a câmara se move.
     *
     * @param dx Quanto a câmara avançou para a direita, em píxeis (ex.: 10)
     */
    @Override
    public void deslocarReferencias(int dx)
    {
        x -= dx;
    }

    /**
     * Ciclo do projétil, chamado pelo Greenfoot em cada ciclo (redefine ElementoInterativo.act).
     * Template method: em voo aplica gravidade, move, remove-o se sair do nível e chama
     * verificarColisoes(); pousado chama comportamentoPousado(); no fim do tempo chama aoExpirar().
     */
    public void act()
    {
        if (getWorld() == null) {
            return;
        }
        idade++;
        if (emVoo) {
            vy += gravidade;
            x += vx;
            y += vy;
            setLocation((int) Math.round(x), (int) Math.round(y));
            if (estaForaDoNivel()) {
                remover();
                return;
            }
            verificarColisoes();
        } else {
            comportamentoPousado();
        }
        if (getWorld() != null && duracaoMaxima > 0 && idade >= duracaoMaxima) {
            aoExpirar();
        }
    }

    /** Contrato: colisões específicas de cada projétil (com agentes, inimigos, paredes...), chamado em cada ciclo de voo. */
    protected abstract void verificarColisoes();

    /** Hook: o que faz depois de pousar (por omissão nada; o amuleto brilha e atrai inimigos). */
    protected void comportamentoPousado()
    {
    }

    /** Hook: chamado quando atinge a duração máxima (por omissão desaparece). */
    protected void aoExpirar()
    {
        remover();
    }

    /**
     * Verifica se o projétil saiu do nível (com 80 px de margem, ou 400 px acima do ecrã).
     *
     * @return true se estiver fora do nível
     */
    private boolean estaForaDoNivel()
    {
        int esquerda = -MARGEM_FORA;
        int direita = getWorld().getWidth() + MARGEM_FORA;
        if (getWorld() instanceof Cenario) {
            esquerda = ((Cenario) getWorld()).getLimiteEsquerdo() - MARGEM_FORA;
            direita = ((Cenario) getWorld()).getLimiteDireito() + MARGEM_FORA;
        }
        return x < esquerda || x > direita || y > getWorld().getHeight() + MARGEM_FORA || y < -400;
    }

    /**
     * Procura a plataforma sólida que contém o ponto atual (parede).
     *
     * @return parede onde o projétil está, ou null
     */
    protected Plataforma paredeNoPonto()
    {
        for (Plataforma p : getWorld().getObjects(Plataforma.class)) {
            if (p.isSolida() && p.isColidivel() && x > p.getEsquerda() && x < p.getDireita()
                && y > p.getTopo() && y < p.getBase()) {
                return p;
            }
        }
        return null;
    }

    /**
     * Procura a plataforma (sólida ou não) cujo topo foi atravessado a cair neste ciclo.
     *
     * @return plataforma atravessada, ou null (também null se o projétil estiver a subir)
     */
    protected Plataforma chaoAtravessado()
    {
        if (vy <= 0) {
            return null;
        }
        double yAnterior = y - vy;
        for (Plataforma p : getWorld().getObjects(Plataforma.class)) {
            if (p.isColidivel() && x >= p.getEsquerda() && x <= p.getDireita()
                && yAnterior <= p.getTopo() && y >= p.getTopo()) {
                return p;
            }
        }
        return null;
    }

    /**
     * Pára o projétil (deixa de voar) na posição indicada.
     *
     * @param xPouso Coordenada x onde fica, em píxeis (ex.: a posição x atual)
     * @param yPouso Coordenada y onde fica, em píxeis (ex.: topo do chão menos metade da altura da imagem)
     */
    protected void pousar(int xPouso, int yPouso)
    {
        emVoo = false;
        x = xPouso;
        y = yPouso;
        setLocation(xPouso, yPouso);
    }

    /** Remove o projétil do mundo em segurança. */
    protected void remover()
    {
        if (getWorld() != null) {
            getWorld().removeObject(this);
        }
    }

    /**
     * Devolve o Cenario onde está.
     *
     * @return o mundo atual convertido em Cenario, ou null se for outro tipo de mundo
     */
    protected Cenario getCenario()
    {
        return (getWorld() instanceof Cenario) ? (Cenario) getWorld() : null;
    }

    /**
     * Indica se o projétil está em voo.
     *
     * @return true enquanto não pousou
     */
    public boolean isEmVoo()       { return emVoo; }

    /**
     * Devolve a velocidade horizontal.
     *
     * @return velocidade em píxeis por ciclo
     */
    public double getVelocidadeX() { return vx; }

    /**
     * Devolve a velocidade vertical.
     *
     * @return velocidade em píxeis por ciclo (positiva = para baixo)
     */
    public double getVelocidadeY() { return vy; }

    /**
     * Define a velocidade horizontal.
     *
     * @param v Nova velocidade em píxeis por ciclo (ex.: 0 para cair a direito)
     */
    protected void setVelocidadeX(double v) { vx = v; }

    /**
     * Devolve a idade do projétil.
     *
     * @return ciclos desde que foi criado
     */
    protected int getIdade()       { return idade; }
}

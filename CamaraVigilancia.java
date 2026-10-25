import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Claude (Anthropic) e Gemini aqui.
 * Âmbito: classe inteira (segundo o comentário da classe); código de desenho/gráficos
 *         (redesenhar, desenharSuporte, desenharCorpo) com IA declarada
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Câmara de vigilância que varre uma zona de um lado para o outro com um cone de visão.
 * Se vir um agente (que não esteja escondido nas sombras), fica vermelha e dispara o
 * ALARME do nível: os agentes perdem sanidade e todos os inimigos vão ao local.
 * Desliga-se para sempre quando o Zag hackeia o terminal a que está ligada (Hackeavel).
 *
 * Ângulos: 0 = direita, 90 = para baixo, 180 = esquerda.
 *
 * Hierarquia: CamaraVigilancia estende ElementoInterativo, que estende Actor.
 * Conceitos de POO: herança, implementação de interface (Hackeavel), composição (CampoVisao
 * partilhado com os cultistas), overloading de construtores (3 versões), overriding de act()
 * e addedToWorld().
 *
 * Nota: desenvolvido com apoio de IA (Claude, Anthropic e Gemini, Google)
 */
public class CamaraVigilancia extends ElementoInterativo implements Hackeavel
{
    /** Velocidade de varrimento, em graus por ciclo (0,6). */
    private static final double VELOCIDADE_VARRIMENTO = 0.6;   // graus por frame
    /** Ciclos de pausa em cada ponta do varrimento (50). */
    private static final int PAUSA_NAS_PONTAS = 50;
    /** Ciclos em alerta (parada e vermelha) depois de detetar um agente (150). */
    private static final int FRAMES_ALERTA = 150;
    /** Metade da abertura do cone, em graus (17). */
    private static final int SEMI_ANGULO = 17;

    /** Ângulo mínimo do varrimento, em graus (ex.: 60). */
    private final double anguloMinimo;
    /** Ângulo máximo do varrimento, em graus (ex.: 120). */
    private final double anguloMaximo;
    /** Altura do poste em píxeis; 0 se estiver presa ao teto. */
    private final int alturaPoste;
    /** Campo de visão da câmara (alcance e abertura). */
    private final CampoVisao visao;
    /** Lado da imagem quadrada, em píxeis (o dobro do alcance mais 8). */
    private final int tamanho;
    /** Ângulo atual da câmara, em graus. */
    private double angulo;
    /** Sentido do varrimento: 1 ou -1. */
    private int sentido;
    /** Ciclos de pausa restantes numa ponta. */
    private int pausa;
    /** Ciclos de alerta restantes. */
    private int framesAlerta;
    /** true enquanto a câmara funciona (false depois de hackeada). */
    private boolean ativa;
    /** Contador de ciclos (redesenha a cada 2). */
    private int contador;

    /**
     * Câmara de teto com alcance padrão de 380 px (overloading).
     *
     * @param anguloMinimo Ângulo mínimo do varrimento, em graus (ex.: 60)
     * @param anguloMaximo Ângulo máximo do varrimento, em graus (ex.: 120)
     */
    public CamaraVigilancia(int anguloMinimo, int anguloMaximo)
    {
        this(anguloMinimo, anguloMaximo, 380);
    }

    /**
     * Câmara de teto com alcance à escolha (overloading).
     *
     * @param anguloMinimo Ângulo mínimo do varrimento, em graus (ex.: 70, na câmara do cofre do Cenario1)
     * @param anguloMaximo Ângulo máximo do varrimento, em graus (ex.: 110)
     * @param alcance Alcance da visão, em píxeis (ex.: 440)
     */
    public CamaraVigilancia(int anguloMinimo, int anguloMaximo, int alcance)
    {
        this(anguloMinimo, anguloMaximo, alcance, 0);
    }

    /**
     * Câmara completa. Os ângulos são ordenados automaticamente se vierem trocados.
     *
     * @param anguloMinimo Ângulo mínimo do varrimento, em graus (ex.: 60)
     * @param anguloMaximo Ângulo máximo do varrimento, em graus (ex.: 120)
     * @param alcance Alcance da visão, em píxeis (ex.: 420)
     * @param alturaPoste 0 = presa ao teto; maior que 0 = montada num poste com essa altura
     *                    (ex.: CHAO - 150 = 370, no pátio do Cenario1)
     */
    public CamaraVigilancia(int anguloMinimo, int anguloMaximo, int alcance, int alturaPoste)
    {
        this.anguloMinimo = Math.min(anguloMinimo, anguloMaximo);
        this.anguloMaximo = Math.max(anguloMinimo, anguloMaximo);
        this.alturaPoste = Math.max(0, alturaPoste);
        this.visao = new CampoVisao(alcance, SEMI_ANGULO);
        this.tamanho = alcance * 2 + 8;
        this.angulo = this.anguloMinimo;
        this.sentido = 1;
        this.ativa = true;
        setImage(new GreenfootImage(tamanho, tamanho));
    }

    /**
     * Desenha a câmara quando é colocada no mundo (o cone precisa do mundo para ser recortado pelas paredes).
     *
     * @param mundo Mundo onde foi colocada (ex.: um Cenario1)
     */
    @Override
    protected void addedToWorld(World mundo)
    {
        redesenhar();
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine ElementoInterativo.act): se estiver ativa e sem
     * alerta, varre e procura agentes; redesenha a cada 2 ciclos.
     */
    public void act()
    {
        contador++;
        if (ativa) {
            if (framesAlerta > 0) {
                framesAlerta--;
            } else {
                varrer();
                procurarAgentes();
            }
        }
        if (contador % 2 == 0) {
            redesenhar();
        }
    }

    /** Roda a câmara 0,6 graus e inverte o sentido (com pausa) ao chegar a uma ponta. */
    private void varrer()
    {
        if (pausa > 0) {
            pausa--;
            return;
        }
        angulo += sentido * VELOCIDADE_VARRIMENTO;
        if (angulo >= anguloMaximo || angulo <= anguloMinimo) {
            angulo = Math.max(anguloMinimo, Math.min(anguloMaximo, angulo));
            sentido = -sentido;
            pausa = PAUSA_NAS_PONTAS;
        }
    }

    /** Se vir algum agente, entra em alerta e dispara o alarme do Cenario na posição desse agente. */
    private void procurarAgentes()
    {
        for (AgenteInvestigador agente : getWorld().getObjects(AgenteInvestigador.class)) {
            if (visao.ve(getWorld(), getX(), getY(), angulo, agente)) {
                framesAlerta = FRAMES_ALERTA;
                if (getWorld() instanceof Cenario) {
                    ((Cenario) getWorld()).dispararAlarme(agente.getX(), agente.getY());
                }
                return;
            }
        }
    }

    /** Hackeavel: desliga a câmara para sempre (com faíscas). */
    @Override
    public void desativarPorHacking()
    {
        if (!ativa) {
            return;
        }
        ativa = false;
        framesAlerta = 0;
        if (getWorld() != null) {
            getWorld().addObject(new EfeitoParticulas(new Color(255, 230, 120), 12), getX(), getY());
        }
        redesenhar();
    }

    /**
     * Implementa Hackeavel.
     *
     * @return true se a câmara já foi desligada
     */
    @Override
    public boolean isHackeado()
    {
        return !ativa;
    }

    /**
     * Indica se a câmara está em alerta (acabou de ver um agente).
     *
     * @return true durante os ciclos de alerta
     */
    public boolean isEmAlerta()
    {
        return framesAlerta > 0;
    }

    /**
     * Devolve o ângulo atual.
     *
     * @return ângulo em graus (0 = direita, 90 = baixo)
     */
    public double getAngulo()
    {
        return angulo;
    }

    // desenho, utilizado AI para fazer os desenhos

    /** Redesenha a imagem: cone (só se ativa; vermelho em alerta), suporte e corpo da câmara. */
    private void redesenhar()
    {
        if (getWorld() == null) {
            return;
        }
        GreenfootImage img = getImage();
        img.clear();
        int c = tamanho / 2;
        if (ativa) {
            boolean alerta = framesAlerta > 0;
            Color preenchimento = alerta ? new Color(255, 40, 40, 70) : new Color(255, 245, 190, 40);
            Color contorno = alerta ? new Color(255, 60, 60, 150) : new Color(255, 245, 190, 90);
            visao.desenhar(img, c, c, getWorld(), getX(), getY(), angulo, preenchimento, contorno);
        }
        desenharSuporte(img, c);
        desenharCorpo(img, c);
    }

    /**
     * Desenha o suporte: um poste até ao chão ou um braço preso ao teto.
     *
     * @param img Imagem da câmara
     * @param c Coordenada do centro da imagem, em píxeis (ex.: 424 para alcance 420)
     */
    private void desenharSuporte(GreenfootImage img, int c)
    {
        img.setColor(new Color(50, 54, 64));
        if (alturaPoste > 0) {
            img.fillRect(c - 3, c, 6, alturaPoste);
            img.fillRect(c - 10, c + alturaPoste - 6, 20, 6);
            img.setColor(new Color(80, 86, 98));
            img.fillRect(c - 3, c, 2, alturaPoste);
        } else {
            img.fillRect(c - 3, c - 22, 6, 22);
            img.fillRect(c - 12, c - 24, 24, 5);
        }
    }

    /**
     * Corpo da câmara: um retângulo rodado na direção para onde olha, com a lente e um LED
     * (verde ativa, vermelho em alerta, cinzento desligada). Foi utilizado Gemini.
     *
     * @param img Imagem da câmara
     * @param c Coordenada do centro da imagem, em píxeis (ex.: 424 para alcance 420)
     */
    private void desenharCorpo(GreenfootImage img, int c)
    {
        double rad = Math.toRadians(angulo);
        double fx = Math.cos(rad);
        double fy = Math.sin(rad);
        double px = -fy;
        double py = fx;
        double comprimento = 22;
        double meiaLargura = 7;
        int[] xs = new int[4];
        int[] ys = new int[4];
        double[][] cantos = { { -6, -meiaLargura }, { comprimento, -meiaLargura }, { comprimento, meiaLargura }, { -6, meiaLargura } };
        for (int i = 0; i < 4; i++) {
            xs[i] = c + (int) Math.round(fx * cantos[i][0] + px * cantos[i][1]);
            ys[i] = c + (int) Math.round(fy * cantos[i][0] + py * cantos[i][1]);
        }
        img.setColor(new Color(200, 204, 214));
        img.fillPolygon(xs, ys, 4);
        img.setColor(new Color(30, 30, 40));
        img.drawPolygon(xs, ys, 4);
        int lx = c + (int) Math.round(fx * comprimento);
        int ly = c + (int) Math.round(fy * comprimento);
        img.setColor(new Color(20, 24, 34));
        img.fillOval(lx - 5, ly - 5, 10, 10);
        Color led = !ativa ? new Color(60, 60, 60) : (framesAlerta > 0 ? new Color(255, 40, 40) : new Color(80, 255, 120));
        img.setColor(led);
        img.fillOval(c - 3, c - 3, 6, 6);
    }
}

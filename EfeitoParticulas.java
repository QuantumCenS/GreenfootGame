import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: código de desenho/gráficos (classe inteira, que só desenha partículas)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Pequena explosão de partículas (vidro do frasco, faíscas de uma câmara desligada...).
 * As partículas são simuladas dentro da própria imagem do ator.
 *
 * Hierarquia: EfeitoParticulas estende EfeitoVisual, que estende Actor.
 * Conceitos de POO: herança (reutiliza remover()), overloading de construtores (2 versões),
 * overriding de act(), encapsulamento (vetores privados e finais).
 */
public class EfeitoParticulas extends EfeitoVisual
{
    /** Lado da imagem quadrada onde as partículas se movem, em píxeis (120). */
    private static final int TAMANHO = 120;
    /** Duração do efeito, em ciclos (28). */
    private static final int DURACAO = 28;

    /** Posição x de cada partícula dentro da imagem. */
    private final double[] px;
    /** Posição y de cada partícula dentro da imagem. */
    private final double[] py;
    /** Velocidade x de cada partícula, em píxeis por ciclo. */
    private final double[] vx;
    /** Velocidade y de cada partícula, em píxeis por ciclo. */
    private final double[] vy;
    /** Cor das partículas. */
    private final Color cor;
    /** Ciclos desde que o efeito começou. */
    private int idade;

    /**
     * Explosão com 14 partículas (overloading).
     *
     * @param cor Cor das partículas (ex.: new Color(150, 220, 255), o vidro do frasco de éter)
     */
    public EfeitoParticulas(Color cor)
    {
        this(cor, 14);
    }

    /**
     * Explosão com o número de partículas indicado, com direções e velocidades aleatórias
     * (ligeiramente para cima).
     *
     * @param cor Cor das partículas (ex.: new Color(255, 80, 100), faíscas da barreira laser)
     * @param quantidade Número de partículas (ex.: 8)
     */
    public EfeitoParticulas(Color cor, int quantidade)
    {
        this.cor = cor;
        px = new double[quantidade];
        py = new double[quantidade];
        vx = new double[quantidade];
        vy = new double[quantidade];
        for (int i = 0; i < quantidade; i++) {
            double angulo = Math.toRadians(Greenfoot.getRandomNumber(360));
            double velocidade = 1.5 + Greenfoot.getRandomNumber(30) / 10.0;
            px[i] = TAMANHO / 2;
            py[i] = TAMANHO / 2;
            vx[i] = Math.cos(angulo) * velocidade;
            vy[i] = Math.sin(angulo) * velocidade - 2;
        }
        setImage(new GreenfootImage(TAMANHO, TAMANHO));
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine Actor.act): move as partículas com gravidade,
     * redesenha-as cada vez mais transparentes e remove o efeito ao fim de 28 ciclos.
     */
    public void act()
    {
        idade++;
        if (idade >= DURACAO) {
            remover();
            return;
        }
        GreenfootImage img = getImage();
        img.clear();
        int alfa = 255 * (DURACAO - idade) / DURACAO;
        img.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), alfa));
        for (int i = 0; i < px.length; i++) {
            vy[i] += 0.3;
            px[i] += vx[i];
            py[i] += vy[i];
            img.fillRect((int) px[i], (int) py[i], 3, 3);
        }
    }
}

import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: código de desenho/gráficos (criarSombra)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Zona escura do cenário. Um agente agachado cuja posição x esteja na parte útil da zona
 * (sem as margens esbatidas) e cujos pés estejam dentro dela fica escondido
 * (AgenteInvestigador.isEscondido()). É desenhada por cima dos agentes,
 * por isso eles ficam visualmente na penumbra.
 *
 * Hierarquia: ZonaSombra estende ElementoInterativo, que estende Actor.
 * Conceitos de POO: herança (reutiliza getEsquerda/getDireita/getTopo/getBase), overloading de
 * construtores (2 versões), método privado estático para criar a imagem.
 */
public class ZonaSombra extends ElementoInterativo
{
    /** Opacidade máxima da sombra, de 0 a 255 (170). */
    private static final int OPACIDADE_MAXIMA = 170;
    /** Largura das margens laterais esbatidas, em píxeis (18). */
    private static final int MARGEM_ESBATIDA = 18;

    /** Sombra de 120 x 100 píxeis (para criar pelo menu do Greenfoot). */
    public ZonaSombra()
    {
        this(120, 100);
    }

    /**
     * Sombra com as dimensões indicadas (mínimo 2 x 2).
     *
     * @param largura Largura em píxeis (ex.: 160, a primeira sombra do Cenario1)
     * @param altura Altura em píxeis (ex.: 110)
     */
    public ZonaSombra(int largura, int altura)
    {
        setImage(criarSombra(Math.max(2, largura), Math.max(2, altura)));
    }

    /**
     * Cria a imagem em degradê: transparente em cima, escuro em baixo, com os lados esbatidos.
     *
     * @param largura Largura em píxeis (ex.: 160)
     * @param altura Altura em píxeis (ex.: 110)
     * @return nova imagem da sombra
     */
    private static GreenfootImage criarSombra(int largura, int altura)
    {
        GreenfootImage imagem = new GreenfootImage(largura, altura);
        for (int y = 0; y < altura; y++) {
            double fy = Math.min(1.0, y / (altura * 0.45));
            for (int x = 0; x < largura; x++) {
                double fx = Math.min(1.0, Math.min(x, largura - 1 - x) / (double) MARGEM_ESBATIDA);
                int alfa = (int) (OPACIDADE_MAXIMA * fx * fy);
                imagem.setColorAt(x, y, new Color(6, 4, 16, alfa));
            }
        }
        return imagem;
    }

    /**
     * Verifica se a coordenada x está dentro da parte útil da sombra (descontando metade das margens esbatidas).
     *
     * @param x Coordenada x a testar, em píxeis (ex.: agente.getX())
     * @return true se estiver dentro da parte útil
     */
    public boolean cobre(int x)
    {
        return x > getEsquerda() + MARGEM_ESBATIDA / 2 && x < getDireita() - MARGEM_ESBATIDA / 2;
    }
}

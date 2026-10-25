import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Sequência de frames de uma animação, com versão virada para a direita e para a esquerda.
 * As imagens são carregadas uma única vez (no construtor) e reutilizadas.
 *
 * Classe auxiliar (não é um Actor): usada por AgenteInvestigador e InimigoCosmico (composição).
 * Conceitos de POO: encapsulamento (atributos privados e finais), overloading de construtores
 * (3 versões encadeadas com this(...)), métodos privados estáticos auxiliares.
 */
public class Animacao
{
    /** Frames originais, desenhados virados para a direita. */
    private final GreenfootImage[] framesDireita;
    /** Cópias espelhadas dos frames, viradas para a esquerda. */
    private final GreenfootImage[] framesEsquerda;
    /** Ciclos de act() que cada frame fica visível (mínimo 1). */
    private final int atraso;
    /** Índice do frame atual. */
    private int indice;
    /** Ciclos já passados no frame atual. */
    private int contador;

    /**
     * Carrega prefixo_0.png ... prefixo_(n-1).png (imagens desenhadas viradas para a direita).
     *
     * @param prefixo Início do nome dos ficheiros (ex.: "zig_correr" ou "cultista_andar")
     * @param numeroFrames Número de imagens a carregar (ex.: 6)
     * @param atraso número de ciclos (act) que cada frame fica visível (ex.: 5)
     */
    public Animacao(String prefixo, int numeroFrames, int atraso)
    {
        this(carregarFrames(prefixo, numeroFrames), atraso);
    }

    /**
     * Igual ao anterior mas redimensiona cada frame (útil para usar sprites próprios).
     * Versão sobrecarregada (overloading) com largura e altura; atualmente não é usada no projeto.
     *
     * @param prefixo Início do nome dos ficheiros (ex.: "zig_correr")
     * @param numeroFrames Número de imagens a carregar (ex.: 6)
     * @param atraso Ciclos de act() que cada frame fica visível (ex.: 5)
     * @param largura Nova largura de cada frame em píxeis (ex.: 48)
     * @param altura Nova altura de cada frame em píxeis (ex.: 80)
     */
    public Animacao(String prefixo, int numeroFrames, int atraso, int largura, int altura)
    {
        this(redimensionar(carregarFrames(prefixo, numeroFrames), largura, altura), atraso);
    }

    /**
     * Cria a animação a partir de imagens já existentes e gera as versões espelhadas.
     *
     * @param frames Imagens viradas para a direita (ex.: as devolvidas por carregarFrames)
     * @param atraso Ciclos de act() que cada frame fica visível; valores menores que 1 passam a 1 (ex.: 9)
     * @throws IllegalArgumentException se frames for null ou estiver vazio
     */
    public Animacao(GreenfootImage[] frames, int atraso)
    {
        if (frames == null || frames.length == 0) {
            throw new IllegalArgumentException("Uma animação precisa de pelo menos um frame");
        }
        this.atraso = Math.max(1, atraso);
        this.framesDireita = new GreenfootImage[frames.length];
        this.framesEsquerda = new GreenfootImage[frames.length];
        for (int i = 0; i < frames.length; i++) {
            framesDireita[i] = frames[i];
            framesEsquerda[i] = new GreenfootImage(frames[i]);
            framesEsquerda[i].mirrorHorizontally();
        }
    }

    /**
     * Carrega as imagens prefixo_0.png até prefixo_(numeroFrames-1).png da pasta images.
     *
     * @param prefixo Início do nome dos ficheiros (ex.: "criatura_rastejar")
     * @param numeroFrames Número de imagens (ex.: 4)
     * @return vetor com as imagens carregadas, pela ordem
     */
    private static GreenfootImage[] carregarFrames(String prefixo, int numeroFrames)
    {
        GreenfootImage[] frames = new GreenfootImage[numeroFrames];
        for (int i = 0; i < numeroFrames; i++) {
            frames[i] = new GreenfootImage(prefixo + "_" + i + ".png");
        }
        return frames;
    }

    /**
     * Redimensiona todas as imagens do vetor (altera as próprias imagens).
     *
     * @param frames Imagens a redimensionar
     * @param largura Nova largura em píxeis (ex.: 48)
     * @param altura Nova altura em píxeis (ex.: 80)
     * @return o mesmo vetor, já com as imagens redimensionadas
     */
    private static GreenfootImage[] redimensionar(GreenfootImage[] frames, int largura, int altura)
    {
        for (GreenfootImage frame : frames) {
            frame.scale(largura, altura);
        }
        return frames;
    }

    /** Volta ao primeiro frame (chamado quando o agente muda de estado). */
    public void reiniciar()
    {
        indice = 0;
        contador = 0;
    }

    /**
     * Avança o relógio da animação um ciclo e devolve o frame a mostrar.
     *
     * @param paraDireita true para o frame virado à direita (ex.: isViradoParaDireita() do ator)
     * @return imagem a usar neste ciclo
     */
    public GreenfootImage avancar(boolean paraDireita)
    {
        contador++;
        if (contador >= atraso) {
            contador = 0;
            indice = (indice + 1) % framesDireita.length;
        }
        return getFrameAtual(paraDireita);
    }

    /**
     * Devolve o frame atual sem avançar a animação.
     *
     * @param paraDireita true para a versão virada à direita (ex.: true)
     * @return imagem do frame atual na orientação pedida
     */
    public GreenfootImage getFrameAtual(boolean paraDireita)
    {
        return paraDireita ? framesDireita[indice] : framesEsquerda[indice];
    }

    /**
     * Devolve o número de frames da animação.
     *
     * @return número de frames (ex.: 6)
     */
    public int getNumeroFrames()
    {
        return framesDireita.length;
    }

    /**
     * Devolve o índice do frame atual.
     *
     * @return índice entre 0 e getNumeroFrames() - 1
     */
    public int getIndice()
    {
        return indice;
    }
}

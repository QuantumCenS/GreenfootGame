import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: código de desenho/gráficos (método desenharBrilho)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Amuleto luminoso (distração do Zag). Não se parte: cai, fica no chão a brilhar
 * durante 6 segundos e, a cada meio segundo, atrai os inimigos num raio de 260 px,
 * que ficam a olhar fascinados para ele (de costas para quem vem por trás...).
 *
 * Hierarquia: AmuletoLuminoso estende ProjectilDistracao, que estende Projectil, que estende
 * ElementoInterativo, que estende Actor (4 níveis além de Actor).
 * Conceitos de POO: herança, implementação dos métodos abstratos (aoAtingirInimigo, aoAterrar),
 * overriding do hook comportamentoPousado, overloading de construtores (2 versões).
 */
public class AmuletoLuminoso extends ProjectilDistracao
{
    /** Ciclos que o amuleto brilha no chão (360, cerca de 6 s). */
    private static final int DURACAO_BRILHO = 360;
    /** Ciclos entre dois ruídos de atração (30, cerca de meio segundo). */
    private static final int INTERVALO_ATRACAO = 30;

    /** Imagem do amuleto sem brilho ("amuleto.png"). */
    private final GreenfootImage imagemBase;
    /** Ciclos desde que pousou. */
    private int framesPousado;

    /** Amuleto lançado para a direita (para criar pelo menu do Greenfoot). */
    public AmuletoLuminoso()
    {
        this(1);
    }

    /**
     * Cria o amuleto, sem limite de duração em voo.
     *
     * @param direcao Sentido do lançamento: 1 direita, -1 esquerda (ex.: getDirecao() do Zag)
     */
    public AmuletoLuminoso(int direcao)
    {
        super(direcao, 0);
        imagemBase = new GreenfootImage("amuleto.png");
        setImage(new GreenfootImage(imagemBase));
    }

    /**
     * Atingir um inimigo não o fere: o amuleto cai aos pés dele (perde a velocidade horizontal).
     *
     * @param inimigo Inimigo atingido (ex.: um CultistaPatrulheiro)
     */
    @Override
    protected void aoAtingirInimigo(InimigoCosmico inimigo)
    {
        setVelocidadeX(0);
    }

    /**
     * Pousa no chão, toca o som e emite o primeiro ruído TipoRuido.AMULETO.
     *
     * @param x Coordenada x onde caiu, em píxeis (ex.: getX())
     * @param yChao Coordenada y do topo da plataforma, em píxeis (ex.: 520)
     */
    @Override
    protected void aoAterrar(int x, int yChao)
    {
        pousar(x, yChao - imagemBase.getHeight() / 2);
        Audio.tocar("amuleto.wav");
        fazerRuido(getX(), getY(), TipoRuido.AMULETO);
    }

    /** Redefine o hook de Projectil: no chão pulsa e atrai periodicamente; depois de 360 ciclos apaga-se. */
    @Override
    protected void comportamentoPousado()
    {
        framesPousado++;
        if (framesPousado % INTERVALO_ATRACAO == 0 && framesPousado < DURACAO_BRILHO) {
            fazerRuido(getX(), getY(), TipoRuido.AMULETO);
        }
        desenharBrilho();
        if (framesPousado >= DURACAO_BRILHO) {
            remover();
        }
    }
    //utilizado AI, Gemini
    /** Desenha o halo verde pulsante (cada vez mais fraco no fim do tempo) com o amuleto por cima. */
    private void desenharBrilho()
    {
        int raio = 22 + (int) (6 * Math.sin(framesPousado * 0.15));
        int tamanho = 64;
        GreenfootImage img = new GreenfootImage(tamanho, tamanho);
        int restante = DURACAO_BRILHO - framesPousado;
        int alfaMax = Math.min(90, restante * 2);
        for (int r = raio; r > 4; r -= 4) {
            int alfa = alfaMax * (raio - r + 4) / raio;
            img.setColor(new Color(190, 255, 150, Math.max(0, Math.min(255, alfa))));
            img.fillOval(tamanho / 2 - r, tamanho / 2 - r, r * 2, r * 2);
        }
        img.drawImage(imagemBase, (tamanho - imagemBase.getWidth()) / 2, (tamanho - imagemBase.getHeight()) / 2);
        setImage(img);
    }
}

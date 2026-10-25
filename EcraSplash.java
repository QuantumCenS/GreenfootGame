import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: código de desenho/gráficos (desenharLogotipo, desenhar e o fade em act)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Ecrã de abertura ("splash"): o logótipo da Divisão Ómega aparece e desaparece,
 * depois passa ao menu principal (ou antes, ao carregar numa tecla/clicar).
 * É o primeiro mundo do jogo (o mundo que o Greenfoot abre por omissão neste projeto).
 *
 * Hierarquia: EcraSplash estende World.
 * Conceitos de POO: herança, overriding de World.act(), encapsulamento.
 */
public class EcraSplash extends World
{
    /** Duração total do ecrã, em ciclos (210, cerca de 3,5 s). */
    private static final int DURACAO = 210;

    /** Imagem do logótipo, desenhada uma vez no construtor. */
    private final GreenfootImage logotipo;
    /** Ciclos desde que o ecrã abriu. */
    private int frames;

    /** Cria o ecrã com o tamanho do jogo e desenha o logótipo (inicialmente invisível). */
    public EcraSplash()
    {
        super(Cenario.LARGURA, Cenario.ALTURA, 1);
        logotipo = desenharLogotipo();
        setBackground(new GreenfootImage(getWidth(), getHeight()));
        desenhar(0);
    }

    /**
     * Desenha o logótipo: um olho dentro de um círculo e os textos "DIVISÃO ÓMEGA" e a apresentação.
     *
     * @return nova imagem do tamanho do ecrã com o logótipo
     */
    private GreenfootImage desenharLogotipo()
    {
        GreenfootImage img = new GreenfootImage(getWidth(), getHeight());
        img.setColor(new Color(130, 255, 200));
        int cx = getWidth() / 2;
        int cy = 230;
        for (int r = 70; r >= 64; r--) {                 // símbolo Ómega estilizado: olho dentro de um círculo
            img.drawOval(cx - r, cy - r, r * 2, r * 2);
        }
        img.fillOval(cx - 44, cy - 16, 88, 32);
        img.setColor(Color.BLACK);
        img.fillOval(cx - 14, cy - 14, 28, 28);
        img.setColor(new Color(130, 255, 200));
        img.fillOval(cx - 4, cy - 10, 8, 8);
        img.setFont(new Font("Monospaced", true, false, 22));
        String t1 = "DIVISÃO ÓMEGA";
        img.drawString(t1, cx - (int) (t1.length() * 22 * 0.61) / 2, cy + 116);
        img.setFont(new Font("Monospaced", false, false, 15));
        img.setColor(new Color(200, 200, 220));
        String t2 = "apresenta  —  um jogo de Zig & Zag";
        img.drawString(t2, cx - (int) (t2.length() * 15 * 0.61) / 2, cy + 146);
        return img;
    }

    /**
     * Pinta o fundo de preto e desenha o logótipo com a opacidade indicada.
     *
     * @param alfa Opacidade do logótipo, de 0 (invisível) a 255 (opaco) (ex.: 128 a meio do fade)
     */
    private void desenhar(int alfa)
    {
        GreenfootImage fundo = getBackground();
        fundo.setColor(Color.BLACK);
        fundo.fillRect(0, 0, getWidth(), getHeight());
        GreenfootImage logo = new GreenfootImage(logotipo);
        logo.setTransparency(Math.max(0, Math.min(255, alfa)));
        fundo.drawImage(logo, 0, 0);
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine World.act): o logótipo aparece nos primeiros
     * 60 ciclos e desaparece nos últimos 50; toca o sino no 2.º ciclo; ao fim de 210 ciclos (ou com
     * uma tecla/clique depois do ciclo 30) passa ao MenuInicial.
     */
    @Override
    public void act()
    {
        frames++;
        int alfa = frames < 60 ? frames * 255 / 60 : (frames > DURACAO - 50 ? (DURACAO - frames) * 255 / 50 : 255);
        desenhar(alfa);
        if (frames == 2) {
            Audio.setSomAtivo(SistemaSave.isSomAtivo());
            Audio.tocar("sino.wav");
        }
        boolean saltar = frames > 30 && (Greenfoot.getKey() != null || Greenfoot.mouseClicked(null));
        if (frames >= DURACAO || saltar) {
            Greenfoot.setWorld(new MenuInicial());
        }
    }
}

import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: código de desenho/gráficos (método criarImagem)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Balão de texto que segue um ator (por cima da cabeça): "!" de alerta, "?" de suspeita,
 * dicas de teclas ("ENTER: hackear"), comentários dos agentes...
 *
 * Hierarquia: Balao estende EfeitoVisual, que estende Actor.
 * Conceitos de POO: herança (reutiliza remover()), overloading de construtores (3 versões),
 * método estático público reutilizado por outras classes (criarImagem, usado também por Dica),
 * overriding de act() e addedToWorld().
 */
public class Balao extends EfeitoVisual
{
    /** Duração que significa "até ser removido por quem o criou". */
    public static final int PERMANENTE = -1;
    /** Margem interior do balão, em píxeis (6). */
    private static final int MARGEM = 6;

    /** Ator que o balão segue. */
    private final Actor dono;
    /** Distância extra acima da cabeça do dono, em píxeis (ex.: 8). */
    private final int deslocamentoY;
    /** Ciclos que faltam para desaparecer, ou PERMANENTE. */
    private int duracao;
    /** Texto do balão. */
    private String texto;

    /**
     * Balão branco com texto escuro (overloading). Atualmente não é chamado no projeto.
     *
     * @param dono Ator a seguir (ex.: um CultistaPatrulheiro)
     * @param texto Texto a mostrar (ex.: "?")
     * @param duracao Duração em ciclos, ou Balao.PERMANENTE (ex.: 50)
     */
    public Balao(Actor dono, String texto, int duracao)
    {
        this(dono, texto, new Color(20, 18, 30), duracao);
    }

    /**
     * Balão com cor de texto à escolha, 8 píxeis acima do dono (overloading).
     *
     * @param dono Ator a seguir (ex.: o SinoAncestral)
     * @param texto Texto a mostrar; pode ter várias linhas com '\n' (ex.: "DONG!")
     * @param corTexto Cor do texto (ex.: new Color(160, 120, 30))
     * @param duracao Duração em ciclos, ou Balao.PERMANENTE (ex.: 50)
     */
    public Balao(Actor dono, String texto, Color corTexto, int duracao)
    {
        this(dono, texto, corTexto, duracao, 8);
    }

    /**
     * Balão completo.
     *
     * @param dono Ator a seguir (ex.: um CultistaPatrulheiro)
     * @param texto Texto a mostrar (ex.: "[E]", a dica do golpe do Zig)
     * @param corTexto Cor do texto (ex.: new Color(30, 120, 60))
     * @param duracao Duração em ciclos, ou Balao.PERMANENTE (ex.: Balao.PERMANENTE)
     * @param deslocamentoY distância extra acima da cabeça do dono, em píxeis (ex.: 36)
     */
    public Balao(Actor dono, String texto, Color corTexto, int duracao, int deslocamentoY)
    {
        this.dono = dono;
        this.duracao = duracao;
        this.deslocamentoY = deslocamentoY;
        this.texto = texto;
        setImage(criarImagem(texto, corTexto));
    }

    /**
     * Desenha o balão: caixa com borda e um pequeno bico em baixo. (Criado com ajuda do Gemini, desenho simples)
     *
     * @param texto Texto a escrever; cada '\n' começa uma nova linha (ex.: "Pesado demais!\nEmpurrem os dois.")
     * @param corTexto Cor do texto (ex.: new Color(120, 60, 20))
     * @return nova imagem do balão, com o tamanho ajustado ao texto
     */
    public static GreenfootImage criarImagem(String texto, Color corTexto)
    {
        Font fonte = new Font("Monospaced", true, false, 15);
        String[] linhas = texto.split("\n");
        int maior = 1;
        for (String l : linhas) {
            maior = Math.max(maior, l.length());
        }
        int largura = maior * 9 + MARGEM * 2;
        int altura = linhas.length * 17 + MARGEM * 2 + 6;
        GreenfootImage img = new GreenfootImage(largura, altura);
        img.setColor(new Color(245, 240, 225, 235));
        img.fillRect(1, 1, largura - 2, altura - 8);
        img.setColor(new Color(20, 18, 30));
        img.drawRect(0, 0, largura - 1, altura - 7);
        int meio = largura / 2;
        img.setColor(new Color(245, 240, 225, 235));
        img.fillPolygon(new int[] { meio - 5, meio + 5, meio }, new int[] { altura - 7, altura - 7, altura - 1 }, 3);
        img.setColor(corTexto);
        img.setFont(fonte);
        for (int i = 0; i < linhas.length; i++) {
            img.drawString(linhas[i], MARGEM, MARGEM + 13 + i * 17);
        }
        return img;
    }

    /**
     * Posiciona o balão por cima do dono logo que entra no mundo (overriding de Actor.addedToWorld).
     *
     * @param mundo Mundo onde foi colocado (ex.: um Cenario1)
     */
    @Override
    protected void addedToWorld(World mundo)
    {
        seguirDono();
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine Actor.act): desaparece se o dono saiu do mundo;
     * segue o dono e, se não for permanente, desconta a duração e desvanece nos últimos 15 ciclos.
     */
    public void act()
    {
        if (dono == null || dono.getWorld() == null) {
            remover();
            return;
        }
        seguirDono();
        if (duracao != PERMANENTE) {
            duracao--;
            if (duracao <= 0) {
                remover();
            } else if (duracao < 15) {
                getImage().setTransparency(duracao * 17);
            }
        }
    }

    /** Coloca o balão por cima da cabeça do dono (usa o topo da hitbox se o dono for uma EntidadeAnimada). */
    private void seguirDono()
    {
        if (dono != null && dono.getWorld() != null) {
            int topo = dono.getY() - dono.getImage().getHeight() / 2;
            if (dono instanceof EntidadeAnimada) {
                topo = ((EntidadeAnimada) dono).getTopoColisao();
            }
            setLocation(dono.getX(), topo - deslocamentoY - getImage().getHeight() / 2);
        }
    }

    /**
     * Devolve o texto do balão.
     *
     * @return texto mostrado (ex.: "?")
     */
    public String getTexto()
    {
        return texto;
    }
}

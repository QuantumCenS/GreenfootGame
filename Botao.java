import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: código de desenho/gráficos (método desenhar)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Botão clicável com o rato e selecionável com o teclado (usado em todos os menus,
 * no ecrã de leitura das notas e na pausa).
 * Quem o usa pergunta foiClicado() em cada frame.
 *
 * Hierarquia: Botao estende ElementoEcra, que estende Actor (fica fixo no ecrã).
 * Conceitos de POO: herança (usa fonte() e escreverCentrado() de ElementoEcra), overloading de
 * construtores (3 versões), encapsulamento (estado privado alterado por setters que redesenham
 * o botão), overriding de act().
 */
public class Botao extends ElementoEcra
{
    /** Altura do botão, em píxeis (40). */
    private static final int ALTURA = 40;
    /** Tamanho da letra do texto (18). */
    private static final int TAMANHO_LETRA = 18;

    /** Largura do botão, em píxeis (ex.: 380). */
    private final int largura;
    /** Cor da borda quando selecionado. */
    private final Color corDestaque;
    /** Texto do botão. */
    private String texto;
    /** true se é o botão selecionado no menu. */
    private boolean selecionado;
    /** false se o botão está desativado (cinzento e sem resposta). */
    private boolean ativo = true;
    /** true se foi clicado e ainda ninguém leu o clique. */
    private boolean clicado;
    /** true se o rato está por cima do botão. */
    private boolean ratoPorCima;

    /**
     * Botão com 380 px de largura e destaque lilás (overloading).
     *
     * @param texto Texto do botão (ex.: "Novo jogo")
     */
    public Botao(String texto)
    {
        this(texto, 380);
    }

    /**
     * Botão com largura à escolha e destaque lilás (overloading).
     *
     * @param texto Texto do botão (ex.: "Continuar")
     * @param largura Largura em píxeis (ex.: 380)
     */
    public Botao(String texto, int largura)
    {
        this(texto, largura, new Color(200, 180, 255));
    }

    /**
     * Botão completo.
     *
     * @param texto Texto do botão (ex.: "FECHAR  [ENTER / ESPAÇO]")
     * @param largura Largura em píxeis (ex.: 340)
     * @param corDestaque Cor da borda quando selecionado (ex.: new Color(255, 220, 140))
     */
    public Botao(String texto, int largura, Color corDestaque)
    {
        this.texto = texto;
        this.largura = largura;
        this.corDestaque = corDestaque;
        desenhar();
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine Actor.act): se estiver ativo, regista se o
     * rato está por cima e se houve um clique.
     */
    public void act()
    {
        if (!ativo) {
            return;
        }
        if (Greenfoot.mouseMoved(null)) {
            ratoPorCima = Greenfoot.mouseMoved(this);
        }
        if (Greenfoot.mouseClicked(this)) {
            clicado = true;
        }
    }

    /**
     * Lê e limpa o clique pendente.
     *
     * @return true uma única vez depois de o botão ser clicado com o rato
     */
    public boolean foiClicado()
    {
        boolean c = clicado;
        clicado = false;
        return c;
    }

    /**
     * Indica se o rato está por cima (o menu usa isto para mudar a seleção).
     *
     * @return true se o rato estiver por cima do botão
     */
    public boolean isRatoPorCima()
    {
        return ratoPorCima;
    }

    /**
     * Simula um clique, se o botão estiver ativo. Atualmente não é chamado no projeto
     * (o teclado é tratado diretamente pelo EcraMenu e não existem testes).
     */
    public void clicar()
    {
        if (ativo) {
            clicado = true;
        }
    }

    /**
     * Marca ou desmarca o botão como selecionado (só redesenha se mudar).
     *
     * @param s true para selecionar (ex.: true no botão escolhido com as setas)
     */
    public void setSelecionado(boolean s)
    {
        if (s != selecionado) {
            selecionado = s;
            desenhar();
        }
    }

    /**
     * Muda o texto do botão (só redesenha se mudar).
     *
     * @param t Novo texto (ex.: "Som: DESLIGADO")
     */
    public void setTexto(String t)
    {
        if (!t.equals(texto)) {
            texto = t;
            desenhar();
        }
    }

    /**
     * Ativa ou desativa o botão e redesenha-o.
     *
     * @param a true para ativo, false para desativado (ex.: false em "Continuar (sem progresso)")
     */
    public void setAtivo(boolean a)
    {
        ativo = a;
        desenhar();
    }

    /**
     * Devolve o texto do botão.
     *
     * @return texto atual (ex.: "Novo jogo")
     */
    public String getTexto()    { return texto; }

    /**
     * Indica se o botão está ativo.
     *
     * @return true se estiver ativo
     */
    public boolean isAtivo()    { return ativo; }

    /**
     * Indica se o botão está selecionado.
     *
     * @return true se for o botão selecionado
     */
    public boolean isSelecionado() { return selecionado; }
    //utilizado AI auxiliar para desenhar
    /** Desenha o botão: fundo, borda (dupla se selecionado) e texto centrado (entre setas quando selecionado). */
    private void desenhar()
    {
        GreenfootImage img = new GreenfootImage(largura, ALTURA);
        Color fundo = selecionado ? new Color(60, 40, 90, 230) : new Color(14, 10, 26, 210);
        img.setColor(fundo);
        img.fillRect(0, 0, largura, ALTURA);
        Color borda = !ativo ? new Color(70, 70, 80) : (selecionado ? corDestaque : new Color(110, 100, 140));
        img.setColor(borda);
        img.drawRect(0, 0, largura - 1, ALTURA - 1);
        if (selecionado) {
            img.drawRect(2, 2, largura - 5, ALTURA - 5);
        }
        img.setFont(fonte(TAMANHO_LETRA, selecionado));
        img.setColor(!ativo ? new Color(110, 110, 120) : (selecionado ? Color.WHITE : new Color(220, 214, 200)));
        String t = selecionado ? "> " + texto + " <" : texto;
        escreverCentrado(img, t, 26, TAMANHO_LETRA);
        setImage(img);
    }
}

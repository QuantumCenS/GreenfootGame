import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;
import java.util.List;

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira (segundo o comentário da classe)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Classe base ABSTRATA de todos os ecrãs com botões (menu inicial, pausa, leitura de notas,
 * fim de nível...). Trata da navegação, igual em todos:
 *  - CIMA/BAIXO (ou W/S) muda a seleção, ENTER/ESPAÇO/E escolhe;
 *  - ESQUERDA/DIREITA (ou A/D) chama aoMudarLateral() (ex.: escolher nível);
 *  - o rato também funciona: passar por cima seleciona, clicar escolhe.
 * As subclasses só dizem o que cada botão faz (template method: aoEscolher).
 *
 * Hierarquia: EcraMenu estende World; subclasses: MenuInicial, EcraHistoria, EcraInstrucoes,
 * EcraLeitura, EcraPausa e EcraFim.
 * Conceitos de POO: classe abstrata com método abstrato (aoEscolher), template method (act),
 * métodos hook com comportamento vazio por omissão (aoMudarLateral, aoVoltar), overloading
 * (adicionarBotao x2), overriding de World.act(), métodos estáticos utilitários de desenho.
 *
 * Nota: desenvolvido com apoio de IA (Gemini) — conversa em anexo no relatório.
 */
public abstract class EcraMenu extends World
{
    /** Ciclos iniciais em que as teclas são ignoradas (12). */
    private static final int ESPERA_INICIAL = 12;   // ignora teclas que vinham premidas do ecrã anterior

    /** Botões do ecrã, pela ordem de navegação. */
    private final List<Botao> botoes = new ArrayList<Botao>();
    /** Índice do botão selecionado. */
    private int selecionado;
    /** Ciclos desde que o ecrã abriu. */
    private int frames;
    /** Estado da tecla para cima no ciclo anterior (começa a true para ignorar teclas já premidas). */
    private boolean cimaAntes = true;
    /** Estado da tecla para baixo no ciclo anterior. */
    private boolean baixoAntes = true;
    /** Estado das teclas de escolher no ciclo anterior. */
    private boolean escolherAntes = true;
    /** Estado da tecla para a esquerda no ciclo anterior. */
    private boolean esquerdaAntes = true;
    /** Estado da tecla para a direita no ciclo anterior. */
    private boolean direitaAntes = true;
    /** Estado das teclas de voltar no ciclo anterior. */
    private boolean voltarAntes = true;

    /** Cria um ecrã com o tamanho do jogo (1024 x 572). */
    public EcraMenu()
    {
        super(Cenario.LARGURA, Cenario.ALTURA, 1);
    }

    /**
     * Contrato: o botão número indice foi escolhido.
     *
     * @param indice Posição do botão escolhido, a começar em 0 (ex.: 1 para "Novo jogo" no MenuInicial)
     */
    protected abstract void aoEscolher(int indice);

    /**
     * Hook: esquerda/direita sobre o botão indice (por omissão nada).
     *
     * @param indice Botão selecionado (ex.: 2, "Escolher missão" no MenuInicial)
     * @param direcao -1 esquerda, 1 direita (ex.: 1)
     */
    protected void aoMudarLateral(int indice, int direcao)
    {
    }

    /** Hook: ESC / BACKSPACE (por omissão nada). */
    protected void aoVoltar()
    {
    }

    /**
     * Acrescenta um botão centrado horizontalmente na altura y (overloading).
     *
     * @param botao Botão a acrescentar (ex.: new Botao("Novo jogo"))
     * @param y Coordenada y do centro do botão, em píxeis (ex.: 228)
     * @return o mesmo botão, para o guardar numa variável se for preciso
     */
    protected Botao adicionarBotao(Botao botao, int y)
    {
        return adicionarBotao(botao, getWidth() / 2, y);
    }

    /**
     * Acrescenta um botão na posição indicada (overloading).
     *
     * @param botao Botão a acrescentar (ex.: new Botao("Voltar"))
     * @param x Coordenada x do centro, em píxeis (ex.: 512)
     * @param y Coordenada y do centro, em píxeis (ex.: 520)
     * @return o mesmo botão
     */
    protected Botao adicionarBotao(Botao botao, int x, int y)
    {
        botoes.add(botao);
        addObject(botao, x, y);
        atualizarSelecao();
        return botao;
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine World.act): trata o rato (passar por cima
     * seleciona, clicar escolhe) e, depois dos 12 ciclos iniciais, as teclas de navegação.
     * Só reage no ciclo em que cada tecla é premida.
     */
    @Override
    public void act()
    {
        frames++;
        if (botoes.isEmpty()) {
            return;
        }
        if (!botoes.get(selecionado).isAtivo() || !botoes.get(selecionado).isSelecionado()) {
            atualizarSelecao();
        }
        for (int i = 0; i < botoes.size(); i++) {
            Botao b = botoes.get(i);
            if (b.isRatoPorCima() && b.isAtivo() && i != selecionado) {
                selecionado = i;
                atualizarSelecao();
            }
        }
        for (int i = 0; i < botoes.size(); i++) {
            if (botoes.get(i).foiClicado()) {
                selecionado = i;
                atualizarSelecao();
                escolher();
                return;
            }
        }
        boolean cima = Greenfoot.isKeyDown("up") || Greenfoot.isKeyDown("w");
        boolean baixo = Greenfoot.isKeyDown("down") || Greenfoot.isKeyDown("s");
        boolean escolherTecla = Greenfoot.isKeyDown("enter") || Greenfoot.isKeyDown("space") || Greenfoot.isKeyDown("e");
        boolean esquerda = Greenfoot.isKeyDown("left") || Greenfoot.isKeyDown("a");
        boolean direita = Greenfoot.isKeyDown("right") || Greenfoot.isKeyDown("d");
        boolean voltar = Greenfoot.isKeyDown("escape") || Greenfoot.isKeyDown("backspace");
        if (frames > ESPERA_INICIAL) {
            if (cima && !cimaAntes) {
                mover(-1);
            }
            if (baixo && !baixoAntes) {
                mover(1);
            }
            if (esquerda && !esquerdaAntes) {
                aoMudarLateral(selecionado, -1);
            }
            if (direita && !direitaAntes) {
                aoMudarLateral(selecionado, 1);
            }
            if (voltar && !voltarAntes) {
                aoVoltar();
            }
            if (escolherTecla && !escolherAntes) {
                escolher();
            }
        }
        cimaAntes = cima;
        baixoAntes = baixo;
        escolherAntes = escolherTecla;
        esquerdaAntes = esquerda;
        direitaAntes = direita;
        voltarAntes = voltar;
    }

    /** Escolhe o botão selecionado, se estiver ativo: toca o som e chama aoEscolher(). */
    private void escolher()
    {
        if (botoes.get(selecionado).isAtivo()) {
            Audio.tocar("menu_ok.wav");
            aoEscolher(selecionado);
        }
    }

    /**
     * Move a seleção, saltando botões inativos (dá a volta nas pontas).
     *
     * @param direcao -1 para cima, 1 para baixo (ex.: 1)
     */
    private void mover(int direcao)
    {
        for (int tentativa = 0; tentativa < botoes.size(); tentativa++) {
            selecionado = (selecionado + direcao + botoes.size()) % botoes.size();
            if (botoes.get(selecionado).isAtivo()) {
                break;
            }
        }
        Audio.tocar("menu_mover.wav");
        atualizarSelecao();
    }

    /** Garante que a seleção está num botão ativo e marca só esse botão como selecionado. */
    private void atualizarSelecao()
    {
        if (!botoes.isEmpty() && !botoes.get(selecionado).isAtivo()) {
            for (int i = 0; i < botoes.size(); i++) {
                if (botoes.get(i).isAtivo()) {
                    selecionado = i;
                    break;
                }
            }
        }
        for (int i = 0; i < botoes.size(); i++) {
            botoes.get(i).setSelecionado(i == selecionado);
        }
    }

    /** Volta a validar a seleção (chamar depois de ativar/desativar botões). */
    protected void reverSelecao()
    {
        atualizarSelecao();
    }

    /**
     * Escolhe um botão por programa, como se o jogador o escolhesse. Atualmente não é chamado
     * no projeto (não existem testes nem atalhos que o usem).
     *
     * @param indice Posição do botão, a começar em 0 (ex.: 0)
     */
    public void escolherBotao(int indice)
    {
        selecionado = indice;
        atualizarSelecao();
        escolher();
    }

    /**
     * Devolve um botão pela posição.
     *
     * @param indice Posição do botão, a começar em 0 (ex.: 1)
     * @return o botão nessa posição
     */
    protected Botao getBotao(int indice)
    {
        return botoes.get(indice);
    }

    /**
     * Devolve o número de botões do ecrã.
     *
     * @return número de botões (ex.: 2 no EcraFim de nível concluído)
     */
    public int getNumeroBotoes()
    {
        return botoes.size();
    }

    /**
     * Devolve o índice do botão selecionado.
     *
     * @return posição do botão selecionado, a começar em 0
     */
    public int getSelecionado()
    {
        return selecionado;
    }

    /**
     * Devolve o texto de um botão.
     *
     * @param indice Posição do botão, a começar em 0 (ex.: 0)
     * @return texto do botão
     */
    public String getTextoBotao(int indice)
    {
        return botoes.get(indice).getTexto();
    }

    // utilitários de desenho

    /**
     * Cria o fundo do menu: uma cópia da imagem escurecida com uma camada preta semitransparente.
     *
     * @param base Imagem de base (ex.: new GreenfootImage("fundo_menu.png") ou a captura do nível)
     * @param alfa Opacidade da camada preta, de 0 a 255 (ex.: 170)
     * @return nova imagem escurecida
     */
    protected static GreenfootImage fundoEscurecido(GreenfootImage base, int alfa)
    {
        GreenfootImage img = new GreenfootImage(base);
        img.setColor(new Color(0, 0, 0, alfa));
        img.fillRect(0, 0, img.getWidth(), img.getHeight());
        return img;
    }

    /**
     * Escreve texto centrado horizontalmente numa imagem (com a fonte e a cor já definidas).
     *
     * @param img Imagem onde escrever (ex.: o fundo do ecrã)
     * @param texto Texto a escrever (ex.: "INSTRUÇÕES")
     * @param y Coordenada y da linha de base, em píxeis (ex.: 56)
     * @param tamanho Tamanho da letra usado para estimar a largura (ex.: 34)
     */
    protected static void centrar(GreenfootImage img, String texto, int y, int tamanho)
    {
        int largura = (int) (texto.length() * tamanho * 0.61);
        img.drawString(texto, (img.getWidth() - largura) / 2, y);
    }

    /**
     * Escreve um parágrafo centrado, partido em linhas.
     *
     * @param img Imagem onde escrever (ex.: o fundo do EcraHistoria)
     * @param texto Texto do parágrafo (ex.: ArquivoLore.TEXTO_INTRODUCAO)
     * @param y Coordenada y da primeira linha, em píxeis (ex.: 150)
     * @param tamanho Tamanho da letra (ex.: 17)
     * @param caracteres Máximo de caracteres por linha (ex.: 84)
     * @param espaco Distância entre linhas, em píxeis (ex.: 23)
     * @return y depois do texto
     */
    protected static int paragrafo(GreenfootImage img, String texto, int y, int tamanho, int caracteres, int espaco)
    {
        for (String linha : ElementoEcra.quebrarTexto(texto, caracteres)) {
            centrar(img, linha, y, tamanho);
            y += espaco;
        }
        return y;
    }
}

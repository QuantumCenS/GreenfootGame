import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Ecrã de leitura de uma nota ou ficheiro classificado. O jogo fica PAUSADO
 * (este é outro World) e por trás vê-se a imagem congelada do nível.
 * Fecha-se com o botão "FECHAR" (rato) ou ENTER / ESPAÇO / E / ESC / BACKSPACE — e o jogo
 * continua exatamente onde estava (volta ao mesmo objeto Cenario).
 *
 * Hierarquia: EcraLeitura estende EcraMenu, que estende World.
 * Conceitos de POO: herança, overriding de aoEscolher (abstrato) e do hook aoVoltar,
 * composição (mostra um PainelNota).
 */
public class EcraLeitura extends EcraMenu
{
    /** Nível de onde se veio (para onde se volta ao fechar). */
    private final Cenario origem;

    /**
     * Cria o ecrã: fundo com a imagem do nível escurecida, o pergaminho e o botão "FECHAR".
     *
     * @param origem Nível de onde se veio (ex.: this, em Cenario.recolherNota)
     * @param titulo Título do texto (ex.: ArquivoLore.getTituloNota("N1-1"))
     * @param texto Texto completo (ex.: ArquivoLore.getTextoNota("N1-1"))
     * @param classificado true para um ficheiro classificado, false para uma nota (ex.: false)
     */
    public EcraLeitura(Cenario origem, String titulo, String texto, boolean classificado)
    {
        this.origem = origem;
        setBackground(fundoEscurecido(origem.capturarEcra(), 150));
        PainelNota painel = new PainelNota(titulo, texto, classificado);
        int alturaPainel = painel.getImage().getHeight();
        int y = Math.max(60 + alturaPainel / 2, (getHeight() - 60) / 2);
        addObject(painel, getWidth() / 2, y);
        int yBotao = Math.min(getHeight() - 28, y + alturaPainel / 2 + 30);
        adicionarBotao(new Botao("FECHAR  [ENTER / ESPAÇO]", 340, new Color(255, 220, 140)), yBotao);
    }

    /**
     * Implementa EcraMenu.aoEscolher: fecha e volta ao nível.
     *
     * @param indice Botão escolhido (ex.: 0, "FECHAR")
     */
    @Override
    protected void aoEscolher(int indice)
    {
        fechar();
    }

    /** Redefine o hook de EcraMenu: ESC/BACKSPACE também fecha. */
    @Override
    protected void aoVoltar()
    {
        fechar();
    }

    /** Volta ao nível de origem, que continua exatamente onde estava. */
    private void fechar()
    {
        Greenfoot.setWorld(origem);
    }

    /**
     * Devolve o nível de origem.
     *
     * @return nível para onde o ecrã volta ao fechar
     */
    public Cenario getOrigem()
    {
        return origem;
    }
}

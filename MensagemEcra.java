import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: código de desenho/gráficos (construtor e desvanecimento em act)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Mensagem grande e temporária no centro-topo do ecrã ("DETETADOS!", nome do nível...).
 * Criada por Cenario.mostrarMensagem(), que substitui a mensagem anterior.
 *
 * Hierarquia: MensagemEcra estende ElementoEcra, que estende Actor (fica fixa no ecrã).
 * Conceitos de POO: herança (usa fonte(), larguraCaracter(), escreverCentrado() e remover()),
 * overriding de act().
 */
public class MensagemEcra extends ElementoEcra
{
    /** Tamanho da letra da mensagem (22). */
    private static final int TAMANHO_LETRA = 22;

    /** Duração total, em ciclos (mínimo 1). */
    private final int duracaoTotal;
    /** Ciclos que faltam para desaparecer. */
    private int duracao;

    /**
     * Cria a mensagem: faixa escura semitransparente com riscas da cor indicada e o texto centrado.
     *
     * @param texto Texto a mostrar (ex.: "DETETADOS! -1 sanidade")
     * @param cor Cor das riscas e do texto (ex.: new Color(255, 70, 70))
     * @param duracao Duração em ciclos, mínimo 1 (ex.: 120)
     */
    public MensagemEcra(String texto, Color cor, int duracao)
    {
        this.duracaoTotal = Math.max(1, duracao);
        this.duracao = this.duracaoTotal;
        int largura = Math.max(200, texto.length() * larguraCaracter(TAMANHO_LETRA) + 60);
        GreenfootImage img = new GreenfootImage(largura, 44);
        img.setColor(new Color(0, 0, 0, 170));
        img.fillRect(0, 0, largura, 44);
        img.setColor(cor);
        img.fillRect(0, 0, largura, 2);
        img.fillRect(0, 42, largura, 2);
        img.setFont(fonte(TAMANHO_LETRA, true));
        escreverCentrado(img, texto, 30, TAMANHO_LETRA);
        setImage(img);
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine Actor.act): desconta a duração, desvanece nos
     * últimos 20 ciclos e remove-se no fim.
     */
    public void act()
    {
        duracao--;
        if (duracao <= 0) {
            remover();
        } else if (duracao < 20) {
            getImage().setTransparency(duracao * 12);
        }
    }
}

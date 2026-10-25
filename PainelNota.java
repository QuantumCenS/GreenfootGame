import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: código de desenho/gráficos (método criarImagem)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Pergaminho com o texto de uma nota de lore ou de um ficheiro classificado.
 * Aparece no EcraLeitura (o jogo fica em pausa) até o jogador o fechar com o botão.
 *
 * Hierarquia: PainelNota estende ElementoEcra, que estende Actor.
 * Conceitos de POO: herança (usa fonte(), larguraCaracter() e quebrarTexto() de ElementoEcra),
 * método estático público reutilizável (criarImagem).
 */
public class PainelNota extends ElementoEcra
{
    /** Largura do pergaminho, em píxeis (680). */
    public static final int LARGURA = 680;
    /** Tamanho da letra do texto (16). */
    private static final int TAMANHO_TEXTO = 16;
    /** Distância entre linhas de texto, em píxeis (22). */
    private static final int ESPACO_LINHA = 22;

    /**
     * Cria o painel com a imagem do pergaminho.
     *
     * @param titulo Título da nota ou dossier (ex.: "Diário de um pescador — 3 de março")
     * @param texto Texto completo (ex.: ArquivoLore.getTextoNota("N1-1"))
     * @param classificado true para o estilo de dossier (carimbo vermelho) (ex.: true num FicheiroSecreto)
     */
    public PainelNota(String titulo, String texto, boolean classificado)
    {
        setImage(criarImagem(titulo, texto, classificado));
    }

    /**
     * Desenha o pergaminho (estático para poder ser reutilizado noutros ecrãs). Foi utilizado AI para estes efeitos.
     * A altura ajusta-se ao número de linhas do texto.
     *
     * @param titulo Título a escrever no topo (ex.: "PROCESSO 7-B — MANIFESTO \"MARÉ NEGRA\"")
     * @param texto Texto a partir em linhas (ex.: ArquivoLore.getTextoDossier("D1"))
     * @param classificado true para o papel mais escuro, título vermelho e carimbo "CLASSIFICADO";
     *                     false para mostrar "+100 pontos" (ex.: false numa nota de lore)
     * @return nova imagem do pergaminho
     */
    public static GreenfootImage criarImagem(String titulo, String texto, boolean classificado)
    {
        int caracteres = (LARGURA - 56) / larguraCaracter(TAMANHO_TEXTO);
        java.util.List<String> linhas = quebrarTexto(texto, caracteres);
        int altura = 84 + linhas.size() * ESPACO_LINHA + 30;
        GreenfootImage img = new GreenfootImage(LARGURA, altura);
        img.setColor(classificado ? new Color(214, 190, 140) : new Color(226, 212, 170));
        img.fillRect(0, 0, LARGURA, altura);
        img.setColor(new Color(150, 120, 80, 50));
        for (int i = 0; i < 46; i++) {    // manchas de papel velho
            img.fillOval((i * 97) % LARGURA, (i * 53) % altura, 8 + i % 7, 5 + i % 5);
        }
        img.setColor(new Color(110, 84, 50));
        img.drawRect(0, 0, LARGURA - 1, altura - 1);
        img.drawRect(4, 4, LARGURA - 9, altura - 9);
        img.setColor(classificado ? new Color(160, 20, 30) : new Color(60, 40, 30));
        img.setFont(fonte(18, true));
        img.drawString(titulo, 26, 40);
        img.setColor(new Color(110, 84, 50));
        img.drawLine(26, 52, LARGURA - 26, 52);
        img.setColor(new Color(40, 28, 20));
        img.setFont(fonte(TAMANHO_TEXTO, false));
        for (int i = 0; i < linhas.size(); i++) {
            img.drawString(linhas.get(i), 26, 82 + i * ESPACO_LINHA);
        }
        if (classificado) {
            img.setColor(new Color(190, 20, 30, 200));
            img.setFont(fonte(17, true));
            img.drawRect(LARGURA - 216, altura - 46, 184, 30);
            img.drawRect(LARGURA - 214, altura - 44, 180, 26);
            img.drawString("CLASSIFICADO", LARGURA - 196, altura - 25);
        } else {
            img.setColor(new Color(110, 84, 50));
            img.setFont(fonte(14, false));
            img.drawString("+" + EstadoMissao.PONTOS_POR_NOTA + " pontos", 26, altura - 18);
        }
        return img;
    }
}

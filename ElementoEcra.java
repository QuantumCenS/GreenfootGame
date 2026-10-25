import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira (utilitários de texto: quebrarTexto, larguraCaracter)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Classe base ABSTRATA dos elementos de interface fixos no ecrã (HUD, painéis, mensagens).
 * O Cenario NÃO os desloca quando a câmara faz scroll.
 * Inclui utilitários de texto partilhados (quebra de linhas, fonte).
 *
 * Hierarquia: ElementoEcra estende Actor; subclasses: Botao, Hud, MensagemEcra, PainelHacking, PainelNota.
 * Conceitos de POO: classe abstrata, herança (as subclasses reutilizam os métodos protegidos
 * de texto e remover()), métodos estáticos utilitários.
 */
public abstract class ElementoEcra extends Actor
{
    /**
     * Cria a fonte monoespaçada usada na interface: cada carácter ocupa ~0,6 do tamanho,
     * o que facilita medir texto.
     *
     * @param tamanho Tamanho da letra em pontos (ex.: 17 no HUD)
     * @param negrito true para negrito (ex.: true nos títulos)
     * @return fonte "Monospaced" com o tamanho e estilo pedidos
     */
    protected static Font fonte(int tamanho, boolean negrito)
    {
        return new Font("Monospaced", negrito, false, tamanho);
    }

    /**
     * Calcula a largura aproximada de um carácter na fonte monoespaçada.
     *
     * @param tamanho Tamanho da letra em pontos (ex.: 22 em MensagemEcra)
     * @return largura aproximada de um carácter em píxeis (ex.: 14 para tamanho 22)
     */
    protected static int larguraCaracter(int tamanho)
    {
        return (int) Math.ceil(tamanho * 0.61);
    }

    /**
     * Parte o texto em linhas com no máximo maxCaracteres, sem cortar palavras
     * e respeitando as quebras de linha ('\n') já existentes.
     *
     * @param texto Texto a partir (ex.: o texto de uma nota de lore)
     * @param maxCaracteres Número máximo de caracteres por linha (ex.: 34 nas placas Dica)
     * @return lista de linhas pela ordem em que devem ser escritas
     */
    public static java.util.List<String> quebrarTexto(String texto, int maxCaracteres)
    {
        java.util.List<String> linhas = new java.util.ArrayList<String>();
        for (String paragrafo : texto.split("\n")) {
            StringBuilder linha = new StringBuilder();
            for (String palavra : paragrafo.split(" ")) {
                if (linha.length() > 0 && linha.length() + 1 + palavra.length() > maxCaracteres) {
                    linhas.add(linha.toString());
                    linha = new StringBuilder();
                }
                if (linha.length() > 0) {
                    linha.append(' ');
                }
                linha.append(palavra);
            }
            linhas.add(linha.toString());
        }
        return linhas;
    }

    /**
     * Escreve texto centrado horizontalmente numa imagem (usa a fonte já definida na imagem).
     *
     * @param img Imagem onde escrever (ex.: a imagem de um Botao)
     * @param texto Texto a escrever (ex.: "ACESSO CONCEDIDO")
     * @param y Coordenada y da linha de base do texto, em píxeis (ex.: 26)
     * @param tamanho Tamanho da letra usado para estimar a largura (ex.: 18)
     */
    protected static void escreverCentrado(GreenfootImage img, String texto, int y, int tamanho)
    {
        int largura = texto.length() * larguraCaracter(tamanho);
        img.drawString(texto, (img.getWidth() - largura) / 2, y);
    }

    /** Remove o elemento do mundo em segurança (não faz nada se já tiver sido removido). */
    protected void remover()
    {
        if (getWorld() != null) {
            getWorld().removeObject(this);
        }
    }
}

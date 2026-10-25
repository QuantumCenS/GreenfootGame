import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Placa de madeira com uma dica que aparece quando um agente se aproxima (tutorial).
 *
 * Hierarquia: Dica estende ElementoInterativo, que estende Actor.
 * Conceitos de POO: herança, overriding de act(), reutilização de métodos estáticos de outras
 * classes (Balao.criarImagem, ElementoEcra.quebrarTexto).
 */
public class Dica extends ElementoInterativo
{
    /** Distância horizontal a que a dica aparece, em píxeis (150). */
    private static final int DISTANCIA = 150;
    /** Número máximo de caracteres por linha do texto (34). */
    private static final int CARACTERES_POR_LINHA = 34;

    /** Imagem só com a placa (dica escondida). */
    private final GreenfootImage imagemPlaca;
    /** Imagem com a placa e o balão de texto por cima. */
    private final GreenfootImage imagemComTexto;
    /** true se o texto está visível. */
    private boolean visivel;

    /**
     * Cria a placa e prepara as duas imagens (com e sem o balão de texto).
     *
     * @param texto Texto da dica (ex.: "Este caixote é pesado: empurrem-no OS DOIS até à placa.")
     */
    public Dica(String texto)
    {
        GreenfootImage placa = new GreenfootImage("placa_aviso.png");
        GreenfootImage balao = Balao.criarImagem(juntar(ElementoEcra.quebrarTexto(texto, CARACTERES_POR_LINHA)),
                                                 new Color(40, 30, 20));
        int largura = Math.max(placa.getWidth(), balao.getWidth());
        int altura = placa.getHeight() + balao.getHeight() + 4;
        imagemPlaca = new GreenfootImage(largura, altura);
        imagemPlaca.drawImage(placa, (largura - placa.getWidth()) / 2, altura - placa.getHeight());
        imagemComTexto = new GreenfootImage(imagemPlaca);
        imagemComTexto.drawImage(balao, (largura - balao.getWidth()) / 2, 0);
        setImage(imagemPlaca);
    }

    /**
     * Junta as linhas num único texto separado por quebras de linha.
     *
     * @param linhas Linhas a juntar (ex.: o resultado de ElementoEcra.quebrarTexto)
     * @return texto com as linhas separadas por '\n'
     */
    private static String juntar(java.util.List<String> linhas)
    {
        StringBuilder sb = new StringBuilder();
        for (String l : linhas) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(l);
        }
        return sb.toString();
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine ElementoInterativo.act): mostra o texto se algum
     * agente estiver a menos de 150 px na horizontal e esconde-o caso contrário.
     */
    public void act()
    {
        boolean perto = false;
        for (AgenteInvestigador a : getWorld().getObjects(AgenteInvestigador.class)) {
            if (Math.abs(a.getX() - getX()) < DISTANCIA) {
                perto = true;
            }
        }
        if (perto != visivel) {
            visivel = perto;
            setImage(visivel ? imagemComTexto : imagemPlaca);
        }
    }
}

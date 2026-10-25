import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Elevador de pedra rúnica que só sobe quando os DOIS agentes (Zig e Zag) estão em cima.
 * Se algum sair, as runas apagam-se e a rocha volta devagar à origem.
 * É o mecanismo de cooperação obrigatória dos níveis.
 *
 * Hierarquia: Actor -> ElementoInterativo -> Plataforma -> PlataformaMovel -> RochaRunica
 * (4 níveis além de Actor).
 * Conceitos de POO: herança em vários níveis, overriding de calcularProgresso() (muda só a regra
 * do movimento e reutiliza o resto de PlataformaMovel), overloading de construtores (3 versões).
 */
public class RochaRunica extends PlataformaMovel
{
    /** Ciclos para chegar ao destino com os dois agentes em cima (200). */
    private static final int FRAMES_PERCURSO = 200;   // frames para chegar ao destino

    /** Imagem com as runas apagadas (parada ou a descer). */
    private final GreenfootImage imagemApagada;
    /** Imagem com as runas acesas (a subir). */
    private final GreenfootImage imagemAcesa;
    /** Progresso linear entre 0 (origem) e 1 (destino). */
    private double progresso;
    /** true se no último ciclo os dois agentes estavam em cima. */
    private boolean ativada;

    /**
     * Rocha que sobe/desce na vertical (overloading).
     *
     * @param largura Largura em píxeis (ex.: 96)
     * @param alcanceY Deslocamento vertical, negativo = para cima (ex.: -326 no Cenario2)
     */
    public RochaRunica(int largura, int alcanceY)
    {
        this(largura, 0, alcanceY);
    }

    /**
     * Rocha com as texturas rúnicas padrão (overloading).
     *
     * @param largura Largura em píxeis (ex.: 96)
     * @param alcanceX Deslocamento horizontal em píxeis (ex.: 0)
     * @param alcanceY Deslocamento vertical, negativo = para cima (ex.: -350 no Cenario5)
     */
    public RochaRunica(int largura, int alcanceX, int alcanceY)
    {
        this(largura, alcanceX, alcanceY, "runica_off.png", "runica_on.png");
    }

    /**
     * Versão com outras texturas (ex.: o monta-cargas da biblioteca: "elevador_off.png"/"elevador_on.png").
     * O mecanismo é o mesmo: só se move com os dois agentes em cima.
     *
     * @param largura Largura em píxeis (ex.: 96)
     * @param alcanceX Deslocamento horizontal em píxeis (ex.: 0)
     * @param alcanceY Deslocamento vertical, negativo = para cima (ex.: -330 no Cenario3)
     * @param texturaApagada Ficheiro da textura com as runas apagadas (ex.: "elevador_off.png")
     * @param texturaAcesa Ficheiro da textura com as runas acesas (ex.: "elevador_on.png")
     */
    public RochaRunica(int largura, int alcanceX, int alcanceY, String texturaApagada, String texturaAcesa)
    {
        super(largura, TipoPlataforma.RUNICA, alcanceX, alcanceY, FRAMES_PERCURSO * 2);
        imagemApagada = criarImagem(largura, getAltura(), texturaApagada, texturaApagada);
        imagemAcesa = criarImagem(largura, getAltura(), texturaAcesa, texturaAcesa);
        setImage(imagemApagada);
    }

    /**
     * Overriding de PlataformaMovel.calcularProgresso: avança com os dois agentes em cima,
     * recua (a metade da velocidade) caso contrário, e troca a imagem (runas acesas/apagadas).
     *
     * @return progresso suavizado entre 0.0 e 1.0
     */
    @Override
    protected double calcularProgresso()
    {
        ativada = todosOsAgentesEmCima();
        double passo = 1.0 / FRAMES_PERCURSO;
        if (ativada) {
            progresso = Math.min(1.0, progresso + passo);
        } else {
            progresso = Math.max(0.0, progresso - passo / 2);
        }
        setImage(ativada ? imagemAcesa : imagemApagada);
        return progresso * progresso * (3 - 2 * progresso);   // suaviza arranque e paragem
    }

    /**
     * Verifica se todos os agentes do nível estão apoiados nesta rocha.
     *
     * @return true se houver agentes e todos estiverem em cima; false caso contrário
     */
    public boolean todosOsAgentesEmCima()
    {
        List<AgenteInvestigador> agentes = getWorld().getObjects(AgenteInvestigador.class);
        if (agentes.isEmpty()) {
            return false;
        }
        for (AgenteInvestigador agente : agentes) {
            if (agente.getPlataformaApoio() != this) {
                return false;
            }
        }
        return true;
    }

    /**
     * Indica se a rocha está ativada (os dois agentes em cima).
     *
     * @return true se no último ciclo estava a subir
     */
    public boolean isAtivada()
    {
        return ativada;
    }
}

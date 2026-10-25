import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Placa de pressão no chão. Fica premida enquanto um agente ou uma CaixaPesada estiver
 * em cima dela; abre os mecanismos ligados ao ser premida e fecha-os ao ser solta.
 * Também pode não ter mecanismos e ser lida por um PortaoSelado (várias placas ao mesmo tempo).
 *
 * Hierarquia: PlacaPressao estende ElementoInterativo, que estende Actor.
 * Conceitos de POO: herança, polimorfismo através da interface Mecanismo (abre qualquer mecanismo
 * sem conhecer a classe concreta), parâmetros variáveis (varargs), overriding de act().
 */
public class PlacaPressao extends ElementoInterativo
{
    /** Diferença máxima entre os pés (ou a base da caixa) e a base da placa, em píxeis (4). */
    private static final int TOLERANCIA = 4;

    /** Mecanismos abertos/fechados por esta placa. */
    private final List<Mecanismo> mecanismos;
    /** Imagem da placa solta. */
    private final GreenfootImage imagemSolta;
    /** Imagem da placa premida. */
    private final GreenfootImage imagemPremida;
    /** true enquanto tem peso em cima. */
    private boolean premida;

    /**
     * Cria uma placa ligada aos mecanismos indicados (pode não ter nenhum).
     *
     * @param mecanismos Mecanismos a acionar (ex.: o portão do pátio no Cenario1; nenhum nas placas de um PortaoSelado)
     */
    public PlacaPressao(Mecanismo... mecanismos)
    {
        this.mecanismos = new ArrayList<Mecanismo>(Arrays.asList(mecanismos));
        imagemSolta = new GreenfootImage("placa_solta.png");
        imagemPremida = new GreenfootImage("placa_premida.png");
        setImage(imagemSolta);
    }

    /**
     * Liga mais um mecanismo a esta placa. Atualmente não é chamado no projeto.
     *
     * @param mecanismo Mecanismo a acionar também (ex.: um Portao)
     */
    public void ligar(Mecanismo mecanismo)
    {
        mecanismos.add(mecanismo);
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine ElementoInterativo.act): quando o estado muda
     * (premida/solta), troca a imagem, toca o som e abre ou fecha os mecanismos.
     */
    public void act()
    {
        boolean agora = temPesoEmCima();
        if (agora != premida) {
            premida = agora;
            setImage(premida ? imagemPremida : imagemSolta);
            Audio.tocar("placa.wav");
            for (Mecanismo m : mecanismos) {
                if (premida) {
                    m.abrir();
                } else {
                    m.fechar();
                }
            }
        }
    }

    /**
     * Verifica se há peso em cima: um agente no chão com os pés na placa, ou uma caixa pesada
     * que cubra pelo menos metade da placa.
     *
     * @return true se houver peso em cima
     */
    private boolean temPesoEmCima()
    {
        for (AgenteInvestigador a : getWorld().getObjects(AgenteInvestigador.class)) {
            if (a.isNoChao() && Math.abs(a.getBaseColisao() - getBase()) <= TOLERANCIA
                && a.getX() >= getEsquerda() && a.getX() <= getDireita()) {
                return true;
            }
        }
        for (CaixaPesada c : getWorld().getObjects(CaixaPesada.class)) {
            int sobreposicao = Math.min(c.getDireita(), getDireita()) - Math.max(c.getEsquerda(), getEsquerda());
            if (Math.abs(c.getBase() - getBase()) <= TOLERANCIA && sobreposicao >= getLargura() / 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indica se a placa está premida.
     *
     * @return true enquanto tiver peso em cima
     */
    public boolean isPremida()
    {
        return premida;
    }
}

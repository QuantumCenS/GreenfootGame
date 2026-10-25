import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Selo de pedra que só abre quando TODAS as suas placas estão premidas ao mesmo tempo
 * (um agente em cada uma) e depois fica aberto para sempre. Cooperação obrigatória.
 *
 * Hierarquia: PortaoSelado estende Portao, que estende Plataforma, que estende ElementoInterativo,
 * que estende Actor (4 níveis além de Actor).
 * Conceitos de POO: herança, overriding com reutilização de super (act, fechar), parâmetros
 * variáveis (varargs) no construtor.
 */
public class PortaoSelado extends Portao
{
    /** Placas que têm de estar todas premidas ao mesmo tempo. */
    private final List<PlacaPressao> placas;
    /** true depois de o selo abrir (já não volta a fechar). */
    private boolean quebrado;

    /**
     * Cria um selo de pedra ligado às placas indicadas.
     *
     * @param altura Altura do selo fechado, em píxeis (ex.: 156, no Cenario2)
     * @param placas Placas que têm de estar premidas ao mesmo tempo (ex.: placaChao, placaAlta)
     */
    public PortaoSelado(int altura, PlacaPressao... placas)
    {
        super(altura, TipoPlataforma.PORTAO_PEDRA);
        this.placas = new ArrayList<PlacaPressao>(Arrays.asList(placas));
    }

    /**
     * Redefine Portao.act (chamado pelo Greenfoot em cada ciclo): na primeira vez em que todas
     * as placas estão premidas, abre e mostra "O selo abriu-se!"; depois executa o act() do Portao.
     */
    @Override
    public void act()
    {
        if (!quebrado && !placas.isEmpty() && todasPremidas()) {
            quebrado = true;
            abrir();
            if (getWorld() instanceof Cenario) {
                ((Cenario) getWorld()).mostrarMensagem("O selo abriu-se!", new Color(190, 130, 255), 120);
            }
        }
        super.act();
    }

    /**
     * Verifica se todas as placas ligadas estão premidas.
     *
     * @return true se todas estiverem premidas
     */
    private boolean todasPremidas()
    {
        for (PlacaPressao p : placas) {
            if (!p.isPremida()) {
                return false;
            }
        }
        return true;
    }

    /** Depois de quebrado já não fecha (overriding de Portao.fechar). */
    @Override
    public void fechar()
    {
        if (!quebrado) {
            super.fechar();
        }
    }

    /**
     * Indica se o selo já foi quebrado.
     *
     * @return true se já abriu
     */
    public boolean isQuebrado()
    {
        return quebrado;
    }
}

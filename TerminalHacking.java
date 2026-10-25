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
 * Terminal de segurança da Ordem Abissal. Só o Zag (podeHackear) o consegue usar:
 * junto ao terminal, a tecla de ação abre o PainelHacking. Se resolver o puzzle a tempo,
 * todos os sistemas ligados (Hackeavel: câmaras, barreiras laser, cofres) são desativados.
 * Se o tempo acabar, dispara o alarme e o terminal bloqueia uns segundos.
 *
 * Hierarquia: TerminalHacking estende ElementoInterativo, que estende Actor.
 * Conceitos de POO: herança, polimorfismo através da interface Hackeavel (desativa alvos de
 * classes diferentes da mesma forma), enum interno privado, overloading de construtores,
 * overriding de act().
 */
public class TerminalHacking extends ElementoInterativo
{
    /** Estados do terminal. */
    private enum EstadoTerminal {
        /** Disponível para hackear. */
        ATIVO,
        /** O mini-jogo está a decorrer. */
        EM_USO,
        /** Bloqueado durante alguns segundos depois de um hacking falhado. */
        BLOQUEADO,
        /** Hackeado com sucesso (permanente). */
        HACKEADO
    }

    /** Distância horizontal máxima para usar o terminal, em píxeis (46). */
    private static final int DISTANCIA_USO = 46;
    /** Ciclos de bloqueio depois de um hacking falhado (180, cerca de 3 s). */
    private static final int FRAMES_BLOQUEIO = 180;

    /** Sistemas desativados quando o hacking é bem-sucedido. */
    private final List<Hackeavel> alvos;
    /** Largura da grelha do puzzle (ex.: 5). */
    private final int colunas;
    /** Altura da grelha do puzzle (ex.: 4). */
    private final int linhas;
    /** Tempo limite do puzzle, em segundos (ex.: 35). */
    private final int segundos;
    /** Estado atual do terminal. */
    private EstadoTerminal estado;
    /** Ciclos de bloqueio restantes. */
    private int framesBloqueio;
    /** Balão com a dica de tecla, ou null. */
    private Balao dica;
    /** Texto do balão atual (para não o recriar em cada ciclo). */
    private String textoDica;
    /** Agente que está a hackear, ou null. */
    private AgenteInvestigador hackerAtual;

    /**
     * Terminal com puzzle 5x4 e 35 segundos (overloading).
     *
     * @param alvos Sistemas desativados quando o hacking é bem-sucedido (ex.: camara, barreira)
     */
    public TerminalHacking(Hackeavel... alvos)
    {
        this(5, 4, 35, alvos);
    }

    /**
     * Terminal com puzzle e tempo à escolha.
     *
     * @param colunas  largura da grelha do puzzle (ex.: 5 no Cenario1, 6 no Cenario4)
     * @param linhas   altura da grelha (ex.: 4)
     * @param segundos tempo limite, em segundos (ex.: 35)
     * @param alvos    sistemas desativados quando o hacking é bem-sucedido (ex.: camara, barreira)
     */
    public TerminalHacking(int colunas, int linhas, int segundos, Hackeavel... alvos)
    {
        this.alvos = new ArrayList<Hackeavel>(Arrays.asList(alvos));
        this.colunas = colunas;
        this.linhas = linhas;
        this.segundos = segundos;
        this.estado = EstadoTerminal.ATIVO;
        setImage("terminal_ativo.png");
    }

    /**
     * Liga mais um sistema a este terminal. Atualmente não é chamado no projeto.
     *
     * @param alvo Sistema a desativar também (ex.: um Cofre)
     */
    public void adicionarAlvo(Hackeavel alvo)
    {
        alvos.add(alvo);
    }

    /**
     * Procura um terminal ativo junto do agente.
     *
     * @param agente Agente que quer hackear (ex.: o Zag, ao carregar em ENTER)
     * @return terminal ativo perto do agente, ou null se não houver
     */
    public static TerminalHacking procurarPara(AgenteInvestigador agente)
    {
        for (TerminalHacking t : agente.getWorld().getObjects(TerminalHacking.class)) {
            if (t.estado == EstadoTerminal.ATIVO && t.estaPerto(agente)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Verifica se o agente está perto o suficiente para usar o terminal
     * (46 px na horizontal e 30 px de diferença na altura dos pés).
     *
     * @param agente Agente a testar (ex.: o Zag)
     * @return true se estiver ao alcance
     */
    public boolean estaPerto(AgenteInvestigador agente)
    {
        return Math.abs(agente.getX() - getX()) <= DISTANCIA_USO && Math.abs(agente.getBaseColisao() - getBase()) <= 30;
    }

    /**
     * Começa o mini-jogo: ocupa o agente com a animação de hackear e abre o PainelHacking
     * no topo do ecrã, com um puzzle novo e o tempo em ciclos (segundos x 60).
     *
     * @param agente Agente que vai hackear (ex.: o Zag)
     * @return false se o agente não sabe hackear ou o terminal não está disponível
     */
    public boolean iniciarHacking(AgenteInvestigador agente)
    {
        if (!agente.podeHackear() || estado != EstadoTerminal.ATIVO || getWorld() == null) {
            return false;
        }
        estado = EstadoTerminal.EM_USO;
        hackerAtual = agente;
        esconderDica();
        agente.virarPara(getX());
        agente.ocuparIndefinidamente(EstadoAgente.HACKEAR);
        Audio.tocar("terminal.wav");
        PainelHacking painel = new PainelHacking(this, agente, new PuzzleCircuito(colunas, linhas), segundos * 60);
        getWorld().addObject(painel, getWorld().getWidth() / 2, 52 + painel.getImage().getHeight() / 2);
        return true;
    }

    /**
     * Chamado pelo PainelHacking quando o mini-jogo termina: liberta o agente e, conforme o resultado,
     * desativa os alvos (SUCESSO), dispara o alarme e bloqueia (FALHOU) ou volta a ficar ativo (CANCELADO).
     *
     * @param resultado Resultado do mini-jogo (ex.: PainelHacking.Resultado.SUCESSO)
     */
    public void concluirHacking(PainelHacking.Resultado resultado)
    {
        if (hackerAtual != null) {
            hackerAtual.libertar();
            hackerAtual = null;
        }
        Cenario cenario = (getWorld() instanceof Cenario) ? (Cenario) getWorld() : null;
        switch (resultado) {
            case SUCESSO:
                estado = EstadoTerminal.HACKEADO;
                setImage("terminal_hackeado.png");
                for (Hackeavel alvo : alvos) {
                    alvo.desativarPorHacking();
                }
                Audio.tocar("hack_ok.wav");
                if (cenario != null) {
                    cenario.mostrarMensagem("SISTEMAS DESATIVADOS", new Color(110, 255, 160), 120);
                }
                break;
            case FALHOU:
                estado = EstadoTerminal.BLOQUEADO;
                framesBloqueio = FRAMES_BLOQUEIO;
                setImage("terminal_erro.png");
                Audio.tocar("hack_erro.wav");
                if (cenario != null) {
                    cenario.dispararAlarme(getX(), getY());
                }
                break;
            default:
                estado = EstadoTerminal.ATIVO;
                break;
        }
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine ElementoInterativo.act): conta o tempo de bloqueio
     * e atualiza a dica de tecla.
     */
    public void act()
    {
        if (estado == EstadoTerminal.BLOQUEADO) {
            framesBloqueio--;
            if (framesBloqueio <= 0) {
                estado = EstadoTerminal.ATIVO;
                setImage("terminal_ativo.png");
            }
        }
        atualizarDica();
    }

    /** Mostra a tecla a usar quando um agente se aproxima ("Só o Zag sabe hackear" para o Zig). */
    private void atualizarDica()
    {
        String texto = null;
        if (estado == EstadoTerminal.ATIVO) {
            for (AgenteInvestigador a : getWorld().getObjects(AgenteInvestigador.class)) {
                if (estaPerto(a)) {
                    if (a.podeHackear()) {
                        texto = "[" + a.getNomeTeclaAcao() + "] hackear";
                        break;
                    }
                    texto = "Só o Zag sabe hackear";
                }
            }
        }
        if (texto == null) {
            esconderDica();
        } else if (dica == null || !texto.equals(textoDica)) {
            esconderDica();
            textoDica = texto;
            dica = new Balao(this, texto, new Color(20, 90, 50), Balao.PERMANENTE);
            getWorld().addObject(dica, getX(), getY());
        }
    }

    /** Remove o balão da dica, se existir. */
    private void esconderDica()
    {
        if (dica != null && dica.getWorld() != null) {
            dica.getWorld().removeObject(dica);
        }
        dica = null;
        textoDica = null;
    }

    /**
     * Indica se o terminal já foi hackeado com sucesso.
     *
     * @return true se estiver no estado HACKEADO
     */
    public boolean isHackeado()
    {
        return estado == EstadoTerminal.HACKEADO;
    }

    /**
     * Indica se o terminal está a ser usado.
     *
     * @return true durante o mini-jogo
     */
    public boolean isEmUso()
    {
        return estado == EstadoTerminal.EM_USO;
    }
}

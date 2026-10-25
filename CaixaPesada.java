import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.HashSet;
import java.util.Set;

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Objeto pesado (caixote, estátua) que só se move quando os DOIS agentes o empurram
 * ao mesmo tempo, do mesmo lado. É uma plataforma sólida: pode-se subir para cima dela,
 * usá-la como degrau ou pousá-la numa placa de pressão. Tem gravidade.
 * Arrastá-la faz barulho (as crias ouvem!).
 *
 * Funcionamento: ao andar contra a caixa, cada agente chama registarEmpurrao(). A caixa
 * atua antes dos agentes, por isso no frame seguinte vê quem a empurrou e move-se.
 *
 * Hierarquia: CaixaPesada estende Plataforma, que estende ElementoInterativo, que estende Actor.
 * Conceitos de POO: herança, overriding (act, isMovel, getDeltaX, getDeltaY), overloading de
 * construtores (2 versões), encapsulamento (conjuntos privados de agentes).
 */
public class CaixaPesada extends Plataforma
{
    /** Velocidade horizontal quando empurrada, em píxeis por ciclo (2). */
    private static final int VELOCIDADE = 2;
    /** Velocidade máxima de queda, em píxeis por ciclo (12). */
    private static final int QUEDA_MAXIMA = 12;
    /** Ciclos de arrasto entre dois ruídos (30). */
    private static final int INTERVALO_RUIDO = 30;
    /** Ciclos a empurrar sozinho até aparecer o aviso "Pesado demais!" (25). */
    private static final int AVISO_SOZINHO = 25;
    /** Número de agentes necessários para mover a caixa (2). */
    private static final int NECESSARIOS = 2;

    /** Agentes que empurraram para a direita no último ciclo. */
    private Set<AgenteInvestigador> empurrarDireita = new HashSet<AgenteInvestigador>();
    /** Agentes que empurraram para a esquerda no último ciclo. */
    private Set<AgenteInvestigador> empurrarEsquerda = new HashSet<AgenteInvestigador>();
    /** Velocidade vertical (gravidade), em píxeis por ciclo. */
    private int velocidadeVertical;
    /** Deslocamento horizontal no último ciclo (para transportar quem está em cima). */
    private int deltaX;
    /** Deslocamento vertical no último ciclo. */
    private int deltaY;
    /** Ciclos seguidos com apenas um agente a empurrar. */
    private int framesSozinho;
    /** Contador de ciclos de arrasto para o ruído. */
    private int framesRuido;

    /** Caixote da conserveira (para criar pelo menu do Greenfoot). */
    public CaixaPesada()
    {
        this("caixa_pesada.png");
    }

    /**
     * Caixa pesada com a imagem indicada (sólida).
     *
     * @param ficheiroImagem Nome do ficheiro na pasta images (ex.: "estatua.png" no Cenario2,
     *                       "carrinho_livros.png" no Cenario3)
     */
    public CaixaPesada(String ficheiroImagem)
    {
        super(new GreenfootImage(ficheiroImagem), true);
    }

    /**
     * Regista que um agente está a empurrar nesta direção (só conta quem está ao nível do chão da caixa).
     *
     * @param agente Agente que empurra (ex.: o Zig)
     * @param direcao Sentido do empurrão: 1 = direita, -1 = esquerda (ex.: 1)
     */
    public void registarEmpurrao(AgenteInvestigador agente, int direcao)
    {
        if (Math.abs(agente.getBaseColisao() - getBase()) > 8) {
            return;   // só conta quem está ao nível do chão da caixa
        }
        if (direcao > 0) {
            empurrarDireita.add(agente);
        } else {
            empurrarEsquerda.add(agente);
        }
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine ElementoInterativo.act): move-se se dois agentes
     * empurraram do mesmo lado, faz ruído ao arrastar, avisa quem empurra sozinho e aplica a gravidade.
     */
    public void act()
    {
        int xAntes = getX();
        int yAntes = getY();
        int dx = 0;
        if (empurrarDireita.size() >= NECESSARIOS) {
            dx = VELOCIDADE;
        } else if (empurrarEsquerda.size() >= NECESSARIOS) {
            dx = -VELOCIDADE;
        }
        verificarEmpurraoSozinho(empurrarDireita.size() + empurrarEsquerda.size());
        empurrarDireita = new HashSet<AgenteInvestigador>();
        empurrarEsquerda = new HashSet<AgenteInvestigador>();

        if (dx != 0 && moverHorizontal(dx)) {
            framesRuido++;
            if (framesRuido >= INTERVALO_RUIDO && getWorld() instanceof Cenario) {
                framesRuido = 0;
                ((Cenario) getWorld()).emitirRuido(getX(), getBase(), TipoRuido.ARRASTAR);
            }
        }
        aplicarGravidade();
        deltaX = getX() - xAntes;
        deltaY = getY() - yAntes;
    }

    /**
     * Mostra o balão "Pesado demais! Empurrem os dois." se um só agente empurrar durante 25 ciclos.
     *
     * @param empurradores Número de empurrões registados neste ciclo (ex.: 1)
     */
    private void verificarEmpurraoSozinho(int empurradores)
    {
        if (empurradores == 1) {
            framesSozinho++;
            if (framesSozinho == AVISO_SOZINHO && getWorld() != null) {
                getWorld().addObject(new Balao(this, "Pesado demais!\nEmpurrem os dois.", new Color(120, 60, 20), 120), getX(), getY());
            }
        } else if (empurradores == 0) {
            framesSozinho = 0;
        }
    }

    /**
     * Tenta mover a caixa na horizontal; desfaz o movimento se bater numa plataforma sólida.
     *
     * @param dx Deslocamento em píxeis (ex.: 2 ou -2)
     * @return true se se moveu
     */
    private boolean moverHorizontal(int dx)
    {
        setLocation(getX() + dx, getY());
        for (Plataforma p : getWorld().getObjects(Plataforma.class)) {
            if (p != this && p.isSolida() && p.isColidivel() && sobrepoe(p)) {
                setLocation(getX() - dx, getY());
                return false;
            }
        }
        return true;
    }

    /** Aplica a gravidade, aterra na plataforma mais alta atravessada e remove a caixa se cair do mundo. */
    private void aplicarGravidade()
    {
        int baseAnterior = getBase();
        velocidadeVertical = Math.min(velocidadeVertical + 1, QUEDA_MAXIMA);
        setLocation(getX(), getY() + velocidadeVertical);
        Plataforma melhor = null;
        for (Plataforma p : getWorld().getObjects(Plataforma.class)) {
            if (p != this && p.isColidivel() && getDireita() > p.getEsquerda() && getEsquerda() < p.getDireita()
                && baseAnterior <= p.getTopo() + 1 && getBase() >= p.getTopo()) {
                if (melhor == null || p.getTopo() < melhor.getTopo()) {
                    melhor = p;
                }
            }
        }
        if (melhor != null) {
            setLocation(getX(), getY() - (getBase() - melhor.getTopo()));
            velocidadeVertical = 0;
        }
        if (getTopo() > getWorld().getHeight() + 100) {
            getWorld().removeObject(this);
        }
    }

    /**
     * Verifica se a caixa se sobrepõe à plataforma indicada.
     *
     * @param p Plataforma a testar (ex.: o batente de METAL junto à placa do Cenario1)
     * @return true se os retângulos se intersetam
     */
    private boolean sobrepoe(Plataforma p)
    {
        return getEsquerda() < p.getDireita() && getDireita() > p.getEsquerda()
            && getTopo() < p.getBase() && getBase() > p.getTopo();
    }

    /**
     * Redefine Plataforma.isMovel: a caixa move-se (transporta quem está em cima).
     *
     * @return true
     */
    @Override
    public boolean isMovel()
    {
        return true;
    }

    /**
     * Redefine Plataforma.getDeltaX: deslocamento horizontal no último ciclo.
     *
     * @return deslocamento em píxeis
     */
    @Override
    public int getDeltaX()
    {
        return deltaX;
    }

    /**
     * Redefine Plataforma.getDeltaY: deslocamento vertical no último ciclo.
     *
     * @return deslocamento em píxeis
     */
    @Override
    public int getDeltaY()
    {
        return deltaY;
    }
}

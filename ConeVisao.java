import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: código de desenho/gráficos (classe inteira, que só desenha o cone)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Desenho do cone de visão de um cultista (Criado com ajuda do Gemini). Segue os olhos do dono e muda de cor
 * conforme o estado (calmo / desconfiado / alerta). O cone é recortado pelas paredes,
 * por isso o jogador vê exatamente onde pode ou não pode estar.
 *
 * Hierarquia: ConeVisao estende EfeitoVisual, que estende Actor.
 * Conceitos de POO: herança (reutiliza remover() de EfeitoVisual), overriding de act(),
 * colaboração com CampoVisao (delega o desenho no campo de visão do dono).
 */
public class ConeVisao extends EfeitoVisual
{
    /** Ciclos entre redesenhos (3), para poupar processamento. */
    private static final int INTERVALO_REDESENHO = 3;

    /** Cultista a quem pertence o cone. */
    private final CultistaPatrulheiro dono;
    /** Lado da imagem quadrada, em píxeis (o dobro do alcance mais 4; ex.: 484). */
    private final int tamanho;
    /** Contador de ciclos para o redesenho. */
    private int contador;

    /**
     * Cria o cone de um cultista, com uma imagem do tamanho do alcance da visão.
     *
     * @param dono Cultista que tem o cone (ex.: o próprio CultistaPatrulheiro, em addedToWorld)
     */
    public ConeVisao(CultistaPatrulheiro dono)
    {
        this.dono = dono;
        this.tamanho = dono.getCampoVisao().getAlcance() * 2 + 4;
        setImage(new GreenfootImage(tamanho, tamanho));
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine Actor.act): desaparece se o dono saiu ou foi
     * neutralizado; caso contrário acompanha os olhos do dono e redesenha a cada 3 ciclos.
     */
    public void act()
    {
        if (dono.getWorld() == null || dono.isNeutralizado()) {
            remover();
            return;
        }
        setLocation(dono.getOlhoX(), dono.getOlhoY());
        contador++;
        if (contador % INTERVALO_REDESENHO == 0) {
            redesenhar();
        }
    }

    /** Limpa a imagem e, se a visão do dono estiver ativa, desenha o cone com a cor do estado. */
    private void redesenhar()
    {
        GreenfootImage img = getImage();
        img.clear();
        if (!dono.isVisaoAtiva()) {
            return;
        }
        Color cor = dono.getCorCone();
        Color contorno = new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), Math.min(255, cor.getAlpha() * 2));
        dono.getCampoVisao().desenhar(img, tamanho / 2, tamanho / 2, getWorld(), dono.getOlhoX(), dono.getOlhoY(),
                                      dono.getDirecaoVisao(), cor, contorno);
    }
}

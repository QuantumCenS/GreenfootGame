import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Porta blindada do cofre da conserveira. É um Portao (abre a enrolar) mas, em vez de
 * placas de pressão, abre quando o Zag hackeia o terminal a que está ligada (Hackeavel).
 *
 * Hierarquia: Cofre estende Portao, que estende Plataforma, que estende ElementoInterativo,
 * que estende Actor (4 níveis além de Actor).
 * Conceitos de POO: herança (reutiliza a animação de abrir do Portao), implementação de
 * interface (Hackeavel) e overriding de fechar().
 */
public class Cofre extends Portao implements Hackeavel
{
    /** true depois de o terminal ligado ter sido hackeado. */
    private boolean hackeado;

    /** Cria a porta do cofre com a imagem "cofre.png". */
    public Cofre()
    {
        super(new GreenfootImage("cofre.png"));
    }

    /** Implementa Hackeavel: marca o cofre como hackeado e abre-o. */
    @Override
    public void desativarPorHacking()
    {
        hackeado = true;
        abrir();
    }

    /**
     * Implementa Hackeavel.
     *
     * @return true se o cofre já foi hackeado
     */
    @Override
    public boolean isHackeado()
    {
        return hackeado;
    }

    /** Depois de hackeado fica aberto (overriding de Portao.fechar). */
    @Override
    public void fechar()
    {
        if (!hackeado) {
            super.fechar();
        }
    }
}

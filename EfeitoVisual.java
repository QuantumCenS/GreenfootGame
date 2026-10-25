import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Classe base ABSTRATA dos efeitos puramente visuais que vivem no nível
 * (balões, cones de visão, partículas). Não contam para colisões e
 * acompanham o scroll da câmara como qualquer outro objeto do nível.
 *
 * Hierarquia: EfeitoVisual estende Actor; subclasses: Balao, ConeVisao, EfeitoParticulas.
 * Conceitos de POO: classe abstrata (não faz sentido criar um "efeito" genérico) e herança
 * (as subclasses reutilizam remover()).
 */
public abstract class EfeitoVisual extends Actor
{
    /** Remove o efeito do mundo em segurança (não faz nada se já tiver sido removido). */
    protected void remover()
    {
        if (getWorld() != null) {
            getWorld().removeObject(this);
        }
    }
}

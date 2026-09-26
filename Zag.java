import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class Zag here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Zag extends Agents
{
    private GreenfootImage imageZig;
    public Zag()
    {
        imageZig = getImage();
        int larguraAtual = imageZig.getWidth();
        int alturaAtual = imageZig.getHeight();
        
        imageZig.scale(larguraAtual/4,alturaAtual/4);
    }
    public void act()
    {
        jump("up");
        movingLR("left", "right");
    }  
}

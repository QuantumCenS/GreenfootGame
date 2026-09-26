import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class Zig here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Zig extends Agents
{
    /**
     * Act - do whatever the Zig wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    private GreenfootImage imageZig;
    public Zig()
    {
        imageZig = getImage();
        int larguraAtual = imageZig.getWidth();
        int alturaAtual = imageZig.getHeight();
        
        imageZig.scale(larguraAtual/4,alturaAtual/4);
    }
    public void act()
    {
        jump("w");
        movingLR("a", "d");
    }   
}

import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;
import java.util.ArrayList;
/**
 * Write a description of class Zig here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Zig extends Agents
{
    private GreenfootImage imageZig;
    public Zig(int velocidade)
    {
        super(velocidade);
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

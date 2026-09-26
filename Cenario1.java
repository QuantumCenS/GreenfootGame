import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class MyWorld here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Cenario1 extends World
{

    /**
     * Constructor for objects of class MyWorld.
     * 
     */
    public Cenario1() 
    {
        super(1024, 572, 1);
        Background();
        populateWorld();
    }
    public final void Background()
    {
        setBackground("POOCenario111.jpg");
    }
    
    public void populateWorld()
    {
        Zig zig = new Zig();
        addObject(zig,100,452);
        
        Zag zag = new Zag();
        addObject(zag, 900, 452);
        
        Ground ground = new Ground();
        addObject(ground,getWidth()/2,getHeight() - ground.getImage().getHeight() / 2);
    }
}

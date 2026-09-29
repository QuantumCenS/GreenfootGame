import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class ElementoInterativo here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class ElementoInterativo extends Actor
{
    /**
     * Act - do whatever the ElementoInterativo wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    private GreenfootImage imageGround;
    public ElementoInterativo()
    {
        imageGround = getImage();
        imageGround.setTransparency(0);
    }
    public void act()
    {
        // Add your action code here.
    }
}

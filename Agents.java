import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class Agents here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Agents extends Actor
{
    /**
     * Act - do whatever the Agents wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    private int ySpeed;
    
    public Agents()
    {
    }
    public void act()
    {
        
    }
    public void movingLR(String leftKey, String rightKey)
    {
        if (Greenfoot.isKeyDown(rightKey))
        {
            move(5);
        }
    if (Greenfoot.isKeyDown(leftKey))
        {
            move(-5);
        }
    }
    public void jump(String jumpKey)
    {
        int groundLevel = getWorld().getWidth() - getImage().getHeight()/2;
        boolean onGround = isTouching(Ground.class);
        if (!onGround) // in middle of jump
        {
            ySpeed++; // adds gravity effect
            setLocation(getX(), getY()+ySpeed); // fall (rising slower or falling faster)
            if (getY()>=groundLevel) // has landed (reached ground level)
            {
                setLocation(getX(), groundLevel); // set on ground
                Greenfoot.getKey(); // clears any key pressed during jump
           }
        }
        else // on ground
        {
            if (Greenfoot.isKeyDown(jumpKey)) // jump key detected
            {
                ySpeed = -15; // add jump speed
                setLocation(getX(), getY()+ySpeed); // leave ground
            }
        }
    }
}

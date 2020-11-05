package main.entities.creatures;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.text.html.parser.Entity;

import main.Handler;
import main.gfx.Animation;
import main.gfx.Assets;

public class Player extends Creature{ //no longer abstract, so we need a tick and render method
	
	//Animations
	
	private Animation animRight, animLeft;
	private String lastAnim;
	private long lastAttackTimer, attackCooldown = 500, attackTimer = attackCooldown;
	private int kickDamage = 3;
	
	public Player(Handler handler, float x, float y) {
		super(handler, x, y, Creature.DEFAULT_CREATURE_WIDTH, Creature.DEFAULT_CREATURE_HEIGHT);
	
		bounds.x = 0;
		bounds.y = 0;
		bounds.width = 50;
		bounds.height = 50;
	}


	public void tick() {
		/*
		
		//Animations
		animRight.tick();
		animLeft.tick();
		//movement
		getInput();
		move();
		
		//handler.getGameCamera().centerOnEntity(this); //to center THIS player
		
		*/
		move();
		getInput();
	}
	
	public void die() {
		System.out.println("you lose");
	}

	private void getInput() {
		xMove = 0;
		yMove = 0;
		
		if(handler.getKeyManager().left) {
			xMove = -speed;			
		}		
		if(handler.getKeyManager().right) {
			xMove = speed;
		}
		if(handler.getKeyManager().up) {
			yMove = -speed;
		}
		if(handler.getKeyManager().down) {
			yMove = speed;
		}		
		if(handler.getKeyManager().space) {
			
	        
	       
				Rectangle collisionBounds = getCollisionBounds(0, 0); //variable del offset
				Rectangle kickRect = new Rectangle();
				kickRect.width = 20;
				kickRect.height = 20;
				
				if(xMove<=0 ) {
					kickRect.x = collisionBounds.x + kickRect.width; //Pegada a la izq
					kickRect.y = collisionBounds.y + collisionBounds.height/2 - kickRect.height/2;
				}
				if(xMove>=0) {
					kickRect.x = collisionBounds.x + collisionBounds.width; //Pegada a la dch
					kickRect.y = collisionBounds.y + collisionBounds.height/2 - kickRect.height/2;
				}
				if(yMove<=0) {
					kickRect.x = collisionBounds.x + collisionBounds.width/2 - kickRect.width/2; //Pegada hacia arriba
					kickRect.y = collisionBounds.y - kickRect.height;
				}
				if(yMove>=0) {
					kickRect.x = collisionBounds.x + collisionBounds.width/2 -kickRect.width/2; //Pegada hacia abajo 
					kickRect.y = collisionBounds.y;
				}
				for(main.entities.Entity e : handler.getWorld().getEntityManager().getEntities()) {
		            if(e.equals(this))
		                continue; //pasa al siguiente valor del for loop ya que no nos queremos herir a nosotros
		            if(e.getCollisionBounds(0,0).intersects(kickRect)) {
		                e.hurt(kickDamage);//we only hurt one entity at a time
		                long before = System.nanoTime();
		                long now = System.nanoTime();
		                long restedEnough = now-before;
		                if (restedEnough<1000000000) {
		                	now = System.nanoTime();
		                	restedEnough = now-before;
		                }
		                return;
		            }
				}
			}
		}
	

	public void render(Graphics g) {
		g.setColor(Color.BLUE);
		g.fillRect((int)x, (int) y, 50, 50);
		//g.drawImage(getCurrentAnimationFrame(), (int) (x), (int) (y), width, height,  null);
		//g.setColor(Color.red);
		//g.fillRect((int) (x + bounds.x), (int) (y + bounds.y), bounds.width, bounds.height);
	}
	
	/*private BufferedImage getCurrentAnimationFrame() {
			
		
		
		if(xMove < 0 && yMove != 0) { //moves left and in any direction in the y axis
			lastAnim = "left";
			return animLeft.getCurrentFrame();
		}else if(xMove > 0 && yMove != 0) { //moves right and in any direction in the y axis
			lastAnim = "right";
			return animRight.getCurrentFrame();
		}else if(xMove < 0) { //only moves left
			lastAnim = "left";
			return animLeft.getCurrentFrame(); 
		}else if(xMove > 0) { //only moves right
			lastAnim = "right";
			return animRight.getCurrentFrame();
		}else if(yMove != 0) { //only moves up or down
			if(lastAnim == "left") return animLeft.getCurrentFrame();
			else return animRight.getCurrentFrame();
		}else {
			if(lastAnim == "left") return Assets.player_standStill_left;
			else return Assets.player_standStill_right;
		}
	}*/
}

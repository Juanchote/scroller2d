package es.sorikaStudio.scroller2d.Factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import es.sorikaStudio.scroller2d.CategoryAnimations;
import es.sorikaStudio.scroller2d.Scroller2d;
import es.sorikaStudio.scroller2d.controller.WorldController;

public class EnemyFactory {

	private static ArrayList<Enemy> enemyArray;
	private WorldController worldController;
	private float unitScale;
	
	private static EnemyFactory INSTANCE = new EnemyFactory();
	
	private class Enemy {
		private Vector2 position;
		private float health;
		private float shield = 0;
		private float speed;
		private Rectangle sprite;
		private int FRAME_COLS;
		private TreeMap<Integer,Animation> animations;
		private Texture texture;
		BodyDef body;
		FixtureDef fixtureDef;
		
		private Enemy(Vector2 position, float health, float shield, float speed, TreeMap<Integer,Pair<Integer,Integer>> FRAME_ROWS, int FRAME_COLS, Texture texture) {
			this.position = position;
			this.health = health;
			this.shield = shield;
			this.speed = speed;
			this.sprite = new Rectangle();
			this.texture = texture;
			
			this.animations = new TreeMap<Integer, Animation>();
			
		    TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth() / 
		            8, texture.getHeight() / FRAME_COLS);
		    

		    int index = 0;
		    CategoryAnimations.initIterator();
		    Pair pair;
		    do{
		    	//checks for an empty category.
		    	do{
		    		index = CategoryAnimations.currentIndex();
		    		pair = FRAME_ROWS.get(index);
		    		if(pair == null)
		    			System.out.println("null");
		    		else
		    			System.out.println(index);
		    	}while((pair == null)&&(index != CategoryAnimations.EOF));
		    	
		    	if(index != CategoryAnimations.EOF) {
			    	int FRAME_POS = (int) pair.getFirst(); //Row of the texturesheet
			    	int FRAME_ROW = (int) pair.getSecond(); // Number of sprites inside the Row
			    	
			    	TextureRegion[] textureRegion = new TextureRegion[FRAME_ROW];
			    	
			    	//Populates the textureRegion from the Texture split.
				    int ind = 0;
		        	for (int j = 0; j < FRAME_ROW; j++) {
		                textureRegion[ind] = tmp[FRAME_POS][j];
		                ind++;
		        	}
		        	//inserts the animation with the index.
		        	animations.put(index, new Animation(0.15f,textureRegion));
		    	}
		    }while(index != CategoryAnimations.EOF);
			
			sprite.setPosition(position);
			sprite.width = texture.getWidth() / 8;
			sprite.height = texture.getHeight() / FRAME_COLS;
			
			body = new BodyDef();
			body.type = BodyType.DynamicBody;
			body.fixedRotation = true;
			body.angle = 0;
			body.position.set(position.x - (sprite.width / 2), position.y - (sprite.height / 2));
			
			worldController.getWorld().createBody(body);
		}
	}
	
	private EnemyFactory() {
		this.enemyArray = new ArrayList<Enemy>();
		this.worldController = WorldController.getInstance();
		this.unitScale = Scroller2d.getScale();
	}

	public static EnemyFactory getInstance() {
		return INSTANCE;
	}
	
	public static int size() {
		return enemyArray.size();
	}
	
	public ArrayList<Enemy> getEnemys() {
		return enemyArray;
	}
	
	public void createEnemy() {
		
	}
}

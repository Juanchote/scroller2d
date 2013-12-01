package es.sorikaStudio.scroller2d;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import es.sorikaStudio.scroller2d.controller.CameraController;
import es.sorikaStudio.scroller2d.controller.CharacterController;
import es.sorikaStudio.scroller2d.controller.CollisionListener;
import es.sorikaStudio.scroller2d.controller.WorldController;

/**
 * Main Class of the 2dScroller Game using MVC
 * @author Juan Manuel Rodulfo Salcedo
 *
 */
public class Scroller2d implements ApplicationListener {

	private String VERSION = "0.1";
	private Vector2 RESPAWN;
	
	private Box2DDebugRenderer debugRenderer;
	private boolean debugState = true;
	private static float unitScale = 32f;
	
	private SpriteBatch batch;
	private TiledMapRenderer tiledMapRenderer;
	
	private ShapeRenderer shapeRenderer;
	
	private TiledMap map;
	private BitmapFont font;
	
	private WorldController worldController;
	private World world;
	private CharacterController characterController;
	private CameraController cameraController;
	private OrthographicCamera camera;
	private CollisionListener collisionListener;
	
	private Body charBody; //Body definition
	private PolygonShape boxShape;
	
	@Override
	public void create() {		
		RESPAWN = new Vector2(5,5);
		//load assets
		//assets.Load();
		
		//prepares camera
		cameraController = CameraController.getInstance();
		camera = cameraController.getCamera();
		camera.update();
		
		//load the world and char controllers
		worldController = WorldController.getInstance();
		world = worldController.getWorld();	
		characterController = CharacterController.getInstance();
		characterController.initController(debugState, RESPAWN, camera, RESPAWN);
		
	    collisionListener = new CollisionListener(); //collision listener class
	    world.setContactListener(collisionListener);
		
		cameraController.initController(characterController,unitScale);
		
		//load tiledMap
		map = new TmxMapLoader().load("data/tiledMap/map.tmx");
		
		tiledMapRenderer = new OrthogonalTiledMapRenderer(map, 1 / unitScale);
		tiledMapRenderer.setView(camera);

		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(camera.combined);
		
		renderObjectLayer();
		
		//prepares the DebugRenderer
		debugRenderer = new Box2DDebugRenderer();
		font = new BitmapFont();		
		batch = new SpriteBatch();
	}

	/**
	 * Unpacks the object layer from the TiledMap and creates static bodies
	 */
	private void renderObjectLayer() {

		for (MapObject object : map.getLayers().get("objects").getObjects()) {
			if(object instanceof RectangleMapObject) {
				//Rectangle from the layer
				Rectangle rect = ((RectangleMapObject) object).getRectangle();
				
				//all the body + fixture stuff
				BodyDef bodyDef = new BodyDef();
				bodyDef.type = BodyType.StaticBody;
				bodyDef.position.set(new Vector2( ( rect.x + (rect.width / 2 ) ) / unitScale,( rect.y + ( rect.height / 2 ) )/ unitScale));
				Body body = world.createBody(bodyDef);
				PolygonShape bodyShape = new PolygonShape();
				bodyShape.setAsBox(rect.width / ( 2 * unitScale), rect.height / ( 2 * unitScale));
				FixtureDef fixture = new FixtureDef();
				fixture.shape = bodyShape;
				fixture.filter.categoryBits = CategoryGroup.GROUND; //ground category
				body.createFixture(fixture);	
			}
		}
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		//texture.dispose();
		//assets.Dispose();
		worldController.dispose();
	}

	@Override
	public void render() {		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
				
		//renders the tiled map and updates the camera
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();
		
		//draws the debug renderer if debugState is true
		if (debugState) {			
			debugRenderer.render(world, camera.combined);
			//characterController.shapeRender();
			cameraController.shapeRender();			
		}
				
		cameraController.cameraControl();

		camera.update();
				
		batch.begin();
		
		//renders physics
		worldController.render(Gdx.graphics.getDeltaTime());
				
		//renders character
		characterController.render(batch,Gdx.graphics.getDeltaTime());
		
		font.draw(batch,"camera: [" + (int) camera.position.x + "," + (int) camera.position.y + "]", 10, 35);
		font.draw(batch, "TEST VERSION - FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20); 
		batch.end();
		
	}

	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false, Gdx.graphics.getWidth() / unitScale, Gdx.graphics.getHeight() / unitScale);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
	
	public static float getScale() {
		return unitScale;
	}
}

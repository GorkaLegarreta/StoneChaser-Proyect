package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import main.gfx.Assets;
import main.gfx.GameCamera;
import main.input.KeyManager;
import main.input.MouseManager;
import main.input.ScreenMouseMovement;
import main.states.GameState;
import main.states.MenuState;
import main.states.State;
import main.utilities.Spotlight;

public class Game implements Runnable{
	
	private static Window window;	
	private int width, height;
	public String title;
	private static HashMap <Integer,String> playersItems;
	
	//gameloop
	
	private long now, before, sec, timer;
	private float update, ratio;
	private int fps, c;
	
	private boolean running = false;
	
	private Thread thread;
	
	private BufferStrategy bs;
	private Graphics g;
		
	//States
	public State menuState;
	public State gameState;
	
	//spotlight
	@SuppressWarnings("unused")
	private Spotlight spotlight, craftingTableSpotlight, inventorySpotlight1, inventorySpotlight2, 
					  inventorySpotlight3, inventorySpotlight4, inventorySpotlight5, inventorySpotlight6, craftingOutcomeSpotlight;
	
	private boolean enableSpotlight = false;
	
	//Input
	private KeyManager keyManager;
	private MouseManager mouseManager;
	private ScreenMouseMovement mouseMovement; //necesaria ya que te da la posicion del raton independientemente de si se est� draggeando (click a la vez que se mueve)
											   //o si se est� moviendo sin click, en awt estos son eventos distintos y da problemas en el inventario.
	
	//fichero de propiedades
	Properties properties = new Properties();
	InputStream is = null;
	
	//Camera
	private GameCamera gameCamera;
	
	//Handler
	
	private Handler handler;
	
	//Logger
	public final static Logger LOGGER = Logger.getLogger(Game.class.getName());
	public static FileHandler fh;
	
	//static 
	static {		
		try {
			/*
			 *  ESTABLECE LEVEL DEL LOGGER ESCRIBIRA LOGS SUPERIORES A ESTE NIVEL
			 *  CAMBIANDO ESTE NIVEL SE FILTRARAN LOS LOGGERS QUE SE ESCRIBEN EN 
			 *  EL FICHERO; LOS QUE APARECEN EN CONSOLA (LEVEL.INFO) TAMBIEN APARECERAN
			 *  EN EL FICHERO LOGGER.TXT SI EL NIVEL SE LO PERMITE
			 */
			fh = new FileHandler("Logger.txt",false);			
			LOGGER.setLevel(Level.FINEST);			
			LOGGER.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			/*
			 *  LOG QUE APARECE TANTO EN LOGGER.TXT COMO EN CONSOLA POR EL (LEVEL.INFO).
			 *  DEMOSTRACION DE QUE SE IMPRIMEN LOGS DEL MISMO NIVEL AL ESTABLECIDO 
			 * 	POR DEFECTO EN FINEST = 300 NIVEL MUY BAJO QUE SE SUPERA FACILMENTE 
			 */
			LOGGER.log(LOGGER.getLevel()," LOG que se escribe en Logger.txt y es de nivel de prioridad del propio logger, "+LOGGER.getLevel().toString()+": "+LOGGER.getLevel().intValue());
			LOGGER.log(Level.INFO,"Logger y clases del StoneChaser inicializadas"); 
			
			// CREAR LA CONEXION A LA BD
			GameDB.getInstance();
			
		} catch (SecurityException e) {			
			LOGGER.log(Level.SEVERE,Game.getStackTrace(e));
		} catch (IOException e) {			
			LOGGER.log(Level.SEVERE,Game.getStackTrace(e));
		} catch (GameDBException e) {
			e.printStackTrace();		//nos indica que no se ha podido establecer la conexi�n y lo registra en el logger
		}		
	}
	
	public Game(String title, int width, int height) {
		this.width = width;
		this.height = height;
		this.title = title;
		keyManager = new KeyManager();
		mouseManager = new MouseManager();
		//LOGGER
		LOGGER.log(Level.FINEST,"Objeto Game creada en el constructor");		
	}
	
	private void init(){		
		window = new Window(title, width, height,fh);
		window.getFrame().addKeyListener(keyManager);
		window.getFrame().addMouseListener(mouseManager);
		window.getFrame().addMouseMotionListener(mouseManager);
		window.getCanvas().addMouseListener(mouseManager);
		window.getCanvas().addMouseMotionListener(mouseManager);
		mouseMovement = new ScreenMouseMovement(window.getCanvas());
		
		//fichero propiedades
		
		try {
			is = new FileInputStream("datos.properties");
			properties.load(is);
			
		} catch (FileNotFoundException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();}
		
		Assets.init();		
		handler = new Handler(this); //coge el objeto de esta clase
		gameCamera = new GameCamera(handler, 0, 0);
		menuState = new MenuState(handler);
		gameState = new GameState(handler); //nos referimos a la clase game, a esta misma clase		
		State.setState(menuState);
		
	}
	
	private void tick() throws GameDBException {
		
		keyManager.tick();
		mouseMovement.tick();
		
		if(State.getState()!=null && State.getState().equals(menuState)) {
			menuState.tick();
		} else {
			State.getState().tick();
		}
	}
	
	private void render() {
		
		bs = window.getCanvas().getBufferStrategy();
		if(bs  == null) {
			window.getCanvas().createBufferStrategy(3);
			return;
		}		
		g = bs.getDrawGraphics();
		//Limpiamos la pantalla
		g.clearRect(0, 0, width, height);
		
		//A partir de aqui podemos dibujar		
		if(State.getState()!=null) State.getState().render(g);
		
		if(enableSpotlight) { 
			
			craftingTableSpotlight = new Spotlight(getWidth()/2, getHeight()/2 - 50, 0, 0, 500, 300, 0.5f, 0.6f, 250, 0.5f, new Color(0, 0, 0, 0), g);
			inventorySpotlight1 = new Spotlight(195, 370, 0, 300, 225, 100, 0.3f, 0.6f, 50, 0.5f, new Color(0, 0, 0, 0), g);
			inventorySpotlight2 = new Spotlight(255, 370, 225, 300, 63, 100, 0.3f, 0.6f, 50, 0.5f, new Color(0, 0, 0, 0), g);
			inventorySpotlight3 = new Spotlight(315, 370, 288, 300, 70, 100, 0.3f, 0.6f, 50, 0.5f, new Color(0, 0, 0, 0), g);
			inventorySpotlight4 = new Spotlight(380, 370, 358, 300, 55, 100, 0.3f, 0.6f, 50, 0.5f, new Color(0, 0, 0, 0), g);
			inventorySpotlight5 = new Spotlight(440, 370, 413, 300, 62, 100, 0.3f, 0.6f, 50, 0.5f, new Color(0, 0, 0, 0), g);
			inventorySpotlight6 = new Spotlight(505, 370, 475, 300, 250, 100, 0.3f, 0.6f, 50, 0.5f, new Color(0, 0, 0, 0), g);
			craftingOutcomeSpotlight = new Spotlight(560, 149, 500, 0, 200, 300, 0.4f, 0.6f, 75, 0.5f, new Color(0, 0, 0, 0), g);
		}
		
		//Aqui dejamos de dibujar y actualizamos
		bs.show();
		g.dispose();
	}
	
	public void run() {		
		
		init();		
		
		before = System.nanoTime();		//nanoTime mide en nanosegundos
		fps = 60;						//queremos que el juego funcione a 60 frames por segundo
		sec = 1000000000;				//1 seg son 10^9 nanosegs
		ratio = sec/fps;				//60 fps == la pantalla se actualiza cada 1/60 de segundo
		
		while(running) {	
			if (gameIsPaused()) {
				try {
					TickRender();
				} catch (GameDBException e) {
					e.printStackTrace();
				}
			} else {
				try {TickRender();} catch (GameDBException e) {	e.printStackTrace();}
			}
		}stop();
	}
	
	public void TickRender() throws GameDBException{
		now = System.nanoTime();			//now es el tiempo que ha pasado hasta ahora
		update += (now - before)/ratio;		//actualizamos update para saber si ya ha transcurrido m�s de 1/60 de segundo	
		timer += now - before;				//sumamos el tiempo que ha pasado a nuestro "reloj"
		before = now;						//actualizamos el tiempo que ha pasado, para usarlo en el siguiente loop del while y al compararlo con now para ver cuanto tiempo ha pasado.			
		
		if(update >= 1) {
			render();				//cada 1/60 de segundo hacemos que se actualicen m�todos y que se rendericen gr�ficos en pantalla
			tick();
			
			update--;				
			c++;
		}
		
		if(timer >= sec) { 			//si ha pasado m�s de un segundo, imprime por pantalla los fps
			System.out.println("fps counter: " + c);
			c = 0;
			
			timer = 0;
		}
	}
	
	public static String getItemValues(int user_code) {
		return playersItems.get(user_code);
	}
	public static Window getWindow() {
		return window;
	}
	@SuppressWarnings("static-access")
	public boolean gameIsPaused() {
		return keyManager.pause;
	}
	public Properties getPropertiesFile() {
		return properties;
	}
	
	public KeyManager getKeyManager(){
		return keyManager;
	}
	
	public MouseManager getMouseManager(){
		return mouseManager;
	}
	
	public ScreenMouseMovement getMouseMovement() {
		return mouseMovement;
	}
	
	public GameCamera getGameCamera() {
		return gameCamera;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void spotlightEnabler() {
		enableSpotlight = true;
	}

	public void spotlightDisabler() {
		enableSpotlight = false;
	}
			
	public synchronized void start() {
		
		if(running)
			return;
		else
		running = true;
		thread = new Thread(this);
		thread.start();
		
	}
	
	public synchronized void stop() {
		
		if(!running) {
			return;
		}
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Esta funcion es la que permite que el printStackTrace pasar a un String
	 * 
	 * @param Excepcion del que queremos pasar el stackTrace a String
	 * @return El stackTrace de la excepcion en forma de String
	 */
	public static String getStackTrace(Exception e) {
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter(sWriter);
        e.printStackTrace(pWriter);
        return sWriter.toString();
    }
	
}
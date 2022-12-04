package KermitLeRobot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.utility.Delay;


public class Sensors {	
	
	//attributs colorSensor
	private static float[] path_color;
    private SampleProvider average;
    private static EV3ColorSensor colorSensor;
    private final static double ERROR = 0.01;
    
    private Map<String, float[]> hmap;
    private String [] tabIndCouleur;
    private double minimumScal;
	
    // attributs touchSensor
    private EV3TouchSensor touch;
    
    //attributs pour UltraSonicSensor
    private static EV3UltrasonicSensor vision;
	private static SampleProvider sp, spEcoute;
	private static float [] trab, tEcoute;
	private float [] roboData;
	
	
    public Sensors(Port port, Port port2, Port port3)    {    	
    	colorSensor = new EV3ColorSensor(port2);
    		
    	average = new MeanFilter(colorSensor.getRGBMode(), 1);
		colorSensor.setFloodlight(Color.WHITE);
		hmap = new HashMap<String,float[]>();
		tabIndCouleur = new String [] {"rouge","vert","bleu","jaune","noir","blanc","gris"};				// toutes les couleurs sont stockees avec leur RGB
		float [] rouge = new float [] {0.094f,0.032f,0.018f};
		float [] bleu = new float [] {0.012f,0.036f,0.45f};
		float [] vert = new float [] {0.037f,0.1f,0.028f};
		float [] noir = new float [] {0.015f,0.02f,0.0160f};
		float [] jaune = new float [] {0.17f,0.195f,0.04f};
		float [] blanc = new float [] {0.21f,0.26f,0.16f};
		float [] gris = new float [] {0.065f,0.095f,0.065f};
		hmap.put("rouge",rouge);
		hmap.put("bleu",bleu);
		hmap.put("vert",vert);
		hmap.put("noir",noir);
		hmap.put("jaune",jaune);
		hmap.put("blanc",blanc);
		hmap.put("gris",gris);
		// touch Sensor
		touch = new EV3TouchSensor(port3);
		
		// UltraSonic sensor
		vision = new EV3UltrasonicSensor(port);
		sp = vision.getDistanceMode();
		vision.enable();
		trab = new float[sp.sampleSize()];
		sp.fetchSample(trab, 0);
    }
    
    /**
	 * Allume la lumiere permettant de bien voir les couleur
	 * module utilise : colorSensors
	 * module l'utilisant : initialisation
	 */
    public void setFloodLight(boolean on){
		colorSensor.setFloodlight(on);
	}
    
    /**
	 * retourne un double permetant de comparer 2 tableau a 3 case
	 * @param v1  tableau 1 
	 * @param v2 tableau 2
	 * module utilise : math
	 * module l'utilisant : retourCouleur
	 */
    public double scalaire(float[] v1, float[] v2) {
		return Math.sqrt (Math.pow(v1[0] - v2[0], 2.0) +
				Math.pow(v1[1] - v2[1], 2.0) +
				Math.pow(v1[2] - v2[2], 2.0));
	}

    /**
	 * Permet d'obtenir la couleur la plus proche de ce que les capteur renvoie
	 * @param minimumScal ecart minimal entre la couleur de reference et la couleur renvoye par le capteur
	 * @param couleurRetenu (string) donne le nom de la couleur
	 * @param path_color tableau de float moyen renvoié par le capteur
	 * @param roboData copie de path_color
	 * module utilise : scalaire, fetchSample, hmap
	 * module l'utilisant : ramenePalet, retourPerpendiculaire, main, demarrage, retourSansPalet
	 */
    public String retourCouleur() {
    	minimumScal = 1;
		String couleurRetenue = "";
		path_color = new float[3];
		average.fetchSample(path_color, 0);
		roboData = new float [] {path_color[0],path_color[1],path_color[2]};
		
		for (int i=0;i<tabIndCouleur.length;i++) {
			if (scalaire(roboData,(float[])hmap.get(tabIndCouleur [i]))<(minimumScal)) {
					couleurRetenue = tabIndCouleur[i];
					minimumScal =scalaire(roboData,(float[])hmap.get(tabIndCouleur [i]));
			}
		}
		return couleurRetenue;	
		
    }

    /**
	 * retourne true si le capteur de toucher est presser
	 * @param sample ([]float) tableau d'un echantillon
	 * @param touch (EV3TouchSensor) 
	 * module l'utilisant : kermitTouch
	 */
    public boolean isPressed() {
    	// renvoie un boolean indiquant si le bouton de pression est pressÃ©
        float[] sample = new float[1];
        touch.fetchSample(sample, 0);
        
        return sample[0] != 0;
    }
    
    /**
	 * retourne true quand le capteur est presser 
	 * module utilise : isPressed
	 * module l'utilisant : main, demarrage
	 */
    public boolean kermitTouch() {
    	while(this.isPressed() != true) {
	    	if (this.isPressed()== true) {
		    	return true;
		    } 
	    }
    	return false;
    }
    
    /**
	 * retourne la distance en centimetre
	 * @param sp recupere l'info donne par le capteur
	 * @param  trab  ([]float) tableau contenant sp
	 * module utilise : vision
	 * module l'utilisant : vueKermitEcart, recherchePalet, detecterRobot, detecterMur, prendrePalet, retourPerpendiculaire
	 */
    public float getDistance() {
    	sp = vision.getDistanceMode();
        trab = new float[sp.sampleSize()];
        sp.fetchSample(trab, 0);
    	return trab[0]*100;
    }
    
    /**
	 * affiche la difference entre des distance consecutives tant que l'ecart entre les 2 n'est pas important
	 * @param dist ([] float) tableau des differente valeur percu par le capteur
	 * @param  distance 1ere distance re actualiser a chaque incrementation
	 * @param distance2 recupere la valeur de distance au stade i+1
	 * @distanceMinim plus petite distance de l'ensemble des valeur
	 * module utilise : getDistance
	 * module l'utilisant : 
	 */
    // le but est de repérer un moment où il y a une grosse différence entre les valeurs qui prouverait un palet
    public void vueKermitEcart() {
    	ArrayList<Float> dist = new ArrayList<Float>();
    	dist.add(getDistance());
    	float distanceMinim = 0; 
    	float distance = 0;
    	float distance2 = 0;
    	while((distance2-distance)<0.2) {
    		dist.add(getDistance());
    		distance = dist.get(dist.size()-2);
    		distance2 = dist.get(dist.size()-1);
    		System.out.println(distance2-distance);
    		Delay.msDelay(1);
    		distanceMinim = Math.min(distance, distance2);
    	} 
    }
    
    /**
	 * tourne tant qu'il n'a pas repere un ecart de plus de 5cm qui indiquerait un palet, puis se positionne dans sa direction 
	 * @param r (MoteursRoues)
	 * @param sens (char) d si le robot tourne a droite
	 * module utilise : getPilot, isMoving
	 * module l'utilisant : main
	 */
    public boolean recherchePalet(MoteursRoues r, char sens) {
        float va=getDistance(); // va comme "valeur ancienne"
        Delay.msDelay(20);
        float vr=getDistance(); //vr comme "valeur récente"
        //    
        while((vr>va||vr==va) && r.getPilot().isMoving()) {
            va=vr;
            vr=getDistance();            
            if(va-vr>5) {
                r.getPilot().stop();
                System.out.println("stop");
                System.out.println("va : "+va);
                System.out.println("vr : "+vr);if(vr < 65) {
                if(sens == 'd')
                	r.tourner(12, false);
                else
                	r.tourner(-12, false);
                }
                return true;
            }
            if(vr<va) {
                while((vr<va || vr==va)&& r.getPilot().isMoving()) {
                    if(va-vr>5) {
                        r.getPilot().stop();
                        System.out.println("stop");
                        System.out.println("va : "+va);
                        System.out.println("vr : "+vr);
                        if(vr < 65) {
                        	if(sens == 'd')
                        		r.tourner(12, false);
                        	else
                        		r.tourner(-12, false);
                        }
                        return true;
                    }
                    System.out.println("boucle2");
                    va=vr;
                    vr=getDistance();                   
                }
            }                
            Delay.msDelay(20);
        }       
        return false;
    }
    
    /**
	 * permet de detecter un robot en captant les ondes ultrasonic qu'il envoie
	 * @return retourne vrai si un robot se trouve a moins de 25cm
	 * module utilise : getListenMode, getDistanceMode
	 * module l'utilisant : prendrePalet, ramenerPalet
	 */
    public boolean detecterRobot() {
        spEcoute = vision.getListenMode();
        tEcoute = new float[spEcoute.sampleSize()];
        spEcoute.fetchSample(tEcoute, 0);
        if(tEcoute[0] == 1) {
            sp = vision.getDistanceMode();
            trab = new float[sp.sampleSize()];
            sp.fetchSample(trab, 0);
            if(trab[0]*100 <= 25) {
                return true;
            }
        }
        return false;
    }

    /**
	 * permet de detecter si on se trouve en face d'un mur
	 * @return retourne vrai si le robot se trouve a moins de 20cm d'un mur
	 * module utilise : getDistanceMode
	 * module l'utilisant : prendrePalet, ramenerPalet, prendrePaletDemarrage
	 */
    public boolean detecterMur() {
        sp = vision.getDistanceMode();
        trab = new float[sp.sampleSize()];
        sp.fetchSample(trab, 0);
        if(trab[0]*100 <= 20) {
            return true;
        }
        return false;
    }
   

}

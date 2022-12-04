package KermitLeRobot;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;

public class MoteursRoues {

	private EV3LargeRegulatedMotor r1;
	private EV3LargeRegulatedMotor r2;
	private Wheel roue1;
	private Wheel roue2;
	private Chassis chassis; 
	private MovePilot pilot;
	
	public MoteursRoues(String vitesse) {
		this.r1 = new EV3LargeRegulatedMotor(MotorPort.A);
		this.r2 = new EV3LargeRegulatedMotor(MotorPort.C);		
		this.roue1 = WheeledChassis.modelWheel(r1, 43.2).offset(-7.5);
		this.roue2 = WheeledChassis.modelWheel(r2, 43.2).offset(7.5);
		this.chassis = new WheeledChassis(new Wheel[]{roue1, roue2}, WheeledChassis.TYPE_DIFFERENTIAL);
		this.pilot = new MovePilot(chassis);
		setNiveauVitesse(vitesse);
	}
	
	/**
	 * Getter permet d'utiliser la classe move pilot
	 * @return pilot
	 * module l'utilisant : Sensors : recherchePalet ; Main : demarage, prendrePalet, prendrePaletDemarrage, retourSansPalet, ramenePalet, retourPerpendiculaire, main
	 */
	public MovePilot getPilot() {
		return this.pilot;
	}
	
	/**
	 * Modifie la valeur de la vitesse
	 * @param v (string) vitesse en quatre niveau diffÃ©rents : rapide, moyen, opti, lent
	 * module utilise : Move pilot
	 * module l'utilisant : Main : recherchePalet, prendrePalet, prendrePaletDemarrage, ramenePalet, retourPerpendiculaire, main
	 */
	public void setNiveauVitesse(String v) {
		if (v == "rapide") {
			pilot.setLinearAcceleration(300);
			pilot.setAngularAcceleration(2000);
			pilot.setAngularSpeed(1000);
		}
		else if (v == "moyen") {
			pilot.setLinearAcceleration(150);
			pilot.setAngularAcceleration(1000);
		}
		else if (v == "lent"){
			pilot.setLinearAcceleration(75);
			pilot.setAngularAcceleration(150);
		}
		else if (v == "opti"){
            pilot.setLinearAcceleration(115);
            pilot.setAngularAcceleration(400);
            pilot.setAngularSpeed(275);
            
        }
	}
	
	/**
	 * Fait avancer ou reculer le robot
	 * @param distance (int) distance que le robot va parcourir en cm
	 * @param retourImmediat retour immediat après le lancement du programme
	 * module utilise : Move pilot
	 * module l'utilisant : prendrePaletDemarrage, retourSansPalet, ramenePalet, retourPerpendiculaire, demarrage, main
	 */
	public void deplacer(int distance, boolean retourImmediat) {
		// 1cm = 8
		pilot.travel(distance*8, retourImmediat);
	}
	
	/**
	 * Fait reculer le robot
	 * @param retourImmediat retour immediat après le lancement du programme
	 * module utilise : Move pilot
	 * module l'utilisant : demarrage, ramenePalet, retourPerpendiculaire
	 */
	public void reculer() {
		pilot.travel(-100);
	}
	
	/**
	 * Fait tourner le robot
	 * @param angle	angle de rotation en degré
	 * @param retourImmediat retour immediat après le lancement du programme
	 * module utilise : Move pilot
	 * module l'utilisant : retourPerpendiculaire, recherchePalet, demarrage, main
	 */
	public void tourner(int angle, boolean retourImmediat) {
		// 360 degres = 2260
		pilot.rotate(angle*2260/360, retourImmediat);
	}
	
	public void deplacerArc(Sensors sensors, boolean sens) {
	        int vMax = 500;
	        int v1;
	        int v2;
	        
	        if(sens) {
	            v1 = vMax;
	            v2 = (int) (vMax/1.3);
	        }else {
	            v1 = (int) (vMax/1.3);
	            v2 = vMax;
	        }            
	        
	        this.roue1.getMotor().setSpeed(v1);
	        this.roue2.getMotor().setSpeed(v2);
	        
	        this.roue1.getMotor().forward();
	        this.roue2.getMotor().forward();

	        Delay.msDelay(1950);
	        
	        this.roue1.getMotor().setSpeed(v2);
	        this.roue2.getMotor().setSpeed(v1);

	        Delay.msDelay(1300);

	        this.roue1.getMotor().setSpeed(vMax);
	        this.roue2.getMotor().setSpeed(vMax);       
	    }

}

package KermitLeRobot;

import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.utility.Delay;

public class MoteurPince {
	
	private EV3MediumRegulatedMotor pince;
	
	public MoteurPince() {
		pince = new EV3MediumRegulatedMotor(MotorPort.B);
		pince.setSpeed(1000);
	}
	
	/**
	 * Ouvre la pince d'environ 15cm
	 * @param retourImmediat	si false on attend la fin de l'action avant de pouvoir faire autre chose, si true on peut lancer autre chose
	 * module l'utilisant : Main demarage, prendre palet,ramener palet, retour perpendiculaire, main
	 */
	public void ouvrirPince(boolean retourImmediat) {
        //pince.rotate(700, retourImmediat);
		pince.rotate(700, retourImmediat);
	}
	
	/**
	 * Ferme la pince d'environ 15cm
	 * @param retourImmediat	si false on attend la fin de l'action avant de pouvoir faire autre chose, si true on peut lancer autre chose
	 * module l'utilisant : Main demarage, prendre palet,ramener palet, retour perpendiculaire, main
	 */
	public void fermerPince(boolean retourImmediat) {
        //pince.rotate(-1000, retourImmediat);
        pince.rotate(-700, retourImmediat);
	}
	
	/**
	 * Modifie l'angle d'ouverture de la pince
	 * @param angle d'ouverture en entier (angle positif -> ouverture | angle negtif -> fermeture)
	 * @param retourImmediat	si false on attend la fin de l'action avant de pouvoir faire autre chose, si true on peut lancer autre chose
	 * module l'utilisant : Main prendre palet,ramener palet
	 */
	public void modifierPince(int angle, boolean retourImmediat) {
        pince.rotate(angle, retourImmediat);
	}

}

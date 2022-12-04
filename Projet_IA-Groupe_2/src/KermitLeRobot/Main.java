package KermitLeRobot;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.utility.Delay;

public class Main {
	
	private static String couleurOpposee = "";
	private static float distancePalet = 0;
	private static char sens = ' ';
	
	
	/**
	 * Les differents etats du switch : chaque etat correspondant a une ou deux methodes.
	 * Permet de savoir quel comportement doit adopter le robot selon la situation et de se repérer dans le code.
	 */
	public enum Etat {
		Debut,
		RecherchePalet,
		PrendrePalet,
		RamenerPalet,
		TempsMort,
		Fin
	}
	
	/**
	 * Correspond aux differents departs possible, notre robot se place toujours au milieu et on indique la position de l'autre robot. 
	 * Par extension il indique quel deuxieme palet il va devoir prendre ainsi que le trajet prevu pour cette stratégie.
	 * Les 2 premiers palets sont codes en durs afin d'assurer au minimum 2 palets.
	 * Les schémas des différents parcours / stratégies sont accessibles sur notre rapport pour plus de clareté.
	 */
	public enum Depart {
		Droite,
		Gauche,					// Par exemple : ici le robot adverse comme à notre gauche 
		Milieu,
		NewDroite,
		NewGauche,
		MilieuDroiteCentral,			
		MilieuDroiteNormal,
		MilieuGaucheCentral,
		MilieuGaucheNormal
	}
	
	/**
	 * permet de recuperer les 2 premiers palets en durs et est utilisable peu importe le depart choisit.
	 * @param moteursR (MoteurRoues) 
	 * @param moteurP (MoteurPince)
	 * @param sensors (Sensors)
	 * @param premierAngle : angles selon le quel il faut tourner pour recuperer le deuxieme palet
	 * @param deuxiemeAngle : angle complementaire du premier pour le retour
	 * module utilise : MoteurRoues:deplacer,tourner, getPilot, deplacerArc; 
	 * 					MoteurPince: ouvrirPince,fermerPince ;
	 * 					Sensors:kermitTouch, retourCouleur ;
	 * 					Main : prendrePaletDemarrage,ramenePalet;
	 * module l'utilisant : Main
	 */
	//TODO
	public static void demarrage(MoteursRoues moteursR, MoteurPince moteurP, Sensors sensors, int premierAngle, int deuxiemeAngle) {
		moteursR.deplacer(70, true);
		moteurP.ouvrirPince(false);
		sensors.kermitTouch();
		moteurP.fermerPince(false);
		moteursR.deplacerArc(sensors, true);
		
		while(sensors.retourCouleur() != "blanc" && (moteursR.getPilot().isMoving())) {
			Delay.msDelay(50);
		}
		moteursR.getPilot().stop();
		moteurP.ouvrirPince(true);
		moteursR.reculer();
		
		moteursR.tourner(premierAngle, false);
		
		prendrePaletDemarrage(moteursR, moteurP, sensors);
		ramenePalet(moteursR, moteurP, sensors, 180, 1100);
		
		moteursR.tourner(deuxiemeAngle, false);
	}
	
	/**
	 * permet de reperer un palet en tournant sur lui meme et s'arrete quand il a trouve
	 * @return true quand un palet est trouvé
	 * @param moteursR (MoteurRoues) 
	 * @param moteurP (MoteurPince)
	 * @param sensors (Sensors)
	 * @param direction (int) permet de determiner si on tourne vers la droite (1) ou vers la gauche (-1) pendant la recherche
	 * module utilise : MoteurRoues: tourner, setNiveauVitesse, ;
	 * 					Sensors: rechercherPalet
	 * module l'utilisant : Main
	 */
	// direction = 1 -> droite | = -1 -> gauche
	public static boolean recherchePalet(MoteursRoues moteursR, MoteurPince moteurP, Sensors sensors, int direction) {
		if(direction != -1 || direction != 1)
			throw new IllegalArgumentException();
		sens = 'd';
		moteursR.setNiveauVitesse("opti");
		moteursR.tourner(direction * 180, true);
		return sensors.recherchePalet(moteursR, sens);
	}
	
	/** 
	 * avancer jusqu'au palet en ouvrant les pinces quand le touchSensor return true -> fermer les pinces
	 * @return retourne true quand les pinces sont fermées, retourne false quand il fait face a un obstacle, que ce soit un mur ou le robot adverse
	 * @param moteursR (MoteurRoues) 
	 * @param moteurP (MoteurPince)
	 * @param sensors (Sensors)
	 * module utilise : MoteurRoues: deplacer, setNiveauVitesse, getPilot, isMoving, tourner; 
	 * 					MoteurPince: modifiePince
	 * 					Sensors: isPressed, getDistance
	 * module l'utilisant : Main
	 */
	public static boolean prendrePalet(MoteursRoues moteursR, MoteurPince moteurP, Sensors sensors) { 
		moteursR.setNiveauVitesse("rapide");
		distancePalet = sensors.getDistance();
		if(distancePalet > 105)
			distancePalet = 105;
		
		moteursR.deplacer((int) distancePalet - 10, true);
		while(moteursR.getPilot().isMoving()) {
			Delay.msDelay(100);
		}
		moteursR.deplacer(30, true);
		moteurP.modifierPince(-400, true);
		while(sensors.isPressed() == false) {
			Delay.msDelay(50);
			if(sensors.detecterMur()) {
		        moteursR.getPilot().stop();
		        moteursR.deplacer(-10, false);
		        moteursR.tourner(90, false);
		        return false;
		    }
		    else if(sensors.detecterRobot()) {
		        moteursR.getPilot().stop();
		        moteursR.tourner(90, false);
		        moteursR.deplacer(10, false);
		        moteursR.tourner(-90, false);
		        return false;
		    }
		}
		
		moteursR.getPilot().stop();
		if (moteursR.getPilot().isMoving() == true)
			return false;
		Delay.msDelay(100);
		return true;
	}
	
	/** 
	 * avancer jusqu'au palet en ouvrant les pinces quand le touchSensor return true -> fermer les pinces
	 * Ici la différence avec le prendrePalet precedent est que les pinces s'ouvrent moins et est un peu simplifié meme si il verifie encore si il est face à un mur ou un robot.
	 * @return retourne true quand les pinces sont fermées, retourne false quand il fait fasse a un obstacle
	 * @param moteursR (MoteurRoues) 
	 * @param moteurP (MoteurPince)
	 * @param sensors (Sensors)
	 * module utilise : MoteurRoues: deplacer, setNiveauVitesse, getPilot, isMoving, tourner
	 * module l'utilisant : Main
	 */
	public static boolean prendrePaletDemarrage(MoteursRoues moteursR, MoteurPince moteurP, Sensors sensors) {
		
		moteursR.setNiveauVitesse("rapide");
		
		moteursR.deplacer(300, true);
		while(sensors.isPressed() == false) {
			Delay.msDelay(50);
			if(sensors.detecterMur()) {
		        moteursR.getPilot().stop();
		        moteursR.deplacer(-10, false);
		        moteursR.tourner(90, false);
		        return false;
		    }
		    else if(sensors.detecterRobot()) {
		        moteursR.getPilot().stop();
		        moteursR.tourner(90, false);
		        moteursR.deplacer(10, false);
		        moteursR.tourner(-90, false);
		        return false;
		    }
		}
		moteursR.getPilot().stop();
		if (moteursR.getPilot().isMoving() == true)
			return false;
		Delay.msDelay(100);
		return true;
	}
	
	/** 
	 * retourne a la position ou il a commencer la recherche palet precedente si il n'a pas eu un palet (le touchSensor n'a pas retourne true).
	 * @param moteursR (MoteurRoues) 
	 * @param sensors (Sensors)
	 * module utilise : MoteurRoues: deplacer, tourner, getPilot, tournerDe90; 
	 * 					Sensors: retourCouleur
	 * module l'utilisant : Main
	 */
	public static void retourSansPalet(MoteursRoues moteursR, Sensors sensors) {
		moteursR.tourner(180, false);
		moteursR.deplacer(200, true);
		while(sensors.retourCouleur() != "blanc" && (moteursR.getPilot().isMoving())) {
			Delay.msDelay(50);
		}
		moteursR.getPilot().stop();
	}
	
	/** ferme ses pince tourne et avance jusqu'a la ligne blanche depose le palet et se recule
	 * il gere aussi les cas d'erreur, comme tomber sur un mur, un robot ou si il va dans la mauvaise direction (avec la variable couleurOpposee)
	 * @return retourne true quand le palet a bien ete depose
	 * @param moteursR (MoteurRoues) 
	 * @param moteurP (MoteurPince)
	 * @param sensors (Sensors)
	 * @param angle (int) permet de donner l'angle du quel il faut tourner pour effectuer le retour
	 * @param ouverturePince ouverture des pinces ou nous avons le plus de chance de recuperer le palet
	 * module utilise : MoteurRoues: setNiveauVitesse, deplacer, tourner, getPilot, reculer, isMoving; 
	 * 					MoteurPince: fermerPince, modifierPince;
	 * 					Sensors: retourCouleur, detecterMur, detecterRobot;
	 * module l'utilisant : Main
	 */
	public static boolean ramenePalet(MoteursRoues moteursR, MoteurPince moteurP, Sensors sensors, int angle, int ouverturePince) {
		moteursR.setNiveauVitesse("rapide");
		moteurP.fermerPince(true);
		moteursR.tourner(angle, false);
		moteursR.deplacer(200, true);
		
		while(sensors.retourCouleur() != "blanc" && (moteursR.getPilot().isMoving())) {
			System.out.println(sensors.retourCouleur());
			Delay.msDelay(100);
			if(sensors.detecterMur()) {											// si il repère un mur, il s'arrète, recule, et revient vers l'en but
                moteursR.getPilot().stop();
                moteursR.deplacer(-10, false);
                moteursR.tourner(-90, false);
                moteursR.deplacer(180, true);
            }
            else if(sensors.detecterRobot()) {									// si il repere un robot, il s'arrete et fait une esquive afin de continuer sereinement son parcours
                moteursR.getPilot().stop();
                moteursR.deplacerArc(sensors, true);
            }
            else if(sensors.retourCouleur() == couleurOpposee) {				// ici le robot ne va pas dans la bonne direction donc il fait demi tour marquer le palet
                moteursR.getPilot().stop();
            	moteursR.tourner(180, false);
                moteursR.deplacer(400, true);
            }
		}
		moteursR.getPilot().stop();
		moteurP.modifierPince(ouverturePince, true);
		moteursR.reculer();
		return true;
	}

	/**
	 * permet de retourner marquer de maniere perpendiculaire en utilisant les lignes de couleur
	 * @return retourne true quand le palet est deposer dans l'embut
	 * @param moteursR (MoteurRoues) 
	 * @param moteurP (MoteurPince)
	 * @param sensors (Sensors)
	 * module utilise : MoteurRoues: setNiveauVitesse, deplacer,tourner, getPilot, isMoving, stop; 
	 * 					MoteurPince: ouvrirPince;
	 * 					Sensors: vueKermit, retourCouleur
	 * module l'utilisant : Main
	 */
	public static boolean retourPerpendiculaire(MoteursRoues moteursR, MoteurPince moteurP, Sensors sensors) {
        moteursR.setNiveauVitesse("lent");
        moteursR.tourner(180, true);
        String x;
        while(sensors.retourCouleur() == "gris" && moteursR.getPilot().isMoving()) {
            System.out.println("gris");
            Delay.msDelay(90);
        }
        x = sensors.retourCouleur();
        moteursR.getPilot().stop(); 
        if(x == "jaune" || x == "rouge") {
            moteursR.tourner(180, false);
        }
        if(x == "vert" || x == "bleu") {
            moteursR.tourner(90, false);
        }
        if(x == "noir") {
            moteursR.tourner(180, true);
            Delay.msDelay(200);
            while(sensors.retourCouleur() == "gris"&& moteursR.getPilot().isMoving()) {
                System.out.println("gris");
                Delay.msDelay(100);
            }
            x = sensors.retourCouleur();
            moteursR.getPilot().stop(); 
            Delay.msDelay(100);
            if(sensors.retourCouleur() == "vert" || sensors.retourCouleur() == "bleu") {
                moteursR.tourner(90, false);
            }
            else if(sensors.getDistance() <125) {
                moteursR.tourner(90, false);
            }

            System.out.println(sensors.getDistance());
            Delay.msDelay(2000);
        }
        moteursR.setNiveauVitesse("moyen");
        moteursR.deplacer(400, true);
        while(sensors.retourCouleur() != "blanc") {
            System.out.println(sensors.retourCouleur());
            Delay.msDelay(100);
        }
        moteursR.getPilot().stop();
        moteurP.ouvrirPince(false);
        moteursR.reculer();
        moteursR.tourner(90, false);    
        return true;
        
    }
	
	
	public static void main(String[] args) {		
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////// INITIALISATION ////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/**
		 * initialisation 
		 */
		Sensors sensors = new Sensors(LocalEV3.get().getPort("S1"), LocalEV3.get().getPort("S2"), LocalEV3.get().getPort("S4"));
		MoteursRoues moteursR = new MoteursRoues("rapide");
		MoteurPince moteurP = new MoteurPince();

		Etat etat = Etat.Debut;									// on modifie l'Etat selon ce que l'on veut tester / pour un match on commence evidemment par le depart, après un temps mort on modifie l'Etat ici
		Depart dep = Depart.NewGauche;							// juste pour eviter l'erreur NullPointerException() qui a tendance a arriver
		boolean check = true;									// boolean de stockage de retour pour les methodes appelees dans le switch
		int nbPalets = 0;										// le nombre de palets permet de savoir ou en est le robot dans son parcours, après un temps mort on le modifie ici pour avoir la prochaine recherche qui soit adpate aux palets encore presents
		
		
		/**
		 * affichage sur la console ou on indique au robot le cote et le demarrage donc la strategie utilisee pour ce match
		 */
		System.out.println("Cote de depart : ");
		System.out.println("  Cote bleu -> bouton droit");
		System.out.println("  Cote vert -> bouton gauche");
		Button.waitForAnyPress();		
		if (Button.RIGHT.isDown())
			couleurOpposee = "bleu";
		if(Button.LEFT.isDown())
			couleurOpposee = "vert";
		
		Delay.msDelay(1000);
		
		System.out.println("\n Cote robot adverse : ");
		Button.waitForAnyPress();		
		if (Button.RIGHT.isDown())
			dep = Depart.Droite;
		else if(Button.LEFT.isDown())
			dep = Depart.Gauche;
		else if(Button.ENTER.isDown())
			dep = Depart.Milieu;
		else if(Button.DOWN.isDown())
			dep = Depart.NewDroite;
		else if(Button.UP.isDown())
			dep = Depart.NewGauche;
		else
			dep = Depart.NewGauche;
		
		if (dep.equals(Depart.Milieu)) {
			System.out.println("\n Quel centre ?");
			Button.waitForAnyPress();
			if (Button.RIGHT.isDown())
				dep = Depart.MilieuDroiteNormal;
			if(Button.UP.isDown())
				dep = Depart.MilieuGaucheCentral;
			if(Button.LEFT.isDown())
				dep = Depart.MilieuGaucheNormal;
			if(Button.DOWN.isDown())
				dep = Depart.MilieuDroiteCentral;
		}
		
		if (dep.equals(Depart.Droite)) {
			System.out.println("droite");
			Delay.msDelay(3000);
		} else if (dep.equals(Depart.Gauche)) {
			System.out.println("gauche");
			Delay.msDelay(3000);
		} else if (dep.equals(Depart.NewDroite)) {
			System.out.println("newDroite");
			Delay.msDelay(3000);
		} else if (dep.equals(Depart.NewGauche)) {
			System.out.println("newGauche");
			Delay.msDelay(3000);
		} else if (dep.equals(Depart.MilieuDroiteCentral)) {
			System.out.println("milieu droite central");
			Delay.msDelay(3000);
		} else if (dep.equals(Depart.MilieuDroiteNormal)) {
			System.out.println("milieu droite normal");
			Delay.msDelay(3000);
		} else if (dep.equals(Depart.MilieuGaucheCentral)) {
			System.out.println("milieu gauche central");
			Delay.msDelay(3000);
		} else if (dep.equals(Depart.MilieuGaucheNormal)) {
			System.out.println("milieu gauche normal");
			Delay.msDelay(3000);
		} else {
			System.out.println("rien");
			Delay.msDelay(3000);
		}
	
		System.out.println("\n DEMARRER !");
		Button.ENTER.waitForPressAndRelease();						// après que le bouton Enter est presse le robot debute son match
				
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////// SWITCH //////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////		
		
		
		
		while(!(etat.equals(Etat.Fin))) {
			switch(etat) {			
				case Debut:															// selon la stratégie utilisee il adapte le depart
					if(dep.equals(Depart.Droite)) {
						demarrage(moteursR, moteurP, sensors, -135, -100);
					} else if (dep.equals(Depart.Gauche)) {
						demarrage(moteursR, moteurP, sensors, 151, 100);
					} else if (dep.equals(Depart.NewDroite)) {
						demarrage(moteursR, moteurP, sensors, 170, 0);
					} else if (dep.equals(Depart.NewGauche)) {
						demarrage(moteursR, moteurP, sensors, -170, 0);
					} else if (dep.equals(Depart.MilieuDroiteNormal)) {
						demarrage(moteursR, moteurP, sensors, -135, -90);
					} else if (dep.equals(Depart.MilieuDroiteCentral)) {
						demarrage(moteursR, moteurP, sensors, 170, 0);
					} else if (dep.equals(Depart.MilieuGaucheNormal)) {
						demarrage(moteursR, moteurP, sensors, 135, 90);
					} else if (dep.equals(Depart.MilieuGaucheCentral)) {
						demarrage(moteursR, moteurP, sensors, -170, 0);
					}
					nbPalets = 2;													// le nombre de palet passe a 2 a la fin d'un depart
					etat = Etat.RecherchePalet;										// on passe a l'Etat suivant : la recherche 
					break;
					
				case RecherchePalet : 												// Dans cet Etat, selon la strategie et le nombre de palet il adapte la recherche.
					
					if (nbPalets == 9) {											// si on atteint 9 palets ça ne sert à rien d'en chercher encore / pour un match mettre 5 suffit
						etat = Etat.Fin;
						break;
					}
					
					moteursR.setNiveauVitesse("rapide");							
					
					if(dep.equals(Depart.Droite)) {
						if (nbPalets < 3) {											// ici juste après un départ / pour la stratégie Droite / il doit tourner à gauche de 170° avant de commencer une recherche (permet de gagner du temps et de limiter les erreurs de la recherche
							moteursR.tourner(-170, false);
							check = recherchePalet(moteursR, moteurP, sensors, -1);
						} else if (nbPalets == 3) {
							moteursR.tourner(70, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						} else if (nbPalets >= 4) {
							moteursR.tourner(170, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						}
					} else if (dep.equals(Depart.Gauche)) {
						if (nbPalets < 3) {
							moteursR.tourner(170, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						} else if(nbPalets == 3) {
							moteursR.tourner(70, false);
							check = recherchePalet(moteursR, moteurP, sensors, -1);
						} else if (nbPalets == 4) {
							moteursR.tourner(-170, false);
							check = recherchePalet(moteursR, moteurP, sensors, -1);
						}
					} else if (dep.equals(Depart.NewDroite)) {
						if (nbPalets < 3) {
							moteursR.tourner(-180, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						} else if(nbPalets == 3) {
							moteursR.tourner(-200, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						} else if (nbPalets == 4) {
							moteursR.tourner(70, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						}
					} else if (dep.equals(Depart.NewGauche)) {
						if (nbPalets < 3) {
							moteursR.tourner(105, false);	// 60 to 90
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						} else if(nbPalets == 3) {
							moteursR.tourner(170, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						} else if (nbPalets >= 4) {
							moteursR.tourner(75, false);
							moteursR.deplacer(30, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						}
					} else if (dep.equals(Depart.MilieuGaucheCentral)) {
						if (nbPalets < 3) {
							moteursR.tourner(130, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						} else if(nbPalets == 3) {
							moteursR.tourner(170, false);
							moteursR.deplacer(30, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						} else if (nbPalets == 4) {
							moteursR.tourner(-100, false);
							moteursR.deplacer(60, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						}
					} else if (dep.equals(Depart.MilieuGaucheNormal)) {
						if (nbPalets < 3) {
							moteursR.tourner(170, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						} else if (nbPalets == 3) {
							moteursR.tourner(-50, false);
							check = recherchePalet(moteursR, moteurP, sensors, -1);
						} else if (nbPalets == 4){
							moteursR.tourner(-180, false);
							check = recherchePalet(moteursR, moteurP, sensors, -1); 	
						}
					} else if (dep.equals(Depart.MilieuDroiteCentral)) {
						if (nbPalets < 3) {
							moteursR.tourner(-60, false);
							check = recherchePalet(moteursR, moteurP, sensors, -1);
						} else if (nbPalets == 3) {
							moteursR.tourner(-170, false);
							check = recherchePalet(moteursR, moteurP, sensors, -1);
						} else if (nbPalets == 4){
							moteursR.tourner(70, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1); 	
						}
					} else if (dep.equals(Depart.MilieuDroiteNormal)) {
						if (nbPalets < 3) {
							moteursR.tourner(-170, false);
							check = recherchePalet(moteursR, moteurP, sensors, -1);
						} else if (nbPalets == 3) {
							moteursR.tourner(60, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						} else if (nbPalets == 4){
							moteursR.tourner(-180, false);
							check = recherchePalet(moteursR, moteurP, sensors, 1);
						}
					}
					
					moteursR.setNiveauVitesse("rapide");							// comme la vitesse est modifiee dans la methode de recherche il faut la remettre plus haute pour le reste
					
					if (check == true) {											// si la recherche s'est bien passee on peut aller prendre le palet sinon on avance un peu avant une nouvelle recherche
						moteursR.deplacer(10, false);
						etat = Etat.PrendrePalet;
					} else
						etat = Etat.RecherchePalet;
					break;
					
				case PrendrePalet : 												// dans cet Etat le robot va prendre le robot repere
					check = prendrePalet(moteursR, moteurP, sensors);
					System.out.println(check);
					Delay.msDelay(500);
					
					if (check == true) {											// si on a pris le palet on peut le remener sinon on recherche le palet (qui ne doit pas etre tres loin)
						etat = Etat.RamenerPalet;
					} else
						etat = Etat.RecherchePalet;
					break;
					
				case RamenerPalet :													// Dans cet etat il faut juste ramener le palet pris precedemment
					if(nbPalets == 3 && dep.equals(Depart.NewGauche)) { 
						ramenePalet(moteursR, moteurP, sensors, -180, 700);
					} else if(dep.equals(Depart.Droite) || dep.equals(Depart.Gauche) || dep.equals(Depart.NewDroite) || dep.equals(Depart.NewGauche) || dep.equals(Depart.MilieuGaucheCentral) || dep.equals(Depart.MilieuDroiteCentral) || dep.equals(Depart.MilieuDroiteNormal) || dep.equals(Depart.MilieuGaucheCentral)) {
						ramenePalet(moteursR, moteurP, sensors, 180, 700);
					} else if (dep.equals(Depart.MilieuDroiteNormal)){
						if (nbPalets < 3) {
							ramenePalet(moteursR, moteurP, sensors, 180, 700);
						} else if (nbPalets == 3){
							retourPerpendiculaire(moteursR, moteurP, sensors);
						} else {
							retourPerpendiculaire(moteursR, moteurP, sensors);
						}
					}					
					nbPalets++;														// le nombre de palet augmente d'1
					etat = Etat.RecherchePalet;										// apres avoir augmente le nombre de palets ramasses on peut faire une nouvelle recherche adaptee a la strategie et a ce nouveau nombre de palets ramasses
					break;

				case TempsMort:														// Dans cet Etat le robot avance tout droit en prenant un palet et en l'amenenant dans l'en but
					moteursR.deplacer(400, true);
					moteurP.ouvrirPince(false);
					sensors.kermitTouch();
					moteursR.getPilot().stop();
					moteurP.fermerPince(false);
					moteursR.deplacer(200, true);
					
					while(sensors.retourCouleur() != "blanc" && (moteursR.getPilot().isMoving())) {
						Delay.msDelay(50);
					}
					
					moteursR.getPilot().stop();
					moteurP.ouvrirPince(false);
					moteursR.reculer();
					nbPalets ++;
					
					etat = Etat.RecherchePalet;										// une fois qu'il a amene le palet il peut demarrer une recherchePalet 
					break;
					
				default :
					etat = Etat.Fin;												// si il est mal passe d'un Etat a un nouveau (cas n'arrivant pas) il sort du switch et s'arrete  
					break;
			}
		}
						
		System.out.println("En dehors du switch et le nbPalets == "+nbPalets);
		Delay.msDelay(8000);
	}
}
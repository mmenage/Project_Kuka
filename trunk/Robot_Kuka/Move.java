package application;


import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.controllerModel.sunrise.ISafetyState;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.deviceModel.OperationMode;
import com.kuka.roboticsAPI.geometricModel.AbstractFrame;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.geometricModel.math.Transformation;
import com.kuka.roboticsAPI.motionModel.Spline;
import com.kuka.roboticsAPI.motionModel.SplineJP;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianSineImpedanceControlMode;


/* Test des mouvements du robot Kuka avec la base "Paper"
 * 
 */

public class Move extends RoboticsAPIApplication {
	private Controller kuka_Sunrise_Cabinet_1;
	private LBR lbr_iiwa_14_R820_1;

	private MediaFlangeIOGroup ioFlange;
	
	private CartesianSineImpedanceControlMode impedanceControlMode;
	
	private Tool penTool;
	private ObjectFrame penToolTCP;
	
	private ObjectFrame paperBase;
	private ObjectFrame paperP1;
	private ObjectFrame paperP2;
	private ObjectFrame paperP3;
	private ObjectFrame paperP0;
	
	private ObjectFrame nearPaper0;
	private ObjectFrame paperApproach;
	
	
	private Frame P0;
	private Frame P1;
	private Frame P2;
	private Frame P3;
	private Frame nearPaperP0;
	
	
	double x0,x1,x2,x3;
	double y0,y1,y2,y3;
	
	private BezierCurve curve;
	private Vector2[] trajectory;
	private Frame[] frames;
	
	
	
	private Transformation getTranslationWithSpecifiedZ(ObjectFrame frameBefore, Frame p02, double z)
	{
		return Transformation.ofTranslation(
				p02.getX()-frameBefore.getX(), 
				p02.getY()-frameBefore.getY(), 
				z
				);
	}
	
	private Transformation getTranslationFromFrame(Frame frameBefore, Frame frameDestination)
	{
		return Transformation.ofTranslation(
				frameDestination.getX()-frameBefore.getX(), 
				frameDestination.getY()-frameBefore.getY(), 
				frameDestination.getZ()-frameBefore.getZ()
				);
	}
	
	public void initialize() {
		
		// Ligne = new Line(60,140,230,40);
		
		
		
		kuka_Sunrise_Cabinet_1 = getController("KUKA_Sunrise_Cabinet_1");
		lbr_iiwa_14_R820_1 = (LBR) getDevice(kuka_Sunrise_Cabinet_1, "LBR_iiwa_14_R820_1");

		ioFlange = new MediaFlangeIOGroup(kuka_Sunrise_Cabinet_1);
	
		// On cr�e la compliance pour rendre le bras "mou"
		impedanceControlMode = new CartesianSineImpedanceControlMode();
		impedanceControlMode.parametrize(CartDOF.X).setStiffness(3000);
		impedanceControlMode.parametrize(CartDOF.Y).setStiffness(3000);
		impedanceControlMode.parametrize(CartDOF.Z).setStiffness(3000);
		
		impedanceControlMode.parametrize(CartDOF.A).setStiffness(200);
		impedanceControlMode.parametrize(CartDOF.B).setStiffness(200);
		impedanceControlMode.parametrize(CartDOF.C).setStiffness(200);
		
		// On cr�e l'outil stylo, on l'attache au flange et on r�cup�re le point en bout de stylo "penToolTCP"
		penTool = getApplicationData().createFromTemplate("penTool");
		penTool.attachTo(lbr_iiwa_14_R820_1.getFlange() );
		penToolTCP = penTool.getFrame("/penToolTCP");
		
		
		// On charge les points de l'application
		paperBase = getApplicationData().getFrame("/Paper");
		paperP1 = getApplicationData().getFrame("/Paper/P1");
		paperP2 = getApplicationData().getFrame("/Paper/P2");
		paperP3 = getApplicationData().getFrame("/Paper/P3");
		paperP0 = getApplicationData().getFrame("/Paper/P0");
		
		nearPaper0 = getApplicationData().getFrame("/Paper/NearPaper0");
		paperApproach = getApplicationData().getFrame("/Paper/PaperApproach");
		

		
		
		
				
	//	nearPaperP0 = new Frame(paperBase).setX(40).setY(40).setZ(10).setAlphaRad(-0.3).setBetaRad(1.5).setGammaRad(-1.1);
		
	//	P0 = new Frame(nearPaper0).setX(Ligne.Start.x).setY(Ligne.Start.y).setZ(0);
				
	//	P1 = new Frame(nearPaper0).setX(Ligne.End.x).setY(Ligne.End.y).setZ(0);
		
		
	//	P2 = new Frame(nearPaper0).setX(x2).setY(y2).setZ(0);
	//	P3 = new Frame(nearPaper0).setX(x3).setY(y3).setZ(0);
		
		
		
		getLogger().info("Initialization OK");
	}

	
	public void In_Position(double x, double y){
		
		double velocity = 0.2;
		
		P0 = new Frame(paperApproach).setX(x).setY(y).setZ(10);
		P1 = new Frame(x, y, 0);
		penToolTCP.move(
				ptp(paperApproach).setJointVelocityRel(velocity)
			);
		
		penToolTCP.move(
				lin(nearPaper0).setJointVelocityRel(velocity)
			);
		
		
		getLogger().info("En position");
		penToolTCP.move(
				linRel(getTranslationWithSpecifiedZ(nearPaper0,P0 , P0.getZ() - nearPaper0.getZ() ), paperBase)
			);
		
		penToolTCP.move(
				linRel(getTranslationFromFrame(P0,P1), paperBase)
			);
	}
	
	public void Draw(double x, double y, double x2, double y2){
		
		P1 = new Frame(x, y, 0);
		P2 = new Frame(x2, y2, 0);
		
		getLogger().info("Je vais en P1");
				
		
		penToolTCP.move(
				// linRel(20.0,20.0,0.0, paperBase)			
				linRel(getTranslationFromFrame(P1,P2) ,paperBase)
				
			//	linRel(20, 20, 0, paperBase)
			);
		
		
		getLogger().info("Fin du trait");
		
	}
	
	
	public void Tool_Up(double x, double y){
		
		P0 = new Frame(paperApproach).setX(x).setY(y).setZ(10);
		P1 = new Frame(x, y, 0);
		
		penToolTCP.move(
				linRel(getTranslationFromFrame(P1,P0), paperBase)
			);
		
	}
	
	public void Draw_Curve(){
		
	}
	
	
	// fonction pour tracer une ligne
	public void Trace_Line(double x, double y, double x2, double y2){
		
		
//		double velocity = 0.2;
		In_Position(x, y);
		Draw(x, y, x2, y2);
		
	//	nearPaperP0 = new Frame(paperApproach).setX(x).setY(y).setZ(10);
		
		// Je vais en P0
	//	P0 = new Frame(paperApproach).setX(x).setY(y).setZ(0);
		
		
		// je vais en P0 version 2
		
/*		
		P0 = new Frame(paperApproach).setX(x).setY(y).setZ(10);
		
		penToolTCP.move(
				ptp(paperApproach).setJointVelocityRel(velocity)
			);
		
		penToolTCP.move(
				lin(nearPaper0).setJointVelocityRel(velocity)
			);
		
		
		getLogger().info("En position");
		penToolTCP.move(
				linRel(getTranslationWithSpecifiedZ(nearPaper0,P0 , P0.getZ() - nearPaper0.getZ() ), paperBase)
			);
		
*/
	
		
		
		
		// DERNIER COMMENTAIRE ICI
		
/*		
		P1 = new Frame(x, y, 0);
		P2 = new Frame(x2, y2, 0);
		
		getLogger().info("Je vais en P0");
*/
		
		
		
		
		
	/*	
		penToolTCP.move(
				lin(P0).setJointVelocityRel(velocity)
			);
	*/	
		
		// essai en bougeant avec des linRel
		
		
		
		// DERNIER COMMENTAIRE ICI
	/*
		penToolTCP.move(
				// linRel(20.0,20.0,0.0, paperBase)			
				linRel(getTranslationFromFrame(P1,P2) ,paperBase)
				
			//	linRel(20, 20, 0, paperBase)
			);
		
		
		
		
		
		
		getLogger().info("Je vais en P1");
	/*	penToolTCP.move(
				lin(P1).setJointVelocityRel(velocity)
			); 
		
	*/	
		
/*
		
		getLogger().info("Je me leve de la feuille");
		penToolTCP.move(  // on rel�ve la pointe du stylo
				lin(nearPaperP0).setJointVelocityRel(velocity)
			);
		
	*/	
		
	//	getLogger().info("Fin fonction");
		
	}
	// fonction pour tracer un rectangle

	public void Trace_Rectangle(double x, double y, double hauteur, double longueur){
		
		In_Position(x, y);
		
		Draw(x, y, x + longueur, y);
		Draw(x + longueur, y, x + longueur, y + hauteur);
		Draw(x + longueur, y + hauteur, x , y + hauteur);
		Draw(x , y + hauteur , x , y);
		Tool_Up(x, y);
		
	}
	
/*	
	// version avec deux points � l'oppos�
	public void Trace_Rectangle(double x, double y, double x2, double y2){
		
		In_Position(x, y);
		Draw(x, y,x2, y);
		Draw(x2, y, x2, y2);
		Draw(x2, y2, x, y2);
		Draw(x, y2 , x , y);
		Tool_Up(x, y);
	}
	
	
*/
	
	public void Courbe(){
	
		In_Position(30, 30);
		
		Vector2 p0 = new Vector2();
		Vector2 p1 = new Vector2();
		Vector2 p2 = new Vector2();
		Vector2 p3 = new Vector2();
		
		p0.x = 30.0;
		p0.y = 30.0;
		
		p1.x = 80.0;
		p1.y = 100.0;
		
		p2.x = 160.0;
		p2.y = -100.0;
		
		p3.x = 240.0;
		p3.y = 30.0;

		
		curve = new BezierCurve(p0, p1, p2, p3);
	
		trajectory = curve.getTrajectory(20);
		
		// On cr�e des frames robot Kuka depuis notre courbe
		frames = new Frame[trajectory.length];
		
		for (int i=0; i < trajectory.length; i++)
		{
//			getLogger().info("" + trajectory[i].x + " "+ trajectory[i].y);
			
			frames[i] = new Frame(trajectory[i].x, trajectory[i].y, 0);
		}
/*	
		for (int i=0; i < frames.length; i++){
			
			linRel(getTranslationFromFrame(frames[i],frames[i+1]), paperBase);
		}
*/		
	}
	
	 
	
	public void Triangle_Rectangle(double x, double y, double x2, double y2){
		
		Draw(x,y,x2,y2);
		Draw(x2,y2,x2,y);
		Draw(x2,y,x,y);
		
	}
	
	public void Triangle_Isocele(double x, double y, double x2, double y2){
		In_Position(x, y);
		Draw(x,y,x2,y2);
		Draw(x2,y2,x2+x,y);
		Draw(x2+x,y,x,y);
		Tool_Up(x, y);
		
	}
	
	
	public void Triangle_Equilateral(){
		
	}
	
	
	
	public void IMERIR(){
		
		// Je fais le I
		In_Position(5, 5);
		Draw(5, 5, 20, 5);
		Draw(20, 5, 5, 105);
		Draw(5, 105, 20, 105);
		Draw(20, 105, 5, 5);
		Tool_Up(5, 5);
		
		// Je fais le M
		
		In_Position(30, 5);
		Draw(30, 5, 30, 90);
		Draw(30, 90, 40, 90);
		Draw(40, 90, 50, 75);
		Draw(50, 75, 60, 90);
		Draw(60, 90, 70, 90);
		Draw(70, 90, 70, 5);
		Draw(70, 5, 60, 5);
		Draw(60, 5, 60, 75);
		Draw(60, 75, 50, 60);
		Draw(50, 60, 40, 75);
		Draw(40, 75, 40, 5);
		Draw(40, 5, 30, 5);
		Tool_Up(30, 5);
		
		// Je fais le E
		
		In_Position(80, 5);
		Draw(80, 5, 80, 90);
		Draw(80, 90, 115, 90);
		Draw(115, 90, 115, 80);
		Draw(115, 80, 90, 80);
		Draw(90, 80, 90, 55);
		Draw(90, 55, 110, 55);
		Draw(110, 55, 110, 45);
		Draw(110, 45, 90, 45);
		Draw(90, 45, 90, 15);
		Draw(90,15, 115, 15);
		Draw(115, 15, 115, 5);
		Draw(115, 5, 80, 5);
		Tool_Up(80, 5);
		
		// Je fais le  R
		
		In_Position(135, 80);
		Draw(135, 80, 165, 80);
		Draw(165, 80, 165, 65);
		Draw(165, 65, 135, 65);
		Draw(135, 65, 135, 80);
		Tool_Up(135, 80);
		
		In_Position(125, 5);
		Draw(125, 5, 125, 90);
		Draw(125, 90, 170, 90);
		Draw(170, 90, 170, 55);
		Draw(170, 55, 150, 55);
		Draw(150, 55, 170, 5);
		Draw(170, 5, 160, 5);
		Draw(160, 5, 140, 55);
		Draw(140, 55, 135, 55);
		Draw(135, 55, 135, 5);
		Draw(135, 5, 125, 5);
		Tool_Up(125, 5);
		
		Tool_Up(125, 5);
		
		// Je fais le second I
		In_Position(180, 5);
		Draw(180, 5, 180, 90);
		Draw(180, 90, 190, 90);
		Draw(190, 90, 190, 5);
		Draw(190, 5, 180, 5);
		Tool_Up(180, 5);
		
		
		// Je fais le second R
		
			In_Position(210, 80);
			Draw(210, 80, 235, 80);
			Draw(235, 80, 235, 65);
			Draw(235, 65, 210, 65);
			Draw(210, 65, 210, 80);
			Tool_Up(210, 80);
			
			In_Position(200, 5);
			Draw(200, 5, 200, 90);
			Draw(200, 90, 245, 90);
			Draw(245, 90, 245, 55);
			Draw(245, 55, 225, 55);
			Draw(225, 55, 245, 5);
			Draw(245, 5, 235, 5);
			Draw(235, 5, 215, 55);
			Draw(215, 55, 210, 55);
			Draw(210, 55, 210, 5);
			Draw(210, 5, 200, 5);
			Tool_Up(200, 5);	
	}
	
	public void run() {
		
		double velocity = 0.2;
		
		ISafetyState currentState = lbr_iiwa_14_R820_1.getSafetyState();
		OperationMode mode = currentState.getOperationMode();
		if (mode==OperationMode.AUT)
		{
			//******* ATTENTION : AVANT D'ACTIVE LE MODE AUTO, VERIFIER LES TRAJECTOIRES ********//
			// Si on est en mode AUTO, pleine vitesse activ�e
			
			velocity = 1;
		}
		
		lbr_iiwa_14_R820_1.move(ptpHome());
		
		// allumer la lumi�re
		ioFlange.setLEDBlue(true);
		

		
		// Approche de la base "Paper" en PTP puis en LIN
		
		getLogger().info("Move near Paper");
				
		penToolTCP.move(
				ptp(paperApproach).setJointVelocityRel(velocity)
			);
/*		

		
		penToolTCP.move(
				lin(nearPaperP0).setJointVelocityRel(velocity)
			);
		
*/		
		getLogger().info("Je vais me mettre en place");
		
	//	In_Position(x, y);
		//Trace_Line(40, 40, 100, 100);
		Trace_Rectangle(30, 30, 50, 100);
		Trace_Line(30, 30, 130, 80);
		
/*
		penToolTCP.move(
				lin(P0).setJointVelocityRel(velocity)
			);
/*		
		penToolTCP.move(
				lin(P1).setJointVelocityRel(velocity)
			);  
			
*/
		
		// On dessine sur le papier les points nearPaper1 / P0 / P1 / P2 / P3 :
		
/*		getLogger().info("Move on Paper");
		Spline drawingSpline = new Spline(
				
					// On bouge en relatif
					// Premier mouvement, on va en de nearPaper1 vers P0.
					linRel( getTranslationWithSpecifiedZ(nearPaper0, paperP0, paperP0.getZ() - nearPaper0.getZ() ), paperBase),
					
					// On va ensuite � P1, P2, P3 et P0, en sp�cifiant une translation Z nulle 
					linRel( getTranslationWithSpecifiedZ(paperP0, paperP1, 0), paperBase),
					linRel( getTranslationWithSpecifiedZ(paperP1, paperP2, 0), paperBase),
					linRel( getTranslationWithSpecifiedZ(paperP2, paperP3, 0), paperBase),
					linRel( getTranslationWithSpecifiedZ(paperP3, paperP0, 0), paperBase)
				);
		
		penToolTCP.move(
				drawingSpline.setJointVelocityRel(velocity)
				.setJointAccelerationRel(1)
				.setMode(impedanceControlMode)
			);
*/		
		
		
	//	Trace_Line();
		
		
		// On rel�ve la pointe du stylo
/*		penToolTCP.move( 
				linRel( getTranslationFromFrame(paperP0, nearPaper0), paperBase) 
			);
		
*/		
		// On revient � la "maison"
		getLogger().info("Go back to home");
		
		penToolTCP.move( lin(paperApproach).setJointVelocityRel(velocity));
		
		SplineJP moveBackToHome = new SplineJP( ptpHome());
		
		getLogger().info("Move Back");
		lbr_iiwa_14_R820_1.move(
				moveBackToHome.setJointVelocityRel(velocity)
			);
		
		ioFlange.setLEDBlue(false);
	}

	

	public static void main(String[] args) {
		Move app = new Move();
		app.runApplication();
	}
}

package application;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import net.mydreamy.steamboiler.threads.ControlSystem;
import net.mydreamy.steamboiler.threads.PumpController;
import net.mydreamy.steamboiler.threads.PumpSensor;
import net.mydreamy.steamboiler.threads.SteamBoiler;
import net.mydreamy.steamboiler.threads.SteamSensor;
import net.mydreamy.steamboiler.threads.Timer;
import net.mydreamy.steamboiler.threads.WaterSensor;

/**
 * 
 * @author yylonly
 * @organ UM Software Engineering Lab
 */
public class SteamBoilderController implements Initializable {

	@FXML
	Slider interval;
	@FXML
	TextField initq;
	@FXML
	TextField capacity;
	@FXML
	TextField m1;
	@FXML
	TextField m2;
	@FXML
	TextField n1;
	@FXML
	TextField n2;
	@FXML
	TextField op1;
	@FXML
	TextField op2;
	@FXML
	TextField pn;
	@FXML
	TextField pumpone;
	@FXML
	TextField w;
	@FXML
	TextField up;
	@FXML
	TextField down;
	@FXML
	TextField vmin;
	@FXML
	TextField ftru;
	@FXML
	TextField ftrd;
	@FXML
	Label systemstate;
	@FXML
	ProgressBar qbar;
	@FXML
	ProgressBar vbar;
	@FXML
	ProgressBar pbar;
	@FXML
	LineChart qchart;
	@FXML
	LineChart vchart;
	@FXML
	LineChart pchart;
	@FXML
	ProgressIndicator ptimer;
	@FXML
	AnchorPane simulator;
	@FXML
	Button basicinv;
	@FXML
	Button normalinv;
	@FXML
	Label qlabel;
	@FXML
	Label vlabel;
	@FXML
	Label plabel;
	@FXML
	GridPane gp;
	
	@FXML
	Button startProcessesButton;
	@FXML
	Button emergencyStopButton;
	@FXML
	Button brokeWaterSensorButton;
	@FXML
	Button repaireWaterSensorButton;

	private LinkedBlockingQueue<Float> waterchan;
	private LinkedBlockingQueue<Float> steamchan;
	private LinkedBlockingQueue<Float> pumpsensorchan;
	private LinkedBlockingQueue<Boolean> pumpcontrollerchan;
	private SteamBoiler steamBoiler;
	private ControlSystem controlSystem;
	private PumpSensor pumpSensor;
	private SteamSensor steamSensor;
	private WaterSensor waterSensor;
	private PumpController pumpController;
	private Timer timer;
	private ProgressIndicator steamboilerPI;
	private ProgressIndicator controlsytemPI;
	private ProgressIndicator wsPI;
	private ProgressIndicator ssPI;
	private ProgressIndicator psPI;
	private ProgressIndicator pcPI;
	
	
	
	public void preProcessFired(ActionEvent event) {
		
		
		/*
		 * Create Channel
		 */
		
		waterchan = new LinkedBlockingQueue<Float>();
		steamchan = new LinkedBlockingQueue<Float>();
		pumpsensorchan = new LinkedBlockingQueue<Float>();
		pumpcontrollerchan = new LinkedBlockingQueue<Boolean>();

		/*
		 * show on state bar
		 */
		
		systemstate.setText("Preprocess Finished");
		qbar.setProgress(0);
		ptimer.setProgress(0);
		
		/*
		 * INV Button to Green
		 */
		
		basicinv.setStyle("-fx-base: green;");
		normalinv.setStyle("-fx-base: green;");
		
	
		/*
		 * Process Status
		 */
		steamboilerPI = new ProgressIndicator();
		steamboilerPI.setVisible(false);
        gp.add(steamboilerPI, 1, 0);
 
		controlsytemPI = new ProgressIndicator();
		controlsytemPI.setVisible(false);
        gp.add(controlsytemPI, 1, 1);
        
		wsPI = new ProgressIndicator();
		wsPI.setVisible(false);
        gp.add(wsPI, 1, 2);
        
		ssPI = new ProgressIndicator();
		ssPI.setVisible(false);
        gp.add(ssPI, 1, 3);
        
		psPI = new ProgressIndicator();
		psPI.setVisible(false);
        gp.add(psPI, 1, 4);
        
		pcPI = new ProgressIndicator();
		pcPI.setVisible(false);
        gp.add(pcPI, 1, 5);
		
		
		/*
		 * Set simulator GUI
		 */
		setQ(Float.parseFloat(initq.getText()));
		// Draw background
		int PNUM = 5;
		for (int i = 0; i < PNUM; i++)
			pumpOn();
		drawBackGround(simulator);
		// draw mark
		drawMark(simulator);

	}

	public void createProcessesFired(ActionEvent event) {

		/*
		 * ProgressIndicator
		 */

		steamboilerPI.setVisible(true);
		controlsytemPI.setVisible(true);
		wsPI.setVisible(true);
		ssPI.setVisible(true);
		psPI.setVisible(true);
		pcPI.setVisible(true);
     
		/*
		 * Get Parameters
		 */
		
		float initq =Float.parseFloat(this.initq.getText());
		float c = Float.parseFloat(capacity.getText());
		float m1 = Float.parseFloat(this.m1.getText());
		float m2 = Float.parseFloat(this.m2.getText());
		float n1 = Float.parseFloat(this.n1.getText());
		float n2 = Float.parseFloat(this.n2.getText());
		float best1 = Float.parseFloat(this.op1.getText());
		float best2 = Float.parseFloat(this.op2.getText());
		int pN = Integer.parseInt(this.pn.getText());
		float w = Float.parseFloat(this.w.getText());
		float pumpone = Float.parseFloat(this.pumpone.getText());
		float up = Float.parseFloat(this.up.getText());
		float down = Float.parseFloat(this.down.getText());
		float vminout = Float.parseFloat(this.vmin.getText());
		float ftru = Float.parseFloat(this.ftru.getText());
		float ftrd = Float.parseFloat(this.ftrd.getText());
		int interval = (int) this.interval.getValue();
		
		/*
		 * Create Processes
		 */
		timer = new Timer();
		
		steamBoiler = new SteamBoiler(interval, initq, c, m1, m2, n1, n2, best1, best2, pN, w,
				pumpone, up, down, vminout, timer, qbar, vbar, pbar, ptimer, this);

		controlSystem = new ControlSystem(waterchan, steamchan, pumpsensorchan,
				pumpcontrollerchan, best1, best2, ftru, ftrd, pumpone, pN, c, n1, n2, m1, m2, this, interval);

		pumpSensor = new PumpSensor(steamBoiler, pumpsensorchan, timer, interval);
		steamSensor = new SteamSensor(steamBoiler, steamchan, timer, interval);
		waterSensor = new WaterSensor(steamBoiler, waterchan, timer, interval, ptimer);

		pumpController = new PumpController(steamBoiler, pumpcontrollerchan, this);

	

		/*
		 * Show on state bar
		 */
		systemstate.setText("Create Processes Finished");

	}

	public void startProcessesFired(ActionEvent event) {

		/*
		 * Start Processes
		 */
		preProcessFired(event);
		createProcessesFired(event);
		
		timer.start();
		steamBoiler.start();
		pumpSensor.start();
		steamSensor.start();
		waterSensor.start();
		pumpController.start();

		controlSystem.start();


		/*
		 * Show on state bar
		 */
		systemstate.setText("Start Processes");
		
		/*
		 * Change the disable of Button
		 */
		startProcessesButton.setDisable(true);
		emergencyStopButton.setDisable(false);
		brokeWaterSensorButton.setDisable(false);
		repaireWaterSensorButton.setDisable(true);
	}

	public void emergencyStopFired(ActionEvent event) {

		/*
		 * Emergency Stop
		 */
		steamBoiler.setStart(false);
		pumpSensor.setStart(false);
		steamSensor.setStart(false);
		waterSensor.setStart(false);
		pumpController.setStart(false);
		controlSystem.setStart(false);
		controlSystem.setRescue(false);
		timer.setStart(false);
/*		try {
			steamBoiler.join();
			pumpSensor.join();
			steamSensor.join();
			waterSensor.join();
			pumpController.join();
			controlSystem.join();
			timer.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		steamboilerPI.setVisible(false);
		controlsytemPI.setVisible(false);
		wsPI.setVisible(false);
		ssPI.setVisible(false);
		psPI.setVisible(false);
		pcPI.setVisible(false);
		
		/*
		 * Change the disable of Button
		 */
		startProcessesButton.setDisable(false);
		emergencyStopButton.setDisable(true);
		brokeWaterSensorButton.setDisable(true);
		repaireWaterSensorButton.setDisable(true);
		
		/*
		 * Show on state bar
		 */
		systemstate.setText("Emergency Mode");

	}

	public void rescueModeFired(ActionEvent event) {

		/*
		 * BrokeWaterSensor
		 */
		controlSystem.setRescue(true);
		
		/*
		 * Change the disable of Button
		 */
		startProcessesButton.setDisable(true);
		emergencyStopButton.setDisable(false);
		brokeWaterSensorButton.setDisable(true);
		repaireWaterSensorButton.setDisable(false);

		/*
		 * Show on state bar
		 */
		systemstate.setText("Rescue Mode");

	}

	public void repairedWaterSensorFired(ActionEvent event) {

		/*
		 * RepairedWaterSensor
		 */
		controlSystem.setRescue(false);
		
		/*
		 * Change the disable of Button
		 */
		startProcessesButton.setDisable(true);
		emergencyStopButton.setDisable(false);
		brokeWaterSensorButton.setDisable(false);
		repaireWaterSensorButton.setDisable(true);

		/*
		 * Show on state bar
		 */
		systemstate.setText("Normal Mode");

	}

	/**
	 * Draw Steam Boiler
	 * 
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		/*
		 * Init the button
		 */
		startProcessesButton.setDisable(false);
		emergencyStopButton.setDisable(true);
		brokeWaterSensorButton.setDisable(true);
		repaireWaterSensorButton.setDisable(true);
		
		// TODO Auto-generated method stub
		waterRectangle = new Rectangle();
		waterRectangle.setX(352);
		waterRectangle.setY(475);
		waterRectangle.setWidth(247);
		waterRectangle.setHeight(0);
		waterRectangle.setFill(Color.LIGHTCYAN);
		simulator.getChildren().add(waterRectangle);
		
		// The label of mark
		txtM2 = new Text(630,0,"M2");
		txtM1 = new Text(630,0,"M1");
		txtN2 = new Text(630,0,"N2");
		txtN1 = new Text(630,0,"N1");
		txtB2 = new Text(630,0,"B2");
		txtB1 = new Text(630,0,"B1");
		
		simulator.getChildren().add(txtM2);
		simulator.getChildren().add(txtN2);
		simulator.getChildren().add(txtB2);
		simulator.getChildren().add(txtB1);
		simulator.getChildren().add(txtN1);
		simulator.getChildren().add(txtM1);

		// draw background 
		drawBackGround(simulator); 
		
		
		pumpRectangle1 = new Rectangle();
		pumpRectangle1.setX(0);
		pumpRectangle1.setY(202);
		pumpRectangle1.setWidth(0);
		pumpRectangle1.setHeight(31);
		pumpRectangle1.setFill(Color.LIGHTSKYBLUE);
		
		pumpRectangle2 = new Rectangle();
		pumpRectangle2.setX(0);
		pumpRectangle2.setY(262);
		pumpRectangle2.setWidth(0);
		pumpRectangle2.setHeight(31);
		pumpRectangle2.setFill(Color.LIGHTSKYBLUE);
		
		pumpRectangle3 = new Rectangle();
		pumpRectangle3.setX(0);
		pumpRectangle3.setY(322);
		pumpRectangle3.setWidth(0);
		pumpRectangle3.setHeight(31);
		pumpRectangle3.setFill(Color.LIGHTSKYBLUE);
		
		pumpRectangle4 = new Rectangle();
		pumpRectangle4.setX(0);
		pumpRectangle4.setY(382);
		pumpRectangle4.setWidth(0);
		pumpRectangle4.setHeight(31);
		pumpRectangle4.setFill(Color.LIGHTSKYBLUE);
		
		pumpRectangle5 = new Rectangle();
		pumpRectangle5.setX(0);
		pumpRectangle5.setY(442);
		pumpRectangle5.setWidth(0);
		pumpRectangle5.setHeight(31);
		pumpRectangle5.setFill(Color.LIGHTSKYBLUE);
		
		verticalRectangle = new Rectangle();
		verticalRectangle.setX(170);
		verticalRectangle.setY(202);
		verticalRectangle.setWidth(30);
		verticalRectangle.setHeight(0);
		verticalRectangle.setFill(Color.LIGHTSKYBLUE);
		
		horizontalRectangle = new Rectangle();
		horizontalRectangle.setX(197);
		horizontalRectangle.setY(442);
		horizontalRectangle.setWidth(0);
		horizontalRectangle.setHeight(31);
		horizontalRectangle.setFill(Color.LIGHTSKYBLUE);
		
		simulator.getChildren().add(pumpRectangle1);
		simulator.getChildren().add(pumpRectangle2);
		simulator.getChildren().add(pumpRectangle3);
		simulator.getChildren().add(pumpRectangle4);
		simulator.getChildren().add(pumpRectangle5);
		simulator.getChildren().add(verticalRectangle);
		simulator.getChildren().add(horizontalRectangle);
		
		// pump
		rPump1 = new Rectangle();
		rPump1.setX(161);
		rPump1.setY(200);
		rPump1.setWidth(12);
		rPump1.setHeight(35);
		rPump1.setFill(Color.RED);
		
		rPump2 = new Rectangle();
		rPump2.setX(161);
		rPump2.setY(260);
		rPump2.setWidth(12);
		rPump2.setHeight(35);
		rPump2.setFill(Color.RED);
		
		rPump3 = new Rectangle();
		rPump3.setX(161);
		rPump3.setY(320);
		rPump3.setWidth(12);
		rPump3.setHeight(35);
		rPump3.setFill(Color.RED);
		
		rPump4 = new Rectangle();
		rPump4.setX(161);
		rPump4.setY(380);
		rPump4.setWidth(12);
		rPump4.setHeight(35);
		rPump4.setFill(Color.RED);
		
		rPump5 = new Rectangle();
		rPump5.setX(161);
		rPump5.setY(440);
		rPump5.setWidth(12);
		rPump5.setHeight(35);
		rPump5.setFill(Color.RED);
		
		simulator.getChildren().add(rPump1);
		simulator.getChildren().add(rPump2);
		simulator.getChildren().add(rPump3);
		simulator.getChildren().add(rPump4);
		simulator.getChildren().add(rPump5);
		
		txtPump1 = new Text (100, 220, "Off");
		txtPump2 = new Text (100, 280, "Off");
		txtPump3 = new Text (100, 340, "Off");
		txtPump4 = new Text (100, 400, "Off");
		txtPump5 = new Text (100, 460, "Off");
		
		rectangleMark1 = new Rectangle();
		rectangleMark2 = new Rectangle();
		rectangleMark3 = new Rectangle();
		rectangleMark4 = new Rectangle();
		rectangleMark5 = new Rectangle();
		rectangleMark6 = new Rectangle();
		rectangleMark7 = new Rectangle();
		
		simulator.getChildren().add(txtPump1);
		simulator.getChildren().add(txtPump2);
		simulator.getChildren().add(txtPump3);
		simulator.getChildren().add(txtPump4);
		simulator.getChildren().add(txtPump5);
		
		simulator.getChildren().add(rectangleMark1);
		simulator.getChildren().add(rectangleMark2);
		simulator.getChildren().add(rectangleMark3);
		simulator.getChildren().add(rectangleMark4);
		simulator.getChildren().add(rectangleMark5);
		simulator.getChildren().add(rectangleMark6);
		simulator.getChildren().add(rectangleMark7);
		
		this.capacityValue = Float.parseFloat(capacity.getText());;  // The C
		
		double height =  0; // Water height
		// Draw water
		// draw the label and color mark
		drawMark(simulator);
		drowWaterRectangle(waterRectangle, numberTransform(height));
		
		///////////////////////////////////////////////////////////////////////
		// The listen method of TextField input 
		// C
		capacity.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Type Error");
				if (!newValue.matches("\\d*")) {
					capacity.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setContentText("Please enter number!");
					alert.showAndWait();
				}
			}
		});
		capacity.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input value Error");
				// 
				int cValue = Integer.valueOf(capacity.getText());
				if( cValue < 0 ){
					// Warning Dialog
					alert.setContentText("C should not be smaller than Zero!");
					alert.showAndWait();
				}
			}
			
		});
		// initq
		initq.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				if (!newValue.matches("\\d*")) {
					initq.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		initq.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
		    	alert.setTitle("Warning Dialog");
		    	
				if (!initq.getText().matches("\\d*")) {
					initq.setText(initq.getText().replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setContentText("Please enter number!");
					alert.showAndWait();
				}
				
				int initqValue = Integer.valueOf(initq.getText());
				int cValue = Integer.valueOf(capacity.getText());
				if(initqValue >= cValue ){
					// Warning Dialog
					alert.setContentText("The initq should not be bigger than C!");
					alert.showAndWait();
				}
			}
			
		});
		// M1
		m1.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				
				Alert alert = new Alert(AlertType.WARNING);
				if (!newValue.matches("\\d*")) {
					m1.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		m1.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Value Error");
				int m1Value = Integer.valueOf(m1.getText());
				int n1Value = Integer.valueOf(n1.getText());
				if( m1Value >= n1Value ){
					// Warning Dialog
					alert.setContentText("The value of M1 should not be bigger than N1!");
					alert.showAndWait();
				}
			}
			
		});
		//M2 
		m2.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				
				if (!newValue.matches("\\d*")) {
					m2.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		m2.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Value Error");
				int m2Value = Integer.valueOf(m2.getText());
				int n2Value = Integer.valueOf(n2.getText());
				int cValue = Integer.valueOf(capacity.getText());
				if( m2Value >= cValue ){
					// Warning Dialog
					alert.setContentText("The value of M2 should not be bigger than C!");
					alert.showAndWait();
				}
				if( m2Value <= n2Value ){
					// Warning Dialog
					alert.setContentText("The value of M2 should not be smaller than N2!");
					alert.showAndWait();
				}
			}
			
		});
		//N1
		n1.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				if (!newValue.matches("\\d*")) {
					n1.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		n1.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Value Error");
				int n1Value = Integer.valueOf(n1.getText());
				int m1Value = Integer.valueOf(m1.getText());
				int b1Value = Integer.valueOf(op1.getText());
				if( n1Value >= b1Value ){
					// Warning Dialog
					alert.setContentText("The value of N1 should not be bigger than B1!");
					alert.showAndWait();
				}
				if( n1Value <= m1Value ){
					// Warning Dialog
					alert.setContentText("The value of N1 should not be smaller than M1!");
					alert.showAndWait();
				}
			}
		});
		//N2
		n2.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				
				if (!newValue.matches("\\d*")) {
					n2.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		n2.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Value Error");
				int n2Value = Integer.valueOf(n2.getText());
				int m2Value = Integer.valueOf(m2.getText());
				int b2Value = Integer.valueOf(op2.getText());
				if( n2Value >= m2Value ){
					// Warning Dialog
					alert.setContentText("The value of N2 should not be bigger than M2!");
					alert.showAndWait();
				}
				if( n2Value <= b2Value ){
					// Warning Dialog
					alert.setContentText("The value of N2 should not be smaller than B2!");
					alert.showAndWait();
				}
			}
		});
		// B1
		op1.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				if (!newValue.matches("\\d*")) {
					op1.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		op1.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Value Error");
				int b1Value = Integer.valueOf(op1.getText());
				int n1Value = Integer.valueOf(n1.getText());
				int b2Value = Integer.valueOf(op2.getText());
				if( b1Value >= b2Value ){
					// Warning Dialog
					alert.setContentText("The value of B1 should not be bigger than B2!");
					alert.showAndWait();
				}
				if( b1Value <= n1Value ){
					// Warning Dialog
					alert.setContentText("The value of B1 should not be smaller than N1!");
					alert.showAndWait();
				}
			}
		});
		//B2
		op2.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				if (!newValue.matches("\\d*")) {
					op2.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		op2.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Value Error");
				int b2Value = Integer.valueOf(op2.getText());
				int n2Value = Integer.valueOf(n2.getText());
				int b1Value = Integer.valueOf(op1.getText());
				if( b2Value >= n2Value ){
					// Warning Dialog
					alert.setContentText("The value of B2 should not be bigger than N2!");
					alert.showAndWait();
				}
				if( b2Value <= b1Value ){
					// Warning Dialog
					alert.setContentText("The value of B2 should not be smaller than B1!");
					alert.showAndWait();
				}
			}
		});
		//PN
		pn.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				if (!newValue.matches("\\d*")) {
					pn.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		pn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Value Error");
				int pnValue = Integer.valueOf(pn.getText());
				if(!(pnValue >= 0 && pnValue <= 5)){
					alert.setContentText("The range of PN :(0-5)!");
					alert.showAndWait();
				}
			}
		});
		//PQ
		pumpone.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				if (!newValue.matches("\\d*")) {
					pumpone.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		pumpone.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Value Error");
				int pumponeValue = Integer.valueOf(pumpone.getText());
				if( pumponeValue < 0 ){
					alert.setContentText("PQ should not be smaller than Zero!");
					alert.showAndWait();
				}
			}
		});
		//W
		w.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				if (!newValue.matches("\\d*")) {
					w.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		w.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Value Error");
				int wValue = Integer.valueOf(w.getText());
				if( wValue < 0 ){
					alert.setContentText("w should not be smaller than Zero!");
					alert.showAndWait();
				}
			}
		});
		//UP
		up.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				if (!newValue.matches("\\d*")) {
					up.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		up.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Value Error");
				int upValue = Integer.valueOf(up.getText());
				int wValue = Integer.valueOf(w.getText());
				if( upValue < 0 ){
					alert.setContentText("UP should not be smaller than Zero!");
					alert.showAndWait();
				}
				if( upValue >= wValue ){
					alert.setContentText("UP should not be bigger than W!");
					alert.showAndWait();
				}
			}
		});
		//DOWN
		down.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				if (!newValue.matches("\\d*")) {
					down.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		down.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Value Error");
				int downValue = Integer.valueOf(down.getText());
				int wValue = Integer.valueOf(w.getText());
				if( downValue < 0 ){
					alert.setContentText("down should not be smaller than Zero!");
					alert.showAndWait();
				}
				if( downValue >= wValue ){
					alert.setContentText("down should not be bigger than W!");
					alert.showAndWait();
				}
			}
		});
		//VMIN
		vmin.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				if (!newValue.matches("\\d*")) {
					vmin.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		vmin.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Value Error");
				int vminValue = Integer.valueOf(vmin.getText());
				int b1Value = Integer.valueOf(op1.getText());
				int b2Value = Integer.valueOf(op2.getText());
				if( vminValue < 0){
					alert.setContentText("vmin should not be smaller than Zero!");
					alert.showAndWait();
				}
				if( vminValue >= Math.min(b1Value, b2Value)){
					alert.setContentText("vmin should not be bigger than B1 and B2!");
					alert.showAndWait();
				}
			}
		});
		// FTRU
		ftru.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				if (!newValue.matches("\\d*")) {
					ftru.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		ftru.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Value Error");
				int ftruValue = Integer.valueOf(ftru.getText());
				int b1Value = Integer.valueOf(op1.getText());
				int b2Value = Integer.valueOf(op2.getText());
				if( ftruValue < 0){
					alert.setContentText("ftru should not be smaller than Zero!");
					alert.showAndWait();
				}
				if( ftruValue >= (b2Value - b1Value)){
					alert.setContentText("ftru should not be bigger than (B2 - B1)!");
					alert.showAndWait();
				}
			}
		});
		// FTRD
		ftrd.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				Alert alert = new Alert(AlertType.WARNING);
				if (!newValue.matches("\\d*")) {
					ftrd.setText(newValue.replaceAll("[^\\d]", ""));
					
					// Warning Dialog
					alert.setTitle("Warning Dialog");
					alert.setHeaderText("Input Type Error");
					alert.setContentText("Please enter number!");

					alert.showAndWait();
				}
			}
		});
		ftrd.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText("Input Value Error");
				int ftrdValue = Integer.valueOf(ftrd.getText());
				int b1Value = Integer.valueOf(op1.getText());
				int b2Value = Integer.valueOf(op2.getText());
				if( ftrdValue < 0){
					alert.setContentText("ftrd should not be smaller than Zero!");
					alert.showAndWait();
				}
				if( ftrdValue >= (b2Value - b1Value)){
					alert.setContentText("ftrd should not be bigger than (B2 - B1)!");
					alert.showAndWait();
				}
			}
		});
		
		
	}
	
	/**
	 *  the height of water 
	 * @param input
	 * @return
	 */
	public double numberTransform(double input){
		// in the UI, the height is 375
		if(input > capacityValue){
			return 0.0;
		}
		double height = (double) (input * 370 / capacityValue);
		return height;
	}
	

	public void drowWaterRectangle(Rectangle rectangle,double height){
		
		double percentm2 = Double.valueOf(m2.getText()) / 100; // m2
		double percentm1 = Double.valueOf(m1.getText()) / 100; // m1
		double percentn2 = Double.valueOf(n2.getText()) / 100; // n2
		double percnetn1 = Double.valueOf(n1.getText()) / 100; // n1
		double percentb2 = Double.valueOf(op2.getText()) / 100; // b2
		double percentb1 = Double.valueOf(op1.getText()) / 100; // b1
		
		if( height > 370 ){
			return;
		}
		
		rectangle.setY(473-height);  // rectangle y value
		rectangle.setHeight(height); // set height
		
		// judge the color of water 
		double percent = 1.0 * height / 370;
		
		if( percent > percentm2 || percent <= percentm1){
			rectangle.setFill(Color.RED);
		}else if((percent <= percentm2 && percent > percentn2 ) || (percent > percentm1 && percent <= percnetn1 )){
			rectangle.setFill(Color.LIGHTSALMON);
		}else if((percent <= percentn2 && percent > percentb2) || (percent > percnetn1 && percent < percentb1)){
			rectangle.setFill(Color.LIGHTBLUE);
		}else if( percent >= percentb1 && percent <= percentb2){
			rectangle.setFill(Color.LIGHTSKYBLUE);
		}
	}
	
	/**
	 *  open a pump
	 */
	public void pumpOn(){
		if(!pump1){
			pump1 = true;
		}else if(!pump2){
			pump2 = true;
		}else if(!pump3){
			pump3 = true;
		}else if(!pump4){
			pump4 = true;
		}else if(!pump5){
			pump5 = true;
		}
		checkPumpOpen();
	}
	
	/**
	 *  close a pump
	 */
	public void pumpDown(){
		if(pump1){
			pump1 = false;
		}else if(pump2){
			pump2 = false;
		}else if(pump3){
			pump3 = false;
		}else if(pump4){
			pump4 = false;
		}else if(pump5){
			pump5 = false;
		}
		checkPumpOpen();
	}
	
	/**
	 * Set the height of water
	 * @param height
	 */
	public void setQ(float height){
		
		drowWaterRectangle(waterRectangle, numberTransform(height));
	}
	
	/**
	 *  Judge the pump is open or close
	 */
	public void checkPumpOpen(){
	
		txtPump1.setText( !pump1 ? "Off" : "On" );
		txtPump2.setText( !pump2 ? "Off" : "On" );
		txtPump3.setText( !pump3 ? "Off" : "On" );
		txtPump4.setText( !pump4 ? "Off" : "On" );
		txtPump5.setText( !pump5 ? "Off" : "On" );
		
		if( pump1 || pump2 || pump3 || pump4 || pump5 ){
			horizontalRectangle.setWidth(155);
		}else{
			horizontalRectangle.setWidth(0);
		}
		
		if(pump1){
			rPump1.setX(161);
			rPump1.setY(200);
			rPump1.setWidth(35);
			rPump1.setHeight(12);
			rPump1.setFill(Color.BLUE);
			pumpRectangle1.setWidth(172);
		}else{
			rPump1.setX(161);
			rPump1.setY(200);
			rPump1.setWidth(12);
			rPump1.setHeight(35);
			rPump1.setFill(Color.RED);
			pumpRectangle1.setWidth(0);
		}
		
		if(pump2){
			rPump2.setX(161);
			rPump2.setY(260);
			rPump2.setWidth(35);
			rPump2.setHeight(12);
			rPump2.setFill(Color.BLUE);
			pumpRectangle2.setWidth(172);
		}else{
			rPump2.setX(161);
			rPump2.setY(260);
			rPump2.setWidth(12);
			rPump2.setHeight(35);
			rPump2.setFill(Color.RED);
			pumpRectangle2.setWidth(0);
		}
		
		if(pump3){
			rPump3.setX(161);
			rPump3.setY(320);
			rPump3.setWidth(35);
			rPump3.setHeight(12);
			rPump3.setFill(Color.BLUE);
			pumpRectangle3.setWidth(172);
		}else{
			rPump3.setX(161);
			rPump3.setY(320);
			rPump3.setWidth(12);
			rPump3.setHeight(35);
			rPump3.setFill(Color.RED);
			pumpRectangle3.setWidth(0);
		}
		
		if(pump4){
			rPump4.setX(161);
			rPump4.setY(380);
			rPump4.setWidth(35);
			rPump4.setHeight(12);
			rPump4.setFill(Color.BLUE);
			pumpRectangle4.setWidth(172);
		}else{
			rPump4.setX(161);
			rPump4.setY(380);
			rPump4.setWidth(12);
			rPump4.setHeight(35);
			rPump4.setFill(Color.RED);
			pumpRectangle4.setWidth(0);
		}
		
		if(pump5){
			rPump5.setX(161);
			rPump5.setY(440);
			rPump5.setWidth(35);
			rPump5.setHeight(12);
			rPump5.setFill(Color.BLUE);
			pumpRectangle5.setWidth(172);
		}else{
			rPump5.setX(161);
			rPump5.setY(440);
			rPump5.setWidth(12);
			rPump5.setHeight(35);
			rPump5.setFill(Color.RED);
			pumpRectangle5.setWidth(0);
		}
		
		if( pump1 ){
			verticalRectangle.setX(170);
			verticalRectangle.setY(202);
			verticalRectangle.setWidth(30);
			verticalRectangle.setHeight(271);
			horizontalRectangle.setWidth(155);
		}else if(pump2){
			verticalRectangle.setX(170);
			verticalRectangle.setY(260);
			verticalRectangle.setWidth(30);
			verticalRectangle.setHeight(216);
			horizontalRectangle.setWidth(155);
		}else if(pump3){
			verticalRectangle.setX(170);
			verticalRectangle.setY(320);
			verticalRectangle.setWidth(30);
			verticalRectangle.setHeight(156);
			horizontalRectangle.setWidth(155);
		}else if(pump4){
			verticalRectangle.setX(170);
			verticalRectangle.setY(380);
			verticalRectangle.setWidth(30);
			verticalRectangle.setHeight(93);
			horizontalRectangle.setWidth(155);
		}else if(pump5){
			verticalRectangle.setX(170);
			verticalRectangle.setY(440);
			verticalRectangle.setWidth(30);
			verticalRectangle.setHeight(33);
			horizontalRectangle.setWidth(155);
		}
	}
	
	/**
	 *  Draw the mark and label
	 * @param root
	 */
	public void drawMark(AnchorPane root){
		
		double mValue1 = Double.valueOf(m1.getText());
		
		double mValue2 = Double.valueOf(m2.getText());
		
		double nValue1 = Double.valueOf(n1.getText());
		
		double nValue2 = Double.valueOf(n2.getText());
		
		double bValue1 = Double.valueOf(op1.getText());
		
		double bValue2 = Double.valueOf(op2.getText());
		
		// M2
		double height = 102;
		rectangleMark1.setX(602);
		rectangleMark1.setY(102);
		rectangleMark1.setWidth(20);
		rectangleMark1.setHeight(370 * (100-mValue2) / 100);
		rectangleMark1.setFill(Color.RED);
		height += 370 * (100-mValue2) / 100;
		txtM2.setY(height);
		// N2
		rectangleMark2.setX(602);
		rectangleMark2.setY(height);
		rectangleMark2.setWidth(20);
		rectangleMark2.setHeight(370 * (mValue2 - nValue2) / 100);
		rectangleMark2.setFill(Color.LIGHTSALMON);
		height += 370 * (mValue2 - nValue2) / 100;
		txtN2.setY(height);
		// b2
		rectangleMark3.setX(602);
		rectangleMark3.setY(height);
		rectangleMark3.setWidth(20);
		rectangleMark3.setHeight(370 *(nValue2 -bValue2) / 100);
		rectangleMark3.setFill(Color.LIGHTBLUE);
		height += 370 *(nValue2 -bValue2) / 100;
		txtB2.setY(height);
		// b1
		rectangleMark4.setX(602);
		rectangleMark4.setY(height);
		rectangleMark4.setWidth(20);
		rectangleMark4.setHeight(370 * (bValue2 - bValue1) / 100);
		rectangleMark4.setFill(Color.LIGHTSKYBLUE);
		height += 370 * (bValue2 - bValue1) / 100;
		txtB1.setY(height);
		// n1
		rectangleMark5.setX(602);
		rectangleMark5.setY(height);
		rectangleMark5.setWidth(20);
		rectangleMark5.setHeight(370 * (bValue1 - nValue1) / 100);
		rectangleMark5.setFill(Color.LIGHTBLUE);
		height += 370 * (bValue1 - nValue1) / 100;
		txtN1.setY(height);
		// m
		rectangleMark6.setX(602);
		rectangleMark6.setY(height);
		rectangleMark6.setWidth(20);
		rectangleMark6.setHeight(370 * (nValue1 - mValue1) / 100);
		rectangleMark6.setFill(Color.LIGHTSALMON);
		height += 370 * (nValue1 - mValue1) / 100;
		// m2
		rectangleMark7.setX(602);
		rectangleMark7.setY(height);
		rectangleMark7.setWidth(20);
		rectangleMark7.setHeight(370 * mValue1 / 100);
		rectangleMark7.setFill(Color.RED);
		txtM1.setY(height);
	}
	
	/**
	 *  Draw the backgroud
	 *  
	 * @param root
	 */
	public void drawBackGround(AnchorPane root){
		
		int LINE_WIDTH = 5;
		Line line1 = new Line();
		line1.setStartX(0);
		line1.setStartY(200);
		line1.setEndX(200);
		line1.setEndY(200);
		line1.setStrokeWidth(LINE_WIDTH);
		
		Line line2 = new Line();
		line2.setStartX(0);
		line2.setStartY(235);
		line2.setEndX(170);
		line2.setEndY(235);
		line2.setStrokeWidth(LINE_WIDTH);
		
		Line line3 = new Line();
		line3.setStartX(0);
		line3.setStartY(260);
		line3.setEndX(170);
		line3.setEndY(260);
		line3.setStrokeWidth(LINE_WIDTH);
		
		Line line4 = new Line();
		line4.setStartX(0);
		line4.setStartY(295);
		line4.setEndX(170);
		line4.setEndY(295);
		line4.setStrokeWidth(LINE_WIDTH);
		
		Line line5 = new Line();
		line5.setStartX(0);
		line5.setStartY(320);
		line5.setEndX(170);
		line5.setEndY(320);
		line5.setStrokeWidth(LINE_WIDTH);
		
		Line line6 = new Line();
		line6.setStartX(0);
		line6.setStartY(355);
		line6.setEndX(170);
		line6.setEndY(355);
		line6.setStrokeWidth(LINE_WIDTH);
		
		Line line7 = new Line();
		line7.setStartX(0);
		line7.setStartY(380);
		line7.setEndX(170);
		line7.setEndY(380);
		line7.setStrokeWidth(LINE_WIDTH);
		
		Line line8 = new Line();
		line8.setStartX(0);
		line8.setStartY(415);
		line8.setEndX(170);
		line8.setEndY(415);
		line8.setStrokeWidth(LINE_WIDTH);
		
		Line line9 = new Line();
		line9.setStartX(0);
		line9.setStartY(440);
		line9.setEndX(170);
		line9.setEndY(440);
		line9.setStrokeWidth(LINE_WIDTH);
		
		Line line10 = new Line();
		line10.setStartX(0);
		line10.setStartY(475);
		line10.setEndX(600);
		line10.setEndY(475);
		line10.setStrokeWidth(LINE_WIDTH);
		
		Line line11 = new Line();
		line11.setStartX(0);
		line11.setStartY(510);
		line11.setEndX(400);
		line11.setEndY(510);
		line11.setStrokeWidth(LINE_WIDTH);
		
		
		Line line12 = new Line();
		line11.setStartX(170);
		line11.setStartY(235);
		line11.setEndX(170);
		line11.setEndY(260);
		line11.setStrokeWidth(LINE_WIDTH);
		
		Line line13 = new Line();
		line13.setStartX(170);
		line13.setStartY(295);
		line13.setEndX(170);
		line13.setEndY(320);
		line13.setStrokeWidth(LINE_WIDTH);
		
		Line line14 = new Line();
		line14.setStartX(170);
		line14.setStartY(355);
		line14.setEndX(170);
		line14.setEndY(380);
		line14.setStrokeWidth(LINE_WIDTH);
		
		Line line15 = new Line();
		line15.setStartX(170);
		line15.setStartY(415);
		line15.setEndX(170);
		line15.setEndY(440);
		line15.setStrokeWidth(LINE_WIDTH);
		
		Line line16 = new Line();
		line16.setStartX(200);
		line16.setStartY(200);
		line16.setEndX(200);
		line16.setEndY(440);
		line16.setStrokeWidth(LINE_WIDTH);
		
		Line line17 = new Line();
		line17.setStartX(200);
		line17.setStartY(440);
		line17.setEndX(350);
		line17.setEndY(440);
		line17.setStrokeWidth(LINE_WIDTH);
		
		Line line18 = new Line();
		line18.setStartX(350);
		line18.setStartY(100);
		line18.setEndX(350);
		line18.setEndY(440);
		line18.setStrokeWidth(LINE_WIDTH);
		
		Line line19 = new Line();
		line19.setStartX(600);
		line19.setStartY(100);
		line19.setEndX(600);
		line19.setEndY(475);
		line19.setStrokeWidth(LINE_WIDTH);
		
		Line line20 = new Line();
		line20.setStartX(350);
		line20.setStartY(100);
		line20.setEndX(450);
		line20.setEndY(100);
		line20.setStrokeWidth(LINE_WIDTH);
		
		Line line21 = new Line();
		line21.setStartX(450);
		line21.setStartY(100);
		line21.setEndX(450);
		line21.setEndY(40);
		line21.setStrokeWidth(LINE_WIDTH);
		
		Line line22 = new Line();
		line22.setStartX(450);
		line22.setStartY(40);
		line22.setEndX(500);
		line22.setEndY(40);
		line22.setStrokeWidth(LINE_WIDTH);
		
		Line line23 = new Line();
		line23.setStartX(500);
		line23.setStartY(40);
		line23.setEndX(500);
		line23.setEndY(100);
		line23.setStrokeWidth(LINE_WIDTH);
		
		Line line24 = new Line();
		line24.setStartX(500);
		line24.setStartY(100);
		line24.setEndX(600);
		line24.setEndY(100);
		line24.setStrokeWidth(LINE_WIDTH);
		
		Rectangle inputsersor = new Rectangle();
		inputsersor.setX(340);
		inputsersor.setY(442);
		inputsersor.setWidth(5);
		inputsersor.setHeight(30);
		inputsersor.setFill(Color.CADETBLUE);
		
		Text inputlableText = new Text(320,460,"p");
		root.getChildren().add(inputlableText);
		
		Rectangle waterSensor1 = new Rectangle();
		waterSensor1.setX(352);
		waterSensor1.setY(102);
		waterSensor1.setWidth(5);
		waterSensor1.setHeight(341);
		waterSensor1.setFill(Color.CADETBLUE);
		
		Text waterlabelText = new Text(330,250,"q");
		root.getChildren().add(waterlabelText);
	
		Rectangle steamSersor = new Rectangle();
		steamSersor.setX(452);
		steamSersor.setY(70);
		steamSersor.setWidth(46);
		steamSersor.setHeight(5);
		steamSersor.setFill(Color.CADETBLUE);
		
		Text steamlabelText = new Text(475,65,"v");
		root.getChildren().add(steamlabelText);
		
		root.getChildren().add(line1);
		root.getChildren().add(line2);
		root.getChildren().add(line3);
		root.getChildren().add(line4);
		root.getChildren().add(line5);
		root.getChildren().add(line6);
		root.getChildren().add(line7);
		root.getChildren().add(line8);
		root.getChildren().add(line9);
		root.getChildren().add(line10);
		root.getChildren().add(line11);
		root.getChildren().add(line12);
		root.getChildren().add(line13);
		root.getChildren().add(line14);
		root.getChildren().add(line15);
		root.getChildren().add(line16);
		root.getChildren().add(line17);
		root.getChildren().add(line18);
		root.getChildren().add(line19);
		root.getChildren().add(line20);
		root.getChildren().add(line21);
		root.getChildren().add(line22);
		root.getChildren().add(line23);
		root.getChildren().add(line24);
		
		root.getChildren().add(inputsersor);
		root.getChildren().add(waterSensor1);
		root.getChildren().add(steamSersor);
	}
	
	public double getCapacityValue() {
		return capacityValue;
	}


	public void setCapacityValue(double capacity) {
		this.capacityValue = capacity;
	}

	public LineChart getQchart() {
		return qchart;
	}

	public void setQchart(LineChart qchart) {
		this.qchart = qchart;
	}

	public LineChart getVchart() {
		return vchart;
	}

	public void setVchart(LineChart vchart) {
		this.vchart = vchart;
	}

	public LineChart getPchart() {
		return pchart;
	}

	public void setPchart(LineChart pchart) {
		this.pchart = pchart;
	}
	
	
	
	public Label getQlabel() {
		return qlabel;
	}

	public void setQlabel(Label qlabel) {
		this.qlabel = qlabel;
	}

	public Label getVlabel() {
		return vlabel;
	}

	public void setVlabel(Label vlabel) {
		this.vlabel = vlabel;
	}

	public Label getPlabel() {
		return plabel;
	}

	public void setPlabel(Label plabel) {
		this.plabel = plabel;
	}



	public Label getSystemstate() {
		return systemstate;
	}

	public void setSystemstate(Label systemstate) {
		this.systemstate = systemstate;
	}

	public Button getBasicinv() {
		return basicinv;
	}

	public void setBasicinv(Button basicinv) {
		this.basicinv = basicinv;
	}

	public Button getNormalinv() {
		return normalinv;
	}

	public void setNormalinv(Button normalinv) {
		this.normalinv = normalinv;
	}



	private double capacityValue; //the capacity of water C
	
	private double inputWaterCapacity; // the input value
	
	private double steamCapacity; // the steam value

	private boolean pump1;  // pump1
	private boolean pump2;  // pump2 
	private boolean pump3;  // pump3 
	private boolean pump4;  // pump4 
	private boolean pump5;  // pump5 
	
	// the rectangle of pump
	private Rectangle pumpRectangle1;  // pump1
	private Rectangle pumpRectangle2;  // pump2
	private Rectangle pumpRectangle3;  // pump3
	private Rectangle pumpRectangle4;  // pump4
	private Rectangle pumpRectangle5;  // pump5
	
	private Rectangle verticalRectangle;
	private Rectangle horizontalRectangle;
	
	private Rectangle waterRectangle; // Water rectangle
	
	//pump rectangle
	private Rectangle rPump1;
	private Rectangle rPump2;
	private Rectangle rPump3;
	private Rectangle rPump4;
	private Rectangle rPump5;
	// The label of pump
	private Text txtPump1;
	private Text txtPump2;
	private Text txtPump3;
	private Text txtPump4;
	private Text txtPump5;
	
	// The label of mark
	private Text txtM1;
	private Text txtM2;
	private Text txtN1;
	private Text txtN2;
	private Text txtB1;
	private Text txtB2;
	
	// The rectangle of mark
	private Rectangle rectangleMark1;
	private Rectangle rectangleMark2;
	private Rectangle rectangleMark3;
	private Rectangle rectangleMark4;
	private Rectangle rectangleMark5;
	private Rectangle rectangleMark6;
	private Rectangle rectangleMark7;
	

}

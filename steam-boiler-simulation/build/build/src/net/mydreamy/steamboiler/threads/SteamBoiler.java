package net.mydreamy.steamboiler.threads;

import application.SteamBoilderController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;

/**
 * 
 * @author yylonly
 * @organ UM Software Engineering Lab
 */
public class SteamBoiler extends Thread {

	/**
	 * 
	 * @param c
	 * @param m1
	 * @param m2
	 * @param n1
	 * @param n2
	 * @param bEST1
	 * @param bEST2
	 * @param pN
	 * @param w
	 * @param pUMPONE
	 * @param uP
	 * @param dOWN
	 * @param vMINOUT
	 */


	public SteamBoiler(int interval, float initQ, float c, float m1, float m2, float n1, float n2,
			float bEST1, float bEST2, int pN, float w, float pUMPONE, float uP,
			float dOWN, float vMINOUT, Timer timer, ProgressBar qbar, ProgressBar vbar, ProgressBar pbar, ProgressIndicator ptimer, SteamBoilderController sc) {

		this.interval = interval;
	    INITQ = initQ;
		C = c;
		M1 = m1;
		M2 = m2;
		N1 = n1;
		N2 = n2;
		BEST1 = bEST1;
		BEST2 = bEST2;
		PN = pN;
		W = w;
		PUMPONE = pUMPONE;
		UP = uP;
		DOWN = dOWN;
		VMINOUT = vMINOUT;
		this.start = true;
		q = initQ;
		p = pN*pUMPONE;
		v = W;
		this.timer = timer;
		this.qbar = qbar;
		this.vbar = vbar;
		this.pbar = pbar;
		this.ptimer = ptimer;
		this.sc = sc;
		qcharseries = new XYChart.Series<Number, Number>();
		vcharseries = new XYChart.Series<Number, Number>();
		pcharseries = new XYChart.Series<Number, Number>();
		sc.getQchart().getData().add(qcharseries);
		sc.getVchart().getData().add(vcharseries);
		sc.getPchart().getData().add(pcharseries);
	}


	@Override
	public void run() {
		index = 0;
		while (start == true) {
			// boiling every 1 second;
			System.out.println("q: " + q);
			System.out.println("v: " + v);
			System.out.println("p: " + p);
		
			timer.tick();
			
			System.out.println("Steam Boiler Tick");
			double qbarValue = q / C;
			qbar.setProgress(qbarValue);
			vbar.setProgress(v / W);
			pbar.setProgress(p / (PUMPONE * PN) );

			/*
			 * Chart 
			 */

			//change q 
			Platform.runLater(new Runnable() 
			{
                public void run() 
                {     	
                	/*
                	 * GUI Invariant
                	 */
                	if (q > N2 || q < N1)
                	{
                		sc.getNormalinv().setText("FALSE");
                		sc.getNormalinv().setStyle("-fx-base: red;");
                	}
                	
                	if (q > M2 || q < M1)
                	{
                		sc.getBasicinv().setText("FALSE");
                		sc.getBasicinv().setStyle("-fx-base: red;");
                	}
                	
                	/*
                	 * GUI Q V P label
                	 */
        			sc.getQlabel().setText(String.valueOf(q));
        			sc.getVlabel().setText(String.valueOf(v));
        			sc.getPlabel().setText(String.valueOf(p));
        			
        			/*
        			 * q v p GUI chart
        			 */
                	qcharseries.getData().add(new XYChart.Data<Number, Number>(index, q));
             //   	qcharseries.getNode().setStyle(" -fx-stroke-width: 2px;");
                	vcharseries.getData().add(new XYChart.Data<Number, Number>(index, v));
                	pcharseries.getData().add(new XYChart.Data<Number, Number>(index, p));

        			sc.setQ(q);
        			sc.getQchart().getStyleClass().add("custom-chart");
        			sc.getVchart().getStyleClass().add("custom-chart");
        			sc.getPchart().getStyleClass().add("custom-chart");
                }
            });
			index++;
			
			if (q >= M2) {
				q = q + (p - v);
				v = VMINOUT;
				Platform.runLater(new Runnable() 
				{
	                public void run() 
	                {    
	                	sc.emergencyStopFired(null);
	                }
				});
				break;
			}

			if (N2 <= q && q < M2) {
				q = q + (p - v);
				v = VMINOUT;
				continue;
			}

			if (BEST2 < q && q < N2 && (p - v) < 0 && (v + UP) < W) {
				q = q + (p - v);
				v = v + UP;
				continue;
			}
			
			if (BEST2 < q && q < N2 && (p-v) == 0 && v == 0)
			{
				q = q + (p - v);
				v = v + UP;
				continue;
				
			}

			if (BEST2 < q && q < N2 && (p - v) < 0 && (v + UP) >= W) {
				q = q + (p - v);
				continue;
			}

			if (BEST2 < q && q < N2 && (p - v) > 0 && v > VMINOUT) {
				q = q + (p - v);
				v = v - DOWN;
				continue;
			}
			
			if (BEST2 < q && q < N2 && (p - v) > 0 && v <= VMINOUT) {
				q = q + (p - v);
				continue;
			}

			if (BEST1 <= q && q <= BEST2) {
				q = q + (p - v);
				v = W;
				continue;
			}

			if (N1 <= q && q < BEST1 && (p - v) < 0) {
				q = q + (p - v);
				v = v - DOWN;
				continue;
			}

			if (N1 <= q && q < BEST1 && (p - v) >= 0 && (v + UP) < W) {
				q = q + (p - v);
				v = v + UP;
				continue;
			}

			if (N1 <= q && q < BEST1 && (p - v) >= 0 && (v + UP) >= W) {
				q = q + (p - v);
				continue;
			}

			if (M1 <= q && q < N1) {
				q = q + (p - v);
				v = VMINOUT;
				continue;
			}

			if (q < M1) {
				q = q + (p - v);
				v = 0;
				Platform.runLater(new Runnable() 
				{
	                public void run() 
	                {    
	                	sc.emergencyStopFired(null);
	                }
				});
				break;
			}
			
			
		}
	}

	synchronized public void pumpOn() {
		for (int i = 0; i < interval; i++)
		{
			timer.tick();
		}
		p = p + PUMPONE;
	}

	synchronized public void pumpOff() {
		p = p - PUMPONE;
	}

	public float getWaterQuantity() {
		return q;
	}

	public float getSteamRate() {
		return v;
	}

	public float getPumpRate() {
		return p;
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	private float INITQ;
	private float C;
	private float M1;
	private float M2;
	private float N1;
	private float N2;
	private float BEST1;
	private float BEST2;
	private int PN;
	private float W;
	private float PUMPONE;
	private float UP;
	private float DOWN;
	private float VMINOUT;
	private float q;
	private float p;
	private float v;
	private boolean start;
	private Timer timer;
	private ProgressBar qbar;
	private ProgressBar vbar;
	private ProgressBar pbar;
	private ProgressIndicator ptimer;
	private SteamBoilderController sc;
	private XYChart.Series<Number, Number> qcharseries;
	private XYChart.Series<Number, Number> vcharseries;
	private XYChart.Series<Number, Number> pcharseries;
	private int index;
	private int interval;
}

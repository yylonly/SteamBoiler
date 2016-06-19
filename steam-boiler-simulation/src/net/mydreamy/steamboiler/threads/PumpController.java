package net.mydreamy.steamboiler.threads;

import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;
import application.SteamBoilderController;

public class PumpController extends Thread {

	/**
	 * 
	 * @param boiler
	 * @param pumpcontrollerchan
	 */
	public PumpController(SteamBoiler boiler,
			LinkedBlockingQueue<Boolean> pumpcontrollerchan, SteamBoilderController sc) {
		this.pumpcontrollerchan = pumpcontrollerchan;
		this.start = true;
		this.boiler = boiler;
		this.sc = sc;
	}

	@Override
	public void run() {
		/*
		 * PUMPCONTROLER = (start -> PUMPCONTROLERUN), PUMPCONTROLERUN =
		 * (pumpcontrollerchan.receive[o:PUMPORDER] -> (when (o == ON) pumpOn ->
		 * PUMPCONTROLERUN | when (o == OFF) pumpOff -> PUMPCONTROLERUN | when
		 * (o == KEEP) keep -> PUMPCONTROLERUN)).
		 */
		while (start == true) {
			// receive order from pump controller channel
			try 
			{
				boolean o = pumpcontrollerchan.take();
				System.out.println("Pump Channel length " + pumpcontrollerchan.size());
				System.out.println("Pump Controller do the order: " + o);
				// do action
				if (o == true) {
					/*
					 * Change GUI
					 */
					Platform.runLater(new Runnable() {

                        public void run() {
                        	sc.pumpOn();
                        }
                    });
					
					boiler.pumpOn();

					
				} else {
					boiler.pumpOff();
					/*
					 * Change GUI
					 */
					Platform.runLater(new Runnable() {

                        public void run() {
                        	sc.pumpDown();
                        }
                    });
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	private SteamBoiler boiler;
	private boolean start;
	private LinkedBlockingQueue<Boolean> pumpcontrollerchan;
	private SteamBoilderController sc;
}

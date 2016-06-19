package net.mydreamy.steamboiler.threads;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;

/**
 * 
 * @author yylonly
 * @organ UM Software Engineering Lab
 */

public class WaterSensor extends Thread {

	/**
	 * 
	 * @param boiler
	 * @param waterchan
	 */
	public WaterSensor(SteamBoiler boiler,
			LinkedBlockingQueue<Float> waterchan, Timer timer, int interval, ProgressIndicator ptimer) {
		this.start = true;
		this.rescue = false;
		this.boiler = boiler;
		this.waterchan = waterchan;
		this.timer = timer;
		this.interval = interval;
		this.ptimer = ptimer;
	}

	@Override
	public void run() {

		/**
		 * FSP **
		 *
		 * WATERSENSOR = (start -> WATERSENSORRUN), WATERSENSORRUN = ( tick ->
		 * tick -> tick -> tick -> tick -> getWaterQuantity[q:0..ErrorQ] ->
		 * waterchan.send[q] -> WATERSENSORRUN).
		 */

		while (start == true) {
			
			try
			{
				// wait 5 seconds
				for (int i = 0; i < interval; i++)
				{
					timer.tick();
					System.out.println("Water Boiler Tick");
					double tp = ((double) i) / interval;
					System.out.println("tp: " + tp);
	//				ptimer.setProgress(0.1);
					Platform.runLater(new Runnable() {

                         public void run() {
                        	 ptimer.setProgress(tp);
                         }
                     });
				}
				
				// get water quantity
				float q = boiler.getWaterQuantity();
	
				// send to waterchan

				waterchan.put(q);

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

	public boolean isRescue() {
		return rescue;
	}

	public void setRescue(boolean rescue) {
		this.rescue = rescue;
	}

	private boolean start;
	private boolean rescue;
	private SteamBoiler boiler;
	private LinkedBlockingQueue<Float> waterchan;
	private Timer timer;
	private int interval;
	private ProgressIndicator ptimer;
}

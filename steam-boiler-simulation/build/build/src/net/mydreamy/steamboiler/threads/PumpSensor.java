package net.mydreamy.steamboiler.threads;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 
 * @author yylonly
 * @organ UM Software Engineering Lab
 */

public class PumpSensor extends Thread {

	/**
	 * 
	 * @param boiler
	 * @param pumpsensorchan
	 */
	public PumpSensor(SteamBoiler boiler,
			LinkedBlockingQueue<Float> pumpsensorchan, Timer timer, int interval) {
		this.boiler = boiler;
		this.start = true;
		this.pumpsensorchan = pumpsensorchan;
		this.timer = timer;
		this.interval = interval;
	}

	@Override
	public void run() {
		/**
		 * PUMPSENSOR = (start -> PUMPSENSORRUN), PUMPSENSORRUN = ( tick -> tick
		 * -> tick -> tick -> tick -> getPumpRate[p:P] -> pumpsensorchan.send[p]
		 * -> PUMPSENSORRUN).
		 */
		while (start == true) {
			
			try 
			{
				// wait 5 seconds
				for (int i = 0; i < interval; i++)
				{
					timer.tick();
					System.out.println("Pump Sensor Tick");
				}
				// getSteamRate
				float p = boiler.getPumpRate();
	
				// send to CS
				pumpsensorchan.put(p);
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

	private boolean start;
	private SteamBoiler boiler;
	private LinkedBlockingQueue<Float> pumpsensorchan;
	private Timer timer;
	private int interval;
}

package net.mydreamy.steamboiler.threads;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 
 * @author yylonly
 * @organ UM Software Engineering Lab
 */
public class SteamSensor extends Thread {

	/**
	 * 
	 * @param boiler
	 * @param steamchan
	 */
	public SteamSensor(SteamBoiler boiler,
			LinkedBlockingQueue<Float> steamchan, Timer timer, int interval) {
		this.start = true;
		this.boiler = boiler;
		this.steamchan = steamchan;
		this.timer = timer;
		this.interval = interval;
	}

	@Override
	public void run() {
		/**
		 * STEAMSENSOR = (start -> STEAMSENSORRUN), STEAMSENSORRUN = ( tick ->
		 * tick -> tick -> tick -> tick -> getSteamRate[v:V] ->
		 * steamchan.send[v] -> STEAMSENSORRUN).
		 */
		while (start == true) {
			
			try 
			{
				// wait 5 seconds
				for (int i = 0; i < interval; i++)
				{
					timer.tick();
					System.out.println("Steam Sensor Tick");
				}
	
				// getSteamRate
				float v = boiler.getSteamRate();
	
				// send to CS
				steamchan.put(v);
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
	private LinkedBlockingQueue<Float> steamchan;
	private Timer timer;
	private int interval;
}

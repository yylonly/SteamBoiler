package net.mydreamy.steamboiler.threads;

/**
 * 
 * @author yylonly
 * @organ UM Software Engineering Lab
 */

public class Timer extends Thread {

	/**
	 * 
	 * @param interval
	 */
	public Timer() {
		this.start = true;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		while (start == true) {
			
			tick();
		}
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	public void tick()
	{
		try {
			// Tick time
			Thread.sleep(1000);
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean start;
}

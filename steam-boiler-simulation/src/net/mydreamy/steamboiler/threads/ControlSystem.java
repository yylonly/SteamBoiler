package net.mydreamy.steamboiler.threads;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;
import application.SteamBoilderController;

/**
 * 
 * @author yylonly
 * @organ UM Software Engineering Lab
 */

public class ControlSystem extends Thread {

	/**
	 * 
	 * @param waterchan
	 * @param steamchan
	 * @param pumpsensorchan
	 * @param pumpcontrollerchan
	 * @param BEST1
	 * @param BEST2
	 * @param FTR
	 * @param PUMPQ
	 * @param C
	 */
	public ControlSystem(LinkedBlockingQueue<Float> waterchan,
			LinkedBlockingQueue<Float> steamchan,
			LinkedBlockingQueue<Float> pumpsensorchan,
			LinkedBlockingQueue<Boolean> pumpcontrollerchan, float BEST1,
			float BEST2, float FTRu, float FTRd, float PQ, int PN, float C, float N1, float N2,  float M1, float M2, SteamBoilderController sc, int interval) {
		this.waterchan = waterchan;
		this.steamchan = steamchan;
		this.pumpsensorchan = pumpsensorchan;
		this.pumpcontrollerchan = pumpcontrollerchan;
		this.start = true;
		this.rescue = false;
		this.BEST1 = BEST1;
		this.BEST2 = BEST2;
		this.FTRU = FTRu;
		this.FTRD = FTRd;
		this.PUMPQ = PQ*PN;
		this.PQ = PQ;
		this.PN = PN;
		this.C = C;
		this.N1 = N1;
		this.N2 = N2;
		this.po = ORDER.OFF;
		this.sc = sc;
		this.M1 = M1;
		this.M2 = M2;
		this.interval = interval;
		this.lastq = 50;
	}

	@Override
	public void run() {

		while (start == true) {
			
			try
			{
				// get q v p
				System.out.println("CS take q v p");
				float q = waterchan.take();
				float v = steamchan.take();
				float p = pumpsensorchan.take();
	
				if (rescue == false)
				{
					
					// ------------------ q < b1 ---------------
					System.out.println("Normal Mode");
					System.out.println("real q is " + q);
					System.out.println("last q is " + lastq);
					// --- 4 pumps ----
					if (q < BEST1+FTRD && p == PUMPQ-PQ && po == ORDER.KEEP)
					{
						pumpcontrollerchan.put(true); 
						lastq = q;
						po = ORDER.ON;
						continue;
					}
						
					if (q < BEST1+FTRD && p == PUMPQ-PQ && po == ORDER.OFF)
					{
							pumpcontrollerchan.put(true); 
							lastq = q;
							po = ORDER.ON;
							continue;
					}
					
					// --- less 4 pumps ----
					if (q < BEST1+FTRD && p <= PUMPQ-PQ && po == ORDER.ON)
					{
							po = ORDER.KEEP;
							lastq = q;
							continue;
					}
					
					if (q < BEST1 + FTRD && p <= (PUMPQ - 2*PQ))
					{
							pumpcontrollerchan.put(true);
							lastq = q;
							po = ORDER.ON;
							continue;
					}
					
					// --------------- q > b2 --------------------
					if (q > BEST2 - FTRU && (p-v) >= 0 && p > 0)
					{
						pumpcontrollerchan.put(false); 
						lastq = q;
						po = ORDER.OFF;
						continue;
					}
					
					if (q > BEST2 - FTRU && v >= 0 && p == 0)
					{
						System.out.print("CS nodecisionresult ");
						po = ORDER.KEEP;
						lastq = q;
						continue;
					}
	
					// ------------------- b1 < q < b2 ----------------
					if (BEST1 + FTRD <= q && q <= BEST2 - FTRU)
					{
						System.out.print("CS nodecisionresult ");
						lastq = q;
						po = ORDER.KEEP;
						continue;
					}	

				}
				else
				{
					// -------------------------- rescue mode -------------------------
					System.out.println("Rescue Mode");
					System.out.println("real q is " + q);
					System.out.println("last q is " + lastq);
					float change = (p-v)*(interval-1);
					lastq = lastq + change;
					
					Platform.runLater(new Runnable() 
					{
		                public void run() 
		                {    
		                	sc.getSystemstate().setText("Rescue Mode, recode q is " + lastq + "evalue change is " + change);
		                }
					});
					
					
					
					// out M1 M2 stop
					if (lastq > M2 || lastq < M1)
					{
						System.out.print("System Stop");
						Platform.runLater(new Runnable() 
						{
			                public void run() 
			                {    
			                	sc.emergencyStopFired(null);
			                }
						});
						break;
					}
									
			        // LASTQ > B2	
			        if (lastq >= BEST2-FTRU && p-v >= PQ*2 && p > 0)
			        {
			        	pumpcontrollerchan.put(false); 
			        	pumpcontrollerchan.put(false);
			        	lastq = lastq+change;
						po = ORDER.OFF;
						continue;
			        }
					
			        if (lastq >= BEST2-FTRU && (p-v) >= 0 && p > 0)
			        {
			        	pumpcontrollerchan.put(false); 
			        	lastq = lastq+change;
						po = ORDER.OFF;
						continue;
			        }
			        
					
					
					// B1 < LASTQ < B2		
			        if (BEST1+FTRD <= lastq && lastq < BEST2-FTRU && (p-v) >= 0 && p > 0) 
			        {
			        	pumpcontrollerchan.put(false); 
			        	lastq = lastq+change;
						po = ORDER.OFF;
						continue;
			        }
			        
			        if (BEST1+FTRD <= lastq && lastq < BEST2-FTRU) 
			        {
			        	 lastq = lastq+change;
			        	 po = ORDER.KEEP;
				         continue;
			        }
			        
		
			        // LAST < B1
					if (lastq < BEST1+FTRD && p == PUMPQ-PQ && po == ORDER.KEEP)
					{
						pumpcontrollerchan.put(true); 
						lastq = lastq+change;
						po = ORDER.ON;
						continue;
					}
						
					if (lastq < BEST1+FTRD && p == PUMPQ-PQ && po == ORDER.OFF)
					{
							pumpcontrollerchan.put(true); 
							lastq = lastq+change;
							po = ORDER.ON;
							continue;
					}
					
					// --- less 4 pumps ----
					if (lastq < BEST1+FTRD && p <= PUMPQ-PQ && po == ORDER.ON)
					{
							po = ORDER.KEEP;
							lastq = lastq+change;
							continue;
					}
					
					if (lastq < BEST1 + FTRD && p <= (PUMPQ - 2*PQ))
					{
							pumpcontrollerchan.put(true);
							lastq = lastq + change;
							po = ORDER.ON;
							continue;
					}
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

	
	public boolean isRescue() {
		return rescue;
	}

	public void setRescue(boolean rescue) {
		this.rescue = rescue;
	}


	private LinkedBlockingQueue<Float> waterchan;
	private LinkedBlockingQueue<Float> steamchan;
	private LinkedBlockingQueue<Float> pumpsensorchan;
	private LinkedBlockingQueue<Boolean> pumpcontrollerchan;
	private boolean start;
	private boolean rescue;
	private float BEST1;
	private float BEST2;
	private float FTRU;
	private float FTRD;
	private float PUMPQ;
	private float C;
	private float lastq;
	private float N1;
	private float N2;
	private float M1;
	private float M2;
	private float PQ;
	private int PN;
	private ORDER po;
	private SteamBoilderController sc;
	private int interval;
}

enum ORDER { ON, OFF, KEEP};
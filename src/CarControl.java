//Prototype implementation of Car Control
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2016

//Hans Henrik Lovengreen    Oct 3, 2016

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

class Gate {

	Semaphore g = new Semaphore(0);
	Semaphore e = new Semaphore(1);
	boolean isopen = false;

	public void pass() throws InterruptedException {
		g.P();
		g.V();
	}

	public void open() {
		try {
			e.P();
		} catch (InterruptedException e) {
		}
		if (!isopen) {
			g.V();
			isopen = true;
		}
		e.V();
	}

	public void close() {
		try {
			e.P();
		} catch (InterruptedException e) {
		}
		if (isopen) {
			try {
				g.P();
			} catch (InterruptedException e) {
			}
			isopen = false;
		}
		e.V();
	}

}

class Car extends Thread {

	int basespeed = 100; // Rather: degree of slowness
	int variation = 50; // Percentage of base speed

	CarDisplayI cd; // GUI part

	int no; // Car number
	Pos startpos; // Startpositon (provided by GUI)
	Pos barpos; // Barrierpositon (provided by GUI)
	Color col; // Car color
	Gate mygate; // Gate at startposition

	int speed; // Current car speed
	Pos curpos; // Current position
	Pos newpos; // New position to go to

	Semaphore[][] mutexPos; // Semaphores for creating mutual exclusion for each position
	Alley alley;  //A monitor for ensuring mutual exclusion in the alley
	Barrier barrier; //A monitor for creating barrier synchronizing

	public Car(int no, CarDisplayI cd, Gate g, Semaphore[][] mutexPos, Alley alley, Barrier barrier) {
		this.alley = alley;
		this.barrier = barrier;
		this.no = no;
		this.cd = cd;
		mygate = g;
		startpos = cd.getStartPos(no);
		barpos = cd.getBarrierPos(no); // For later use

		this.mutexPos = mutexPos;
		col = chooseColor(no);

		// do not change the special settings for car no. 0
		if (no == 0) {
			basespeed = 0;
			variation = 0;
			setPriority(Thread.MAX_PRIORITY);
		}

	}

	public synchronized void setSpeed(int speed) {
		if (no != 0 && speed >= 0) {
			basespeed = speed;
		} else
			cd.println("Illegal speed settings");
	}

	public synchronized void setVariation(int var) {
		if (no != 0 && 0 <= var && var <= 100) {
			variation = var;
		} else
			cd.println("Illegal variation settings");
	}

	synchronized int chooseSpeed() {
		double factor = (1.0D + (Math.random() - 0.5D) * 2 * variation / 100);
		return (int) Math.round(factor * basespeed);
	}

	private int speed() {
		// Slow down if requested
		final int slowfactor = 3;
		return speed * (cd.isSlow(curpos) ? slowfactor : 1);
	}

	Color chooseColor(int no) {
		return Color.blue; // You can get any color, as longs as it's blue
	}

	Pos nextPos(Pos pos) {
		// Get my track from display
		return cd.nextPos(no, pos);
	}

	boolean atGate(Pos pos) {
		return pos.equals(startpos);
	}

	public void run() {
		try {

			speed = chooseSpeed();
			curpos = startpos;
			cd.mark(curpos, col, no);

			while (true) {
				sleep(speed());

				if (atGate(curpos)) {
					mygate.pass();
					speed = chooseSpeed();
				}

				newpos = nextPos(curpos);

				// If the car is about to enter the critical section
				if (no < 5 && no != 0 && (curpos.row == 2 && curpos.col == 1 || curpos.row == 1 && curpos.col == 3)) {
					// CCW
					alley.enter(no);
				} else if (no > 4 && curpos.row == 10 && curpos.col == 0) {
					// CW
					alley.enter(no);
				}
				//If car is at the postion right before the bar
				if (curpos.equals(barpos)) {
					barrier.sync();
				}
				
				//Claim the next position by calling P() on its semaphore. This is done after the checking for 
				//alley entry otherwise you could be waiting at the alley while having claimed the next position.
				mutexPos[newpos.row][newpos.col].P();
				
				// Move to new position
				cd.clear(curpos);
				cd.mark(curpos, newpos, col, no);
				sleep(speed());

				
				cd.clear(curpos, newpos);
				cd.mark(newpos, col, no);
				
				//Right when moving, release the semaphore of the previous position.
				mutexPos[curpos.row][curpos.col].V();
				curpos = newpos;

				// If the car has left the critical section
				if (no < 5 && no != 0 && curpos.row == 9 && curpos.col == 1) {
					alley.leave(no);
				} else if (no > 4 && curpos.row == 0 && curpos.col == 2) {
					alley.leave(no);
				}

			}

		} catch(InterruptedException e) {
			
			if (curpos.row <= 9 && curpos.col == 0 || curpos.row == 1 && curpos.col <= 2) {
				alley.leave(no);
			}
			//Clear tile
			if (!newpos.equals(curpos) && newpos != null) {
				cd.clear(newpos, curpos);
			}
			else {
				cd.clear(curpos);
			}
		} catch (Exception e) {
			cd.println("Exception in Car no. " + no);
			System.err.println("Exception in Car no. " + no + ":" + e);
			e.printStackTrace();
		}
	}
}

class Alley {

	int cwCounter, ccwCounter = 0;

	public synchronized void enter(int no) throws InterruptedException {
		if (no < 5 && no != 0) {
			while (cwCounter != 0) {
				wait();
			}
			ccwCounter++;
		} else if (no > 4) {
			while (ccwCounter != 0) {
				wait();
			}
			cwCounter++;
		}
	}

	public synchronized void leave(int no) {
		if (no < 5 && no != 0) {
			ccwCounter--;
			if (ccwCounter == 0) {
				notifyAll();
			}
		} else if (no > 4) {
			cwCounter--;
			if (cwCounter == 0) {
				notifyAll();
			}
		}
	}

}

class Barrier {
	private boolean isBarrierOn = false;
	private int carAmount;
	private int carsWaiting = 0;

	public Barrier(int carAmount) {
		this.carAmount = carAmount;
		
	}
	private int round = 0;
	
	// Wait for others to arrive (if barrier active)
	public synchronized void sync() throws InterruptedException { 
		if (isBarrierOn) {
			carsWaiting++;
			int myRound = round;//Round is used to prevent spurious wake-ups
			while(myRound == round && carsWaiting < carAmount) {
				wait();
			}
			//Wake up and increment round
			if (carsWaiting == carAmount) {
				carsWaiting = 0;
				round++;
				notifyAll();
			}
		}
	}
	
	public String toString() {
		return "carsWaiting: "+ carsWaiting + " carAmount: "+ carAmount;
	}

	public synchronized void on() { // Activate barrier
		if (!isBarrierOn) {
			carsWaiting = 0;
			isBarrierOn = true;
		}
	}

	public synchronized void off() { // Deactivate barrier
		if (isBarrierOn) {
			round++;
			notifyAll();
			isBarrierOn = false;
		}
	}

}

public class CarControl implements CarControlI {

	CarDisplayI cd; // Reference to GUI
	Car[] car; // Cars
	Gate[] gate; // Gates
	static volatile Alley alley;
	static volatile Barrier barrier;
	static volatile Semaphore[][] mutexPos;

	public CarControl(CarDisplayI cd) {
		this.cd = cd;

		car = new Car[9];
		gate = new Gate[9];
		alley = new Alley();
		barrier = new Barrier(9);

		mutexPos = new Semaphore[11][12];
		
		for(int row = 0; row < 11; row++){
			for(int col = 0; col < 12; col++){
				mutexPos[row][col] = new Semaphore(1);
			}
		}
		
		for (int no = 0; no < 9; no++) {
			gate[no] = new Gate();
			car[no] = new Car(no, cd, gate[no], mutexPos, alley, barrier);
			car[no].start();
		}
	}

	public void startCar(int no) {
		gate[no].open();
	}

	public void stopCar(int no) {
		gate[no].close();
	}

	public void barrierOn() {
		barrier.on();
	}

	public void barrierOff() {
		barrier.off();
	}

	public void barrierSet(int k) {
		cd.println("Barrier threshold setting not implemented in this version");
		// This sleep is for illustrating how blocking affects the GUI
		// Remove when feature is properly implemented.
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
	}

	public void removeCar(int no) {
		car[no].interrupt();
	}

	public void restoreCar(int no) {
		if (!car[no].isAlive()) {//Only restart if it is not running
			car[no] = new Car(no, cd, gate[no], mutexPos, alley, barrier);
			car[no].start();
		}
	}

	/* Speed settings for testing purposes */

	public void setSpeed(int no, int speed) {
		car[no].setSpeed(speed);
	}

	public void setVariation(int no, int var) {
		car[no].setVariation(var);
	}

}
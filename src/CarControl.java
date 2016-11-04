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

	Pos[][] positions; // Keeps track of positions of other cars.
	Semaphore mutexPositions; // Semaphore for mutual exclusion when driving.
	Alley alley; // Contains semaphores and other information for the alley.
	Barrier barrier;
	Semaphore[] bumpingSems;//Used to prevent cars to bump into each other
	boolean carsWaitedFor[]; //Is another car waiting for this car to pass
	
	public Car(int no, CarDisplayI cd, Gate g, Pos[][] positions, Semaphore mutexDrive, Alley alley, Barrier barrier, Semaphore[] bumpingSems, boolean[] carsWaitedFor) {
		this.alley = alley;
		this.barrier = barrier;
		this.no = no;
		this.cd = cd;
		mygate = g;
		startpos = cd.getStartPos(no);
		barpos = cd.getBarrierPos(no); // For later use
		this.positions = positions;
		
		this.bumpingSems = bumpingSems;
		this.carsWaitedFor = carsWaitedFor;
		col = chooseColor(no);

		// do not change the special settings for car no. 0
		if (no == 0) {
			basespeed = 0;
			variation = 0;
			setPriority(Thread.MAX_PRIORITY);
		}
		positions[no][0] = startpos;
		positions[no][1] = startpos;
		this.mutexPositions = mutexDrive;

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

				System.out.println("\nSemaphores: ");
				for (int i =0 ; i< 9; i++) {
					System.out.print(i +": "+ bumpingSems[i] + " ");
				}
				System.out.println("\nSomeone waiting: ");
				for (int i =0 ; i< 9; i++) {
					System.out.print(i +": "+ carsWaitedFor[i]+ " ");
				}
				
				// Check if there is a car at nextpos
				this.avoidBumping();
				// If the car is about to enter the critical section

				if (no < 5 && no != 0 && (curpos.row == 2 && curpos.col == 1 || curpos.row == 1 && curpos.col == 3)) {
					// CCW
					// Remove newpos from the occupied list
					mutexPositions.P();
					positions[no][1] = curpos;
					mutexPositions.V();

					alley.enter(no);

				} else if (no > 4 && curpos.row == 10 && curpos.col == 0) {
					// CW
					// Remove newpos from the occupied list
					mutexPositions.P();
					positions[no][1] = curpos;
					mutexPositions.V();

					alley.enter(no);
					
				}
				// insert newpos in the occupied list
				mutexPositions.P();
				positions[no][1] = newpos;
				mutexPositions.V();
				
				if (curpos.col > 2 && (no <= 4 && curpos.row == 6 || no >= 5 && curpos.row == 5)) {
					// Remove newpos from the occupied list
					mutexPositions.P();
					positions[no][1] = curpos;
					mutexPositions.V();
					barrier.sync();
					mutexPositions.P();
					positions[no][1] = newpos;
					mutexPositions.V();
				}
				boolean someoneWaiting = carsWaitedFor[no];
				
				// Move to new position
				cd.clear(curpos);
				cd.mark(curpos, newpos, col, no);
				sleep(speed());

				// Remove curpos from occupied list. This ensures that there is
				// no gap between the cars
				mutexPositions.P();
				positions[no][0] = newpos;
				mutexPositions.V();

				cd.clear(curpos, newpos);
				cd.mark(newpos, col, no);

				curpos = newpos;

				if (someoneWaiting) {
					bumpingSems[no].V();
					carsWaitedFor[no] =false;
				}
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
			System.out.println("car " + no + " curpos: "+ curpos + " newpos: "+ newpos); 
			
			if (no < 5 && curpos.row == 6 || no > 4 && curpos.row == 5) {
				barrier.decrementCarsWaiting();
			}
			barrier.editCarAmount(-1);
			
			try {
			//Clear tile
			mutexPositions.P();
			Pos pos1 = positions[no][0];
			Pos pos2 = positions[no][1];
			mutexPositions.V();
			if (!pos1.equals(pos2) && newpos != null) {
				System.out.println("clearing pos1 :" + pos1 + " and pos2: "+ pos2);
				cd.clear(pos1, pos2);
			}
			else {
				System.out.println("clearing pos1 :" + pos1);
				cd.clear(pos1);
			}
			
			//Update positions array
				mutexPositions.P();
			} catch (InterruptedException e1) {
				//TODO something good
				e1.printStackTrace();
			}
			if (no == 0) {
				positions[no][0] = new Pos(5,2);
				positions[no][2] = new Pos(5,2);
			} else if (no < 5) {
				positions[no][0] = new Pos(7,3 + no);
				positions[no][1] = new Pos(7,3 + no);
			}
			else if (no > 4){
				positions[no][0] = new Pos(4,7 + no);
				positions[no][1] = new Pos(4,7 + no);
			}
			mutexPositions.V();
		} catch (Exception e) {
			cd.println("Exception in Car no. " + no);
			System.err.println("Exception in Car no. " + no + ":" + e);
			e.printStackTrace();
		}
	}

	private void avoidBumping() throws InterruptedException {
		mutexPositions.P();
		boolean occupied = false;
		int carInTheWay = 0;
		for (int i = 0; i < 9; i++) {
			if (positions[i][0].equals(newpos) || positions[i][1].equals(newpos)) {
				occupied = true;
				carInTheWay = i;
				break;
			}
		}
		mutexPositions.V();
		// If tile is occupied wait
		if (occupied) {
			carsWaitedFor[carInTheWay] = true;
			bumpingSems[carInTheWay].P();
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
			int myRound = round;
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
	public synchronized void editCarAmount(int change) {
		this.carAmount += change;
		notifyAll();
	}
	
	public synchronized void decrementCarsWaiting() {
		this.carsWaiting --;
	}
	
	public String toString() {
		return "carsWaiting: "+ carsWaiting + " carAmount: "+ carAmount;
	}

	public synchronized void on() { // Activate barrier
		carsWaiting = 0;
		isBarrierOn = true;
	}

	public synchronized void off() { // Deactivate barrier
		round++;
		notifyAll();
		isBarrierOn = false;
	}

}

public class CarControl implements CarControlI {

	CarDisplayI cd; // Reference to GUI
	Car[] car; // Cars
	Gate[] gate; // Gates
	static volatile Alley alley;
	static volatile Barrier barrier;
	static volatile Semaphore mutexDrive;
	static volatile Pos[][] positions;
	static volatile Semaphore[] bumpingSems;
	static volatile boolean[] carsWaitedFor;

	public CarControl(CarDisplayI cd) {
		this.cd = cd;

		car = new Car[9];
		gate = new Gate[9];
		alley = new Alley();
		barrier = new Barrier(9);

		positions = new Pos[9][2];
		mutexDrive = new Semaphore(1);
		bumpingSems = new Semaphore[9];
		carsWaitedFor = new boolean[9];

		for (int no = 0; no < 9; no++) {
			bumpingSems[no] = new Semaphore(0);
			carsWaitedFor[no] = false;
			gate[no] = new Gate();
			car[no] = new Car(no, cd, gate[no], positions, mutexDrive, alley, barrier, bumpingSems, carsWaitedFor);
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
			car[no] = new Car(no, cd, gate[no], positions, mutexDrive, alley, barrier, bumpingSems, carsWaitedFor);
			car[no].start();
			barrier.editCarAmount(1);
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
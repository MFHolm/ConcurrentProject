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
        try { e.P(); } catch (InterruptedException e) {}
        if (!isopen) { g.V();  isopen = true; }
        e.V();
    }

    public void close() {
        try { e.P(); } catch (InterruptedException e) {}
        if (isopen) { 
            try { g.P(); } catch (InterruptedException e) {}
            isopen = false;
        }
        e.V();
    }

}

class Car extends Thread {

    int basespeed = 100;             // Rather: degree of slowness
    int variation =  50;             // Percentage of base speed

    CarDisplayI cd;                  // GUI part

    int no;                          // Car number
    Pos startpos;                    // Startpositon (provided by GUI)
    Pos barpos;                      // Barrierpositon (provided by GUI)
    Color col;                       // Car  color
    Gate mygate;                     // Gate at startposition


    int speed;                       // Current car speed
    Pos curpos;                      // Current position 
    Pos newpos;                      // New position to go to

    Pos[][] positions;
    Semaphore mutexDrive;
    
    public Car(int no, CarDisplayI cd, Gate g, Pos[][] positions, Semaphore mutexDrive) {

        this.no = no;
        this.cd = cd;
        mygate = g;
        startpos = cd.getStartPos(no);
        barpos = cd.getBarrierPos(no);  // For later use
        this.positions = positions;

        col = chooseColor();

        // do not change the special settings for car no. 0
        if (no==0) {
            basespeed = 0;  
            variation = 0; 
            setPriority(Thread.MAX_PRIORITY); 
        }
        positions[no][0] = startpos;
        positions[no][1] = startpos;
        this.mutexDrive = mutexDrive;
       
    }

    public synchronized void setSpeed(int speed) { 
        if (no != 0 && speed >= 0) {
            basespeed = speed;
        }
        else
            cd.println("Illegal speed settings");
    }

    public synchronized void setVariation(int var) { 
        if (no != 0 && 0 <= var && var <= 100) {
            variation = var;
        }
        else
            cd.println("Illegal variation settings");
    }

    synchronized int chooseSpeed() { 
        double factor = (1.0D+(Math.random()-0.5D)*2*variation/100);
        return (int) Math.round(factor*basespeed);
    }

    private int speed() {
        // Slow down if requested
        final int slowfactor = 3;  
        return speed * (cd.isSlow(curpos)? slowfactor : 1);
    }

    Color chooseColor() { 
        return Color.blue; // You can get any color, as longs as it's blue 
    }

    Pos nextPos(Pos pos) {
        // Get my track from display
        return cd.nextPos(no,pos);
    }

    boolean atGate(Pos pos) {
        return pos.equals(startpos);
    }

   public void run() {
        try {

            speed = chooseSpeed();
            curpos = startpos;
            cd.mark(curpos,col,no);

            while (true) { 
                sleep(speed());
  
                if (atGate(curpos)) { 
                    mygate.pass(); 
                    speed = chooseSpeed();
                }
                	
                newpos = nextPos(curpos);
                
                //If the car is about to enter the critical section
                if (no < 5 && no !=0 && (curpos.row == 2 || curpos.row == 1) && curpos.col == 3) {
                
                }
                else if (no >4 && curpos.row == 10 && curpos.col == 3) {
                	
                }
                
                //Start of critical section
                mutexDrive.P();
                
                //Check if there is a car at nextpos
                boolean occupied = false;
                for (int i = 0; i < 9; i++) {
                	if (positions[i][0].equals(newpos) || positions[i][1].equals(newpos)){
                		occupied = true;
                		break;
                	}
                }
                if (!occupied) {
	                //Update positions array
	                positions[no][0] = curpos;
	                positions[no][1] = newpos;
                }
                else {
                	//If tile is occupied just continue in the while loop
                	mutexDrive.V();
                	continue;
                }
                //End of critical section
                mutexDrive.V();
                
                //  Move to new position 
	            cd.clear(curpos);
	            cd.mark(curpos,newpos,col,no);
	            sleep(speed());
	            cd.clear(curpos,newpos);
	            cd.mark(newpos,col,no);
	
	            curpos = newpos;
	               
              //If the car has left the critical section
                if (no < 5 && no !=0 && curpos.row == 9 && curpos.col == 1) {
                	
                }
                else if (no > 4 && curpos.row == 0 && curpos.col == 2) {
                	
                }
                
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
	

	public void enter(int no) {
		if (no < 5 && no != 0) {
			ccwCounter++;
		}
		else if (no > 4) {
			cwCounter++;
		}
	}

	 public void leave(int no){
		 if (no < 5 && no != 0) {
				ccwCounter--;
			}
			else if (no > 4) {
				cwCounter--;
			}
	 }
	 
}
public class CarControl implements CarControlI{

    CarDisplayI cd;           // Reference to GUI
    Car[]  car;               // Cars
    Gate[] gate;              // Gates
    static volatile Semaphore carEntry;
    static volatile Semaphore mutexCW;
    static volatile Semaphore mutexCCW;
    static volatile Semaphore mutexDrive;
    Pos[][] positions;
    
    public CarControl(CarDisplayI cd) {
        this.cd = cd;
        car  = new  Car[9];
        gate = new Gate[9];
        
        carEntry = new Semaphore(1);
        mutexCW = new Semaphore(1);
        mutexCCW = new Semaphore(1);
        mutexDrive = new Semaphore(1);
        positions = new Pos[9][2];
        

        for (int no = 0; no < 9; no++) {
            gate[no] = new Gate();
            car[no] = new Car(no,cd,gate[no], positions, mutexDrive);
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
        cd.println("Barrier On not implemented in this version");
    }

    public void barrierOff() { 
        cd.println("Barrier Off not implemented in this version");
    }

    public void barrierSet(int k) { 
        cd.println("Barrier threshold setting not implemented in this version");
         // This sleep is for illustrating how blocking affects the GUI
        // Remove when feature is properly implemented.
        try { Thread.sleep(3000); } catch (InterruptedException e) { }
     }

    public void removeCar(int no) { 
        cd.println("Remove Car not implemented in this version");
    }

    public void restoreCar(int no) { 
        cd.println("Restore Car not implemented in this version");
    }

    /* Speed settings for testing purposes */

    public void setSpeed(int no, int speed) { 
        car[no].setSpeed(speed);
    }

    public void setVariation(int no, int var) { 
        car[no].setVariation(var);
    }

}







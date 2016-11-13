import java.util.Calendar;

//Prototype implementation of Car Test class
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2016

//Hans Henrik Lovengreen    Oct 3, 2016

public class CarTest extends Thread {

    CarTestingI cars;
    int testno;

    public CarTest(CarTestingI ct, int no) {
        cars = ct;
        testno = no;
    }

    public void run() {
        try {
            switch (testno) { 
            case 0:
            	// Start all cars. They are automatically given 
            	// random speeds. Bumping should not occur.
            	// Also checks if the alley works as intended when 
            	// the cars run with regular speed.
            	cars.startAll();

                break;
            
            case 1:
            	//Run cars in one direction.
            	for (int i = 1; i < 5; i++) {
            		cars.startCar(i);
				}
            	sleep(10000);
                cars.stopAll();
                break;
                
            case 2:
            	//Same test as case 0, except with high speeds.

            	cars.startAll();
				for (int i = 1; i < 9; i++) {
					cars.setSpeed(i, 1);
				}
            	
            	break;
            	
            case 3:
            	//Testing barrier with regular speeds.
            	cars.barrierOn();
            	cars.startAll();
            	break;
            	
            	
            case 4:
            	//Testing barrier with extreme speeds.
            	cars.barrierOn();
            	cars.startAll();
            	for (int i = 1; i < 9; i++) {
					cars.setSpeed(i, 1);
				}
            	break;
            
            case 5:
            	// Tests if turning the barrier on and off
            	// with cars waiting at the barrier causes problems.
            	cars.barrierOn();
            	cars.startAll();
            	cars.barrierOff();
            	cars.barrierOn();
            	sleep(5000);
            	cars.barrierOff();
            	cars.barrierOn();
            	break;
            
            case 6: {
            	// Test that turns the barrier on and off
            	// for thirty seconds.
            	cars.startAll();
            	long start = Calendar.getInstance().getTimeInMillis();
            	long now;
            	do {
            		cars.barrierOn();
            		sleep(250);
            		cars.barrierOff();
            		sleep(250);
            		now = Calendar.getInstance().getTimeInMillis();
            	} while(start+30000 > now);
            	break;
            }	
            case 7: {
            	//Same as previous case, except the speed is extreme.
            	for (int i = 1; i < 9; i++) {
					cars.setSpeed(i, 1);
				};
            	cars.startAll();
            	long start = Calendar.getInstance().getTimeInMillis();
            	long now;
            	do {
            		cars.barrierOn();
            		sleep(250);
            		cars.barrierOff();
            		sleep(250);
            		now = Calendar.getInstance().getTimeInMillis();
            	} while(start+30000 > now);
            	break;
            }
            case 8:
            	//Test removing and restoring a car while running
            	cars.startAll();
            	sleep(500);
            	cars.removeCar(1);
            	sleep(500);
            	cars.restoreCar(1);
            	break;
            
            case 9:
            	//Test removing and restoring really fast a car while running
            	cars.startAll();
            	sleep(500);
            	cars.removeCar(1);
            	cars.restoreCar(1);
            	cars.removeCar(1);
            	cars.restoreCar(1);
            	break;
            	
            case 10:
            	//Test removing a car while barrier is on and letting all other cars run
            	//Test illustrates that only present cars are being waited for
            	cars.barrierOn();
            	cars.removeCar(1);
            	cars.startAll();
            	break;
            	
            case 11:
            	//Test removing a car while barrier is on, then switching barrier off
            	//and on again.
            	//Test illustrates that only present cars are being waited for, and 
            	//barrierOn() and barrrierOff() does not change this.
            	cars.barrierOn();
            	cars.removeCar(1);
            	cars.startAll();
            	cars.barrierOff();
            	cars.barrierOn();
            	break;
            	
            case 12:
            	//Test removing a car while barrier is on, then waiting a while and restoring car
            	//Test illustrates that only present cars are being waited for, and 
            	//when a car is restored everything runs as normal
            	cars.barrierOn();
            	cars.startAll();
            	cars.removeCar(1);
            	sleep(1000);
            	cars.restoreCar(1);
            	break;
            case 19:
                // Demonstration of speed setting.
                // Change speed to double of default values
                cars.println("Doubling speeds");
                for (int i = 1; i < 9; i++) {
                    cars.setSpeed(i,50);
                };
                break;

            default:
                cars.println("Test " + testno + " not available");
            }

            cars.println("Test ended");

        } catch (Exception e) {
            System.err.println("Exception in test: "+e);
        }
    }

}

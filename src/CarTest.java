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
            	// Same test as last case, except with high speeds.

            	cars.startAll();
				for (int i = 1; i < 9; i++) {
					cars.setSpeed(i, 1);
				};
            	
            	break;
            	
            case 2:
            	// Testing barrier with regular speeds.
            	cars.barrierOn();
            	cars.startAll();
            	break;
            	
            case 3:
            	// Testing barrier with extreme speeds.
            	cars.barrierOn();
            	cars.startAll();
            	for (int i = 1; i < 9; i++) {
					cars.setSpeed(i, 1);
				};
            	break;
            
            case 4:
            	// Tests if turning the barrier on and off
            	// doesn't cause problems.
            	
            	cars.barrierOn();
            	cars.startAll();
            	sleep(1000);
            	cars.barrierOff();
            	sleep(100);
            	cars.barrierOn();
            	break;
            
            case 5:
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
            
            case 18: 
            	//Barrier on and off
            	cars.barrierOn();
            	sleep(100);
            	cars.startAll();
            	sleep(100);
            	cars.barrierOff();
            	sleep(100);
            	cars.barrierOn();
            	sleep(100);
            	cars.barrierOff();
            	
            
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




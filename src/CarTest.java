//Implementation of Car Test class
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
            	//Same test as previous case, except with high speeds.
            	for (int i = 1; i < 9; i++) {
            		cars.setSpeed(i, 1);
            	}
            	cars.startAll();
            	break; 
            	
            case 2:
            	//Run cars in one direction. All cars should just keep running and
            	//never stop at the alley. This test shows obligingness.
            	for (int i = 1; i < 5; i++) {
            		cars.startCar(i);
				}
            	sleep(10000);
                cars.stopAll();
                break;
                
            case 3:
            	//Testing the alley with slowdown enabled.
            	//Cars should experience starvation to an extend.
            	cars.setSlow(true);
            	cars.startAll();
            	break;
            	
            case 4:
            	//Testing barrier with regular speeds.
            	//The cars should synchronize at the barrier.
            	cars.barrierOn();
            	cars.startAll();
            	break;
            	
            case 5:
            	//Same as previous case, except with high speeds.
            	for (int i = 1; i < 9; i++) {
            		cars.setSpeed(i, 1);
            	}
            	cars.barrierOn();
            	cars.startAll();
            	break;
            
            case 6:
            	// Tests if synchronization still works if the barrier
            	// is turned on and off in rapid succession. Cars should
            	// synchronize as normal.
            	cars.barrierOn();
            	cars.startAll();
            	cars.barrierOff();
            	cars.barrierOn();
            	sleep(5000);
            	cars.barrierOff();
            	cars.barrierOn();
            	break;
            
            case 7: {
            	// Test that turns the barrier on and off
            	// for thirty seconds. Cars should 
            	// synchronize as normal.
            	cars.barrierOn();
            	cars.startAll();
            	long start = System.currentTimeMillis();
            	long now;
            
            	do {
            		cars.barrierOff();
            		sleep(250);
            		cars.barrierOn();
            		sleep(250);
            		now = System.currentTimeMillis();
            	} while(start+30000 > now);
            	break;
            }	
            case 8: {
            	//Same as previous case, except the speed is extreme.
            	for (int i = 1; i < 9; i++) {
					cars.setSpeed(i, 1);
				};
				cars.barrierOn();
            	cars.startAll();
            	long start = System.currentTimeMillis();
            	long now;
            	do {
            		cars.barrierOff();
            		sleep(250);
            		cars.barrierOn();
            		sleep(250);
            		now = System.currentTimeMillis();
            	} while(start+30000 > now);
            }
            	break;
            
            case 9:
            	//Turn barrier on and start all cars but one. 
            	//Cars should not be able to cross the barrier.
            	//This test demonstrates that not only running cars
            	//are being waited for.
            	cars.barrierOn();
            	cars.startCar(0);
            	cars.startCar(1);
            	cars.startCar(2);
            	cars.startCar(3);
            	cars.startCar(4);
            	cars.startCar(5);
            	cars.startCar(6);
            	cars.startCar(7);
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
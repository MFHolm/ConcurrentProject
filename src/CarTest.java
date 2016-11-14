//Implementation of Car Test class
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2016

//Hans Henrik Lovengreen    Oct 3, 2016
//Modified by Marita F. Holm s144445 and Mathias D. Thomsen s132317 Nov 14, 2016

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
            	//Same test as case 0, except with high speeds.
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
            	//Testing barrier with regular speeds. The cars should synchronize at the barrier.
            	cars.barrierOn();
            	cars.startAll();
            	break;
            	
            case 5:
            	//Same as previous case, except with high speeds.
            	cars.barrierOn();
            	for (int i = 1; i < 9; i++) {
            		cars.setSpeed(i, 1);
            	}
            	cars.startAll();
            	break;
            
            case 6:
            	//Test if synchronization still works if the barrier
            	//is turned on and off in rapid succession. Cars should
            	//syncrhonize as normal.
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
            	break;
            }
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
            case 10:
            	//Test removing and restoring a car while running. Car should 
            	//be removed and then restored and run as normal.
            	cars.startAll();
            	sleep(500);
            	cars.removeCar(1);
            	sleep(500);
            	cars.restoreCar(1);
            	break;
            
            case 11:
            	//Test removing and restoring cars in rapid succession. 
            	//Car should be restored and run as normal.
            	cars.startAll();
            	sleep(500);
            	cars.removeCar(1);
            	cars.restoreCar(1);
            	cars.removeCar(1);
            	cars.restoreCar(1);
            	break;
            	
            case 12:
            	//Same as previous case, except the speed is extreme.
            	cars.setSpeed(1, 1);
            	cars.startAll();
            	sleep(500);
            	cars.removeCar(1);
            	cars.restoreCar(1);
            	cars.removeCar(1);
            	cars.restoreCar(1);
            	break;
            case 13:
            	//Test removing a car while barrier is on and letting all other cars run
            	//Test illustrates that only present cars are being waited for
            	cars.barrierOn();
            	cars.removeCar(1);
            	cars.startAll();
            	break;
            	
            case 14:
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
            	
            case 15:
            	//Test removing a car while barrier is on, then waiting a while and restoring car
            	//Test illustrates that only present cars are being waited for, and 
            	//when a car is restored everything runs as normal
            	cars.barrierOn();
            	cars.startAll();
            	cars.removeCar(1);
            	sleep(1000);
            	cars.restoreCar(1);
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

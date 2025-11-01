import java.util.Scanner;

//ONE RUNWAY AIRPORT SIMULATION

public class AirportSimulation {

    enum Op { NONE, LANDING, TAKEOFF }

    /**
     * Simple integer queue implemented with singly linked nodes.
     * Provides enqueue, dequeue, peek, isEmpty, and size.
     */
    static class IntQueue {
        private static class Node {
            int value;
            Node next;
            Node(int v) { value = v; }
        }

        private Node head = null; // remove from head
        private Node tail = null; // insert to tail
        private int size = 0;

        public void enqueue(int v) {
            Node n = new Node(v);
            if (tail == null) {
                head = tail = n;
            } else {
                tail.next = n;
                tail = n;
            }
            size++;
        }

        //sync problems

        public int dequeue() {
            if (head == null) throw new RuntimeException("Queue empty");
            int v = head.value;
            head = head.next;
            if (head == null) tail = null;
            size--;
            return v;
        }

        public int peek() {
            if (head == null) throw new RuntimeException("Queue empty");
            return head.value;
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public int size() {
            return size;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter time needed for one plane to land (minutes, integer):");
        int landingServiceTime = sc.nextInt();

        System.out.println("Enter time needed for one plane to take off (minutes, integer):");
        int takeoffServiceTime = sc.nextInt();

        System.out.println("Enter average time (minutes) between arrivals to landing queue (double > 0):");
        double avgBetweenLandings = sc.nextDouble();

        System.out.println("Enter average time (minutes) between arrivals to takeoff queue (double > 0):");
        double avgBetweenTakeoffs = sc.nextDouble();

        System.out.println("Enter maximum time a plane can wait in landing queue before running out of fuel (minutes, integer):");
        int maxLandingWait = sc.nextInt();

        System.out.println("Enter total length of time to be simulated (minutes, integer):");
        int totalSimTime = sc.nextInt();

        // Basic validation
        if (landingServiceTime <= 0 || takeoffServiceTime <= 0 || avgBetweenLandings <= 0 || avgBetweenTakeoffs <= 0
                || maxLandingWait < 0 || totalSimTime <= 0) {
            System.out.println("Invalid input: all times must be positive (maxLandingWait can be zero). Exiting.");
            sc.close();
            return;
        }

        // Arrival probabilities per minute (discrete-time approx)
        double probLandingArrival = Math.min(1.0, 1.0 / avgBetweenLandings);
        double probTakeoffArrival = Math.min(1.0, 1.0 / avgBetweenTakeoffs);

        // Use custom queues (no JCF)
        IntQueue landingQueue = new IntQueue(); // stores arrival minute for each plane
        IntQueue takeoffQueue = new IntQueue();

        int runwayBusy = 0;         // remaining minutes runway is busy (0 = free)
        Op currentOp = Op.NONE;

        long totalLandingWait = 0;  // sum of waiting times (minutes) for planes that actually started landing
        long totalTakeoffWait = 0;  // sum of waiting times for planes that actually started takeoff
        int numLanded = 0;          // number of planes that started landing during simulation
        int numTookOff = 0;         // number of planes that started takeoff during simulation
        int numCrashed = 0;

        // --- Arrays to record per-minute stats ---
        int[] landingQueueOverTime = new int[totalSimTime];      // 1D: landing queue size each minute
        int[] takeoffQueueOverTime = new int[totalSimTime];      // 1D: takeoff queue size each minute
        // 2D: columns = { landingQueueSize, takeoffQueueSize, runwayState }
        // runwayState: 0 = idle, 1 = landing, 2 = takeoff
        int[][] minuteStats = new int[totalSimTime][3];

        // Simulation loop: minute by minute
        for (int clock = 0; clock < totalSimTime; clock++) {

            // 1) Advance any current service by 1 minute (if runway was busy)
            if (runwayBusy > 0) {
                runwayBusy--;
                if (runwayBusy == 0) {
                    currentOp = Op.NONE; // runway becomes free
                }
            }

            // 2) New arrivals (happen during this minute)
            if (Math.random() < probLandingArrival) {
                landingQueue.enqueue(clock);
            }
            if (Math.random() < probTakeoffArrival) {
                takeoffQueue.enqueue(clock);
            }

            // 3) If runway free, start next operation (landing priority)
            if (runwayBusy == 0) {
                // Try to service a landing (skipping crashed planes)
                while (!landingQueue.isEmpty() && runwayBusy == 0) {
                    int arrivalTime = landingQueue.peek();
                    if (clock - arrivalTime > maxLandingWait) {
                        // This plane has already crashed while waiting
                        numCrashed++;
                        landingQueue.dequeue(); // discard crashed plane
                        // keep searching for next plane
                    } else {
                        // Start landing
                        landingQueue.dequeue();
                        int wait = clock - arrivalTime;
                        totalLandingWait += wait;
                        numLanded++;
                        runwayBusy = landingServiceTime;
                        currentOp = Op.LANDING;
                        // started service this minute; runwayBusy decremented in subsequent minutes
                    }
                }

                // If no landing started, and runway still free, consider takeoff
                if (runwayBusy == 0 && !takeoffQueue.isEmpty()) {
                    int arrivalTime = takeoffQueue.dequeue();
                    int wait = clock - arrivalTime;
                    totalTakeoffWait += wait;
                    numTookOff++;
                    runwayBusy = takeoffServiceTime;
                    currentOp = Op.TAKEOFF;
                }
            }

            // --- Record per-minute statistics AFTER arrivals and after potential service start ---
            landingQueueOverTime[clock] = landingQueue.size();
            takeoffQueueOverTime[clock] = takeoffQueue.size();

            int runwayStateForMinute = 0;
            if (runwayBusy > 0) {
                runwayStateForMinute = (currentOp == Op.LANDING) ? 1 : (currentOp == Op.TAKEOFF ? 2 : 0);
            } else {
                runwayStateForMinute = 0;
            }

            minuteStats[clock][0] = landingQueueOverTime[clock];
            minuteStats[clock][1] = takeoffQueueOverTime[clock];
            minuteStats[clock][2] = runwayStateForMinute;
        } // end for clock

        // After simulation time: inspect remaining landing queue for crashed planes
        while (!landingQueue.isEmpty()) {
            int arrivalTime = landingQueue.dequeue();
            if (totalSimTime - arrivalTime > maxLandingWait) {
                numCrashed++;
            }
        }

        // Compute averages (avoid division by zero)
        double avgTakeoffWait = (numTookOff == 0) ? 0.0 : ((double) totalTakeoffWait / numTookOff);
        double avgLandingWait = (numLanded == 0) ? 0.0 : ((double) totalLandingWait / numLanded);

        // Output results
        System.out.println("\n--- Simulation results ---");
        System.out.println("Total simulated minutes: " + totalSimTime);
        System.out.println("1) Number of planes that took off (started takeoff): " + numTookOff);
        System.out.println("2) Number of planes that landed (started landing): " + numLanded);
        System.out.println("3) Number of planes that crashed due to fuel shortage: " + numCrashed);
        System.out.printf  ("4) Average time a plane spent in takeoff queue: %.3f minutes%n", avgTakeoffWait);
        System.out.printf  ("5) Average time a plane spent in landing queue: %.3f minutes%n", avgLandingWait);
        System.out.println("---------------------------");


        //delete
        // Print per-minute CSV report: minute,landingQueue,takeoffQueue,runwayState
        // runwayState: 0=idle,1=landing,2=takeoff
        //delete

        sc.close();
    }
}


//TESTing new branch test, forked isntead

/* 1. Project Objective 
a. To implement and test a small-scale Java project using the concepts of the OOP, stack, 
and/or queue data structures.  
b. To demonstrate the code and answer some technical questions related to the project 
(if required). 


2. Project Requirements  
a. Code development - All projects MUST implement the following concepts: 

 Loops and user input 

 Methods (void and/or value-returned)  

 1-D and/or 2-D arrays (if deemed to be needed) 

 Object and Classes 

 The concept of STACK and/or QUEUE data structures. All projects MUST  use the 
following base classes (ArrayQueue.java and/or ArrayStack.java) that have been 
given in class. Add methods to these classes whenever are needed in your project. 

 Other concepts could be included if deemed to be necessary for the project. This 
includes (Inheritance, Class Abstraction, and Searching algorithms). 

b. Things that SHOULD BE AVOIDED/NOT TO BE USED in the code development 

 Use any JCF libraries including <Stack>, <Queue>, <LinkedList>, <Vector>, etc. 

 Using any JCF built-in algorithms which have not been discussed in class. 
 
 Any advanced data structures which are yet to be discussed in class. This includes 
Tree, Hash, graphs, or any other data structures. 

3. Project Evaluation 
 Group Project I bears 10 POINTS of your Total Course Evaluation. 

a. Details of Project I Evaluation (10 POINTS (IMPLEMENTATION + DISCUSSION)) - 
Functioning properly and fulfilling project requirements (7 POINTS) 


REQUIREMENTS          
1) Methods, if and loops statements            
2) Uses 1D and/or 2D Arrays             
3) Uses of Objects and Classes          
4) The concept of STACK and/or QUEUE data structures       
5) Program working properly, ‘error free’         
 */
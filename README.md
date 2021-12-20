# Fluxtion 5 minute tutorial

Simple 5 minute tutorial for fluxtion, all the implementation is in one class [Main](https://github.com/gregv12/Fluxtion-5-minute-tutorail/blob/master/src/main/java/com/fluxtion/examoke/fluxtion5minute/Main.java)

## Scenario:
Control a car park entry gate for an automated carpark. Tracks number plates of cars leaving and entering with sensors
-  If there is enough capacity in the car park open the gate,
-  If carpark full flash full sign and close gate
-  If a car leaves and there is capacity open the gate where the first car was waiting

### Events:
-  CarIn - a sensor detects cars coming into the carpark
-  CarOut - a sensor detects cars leaving the carpark
-  RequestEntry - the gate where a car is waiting to enter when park is full

### User classes
-  CarParkMonitor - keeps a count of spaces used by handling carIn and carOut events
-  GateController - controls gate. Child of CarParkMonitor, closes gate when full. Handles RequestEntry events for queued cars and opens the gate when a space is free 

## Running the example
Run the main method from your ide and you should see similar output to below. 

-  A Processor.java file will be generated in: {project_build_dir}\target/generated-sources/fluxtion/com/fluxtion/examoke/fluxtion5minute/main
-  A png of the execution graph will also be generated - {project_build_dir}\src/main/resources/com/fluxtion/examoke/fluxtion5minute/main/

```
car in spaces used:1
car in spaces used:2
car in spaces used:3
car in spaces used:4
car in spaces used:5
car in spaces used:6
car in spaces used:7
car in spaces used:8
car in spaces used:9
car in spaces used:10
CAR PARK FULL - CLOSING GATE
CAR PARK FULL please wait until gate opens
CAR PARK FULL please wait until gate opens
CAR PARK FULL please wait until gate opens
CAR PARK FULL please wait until gate opens
car out spaces used:9
GATE OPEN - GATE:gate 1
car in spaces used:10
CAR PARK FULL - CLOSING GATE
car out spaces used:9
GATE OPEN - GATE:gate 1
car in spaces used:10
CAR PARK FULL - CLOSING GATE
car out spaces used:9
GATE OPEN - GATE:gate 2
car in spaces used:10
CAR PARK FULL - CLOSING GATE
```

### implementation notes:
1. Create normal java classes for Events and event handlers.
1. Annotate event handling methods in the user classes
1. Build an object graph with normal java semantics using new etc.
1. Ask Fluxtion to build an event processor that will route events to the correct methods of user classes
1. call SEPConfig#addNode for any root instance so the Fluxtion compiler knows what to include in the graph
1. An EventProcessor is returned that will accept events from client code.

To use the event processor client code must call init on the event processor to ensure all the instances are in a valid state before sending any events.

### GOOD LUCK :)

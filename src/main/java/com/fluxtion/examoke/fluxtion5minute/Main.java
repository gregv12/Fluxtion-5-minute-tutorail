package com.fluxtion.examoke.fluxtion5minute;

import com.fluxtion.compiler.Fluxtion;
import com.fluxtion.runtim.EventProcessor;
import com.fluxtion.runtim.annotations.EventHandler;
import com.fluxtion.runtim.annotations.OnEvent;
import lombok.Value;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Simple 5 minute tutorial for fluxtion.
 * <p>
 * Scenario:
 * Control a car park entry gate.
 * Automatic carpark, tracks number plates of cars leaving and entering with sensors
 * If there is enough capacity in the car park open the gate,
 * If carpark full flash full sign and close gate
 * If a car leaves and there is capacity open the gate where the first car was waiting
 *
 * Events:
 * CarIn - a sensor detects cars coming into the carpark
 * CarOut - a sensor detects cars leaving the carpark
 * RequestEntry - the gate where a car is waiting to enter when park is full
 *
 *Implementation notes:
 *
 * Create normal java classes for Events and event handlers.
 * Build an object graph with normal java semantics using new etc.
 * As Fluxtion to build an event processor that will route events to correct methods
 * call SEPConfig#addNode for any root instance.
 *
 * An {@link EventProcessor} is returned that will accept events from client code.
 *
 * must call init on the event processor to ensure all the instances are in a valid state before sending any events
 * 
 */
public class Main {

    public static void main(String[] args) {

        EventProcessor processor = Fluxtion.compile(cfg ->
// uncomment this line and run fully interpreted no java code is generated and compiled
//        EventProcessor processor = Fluxtion.interpret(cfg ->
                cfg.addNode( new GateController(new CarParkMonitor(), 10))
        );
        processor.init();
        //fill the car park up
        for (int i = 0; i < 10; i++) {
            processor.onEvent(new CarIn());
        }

        //stack some cars
        processor.onEvent(new RequestEntry("gate 1"));
        processor.onEvent(new RequestEntry("gate 1"));
        processor.onEvent(new RequestEntry("gate 2"));
        processor.onEvent(new RequestEntry("gate 1"));

        //have some cars leave and sensors detect entry
        processor.onEvent(new CarOut());
        processor.onEvent(new CarIn());

        processor.onEvent(new CarOut());
        processor.onEvent(new CarIn());

        processor.onEvent(new CarOut());
        processor.onEvent(new CarIn());

    }

    //USER CLASSES
    public static class CarParkMonitor {

        public int spacesUsed;

        @EventHandler
        public void carIn(CarIn carin) {
            spacesUsed++;
            log("car in spaces used:" + spacesUsed);
        }

        @EventHandler
        public void carOut(CarOut CarIOut) {
            spacesUsed--;
            log("car out spaces used:" + spacesUsed);
        }

        public int getSpacesUsed() {
            return spacesUsed;
        }
    }

    @Value
    public static class GateController {
        CarParkMonitor carParkMonitor;
        int capacity;
        transient Queue<RequestEntry> waitingGates = new LinkedList<>();

        @EventHandler(propagate = false)
        public void carRequestEntry(RequestEntry entry) {
            if (carParkMonitor.getSpacesUsed() >= capacity) {
                log("CAR PARK FULL please wait until gate opens");
                waitingGates.add(entry);
            }
        }

        @OnEvent
        public void checkGatesForWaiting() {
            if (!waitingGates.isEmpty() && carParkMonitor.getSpacesUsed() < capacity) {
                RequestEntry entryRequest = waitingGates.remove();
                log("GATE OPEN - GATE:" + entryRequest.getGateId());
            }else{
                log("CAR PARK FULL - CLOSING GATE");
            }
        }
    }

    //EVENTS
    public static class CarIn {}

    public static class CarOut {}

    @Value
    public static class RequestEntry {
        String gateId;
    }

    public static void log(String message) {
        System.out.println(message);
    }

}

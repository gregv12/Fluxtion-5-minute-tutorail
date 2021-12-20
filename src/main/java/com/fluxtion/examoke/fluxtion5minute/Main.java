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
 * An automated car park tracks number plates of vehicles entering and leaving the car park. Events are fired by the
 * sensors indicating either an arrival or a departure. An automatic gate can be lowered if the car park is full, an
 * event is published from a gate when a car is waiting to enter the car park.
 * <p>
 * The following logic sis required:
 * <ul>
 *     <li>If there is enough capacity in the car park open the gate</li>
 *     <li>If car park full flash full sign and close gate</li>
 *     <li>If a car leaves and there is capacity open the gate for the car that has waited the longest</li>
 * </ul>
 *
 * <p>
 * Events:
 * CarIn - a sensor detects cars coming into the car park
 * CarOut - a sensor detects cars leaving the car park
 * RequestEntry - the gate where a car is waiting to enter when park is full
 * <p>
 * Implementation notes:
 * Create normal java classes for Events and event handlers.
 * Build an object graph with normal java semantics using new etc.
 * Use the Fluxtion utility to build an event processor that will route events to the correct instance methods
 * <p>
 * To build the graph call SEPConfig#addNode to add an instance to the graph. It is sufficient to add just the leaf
 * nodes as Fluxtion will walk references by reflection and add any missing nodes to the graph.
 * <p>
 * Fluxtion will build An {@link EventProcessor} that accepts and processes events by calling {@link EventProcessor#onEvent(Object)}.
 * <p>
 * A client must call init on the event processor to ensure all the instances are in a valid state before sending any events
 */
public class Main {

    public static void main(String[] args) {

// uncomment this line and run fully interpreted no java code is generated and compiled
//      EventProcessor processor = Fluxtion.interpret(cfg ->
        EventProcessor processor = Fluxtion.compile(cfg ->
                cfg.addNode(new GateController(new CarParkMonitor(), 10))
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

    public interface CarParkCounter {
        int getSpacesUsed();
    }

    //USER CLASSES
    public static class CarParkMonitor implements CarParkCounter {

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
        CarParkCounter carParkMonitor;
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
            } else if (carParkMonitor.getSpacesUsed() >= capacity) {
                log("CAR PARK FULL - CLOSING GATE");
            }
        }
    }

    //EVENTS
    public static class CarIn {
    }

    public static class CarOut {
    }

    @Value
    public static class RequestEntry {
        String gateId;
    }

    public static void log(String message) {
        System.out.println(message);
    }

}

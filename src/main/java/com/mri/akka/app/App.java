package com.mri.akka.app;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.routing.RoundRobinRouter;
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeUnit;

import akka.dispatch.OnComplete;
import akka.dispatch.OnSuccess;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import static akka.pattern.Patterns.ask;
import static akka.pattern.Patterns.ask;

import scala.concurrent.Future;
import scala.concurrent.Await;
import scala.concurrent.Promise;
import scala.concurrent.ExecutionContext;
import scala.concurrent.ExecutionContext$;

import akka.util.Timeout;
import akka.event.Logging;
import akka.event.LoggingAdapter;


public class App {

 public enum Operation{
     INCREMENT,
     DECREMENT
 }

 public static void main(String args[]){
     System.out.println("\nTest app created in Akka \nPurpose: to simulate actors modifying shared resource\n");

     ActorSystem system = ActorSystem.create("MyApp");
     ActorRef modifier1 = system.actorOf(new Props(Modifier.class), "Modifier1");
     ActorRef modifier2 = system.actorOf(new Props(Modifier.class), "Modifier2");

     Operation increment = Operation.INCREMENT;
     Operation decrement = Operation.DECREMENT;

     ActorRef sharedObj = system.actorOf(new Props(SharedResource.class), "SharedResource");

     // modifying the shared resource (counter will increase or decrease)
     sharedObj.tell(increment, modifier1);
     sharedObj.tell(increment, modifier1);
     sharedObj.tell(decrement, modifier2);
     sharedObj.tell(decrement, modifier1);
     sharedObj.tell(increment, modifier2);
     sharedObj.tell(increment, modifier2);
     sharedObj.tell(decrement, modifier1);

     system.shutdown();
 }


 public static class SharedResource extends UntypedActor {

     private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
     private static int counter = 0;

     public void onReceive(Object message){
         if (message instanceof Operation){

             log.info("{} said to {}", getSender().toString(), message.toString());

             if ( Operation.INCREMENT == (Operation)message ){
                 counter+=1;
                 log.info("Increment...");
	     }
	     else if ( Operation.DECREMENT == (Operation)message ){
                 counter-=1;
                 log.info("Decrement...");
	     } 
             getSender().tell("count is " + counter, self());
	 }else{
             unhandled(message);
	 }
     } 
 }

 public static class Modifier extends UntypedActor {

     private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


     public void onReceive(Object message){
         if (message instanceof String){
            log.info("received: {}", message); 
         }else{
             unhandled(message);
         }
     }
 }

}


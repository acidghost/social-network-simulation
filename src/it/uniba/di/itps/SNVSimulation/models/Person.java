package it.uniba.di.itps.SNVSimulation.models;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Created by acidghost on 13/09/14.
 */
public class Person extends Agent {
    private Logger logger = Logger.getMyLogger(this.getClass().getName());

    private double agreableness;
    private double extroversion;
    private Interests[] interests;

    private AID[] neighbors;

    private int receivedMessages = 0;
    private int startedMessages = 0;
    private Map<AID, Integer> forwardedFrom = new HashMap<AID, Integer>();
    public static int ticks = 0;

    @Override
    protected void setup() {
        logger.info("Person agent started: " + getLocalName());

        Object[] args = getArguments();
        agreableness = (Double) args[0];
        extroversion = (Double) args[1];
        interests = (Interests[]) args[2];

        SequentialBehaviour sequential = new SequentialBehaviour(this);
        sequential.addSubBehaviour(new UpdateNeighbors(this));
        ParallelBehaviour parallel = new ParallelBehaviour();
        parallel.addSubBehaviour(new ConsiderSendNewMessage(this, 1000));
        parallel.addSubBehaviour(new ReceiveAndForward());
        sequential.addSubBehaviour(parallel);
        addBehaviour(sequential);
    }

    @Override
    protected void takeDown() {
        System.out.println("\n\n\n");
        System.out.println("Agent " + getLocalName());
        System.out.println("startedMessages: " + startedMessages);
        System.out.println("receivedMessages: " + receivedMessages);
        System.out.println("forwardedFrom:");
        for(AID a : forwardedFrom.keySet()) {
            System.out.println(" - " + a.getLocalName() + ": " + forwardedFrom.get(a));
        }
    }

    protected class UpdateNeighbors extends OneShotBehaviour {
        public UpdateNeighbors(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("social-network");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setSender(getAID());
                for (DFAgentDescription aResult : result) {
                    msg.addReceiver(aResult.getName());
                }
                msg.setOntology("get-neighbors");
                msg.setConversationId(myAgent.getName());
                myAgent.send(msg);
                ACLMessage reply = myAgent.blockingReceive(MessageTemplate.MatchConversationId(myAgent.getName()));
                String[] agentsNames = reply.getContent().split("#");
                neighbors = new AID[agentsNames.length];
                for (int i = 0; i < agentsNames.length; i++) {
                    neighbors[i] = new AID(agentsNames[i], AID.ISLOCALNAME);
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }

    protected class ConsiderSendNewMessage extends TickerBehaviour {
        public ConsiderSendNewMessage(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            ticks++;
            double random = Math.random();
            if(random <= extroversion) {
                ACLMessage message = new ACLMessage(ACLMessage.PROPAGATE);
                message.setSender(getAID());
                try {
                    message.setContentObject(interests);
                    for(AID neightbor : neighbors) {
                        message.addReceiver(neightbor);
                    }
                    myAgent.send(message);
                    startedMessages++;
                    logger.info("Sent message from " + getLocalName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected class ReceiveAndForward extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage message = receive();
            if(message != null) {
                try {
                    receivedMessages++;
                    Interests[] content = (Interests[]) message.getContentObject();
                    logger.info(getLocalName() + " received from " + message.getSender().getLocalName());

                    List<Interests> matched = new ArrayList<Interests>();
                    List<Interests> myInterests = Arrays.asList(interests);
                    for (Interests interest : content) {
                        if (myInterests.contains(interest)) {
                            matched.add(interest);
                        }
                    }
                    double interestImpact = matched.size() / interests.length;
                    double prob = (agreableness*extroversion) + ((1-agreableness*extroversion) * interestImpact);
                    double random = Math.random();
                    logger.info(getLocalName() + "     PROB:  " + prob + "   RAND:   " + random);
                    if(random <= prob) {
                        // Forward message
                        ACLMessage newMessage = new ACLMessage(ACLMessage.INFORM);
                        newMessage.setSender(getAID());
                        for(AID neightbor : neighbors) {
                            if(!neightbor.equals(message.getSender())) {
                                newMessage.addReceiver(neightbor);
                            }
                        }
                        newMessage.setContentObject(content);
                        send(newMessage);
                        if(forwardedFrom.containsKey(message.getSender())) {
                            forwardedFrom.put(message.getSender(), forwardedFrom.get(message.getSender())+1);
                        } else {
                            forwardedFrom.put(message.getSender(), 1);
                        }
                        logger.info(getLocalName() + " forwarded a message from " + message.getSender().getLocalName());
                    }
                } catch (UnreadableException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    }
}

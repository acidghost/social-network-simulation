package it.uniba.di.itps.SNVSimulation;

import it.uniba.di.itps.SNVSimulation.models.Interests;
import it.uniba.di.itps.SNVSimulation.models.Person;
import it.uniba.di.itps.SNVSimulation.sn.SocialNetwork;
import it.uniba.di.itps.SNVSimulation.models.Trait;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by acidghost on 13/09/14.
 */
public class Simulation extends Agent {
    private static final Trait[] TRAITS = {
            // agreableness, extroversion, interests[]
            new Trait(0.7, 0.4, Interests.BASKET, Interests.BEACH, Interests.TRAVELS),
            new Trait(0.7, 0.1, Interests.SOCCER, Interests.BASKET, Interests.TENNIS),
            new Trait(0.9, 0.2, Interests.TRAVELS, Interests.BEACH, Interests.FITNESS),
            new Trait(0.7, 0.1, Interests.TECHNOLOGY, Interests.SCIENCE, Interests.DOGS),
            new Trait(0.3, 0.1, Interests.DOGS, Interests.TRAVELS, Interests.CATS)
    };
    public static final int N_AGENTS = TRAITS.length;
    public static final AID SNAGENT = new AID("SNAgent", AID.ISLOCALNAME);

    private List<AID> aidList = new ArrayList<AID>();

    private Logger logger = Logger.getMyLogger(this.getClass().getName());

    @Override
    protected void setup() {
        logger.info("Simulation started: " + getLocalName());

        try {
            AgentController sn = getContainerController().createNewAgent(SNAGENT.getLocalName(), SocialNetwork.class.getName(), null);
            sn.start();
            addBehaviour(new AddNodes());
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("\n\n\n");
        System.out.println("Taking down the simulation");
        System.out.println("total ticks: " + Person.ticks/N_AGENTS);
    }

    protected class AddNodes extends Behaviour {
        private int done = 0;

        @Override
        public void action() {
            String name = "Person-00"+(done+1);
            AID newAID = new AID(name, AID.ISLOCALNAME);
            aidList.add(done, newAID);
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setSender(getAID());
            msg.setOntology("new-node");
            msg.addReceiver(SNAGENT);
            msg.setContent(name);
            send(msg);
            done++;
        }

        @Override
        public boolean done() {
            if (done == N_AGENTS) {
                addBehaviour(new AddConnections());
                return true;
            } else {
                return false;
            }
        }
    }

    protected class AddConnections extends Behaviour {
        private final List<AID[]> CONNECTIONS = new ArrayList<AID[]>();
        {
            for(int i = 1; i<N_AGENTS; i++) {
                CONNECTIONS.add(new AID[] { aidList.get(0), aidList.get(i) });
            }
            CONNECTIONS.add(new AID[] { aidList.get(1), aidList.get(2) });
        }
        private Iterator<AID[]> iterator = CONNECTIONS.iterator();
        private boolean canSend = false;

        @Override
        public void action() {
            if(canSend) {
                AID[] pair = iterator.next();
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setSender(getAID());
                msg.setOntology("new-connection");
                msg.addReceiver(SNAGENT);
                msg.setContent(pair[0].getLocalName() + "#" + pair[1].getLocalName());
                send(msg);
            } else {
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchOntology("can-add-connections"), MessageTemplate.MatchSender(SNAGENT));
                ACLMessage m = receive(mt);
                if(m != null) {
                    canSend = true;
                } else {
                    block();
                }
            }
        }

        @Override
        public boolean done() {
            if(!iterator.hasNext()) {
                addBehaviour(new StartSimulation());
                return true;
            } else {
                return false;
            }
        }
    }

    protected class StartSimulation extends Behaviour {
        private int i = 0;

        @Override
        public void action() {
            try {
                Trait trait = TRAITS[i];
                Object[] args = new Object[] { trait.agreableness, trait.extroversion, trait.interests };
                getContainerController().createNewAgent(aidList.get(i).getLocalName(), Person.class.getName(), args).start();
                i++;
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean done() {
            return i == N_AGENTS;
        }
    }
}

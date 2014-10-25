package it.uniba.di.itps.SNVSimulation.sn;

import it.uniba.di.itps.SNVSimulation.Simulation;
import it.uniba.di.itps.SNVSimulation.sn.graph.Graph;
import it.uniba.di.itps.SNVSimulation.sn.graph.Node;
import it.uniba.di.itps.SNVSimulation.sn.graph.PlainGraph;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

import java.util.List;

/**
 * Created by acidghost on 13/09/14.
 */
public class SocialNetwork extends Agent {
    private Graph graph;
    private static final AID SIMAGENT = new AID("SimAgent", AID.ISLOCALNAME);

    private Logger logger = jade.util.Logger.getMyLogger(this.getClass().getName());

    @Override
    protected void setup() {
        graph = new PlainGraph();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("social-network");
        sd.setName("Social-Network-Agent");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new ListenForNewNodes(this));
        addBehaviour(new ListenForNewConnections(this));

        addBehaviour(new ServeNeighbors(this));
    }

    protected class ListenForNewNodes extends CyclicBehaviour {
        public ListenForNewNodes(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchSender(SIMAGENT), MessageTemplate.MatchOntology("new-node"));
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null && mt.match(msg)) {
                Node node = new Node(new AID(msg.getContent(), AID.ISLOCALNAME));
                graph.addNode(node);
                logger.info("Added node:\t" + node);
                if(graph.getNodes().size() == Simulation.N_AGENTS) {
                    ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                    message.setSender(getAID());
                    message.setOntology("can-add-connections");
                    message.addReceiver(SIMAGENT);
                    send(message);
                }
            } else {
                block();
            }
        }
    }

    protected class ListenForNewConnections extends CyclicBehaviour {
        public ListenForNewConnections(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchSender(SIMAGENT), MessageTemplate.MatchOntology("new-connection"));
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null) {
                String[] nodes = msg.getContent().split("#");
                Node node1 = new Node(new AID(nodes[0], AID.ISLOCALNAME));
                Node node2 = new Node(new AID(nodes[1], AID.ISLOCALNAME));
                graph.addEdge(node1, node2);
                logger.info("Added connection...");
            } else {
                block();
            }
        }
    }

    protected class ServeNeighbors extends CyclicBehaviour {
        public ServeNeighbors(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchOntology("get-neighbors");
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null) {
                AID sender = msg.getSender();
                List<Node> neighbors = graph.getNeighbors(new Node(sender));
                try {
                    ACLMessage reply = msg.createReply();
                    reply.setConversationId(msg.getConversationId());
                    String content = "";
                    for(int i=0; i<neighbors.size(); i++) {
                        content += neighbors.get(i).getInfo().getLocalName();
                        if(i<neighbors.size()-1)
                            content += "#";
                    }
                    reply.setContent(content);
                    myAgent.send(reply);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    }
}

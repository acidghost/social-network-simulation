package it.uniba.di.itps.SNVSimulation.sn;

import it.uniba.di.itps.SNVSimulation.Simulation;
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
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.ListenableUndirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by acidghost on 13/09/14.
 */
public class SocialNetwork extends Agent {
    private ListenableUndirectedGraph<AID, DefaultEdge> graph;
    private static final AID SIMAGENT = new AID("SimAgent", AID.ISLOCALNAME);

    private Logger logger = jade.util.Logger.getMyLogger(this.getClass().getName());
    private GraphGUI gui;

    @Override
    protected void setup() {
        graph = new ListenableUndirectedGraph<AID, DefaultEdge>(DefaultEdge.class);
        gui = new GraphGUI(new JGraphModelAdapter<AID, DefaultEdge>(graph));
        gui.setVisible(true);

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
                AID newAgent = new AID(msg.getContent(), AID.ISLOCALNAME);
                graph.addVertex(newAgent);
                gui.positionNode(newAgent);
                logger.info("Added node...");
                if(graph.vertexSet().size() == Simulation.N_AGENTS) {
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
                graph.addEdge(new AID(nodes[0], AID.ISLOCALNAME), new AID(nodes[1], AID.ISLOCALNAME));
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
                List<AID> neighbors = new ArrayList<AID>();
                try {
                    Set<DefaultEdge> edges = graph.edgesOf(sender);
                    for(DefaultEdge edge : edges) {
                        AID a = graph.getEdgeSource(edge);
                        if (a.equals(sender)) {
                            a = graph.getEdgeTarget(edge);
                        }
                        neighbors.add(a);
                    }
                    ACLMessage reply = msg.createReply();
                    reply.setConversationId(msg.getConversationId());
                    String content = "";
                    for(int i=0; i<neighbors.size(); i++) {
                        content += neighbors.get(i).getLocalName();
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

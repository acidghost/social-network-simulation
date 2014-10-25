package it.uniba.di.itps.SNVSimulation.sn.graph;

import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableUndirectedGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by acidghost on 10/10/14.
 */
public class PlainGraph implements Graph {

    private ListenableUndirectedGraph<Node, DefaultEdge> graph;
    private GraphGUI gui;

    private Logger logger = Logger.getLogger(getClass().getSimpleName());

    public PlainGraph() {
        graph = new ListenableUndirectedGraph<Node, DefaultEdge>(DefaultEdge.class);
        gui = new GraphGUI(new JGraphModelAdapter<Node, DefaultEdge>(graph));
        gui.setVisible(true);
    }

    @Override
    public void addNode(Node node) {
        graph.addVertex(node);
        gui.positionNode(node);
    }

    @Override
    public void addEdge(Node node1, Node node2) {
        //  TODO check error here, thrown:
        //  ***  Uncaught Exception for agent SNAgent  ***
        //  java.lang.IllegalArgumentException: no such vertex in graph
        if(getNodes().contains(node1))
            logger.info("CONTAINS NODE1");
        if(getNodes().contains(node2))
            logger.info("CONTAINS NODE2");
        graph.addEdge(node1, node2);
    }

    @Override
    public List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<Node>();
        Set<DefaultEdge> edges = graph.edgesOf(node);
        for(DefaultEdge edge : edges) {
            Node a = graph.getEdgeSource(edge);
            if (a.equals(node)) {
                a = graph.getEdgeTarget(edge);
            }
            neighbors.add(a);
        }
        return neighbors;
    }

    @Override
    public List<Node> getNodes() {
        List<Node> nodes = new ArrayList<Node>();
        for(Node a : graph.vertexSet()) {
            nodes.add(a);
        }
        return nodes;
    }
}

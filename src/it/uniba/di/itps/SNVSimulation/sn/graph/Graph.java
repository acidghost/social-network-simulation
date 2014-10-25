package it.uniba.di.itps.SNVSimulation.sn.graph;

import java.util.List;

/**
 * Created by acidghost on 10/10/14.
 */
public interface Graph {

    public void addNode(Node node);
    public void addEdge(Node node1, Node node2);
    public List<Node> getNeighbors(Node node);
    public List<Node> getNodes();

}

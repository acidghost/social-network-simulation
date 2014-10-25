package it.uniba.di.itps.SNVSimulation.sn.graph;

import jade.core.AID;

/**
 * Created by acidghost on 10/10/14.
 */
public class Node implements Comparable {

    private AID info;

    public Node(AID info) {
        this.info = info;
    }

    public AID getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return info.getLocalName();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Node) {
            Node n = (Node) obj;
            return info.getName().equals(n.getInfo().getName());
        }
        return false;
    }

    @Override
    public int compareTo(Object o) {
        return equals(o) ? 1 : 0;
    }
}

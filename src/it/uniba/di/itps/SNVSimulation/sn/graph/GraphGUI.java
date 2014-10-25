package it.uniba.di.itps.SNVSimulation.sn.graph;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by acidghost on 14/09/14.
 */
public class GraphGUI extends JFrame {
    private static final Color DEFAULT_BG_COLOR = Color.decode( "#cccccc" );
    private static final Dimension DEFAULT_SIZE = new Dimension( 800, 800 );

    private JGraphModelAdapter<Node, DefaultEdge> adapter;
    private JGraph graph;

    public GraphGUI(JGraphModelAdapter<Node, DefaultEdge> adapter) {
        this.adapter = adapter;
        graph = new JGraph(this.adapter);
        adjustDisplaySettings(graph);
        getContentPane().add(graph);
        setSize(DEFAULT_SIZE);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void adjustDisplaySettings( JGraph jg ) {
        jg.setPreferredSize( DEFAULT_SIZE );
        Color c = DEFAULT_BG_COLOR;
        jg.setBackground( c );
    }

    public void positionNode(Node node) {
        DefaultGraphCell cell = adapter.getVertexCell(node);
        Map attr = cell.getAttributes();
        Rectangle2D b = GraphConstants.getBounds(attr);
        GraphConstants.setBounds( attr, new Rectangle((int) (Math.random()*DEFAULT_SIZE.getWidth()), (int) (Math.random()*DEFAULT_SIZE.getHeight()), (int) b.getWidth(), (int) b.getHeight()) );
        Map cellAttr = new HashMap();
        cellAttr.put(cell, attr);
        adapter.edit(cellAttr, null, null, null);
    }
}

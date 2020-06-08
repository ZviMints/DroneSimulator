package simulator;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import models.Point;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import javax.swing.*;
import java.util.Set;

public class Graph {

	DefaultDirectedGraph<Point, DefaultEdge> g;

    public Graph() {
    	g = new DefaultDirectedGraph<>(DefaultEdge.class);
    }
    public void addVertex(Point name) {
        Point last_vertex = null;
        Set<Point> all = g.vertexSet();
        
        if(all.size() > 0) {
        	last_vertex = getLastElement(all);
        }
        g.addVertex(name);
        if(last_vertex != null) 
        	g.addEdge(last_vertex, name);
    }
    
    public Point getLastElement(Set<Point> c) {
    	Point last = null;
    	if(c.size() > 0) {
    		for(Point x : c) {
    			last = x;
    		}
    	}
        return last;
    }


    public DefaultDirectedGraph<Point, DefaultEdge> getGraph() {
        return g;
    }

    public void drawGraph() {
    	JFrame new_window = new JFrame();
    	new_window.setSize(500,500);
    	new_window.setTitle("simulator.Graph Viewer");
    	JGraphXAdapter<Point, DefaultEdge> graphAdapter = new JGraphXAdapter<Point, DefaultEdge>(g);

        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        new_window.add(new mxGraphComponent(graphAdapter));

        new_window.pack();
        new_window.setLocationByPlatform(true);
        new_window.setVisible(true);
    }
}
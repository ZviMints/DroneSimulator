package simulator;

import algorithms.BaseAlgo;

import javax.swing.*;
import java.awt.*;


public class PainterGraph extends JComponent{
public GraphPath gt ;

	public PainterGraph(GraphPath other) {
		this.gt = other;
	}
	
	@Override
	public void paintComponent(Graphics g) {

	 	super.paintComponent(g);
		gt.paint(g);
	}




}

package simulator;

import java.awt.*;

import javax.swing.JComponent;

import algorithms.BaseAlgo;


public class Painter extends JComponent{

	BaseAlgo algo;
	
	public Painter(BaseAlgo algo) {
		this.algo = algo;
	}
	
	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);
		algo.paint(g);
	}




}

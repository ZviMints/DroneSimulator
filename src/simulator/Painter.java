package simulator;

import java.awt.*;

import javax.swing.JComponent;

import algorithms.Algorithm;


public class Painter extends JComponent{
	Algorithm algo;
	
	public Painter(Algorithm algo) {
		this.algo = algo;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		algo.paint(g);
	}
}

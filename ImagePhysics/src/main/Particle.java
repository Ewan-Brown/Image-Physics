package main;

import java.awt.Color;

public class Particle {

	double x;
	double y;
	double vX;
	double vY;
	long birth = System.nanoTime();
	public Color color;
	public Particle(int x, int y, double vX,double vY,int c){
		this.x = x;
		this.y = y;
		this.vY = vY;
		this.vX = vX;
		this.color = new Color(c);
	}
	
}

package main;

import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Main implements MouseListener {
	static ArrayList<Particle> particles = new ArrayList<>();
	static BufferedImage bI;
	static GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
			.getDefaultConfiguration();
	static VolatileImage vImage;
	static boolean[][] solid;
	public static JFrame frame;

	public static void main(String[] args) {
		FileDialog fd = new FileDialog((java.awt.Frame) null);
		fd.setTitle("Choose an image");
		fd.setVisible(true);
		File f = new File(fd.getDirectory() + fd.getFile());
		if (fd.getDirectory() == null || fd.getFile() == null)
			System.exit(0);
		try {
			bI = ImageIO.read(f);
		} catch (IOException | NullPointerException e) {
			String extension = "";

			int i = f.getName().lastIndexOf('.');
			if (i > 0)
				extension = f.getName().substring(i + 1);
			JOptionPane.showMessageDialog(new JFrame(), "The file chosen was not a readable image!\n" + "File type '."
					+ extension + "' is not an image you goof");
			System.exit(0);
		}
		frame = new JFrame("Image Physics");
		JPanel panel = new JPanel() {
			private static final long serialVersionUID = 476286940990464328L;

			public void paint(Graphics g) {
				if (vImage == null || vImage.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
					vImage = gc.createCompatibleVolatileImage(bI.getWidth(), bI.getHeight());
				}
				Graphics2D gV = (Graphics2D) vImage.getGraphics();
				g.drawImage(bI, 0, 0, null);
				for (int i = 0; i < particles.size(); i++) {
					Particle p = particles.get(i);
					if(p == null)continue;
					g.setColor(p.color);
					g.fillRect((int) p.x, (int) p.y, 1, 1);
				}
//				g.drawImage(vImage, 0, 0, null);
			}
		};
		frame.add(panel);
		frame.setSize(bI.getWidth() + 20, bI.getHeight() + 45);
		solid = new boolean[bI.getWidth()][bI.getHeight()];
		frame.setVisible(true);
		frame.addMouseListener(new Main());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// for (int j = 0; j < 100000; j++) {
		// addParticle(rand.nextInt(bI.getWidth()),
		// rand.nextInt(bI.getHeight()));
		// }
		while (true) {
			panel.repaint();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			updateParticles();
		}
	}

	static Random rand = new Random();

	public static boolean addParticle(int x, int y, double vX, double vY) {
		if (solid[x][y]) {
			return false;
		} else {
			solid[x][y] = true;
			// Particle p = new Particle(x, y, (Math.random() - 0.5) / 10,
			// (Math.random() - 0.5) / 10, bI.getRGB(x, y));
			Particle p = new Particle(x, y, vX, vY, bI.getRGB(x, y));
			bI.setRGB(x, y, 0);
			particles.add(p);
			return true;
		}
	}

	public static void killParticle(Particle p) {
		particles.remove(p);
		solid[(int) p.x][(int) p.y] = false;
		bI.setRGB((int) p.x, (int) p.y, p.color.getRGB());
	}

	public static void updateParticles() {
		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);
			if(p == null) continue;
			// Unused, use for randomish deaths later
//			long life = System.nanoTime() - p.birth;
//			long lifeMillis = life / 1000000L;
			p.x += p.vX;
			p.y += p.vY;
			if (p.x > bI.getWidth() - 1 || p.x < 0) {
				p.x -= p.vX;
				p.vX = -p.vX;
			}
			if (p.y > bI.getHeight() - 1 || p.y < 0) {
				p.y -= p.vY;
				p.vY = -p.vY;
			}
//			double y = 1D - Math.pow(1.5, (double) -lifeMillis / 10000D);
//			if (rand.nextDouble() < y) {
//				killParticle(p);
//			}
		}
	}

	static double flickMult = 0.1;

	public static void flick(int x1, int y1, int x2, int y2, long time) {
		float mills = time / 1000000L;
		float xD = (float) (x2 - x1);
		float yD = (float) (y2 - y1);
		float minX = Math.min(x1, x2);
		float maxX = Math.max(x1, x2);
		float minY = Math.min(y1, y2);
		float maxY = Math.max(y1, y2);
		float m = yD / xD;
		if (m < 0) {
			float f = minY;
			minY = maxY;
			maxY = f;
		}
		float b = y1 - m * x1;
		float mP = -1 / m;
		for (int x = 0; x < bI.getWidth(); x++) {
			for (int y = 0; y < bI.getHeight(); y++) {
				float xA = (float) x;
				float yA = (float) y;
				float bP = yA - mP * xA;
				float xI = -(b - bP) / (m - mP);
				float d = 0;
				boolean bo = false;
				if (xI > minX && xI < maxX) {
					bo = true;
					d = pDistance(xA, yA, x1, y1, x2, y2);
				}
				if (xI > maxX) {
					bo = true;
					d = (float) getDistance(xA, yA, maxX, maxY);
				}
				if (xI < minX) {
					bo = true;
					d = (float) getDistance(xA, yA, minX, minY);
				}
				if (bo) {
					d *= flickMult;
					double vX = xD / d / mills;
					double vY = yD / d / mills;
					double vTotal = getDistance(xD, yD, 0, 0);
					if (vTotal > 0.1) {
						addParticle(x, y, vX, vY);
					}
				}
			}
		}
		for(int i = 0; i < particles.size();i++){
			Particle p = particles.get(i);
			if(p == null)continue;
			float xA = (float) p.x;
			float yA = (float) p.y;
			float bP = yA - mP * xA;
			float xI = -(b - bP) / (m - mP);
			float d = 0;
			boolean bo = false;
			if (xI > minX && xI < maxX) {
				bo = true;
				d = pDistance(xA, yA, x1, y1, x2, y2);
			}
			if (xI > maxX) {
				bo = true;
				d = (float) getDistance(xA, yA, maxX, maxY);
			}
			if (xI < minX) {
				bo = true;
				d = (float) getDistance(xA, yA, minX, minY);
			}
			if (bo) {
				d *= flickMult;
				double vX = xD / d / mills;
				double vY = yD / d / mills;
				p.vX += vX;
				p.vY += vY;
			}
		}
	}

	public static float pDistance(float x, float y, float x1, float y1, float x2, float y2) {

		float A = x - x1; // position of point rel one end of line
		float B = y - y1;
		float C = x2 - x1; // vector along line
		float D = y2 - y1;
		float E = -D; // orthogonal vector
		float F = C;

		float dot = A * E + B * F;
		float len_sq = E * E + F * F;

		return (float) (Math.abs(dot) / Math.sqrt(len_sq));
	}

	public static double getDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e) {
		lastLeftClickPress = getMouseLocation();
		lastLeftClickTime = System.nanoTime();
	}

	static Point lastLeftClickPress = null;
	static long lastLeftClickTime = 0;

	public static Point getMouseLocation() {
		Point p = MouseInfo.getPointerInfo().getLocation();
		p.x -= frame.getLocationOnScreen().x;
		p.y -= frame.getLocationOnScreen().y;
		return p;
	}

	public void mouseReleased(MouseEvent e) {
		if (lastLeftClickPress != null) {
			Point local = getMouseLocation();
			long time = System.nanoTime() - lastLeftClickTime;
			Main.flick(lastLeftClickPress.x, lastLeftClickPress.y, local.x, local.y, time);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}

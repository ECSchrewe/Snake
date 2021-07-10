

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.*;

public class SnakeMain extends JFrame implements KeyListener, ActionListener {
    final int breite = 20; // x-Achse
    final int hoehe = 20; // y-Achse
    String vInfo = "Snake!  v1.01";
    long timer;
    int score;
    Schlange schlange;
    Kachel[][] felder = new Kachel[breite][hoehe];
    JPanel board, controls;
    JButton startStop;
    boolean paused = true;
    boolean gameOver = false;
    Thread t;
    LabelCanvas laengeCanvas, scoreCanvas;
    DecimalFormat df ;

    public SnakeMain() {
        this.setTitle(vInfo);
        df = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.GERMANY));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setSize(800, 900);
        this.setResizable(false);
        board = new JPanel();
        board.setSize(800, 800);
        
        board.setLayout(new GridLayout(hoehe, breite, 5, 5));
        this.addKeyListener(this);

        for (int i = 0; i < breite; i++) {
            felder[i] = new Kachel[hoehe];
            for (int j = 0; j < hoehe; j++) {
                felder[i][j] = new Kachel(i, j);
                board.add(felder[i][j]);
            }
        }

        controls = new JPanel();
        controls.setSize(800, 100);
        controls.setLayout(new FlowLayout());

        laengeCanvas = new LabelCanvas("            0 ");
        scoreCanvas = new LabelCanvas(" 0            ");
        startStop = new JButton("Start");
        startStop.addKeyListener(this);
        startStop.addActionListener(this);
        startStop.setActionCommand("startStop");
        controls.add(laengeCanvas);
        controls.add(startStop);
        controls.add(scoreCanvas);
        this.add(board, BorderLayout.CENTER);
        this.add(controls, BorderLayout.SOUTH);

        schlange = new Schlange(this);
        futterPlatzieren();
        this.pack();
        this.setVisible(true);
        t = new Thread(schlange);
        t.start();
    }

    class LabelCanvas extends Canvas {
        String text;
        Dimension calculatedSize;
        int verticalOffset;
        int descent;
        int height;
        int width;

        public LabelCanvas(String text) {
            this.text = text;
            setFont(new Font("Serif", Font.PLAIN, 14));
            recalc();

        }

        void recalc() {
            FontMetrics fontMetrics = getFontMetrics(getFont());
            descent = fontMetrics.getMaxDescent();
            height = fontMetrics.getHeight();
            width = fontMetrics.stringWidth(text);
            verticalOffset = height - descent;
            calculatedSize = new Dimension(width, height);
        }

        @Override
        public void paint(Graphics g) {
            recalc();
            g.drawString(text, 0, verticalOffset);
        }

        @Override
        public Dimension getPreferredSize() {
            recalc();
            return calculatedSize;
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
    }

    void spielNeustarten() {
        for (int i = 0; i < breite; i++) {
            for (int j = 0; j < hoehe; j++) {
                felder[i][j].schlangenPfad = false;
                felder[i][j].futter = false;
                felder[i][j].aktualisieren();
            }
        }
        schlange = new Schlange(this);
        futterPlatzieren();
        adjustScore(1);
        startStop.setText("Pause");
        gameOver = false;
        t = new Thread(schlange);
        t.start();
    }

    void adjustScore(int laenge) {
        String lae = "" + (laenge - 1);
        while (lae.length() < 13)
            lae = " " + lae;
        laengeCanvas.text = lae+" ";
        laengeCanvas.repaint();
        if (laenge == 1) {
            score = 0;
            timer = 0;
        } else if (laenge == 2) {
            score = 100;
            timer = System.currentTimeMillis();
        } else {
            double bonus = 10.0 - (System.currentTimeMillis() - timer) / 10000.0;
            if (bonus < 1.0) bonus = 1.0;
            score = score + (int) (100 * laenge * bonus);
            timer = System.currentTimeMillis();
        }
        String sc = df.format( score);
        while (sc.length() < 14)
            sc += " ";
        scoreCanvas.text =  sc;
        scoreCanvas.repaint();
    }

    void futterPlatzieren() {
        int futterX = -1;
        int futterY = -1;
        do {
            futterX = (int) (Math.random() * breite);
            futterY = (int) (Math.random() * hoehe);
        } while (felder[futterX][futterY].schlangenPfad && !felder[futterX][futterY].futter);

        felder[futterX][futterY].futter = true;
        felder[futterX][futterY].aktualisieren();
    }

    public static void main(String[] args) {
        SnakeMain f = new SnakeMain();
        JOptionPane pane = new JOptionPane("Willkommen bei Snake! \n \n         Steuerung mit \n                    W \n                A       S \n                     D");
        JDialog d = pane.createDialog(f,f.vInfo );
        d.setSize(180, 250);
        d.setLocation(300, 300);
        d.setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        c = Character.toLowerCase(c);
        if (c == 'w' && schlange.richtung != Richtung.Sued) schlange.richtung = Richtung.Nord;
        if (c == 's' && schlange.richtung != Richtung.Nord) schlange.richtung = Richtung.Sued;
        if (c == 'a' && schlange.richtung != Richtung.Ost) schlange.richtung = Richtung.West;
        if (c == 'd' & schlange.richtung != Richtung.West) schlange.richtung = Richtung.Ost;
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("startStop")) {
            if (gameOver) {
                this.spielNeustarten();
            }
            if (paused) {
                startStop.setText("Pause");
                paused = false;
            } else {
                startStop.setText("Start");
                paused = true;
            }
        }
    }

    class Kachel extends Canvas {

        Color color;
        boolean schlangenPfad;
        boolean futter;
        int x, y;

        Kachel(int x, int y) {
            this.x = x;
            this.y = y;
            schlangenPfad = false;
            futter = false;
            color = Color.green;
            this.setSize(10, 10);
            this.setBackground(color);
            this.setVisible(true);
        }

        public void aktualisieren() {
            if (futter)
                setBackground(Color.red);
            else if (schlangenPfad)
                setBackground(Color.black);
            else
                setBackground(Color.green);
        }

        @Override
        public String toString() {
            String s = "Pos: " + x + " / " + y + " sp " + schlangenPfad + " fu " + futter;
            return s;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (futter)
                this.setBackground(Color.RED);
            else if (schlangenPfad)
                this.setBackground(Color.black);
            else
                this.setBackground(Color.green);
        }
    }

    class Schlange implements Runnable {

        int laenge = 1;
        Richtung richtung;
        LinkedList<Kachel> pfad = new LinkedList<>();
        SnakeMain snake;

        Schlange(SnakeMain s) {
            this.snake = s;

            pfad.add(snake.felder[s.breite / 2][s.hoehe / 2]);
            pfad.getFirst().schlangenPfad = true;
            pfad.getFirst().aktualisieren();
            richtung = Richtung.Nord;

        }

        Kachel getNextKachel(Kachel standort, Richtung r) {
            int xNeu = (standort.x + r.h) % (snake.breite);
            if (xNeu < 0) xNeu = snake.breite - 1;
            int yNeu = (standort.y + r.v) % (snake.hoehe);
            if (yNeu < 0) yNeu = snake.hoehe - 1;
            Kachel k = snake.felder[xNeu][yNeu];
            return k;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    int delay = (int) (500 * (1.01 - (2.25 * laenge / 100.0)));
                    if (delay < 100)
                        delay = 100;
                    Thread.sleep(delay);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!snake.paused && !snake.gameOver) {
                    Kachel next = getNextKachel(pfad.getFirst(), richtung);
                    if (next.schlangenPfad) {
                        snake.gameOver = true;
                        snake.startStop.setText("GameOver");
                        try {
                            snake.t.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    next.schlangenPfad = true;
                    if (next.futter) {
                        this.laenge++;
                        next.futter = false;
                        snake.futterPlatzieren();
                        snake.adjustScore(laenge);
                    }
                    pfad.addFirst(next);
                    next.aktualisieren();
                    pfad.getLast().schlangenPfad = false;
                    pfad.getLast().aktualisieren();
                    if (pfad.size() > laenge) pfad.removeLast();
                }
            }
        }

    }

    enum Richtung {
        Nord(-1, 0), Sued(1, 0), Ost(0, 1), West(0, -1);

        public final int v;
        public final int h;

        Richtung(int h, int v) {
            this.h = h;
            this.v = v;
        }
    }
}

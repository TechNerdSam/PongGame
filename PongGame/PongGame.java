// PongGame.java
// This file contains the main class for the game, which initializes the window and the game panel.
// The game panel (GamePanel) is now a static nested class within PongGame.

import javax.swing.JFrame; // For creating the game window
import javax.swing.JPanel; // For creating a drawing panel
import javax.swing.JOptionPane; // For input dialog boxes
import java.awt.Dimension; // For defining window dimensions
import java.awt.Toolkit; // For getting screen dimensions
import java.awt.Color; // For defining colors
import java.awt.Font; // For defining fonts
import java.awt.Graphics; // For basic drawing operations
import java.awt.Graphics2D; // For more advanced 2D drawing operations
import java.awt.RenderingHints; // To improve rendering quality (anti-aliasing)
import java.awt.event.KeyEvent; // For handling keyboard events
import java.awt.event.KeyListener; // Interface for listening to keyboard events

// Imports for high score persistence
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Main class for the Pong game.
 * It initializes the JFrame window and contains the GamePanel class.
 * Designed for Java 6 and compatible with graphical environments.
 */
public class PongGame {

    // Window dimensions
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final String HIGH_SCORE_FILENAME = "pong_highscores.dat";
    private static final Logger LOGGER = Logger.getLogger(PongGame.class.getName());


    /**
     * Main entry point of the application.
     * @param args Command line arguments (not used here).
     */
    public static void main(String[] args) {
        // Initialize the graphical display module (JFrame)
        JFrame frame = new JFrame("Pong en Java 6 avec Highscores"); // UI Title in French
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        GamePanel gamePanel = new GamePanel(WINDOW_WIDTH, WINDOW_HEIGHT, HIGH_SCORE_FILENAME);
        frame.add(gamePanel);

        frame.pack(); // Sizes the frame so that all its contents are at or above their preferred sizes

        // Center the window on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - WINDOW_WIDTH) / 2;
        int y = (screenSize.height - WINDOW_HEIGHT) / 2;
        frame.setLocation(x, y);

        frame.setVisible(true);
        gamePanel.startGameLoop(); // Start the game loop after the panel is visible and added
    }

    // --- CLASS FOR HIGHSCORE ENTRIES ---
    /**
     * Represents an entry in the high score table.
     * Implements Serializable for persistence and Comparable for sorting.
     */
    static class HighScoreEntry implements Serializable, Comparable<HighScoreEntry> {
        private static final long serialVersionUID = 1L; // For serialization compatibility
        private final String playerName;
        private final int score;

        public HighScoreEntry(String playerName, int score) {
            this.playerName = playerName;
            this.score = score;
        }

        public String getPlayerName() {
            return playerName;
        }

        public int getScore() {
            return score;
        }

        // Sort by score in descending order
        @Override
        public int compareTo(HighScoreEntry other) {
            return Integer.compare(other.score, this.score);
        }

        @Override
        public String toString() {
            return playerName + " : " + score;
        }
    }

    /**
     * Main game panel for Pong.
     * Manages game logic, graphics rendering, and user interactions.
     */
    static class GamePanel extends JPanel implements Runnable, KeyListener {

        // --- Game States ---
        private enum GameState {
            MAIN_MENU,
            PLAYING,
            OPTIONS,
            PAUSED,
            GAME_OVER,
            SHOW_HIGHSCORES
        }
        private GameState currentGameState;
        private GameState previousStateBeforeOptions; // To return to the correct screen from options

        // --- Initialization and Configuration Variables ---
        private final int panelWidth;
        private final int panelHeight;
        private Thread gameThread;
        private volatile boolean isRunning; // volatile for thread safety

        // --- Game Logic Variables ---
        // Ball properties
        private int ballX, ballY;
        private int ballVelocityX, ballVelocityY;
        private static final int BALL_RADIUS = 10;
        private int initialBallSpeed = 3; // Default, can be changed in options

        // Paddle properties
        private int paddle1Y, paddle2Y;
        private static final int PADDLE_WIDTH = 15;
        private static final int PADDLE_HEIGHT = 100;
        private static final int PADDLE_SPEED = 7; // Increased speed for better responsiveness

        // Score
        private int player1Score = 0;
        private int player2Score = 0;
        private static final int MAX_SCORE_TO_WIN = 5;

        // Input flags
        private boolean p1UpPressed, p1DownPressed;
        private boolean p2UpPressed, p2DownPressed; // Ensure p2DownPressed is declared

        // Menu variables
        private int mainMenuSelection = 0;
        private String gameOverMessage = "";

        // Pause menu variables
        private int pauseMenuSelection = 0;

        // Configurable options variables
        private Color paddle1Color = Color.WHITE;
        private Color paddle2Color = Color.WHITE;

        // Default key bindings
        private int player1UpKey = KeyEvent.VK_W;
        private int player1DownKey = KeyEvent.VK_S;
        private int player2UpKey = KeyEvent.VK_UP;
        private int player2DownKey = KeyEvent.VK_DOWN;

        private int currentOptionSelection = 0;
        private boolean isRemappingKey = false;
        private int keyToRemapIndex = 0; // 0: P1_UP, 1: P1_DOWN, 2: P2_UP, 3: P2_DOWN

        private static final Color[] AVAILABLE_COLORS = {
                Color.WHITE, Color.BLUE, Color.RED, Color.GREEN,
                Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.ORANGE
        };
        private static final String[] OPTION_LABELS = {
                "Joueur 1 - Haut: ", "Joueur 1 - Bas: ",    // UI Text in French
                "Joueur 2 - Haut: ", "Joueur 2 - Bas: ",    // UI Text in French
                "Vitesse Balle: ", "Couleur Raquette 1: ", "Couleur Raquette 2: " // UI Text in French
        };
        private static final int MIN_BALL_SPEED = 1;
        private static final int MAX_BALL_SPEED = 10;


        // --- High Score Variables ---
        private List<HighScoreEntry> highScores;
        private static final int MAX_HIGHSCORES_TO_DISPLAY = 7;
        private final String highScoreFilename;
        private boolean highScorePendingCheck = false;
        private int scoreToPotentiallyRecord = 0;
        private int winningPlayerForHighScore = 0; // 1 for player 1, 2 for player 2

        public GamePanel(int width, int height, String highScoreFile) {
            this.panelWidth = width;
            this.panelHeight = height;
            this.highScoreFilename = highScoreFile;

            setPreferredSize(new Dimension(panelWidth, panelHeight));
            setBackground(Color.BLACK);
            setFocusable(true); // Crucial for KeyListener to work
            addKeyListener(this);

            loadHighScores();
            this.currentGameState = GameState.MAIN_MENU;
        }

        /**
         * Initializes or resets game elements to their starting positions and states.
         * Scores are not reset here, only ball and paddles.
         */
        private void initializeRound() {
            ballX = panelWidth / 2;
            ballY = panelHeight / 2;

            // Randomize initial ball direction
            double angle = Math.random() * Math.PI / 2 - Math.PI / 4; // -45 to +45 degrees
            if (Math.random() < 0.5) angle += Math.PI; // Add 180 degrees for other side

            ballVelocityX = (int) (initialBallSpeed * Math.cos(angle));
            ballVelocityY = (int) (initialBallSpeed * Math.sin(angle));

            // Ensure ball is moving
            if (ballVelocityX == 0) ballVelocityX = (Math.random() < 0.5) ? initialBallSpeed : -initialBallSpeed;
            if (ballVelocityY == 0) ballVelocityY = (Math.random() < 0.5) ? initialBallSpeed : -initialBallSpeed;


            paddle1Y = panelHeight / 2 - PADDLE_HEIGHT / 2;
            paddle2Y = panelHeight / 2 - PADDLE_HEIGHT / 2;
        }

        /**
         * Resets scores and initializes the first round. Called when starting a new game.
         */
        private void startNewGame() {
            player1Score = 0;
            player2Score = 0;
            initializeRound();
            currentGameState = GameState.PLAYING;
        }


        public void startGameLoop() {
            isRunning = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        public void stopGameLoop() { // Good practice, though not used directly in this version for exit
            isRunning = false;
            if (gameThread != null) {
                try {
                    gameThread.join(1000); // Wait for the thread to die, with a timeout
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Game thread interruption during stop", e);
                    Thread.currentThread().interrupt(); // Preserve interrupt status
                }
            }
        }

        @Override
        public void run() {
            long lastTime = System.nanoTime();
            double amountOfTicks = 60.0; // Target 60 FPS / UPS
            double nsPerTick = 1000000000 / amountOfTicks;
            double delta = 0;
            long timer = System.currentTimeMillis();
            int frames = 0;
            int updates = 0;

            while (isRunning) {
                long now = System.nanoTime();
                delta += (now - lastTime) / nsPerTick;
                lastTime = now;

                boolean shouldRender = false;
                while (delta >= 1) {
                    updateGameLogic();
                    updates++;
                    delta--;
                    shouldRender = true; // Render only after an update
                }

                // Sleep to free up CPU if possible
                try {
                    Thread.sleep(2); // Small sleep to avoid busy-waiting
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Game loop sleep interrupted", e);
                    Thread.currentThread().interrupt();
                    isRunning = false; // Stop if interrupted
                }


                if (shouldRender) {
                    repaint(); // Calls paintComponent
                    frames++;
                }

                // Optional: Print FPS and UPS once per second
                if (System.currentTimeMillis() - timer > 1000) {
                    timer += 1000;
                    // System.out.println("FPS: " + frames + " UPS: " + updates); // For debugging
                    frames = 0;
                    updates = 0;
                }
            }
        }

        private void updateGameLogic() {
            if (currentGameState != GameState.PLAYING) {
                return;
            }

            // Move paddles
            if (p1UpPressed) paddle1Y -= PADDLE_SPEED;
            if (p1DownPressed) paddle1Y += PADDLE_SPEED;
            if (p2UpPressed) paddle2Y -= PADDLE_SPEED;
            if (p2DownPressed) paddle2Y += PADDLE_SPEED;

            // Clamp paddles to screen bounds
            paddle1Y = Math.max(0, Math.min(paddle1Y, panelHeight - PADDLE_HEIGHT));
            paddle2Y = Math.max(0, Math.min(paddle2Y, panelHeight - PADDLE_HEIGHT));

            // Move ball
            ballX += ballVelocityX;
            ballY += ballVelocityY;

            // Ball collision with top/bottom walls
            if (ballY - BALL_RADIUS < 0) {
                ballVelocityY *= -1;
                ballY = BALL_RADIUS; // Correct position
            } else if (ballY + BALL_RADIUS > panelHeight) {
                ballVelocityY *= -1;
                ballY = panelHeight - BALL_RADIUS; // Correct position
            }


            // Ball collision with left paddle (Player 1)
            if (ballVelocityX < 0 && // Ball moving left
                ballX - BALL_RADIUS <= PADDLE_WIDTH && // Ball x is at or behind paddle front
                ballX - BALL_RADIUS > 0 && // Ball is not beyond the paddle's back edge (prevent sticking)
                ballY + BALL_RADIUS >= paddle1Y &&
                ballY - BALL_RADIUS <= paddle1Y + PADDLE_HEIGHT) {

                ballVelocityX *= -1;
                ballX = PADDLE_WIDTH + BALL_RADIUS; // Correct position to avoid sticking
                // Optional: Add slight angle based on where it hits the paddle
                // Optional: Increase ball speed
            }

            // Ball collision with right paddle (Player 2)
            if (ballVelocityX > 0 && // Ball moving right
                ballX + BALL_RADIUS >= panelWidth - PADDLE_WIDTH && // Ball x is at or beyond paddle front
                ballX + BALL_RADIUS < panelWidth && // Ball is not beyond the paddle's back edge
                ballY + BALL_RADIUS >= paddle2Y &&
                ballY - BALL_RADIUS <= paddle2Y + PADDLE_HEIGHT) {

                ballVelocityX *= -1;
                ballX = panelWidth - PADDLE_WIDTH - BALL_RADIUS; // Correct position
            }


            // Scoring
            if (ballX < 0) { // Player 2 scores
                player2Score++;
                checkGameEndOrNextRound();
            } else if (ballX > panelWidth) { // Player 1 scores
                player1Score++;
                checkGameEndOrNextRound();
            }
        }

        private void checkGameEndOrNextRound() {
            if (player1Score >= MAX_SCORE_TO_WIN) {
                gameOverMessage = "Joueur 1 GAGNE !"; // UI Text in French
                winningPlayerForHighScore = 1;
                scoreToPotentiallyRecord = player1Score; // Or a combined score if relevant
                highScorePendingCheck = true;
                currentGameState = GameState.GAME_OVER;
            } else if (player2Score >= MAX_SCORE_TO_WIN) {
                gameOverMessage = "Joueur 2 GAGNE !"; // UI Text in French
                winningPlayerForHighScore = 2;
                scoreToPotentiallyRecord = player2Score;
                highScorePendingCheck = true;
                currentGameState = GameState.GAME_OVER;
            } else {
                initializeRound(); // Start next round
            }
        }

        /**
         * Handles the high score check and prompt if pending.
         * This should be called when transitioning from GAME_OVER state.
         */
        private void processPendingHighScore() {
            if (highScorePendingCheck) {
                if (isHighScore(scoreToPotentiallyRecord)) {
                    promptAndSaveHighScore(scoreToPotentiallyRecord, winningPlayerForHighScore);
                }
                highScorePendingCheck = false;
                winningPlayerForHighScore = 0;
                scoreToPotentiallyRecord = 0;
            }
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Clears the panel
            Graphics2D g2d = (Graphics2D) g;

            // Enable anti-aliasing for smoother graphics
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


            switch (currentGameState) {
                case MAIN_MENU:
                    drawMainMenu(g2d);
                    break;
                case PLAYING:
                    drawGameElements(g2d);
                    break;
                case OPTIONS:
                    drawOptionsMenu(g2d);
                    break;
                case PAUSED:
                    drawGameElements(g2d); // Draw game state underneath
                    drawPauseMenu(g2d);
                    break;
                case GAME_OVER:
                    drawGameElements(g2d); // Optionally draw final game state
                    drawGameOverScreen(g2d);
                    break;
                case SHOW_HIGHSCORES:
                    drawHighScoresScreen(g2d);
                    break;
            }
            g2d.dispose(); // Dispose of graphics context when done in a paint cycle
        }

        private void drawGameElements(Graphics2D g2d) {
            // Draw ball
            g2d.setColor(Color.WHITE);
            g2d.fillOval(ballX - BALL_RADIUS, ballY - BALL_RADIUS, BALL_RADIUS * 2, BALL_RADIUS * 2);

            // Draw paddles
            g2d.setColor(paddle1Color);
            g2d.fillRect(0, paddle1Y, PADDLE_WIDTH, PADDLE_HEIGHT);
            g2d.setColor(paddle2Color);
            g2d.fillRect(panelWidth - PADDLE_WIDTH, paddle2Y, PADDLE_WIDTH, PADDLE_HEIGHT);

            // Draw center line
            g2d.setColor(Color.GRAY);
            for (int i = 0; i < panelHeight; i += 20) {
                g2d.fillRect(panelWidth / 2 - 2, i, 4, 10);
            }

            // Draw scores
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.drawString(String.valueOf(player1Score), panelWidth / 2 - 80, 50);
            g2d.drawString(String.valueOf(player2Score), panelWidth / 2 + 40, 50);
        }

        private void drawMainMenu(Graphics2D g2d) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 60));
            String title = "PONG"; // UI Text
            int titleWidth = g2d.getFontMetrics().stringWidth(title);
            g2d.drawString(title, (panelWidth - titleWidth) / 2, panelHeight / 4);

            String[] menuOptions = {"Jouer", "Highscores", "Options", "Quitter"}; // UI Text in French
            g2d.setFont(new Font("Arial", Font.PLAIN, 30));

            for (int i = 0; i < menuOptions.length; i++) {
                String optionText = menuOptions[i];
                int optionWidth = g2d.getFontMetrics().stringWidth(optionText);
                int yPos = panelHeight / 2 + i * 50;
                if (i == mainMenuSelection) {
                    g2d.setColor(Color.YELLOW);
                } else {
                    g2d.setColor(Color.WHITE);
                }
                g2d.drawString(optionText, (panelWidth - optionWidth) / 2, yPos);
            }
        }

        // --- HIGHSCORE METHODS ---

        @SuppressWarnings("unchecked") // For the cast of readObject()
        private void loadHighScores() {
            File file = new File(highScoreFilename);
            if (!file.exists() || file.length() == 0) {
                highScores = new ArrayList<>();
                return;
            }

            try (FileInputStream fis = new FileInputStream(file);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {

                Object rawObject = ois.readObject();
                if (rawObject instanceof List<?>) {
                    List<?> rawList = (List<?>) rawObject;
                    highScores = new ArrayList<>();
                    for (Object item : rawList) {
                        if (item instanceof HighScoreEntry) {
                            highScores.add((HighScoreEntry) item);
                        } else {
                            LOGGER.log(Level.WARNING, "Non-HighScoreEntry item found in high scores file. Item: " + item);
                            // Optionally, clear and start fresh if corruption is severe
                        }
                    }
                    if (highScores.size() > MAX_HIGHSCORES_TO_DISPLAY) { // Trim if oversized
                        Collections.sort(highScores);
                        while (highScores.size() > MAX_HIGHSCORES_TO_DISPLAY) {
                           highScores.remove(highScores.size() - 1);
                        }
                    }
                } else {
                    LOGGER.log(Level.WARNING, "High score file format error: Expected List, found " + rawObject.getClass().getName());
                    highScores = new ArrayList<>(); // Start with an empty list on format error
                }
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Error loading high scores (class not found)", e);
                highScores = new ArrayList<>(); // Fallback
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "I/O error loading high scores", e);
                highScores = new ArrayList<>(); // Fallback
            } catch (ClassCastException e) {
                LOGGER.log(Level.SEVERE, "Error loading high scores (incorrect object type in file)", e);
                highScores = new ArrayList<>(); // Fallback
            }

            // Ensure highScores is never null and is sorted
            if (highScores == null) {
                highScores = new ArrayList<>();
            }
            Collections.sort(highScores); // Sorts by score descending (due to Comparable)
        }

        private void saveHighScores() {
            try (FileOutputStream fos = new FileOutputStream(highScoreFilename);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(highScores);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "I/O error saving high scores", e);
            }
        }

        private boolean isHighScore(int score) {
            if (score <= 0) return false; // Don't record zero or negative scores
            if (highScores.size() < MAX_HIGHSCORES_TO_DISPLAY) {
                return true; // Always a high score if there's space
            }
            // Otherwise, check if better than the lowest current high score
            return score > highScores.get(highScores.size() - 1).getScore();
        }

        private void addHighScore(String playerName, int score) {
            highScores.add(new HighScoreEntry(playerName, score));
            Collections.sort(highScores); // Sorts by score descending
            while (highScores.size() > MAX_HIGHSCORES_TO_DISPLAY) {
                highScores.remove(highScores.size() - 1); // Remove the lowest score if list is too long
            }
            saveHighScores();
        }

        private void promptAndSaveHighScore(int scoreToSave, int playerNumber) {
            String promptMessage = "Joueur " + playerNumber + ", vous avez un score de " + scoreToSave + "!\n" + // UI Text
                                   "Entrez votre pseudo (max 10 caractères alphanumériques):"; // UI Text
            String playerName = JOptionPane.showInputDialog(this, promptMessage, "Nouveau Highscore!", JOptionPane.PLAIN_MESSAGE); // UI Text

            if (playerName != null) {
                playerName = playerName.trim();
                if (!playerName.isEmpty()) {
                    playerName = playerName.replaceAll("[^a-zA-Z0-9]", ""); // Keep only alphanumeric
                    if (playerName.length() > 10) {
                        playerName = playerName.substring(0, 10); // Truncate to 10 chars
                    }
                    if (playerName.isEmpty()) {
                        playerName = "Anonyme" + playerNumber; // Default if empty after filtering (UI Text)
                    }
                    addHighScore(playerName, scoreToSave);
                } else {
                    LOGGER.info("Empty player name, score not saved.");
                }
            } else {
                LOGGER.info("Player name input cancelled, score not saved.");
            }
        }

        private void drawHighScoresScreen(Graphics2D g2d) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            String title = "MEILLEURS SCORES"; // UI Text
            int titleWidth = g2d.getFontMetrics().stringWidth(title);
            g2d.drawString(title, (panelWidth - titleWidth) / 2, 80);

            g2d.setFont(new Font("Monospaced", Font.PLAIN, 28)); // Monospaced for alignment
            int startY = 150;
            int lineHeight = 35;

            if (highScores.isEmpty()) {
                String noScoresMsg = "Aucun score enregistré."; // UI Text
                g2d.drawString(noScoresMsg, panelWidth / 2 - g2d.getFontMetrics().stringWidth(noScoresMsg) / 2, startY);
            } else {
                // Headers
                String header = String.format("%-4s %-12s %5s", "RANG", "PSEUDO", "SCORE"); // UI Text
                g2d.drawString(header, panelWidth / 4, startY);
                g2d.drawLine(panelWidth / 4, startY + 10, panelWidth - panelWidth / 4, startY + 10);

                for (int i = 0; i < highScores.size(); i++) {
                    HighScoreEntry entry = highScores.get(i);
                    String rankStr = (i + 1) + ".";
                    String scoreLine = String.format("%-4s %-12s %5d",
                                                     rankStr,
                                                     entry.getPlayerName(),
                                                     entry.getScore());
                    g2d.drawString(scoreLine, panelWidth / 4, startY + (i + 1) * lineHeight + 10);
                }
            }

            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            String returnMsg = "Appuyez sur ESC pour revenir au menu principal"; // UI Text
            int returnMsgWidth = g2d.getFontMetrics().stringWidth(returnMsg);
            g2d.drawString(returnMsg, (panelWidth - returnMsgWidth) / 2, panelHeight - 50);
        }

        private void drawOptionsMenu(Graphics2D g2d) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            String title = "OPTIONS"; // UI Text
            int titleWidth = g2d.getFontMetrics().stringWidth(title);
            g2d.drawString(title, (panelWidth - titleWidth) / 2, 80);

            g2d.setFont(new Font("Arial", Font.PLAIN, 25));
            int startY = 150;
            int lineHeight = 40;

            for (int i = 0; i < OPTION_LABELS.length; i++) {
                String label = OPTION_LABELS[i];
                String value = "";
                switch (i) {
                    case 0: value = KeyEvent.getKeyText(player1UpKey); break;
                    case 1: value = KeyEvent.getKeyText(player1DownKey); break;
                    case 2: value = KeyEvent.getKeyText(player2UpKey); break;
                    case 3: value = KeyEvent.getKeyText(player2DownKey); break;
                    case 4: value = String.valueOf(initialBallSpeed); break;
                    case 5: value = getColorName(paddle1Color); break;
                    case 6: value = getColorName(paddle2Color); break;
                }

                if (i == currentOptionSelection) {
                    g2d.setColor(Color.YELLOW);
                    if (isRemappingKey && (i >= 0 && i <= 3)) {
                        value = "Appuyez sur une touche..."; // UI Text
                    }
                } else {
                    g2d.setColor(Color.WHITE);
                }
                g2d.drawString(label + value, panelWidth / 4, startY + i * lineHeight);
            }
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            String returnMsg = "Appuyez sur ESC pour revenir. Utilisez GAUCHE/DROITE pour changer les valeurs."; // UI Text
            int returnMsgWidth = g2d.getFontMetrics().stringWidth(returnMsg);
            g2d.drawString(returnMsg, (panelWidth - returnMsgWidth) / 2, panelHeight - 50);
        }

        private String getColorName(Color color) {
            if (Color.WHITE.equals(color)) return "Blanc";  // UI Text
            if (Color.BLUE.equals(color)) return "Bleu";    // UI Text
            if (Color.RED.equals(color)) return "Rouge";    // UI Text
            if (Color.GREEN.equals(color)) return "Vert";   // UI Text
            if (Color.YELLOW.equals(color)) return "Jaune"; // UI Text
            if (Color.CYAN.equals(color)) return "Cyan";    // UI Text
            if (Color.MAGENTA.equals(color)) return "Magenta";// UI Text
            if (Color.ORANGE.equals(color)) return "Orange"; // UI Text
            return "Inconnu"; // UI Text
        }

        private Color getNextColor(Color currentColor) {
            for (int i = 0; i < AVAILABLE_COLORS.length; i++) {
                if (AVAILABLE_COLORS[i].equals(currentColor)) {
                    return AVAILABLE_COLORS[(i + 1) % AVAILABLE_COLORS.length];
                }
            }
            return Color.WHITE; // Default fallback
        }
         private Color getPreviousColor(Color currentColor) {
            for (int i = 0; i < AVAILABLE_COLORS.length; i++) {
                if (AVAILABLE_COLORS[i].equals(currentColor)) {
                    return AVAILABLE_COLORS[(i - 1 + AVAILABLE_COLORS.length) % AVAILABLE_COLORS.length];
                }
            }
            return Color.WHITE; // Default fallback
        }


        private void drawGameOverScreen(Graphics2D g2d) {
            // Semi-transparent overlay
            g2d.setColor(new Color(0, 0, 0, 180)); // Darker overlay
            g2d.fillRect(0, 0, panelWidth, panelHeight);

            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            int msgWidth = g2d.getFontMetrics().stringWidth(gameOverMessage);
            g2d.drawString(gameOverMessage, (panelWidth - msgWidth) / 2, panelHeight / 2 - 50);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 25));
            String restartMsg = "Appuyez sur ENTREE pour rejouer"; // UI Text
            int restartMsgWidth = g2d.getFontMetrics().stringWidth(restartMsg);
            g2d.drawString(restartMsg, (panelWidth - restartMsgWidth) / 2, panelHeight / 2 + 50);

            String menuMsg = "Appuyez sur ESC pour le menu principal"; // UI Text
            int menuMsgWidth = g2d.getFontMetrics().stringWidth(menuMsg);
            g2d.drawString(menuMsg, (panelWidth - menuMsgWidth) / 2, panelHeight / 2 + 100);
        }

        private void drawPauseMenu(Graphics2D g2d) {
            // Semi-transparent overlay
            g2d.setColor(new Color(0, 0, 0, 180)); // Darker, more noticeable overlay
            g2d.fillRect(0, 0, panelWidth, panelHeight);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            String title = "PAUSE"; // UI Text
            int titleWidth = g2d.getFontMetrics().stringWidth(title);
            g2d.drawString(title, (panelWidth - titleWidth) / 2, panelHeight / 4);

            String[] pauseOptions = {"Reprendre", "Options", "Menu Principal", "Quitter"}; // UI Text
            g2d.setFont(new Font("Arial", Font.PLAIN, 30));

            for (int i = 0; i < pauseOptions.length; i++) {
                String optionText = pauseOptions[i];
                int optionWidth = g2d.getFontMetrics().stringWidth(optionText);
                int yPos = panelHeight / 2 + i * 50;
                if (i == pauseMenuSelection) {
                    g2d.setColor(Color.YELLOW);
                } else {
                    g2d.setColor(Color.WHITE);
                }
                g2d.drawString(optionText, (panelWidth - optionWidth) / 2, yPos);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();

            switch (currentGameState) {
                case MAIN_MENU:
                    handleMainMenuInput(keyCode);
                    break;
                case PLAYING:
                    handlePlayingInput(keyCode);
                    break;
                case PAUSED:
                    handlePausedInput(keyCode);
                    break;
                case OPTIONS:
                    handleOptionsInput(keyCode);
                    break;
                case GAME_OVER:
                    handleGameOverInput(keyCode);
                    break;
                case SHOW_HIGHSCORES:
                    handleShowHighScoresInput(keyCode);
                    break;
            }
        }

        private void handleMainMenuInput(int keyCode) {
            String[] menuOptions = {"Jouer", "Highscores", "Options", "Quitter"};
            if (keyCode == KeyEvent.VK_UP) {
                mainMenuSelection = (mainMenuSelection - 1 + menuOptions.length) % menuOptions.length;
            } else if (keyCode == KeyEvent.VK_DOWN) {
                mainMenuSelection = (mainMenuSelection + 1) % menuOptions.length;
            } else if (keyCode == KeyEvent.VK_ENTER) {
                switch (mainMenuSelection) {
                    case 0: // Jouer
                        startNewGame();
                        break;
                    case 1: // Highscores
                        currentGameState = GameState.SHOW_HIGHSCORES;
                        break;
                    case 2: // Options
                        previousStateBeforeOptions = GameState.MAIN_MENU;
                        currentGameState = GameState.OPTIONS;
                        currentOptionSelection = 0;
                        isRemappingKey = false;
                        break;
                    case 3: // Quitter
                        System.exit(0);
                        break;
                }
            }
        }

        private void handlePlayingInput(int keyCode) {
            if (keyCode == player1UpKey) p1UpPressed = true;
            if (keyCode == player1DownKey) p1DownPressed = true;
            if (keyCode == player2UpKey) p2UpPressed = true;
            if (keyCode == player2DownKey) p2DownPressed = true;
            if (keyCode == KeyEvent.VK_ESCAPE) {
                currentGameState = GameState.PAUSED;
                pauseMenuSelection = 0; // Reset pause menu selection
            }
        }

        private void handlePausedInput(int keyCode) {
            String[] pauseOptions = {"Reprendre", "Options", "Menu Principal", "Quitter"};
            if (keyCode == KeyEvent.VK_UP) {
                pauseMenuSelection = (pauseMenuSelection - 1 + pauseOptions.length) % pauseOptions.length;
            } else if (keyCode == KeyEvent.VK_DOWN) {
                pauseMenuSelection = (pauseMenuSelection + 1) % pauseOptions.length;
            } else if (keyCode == KeyEvent.VK_ENTER) {
                switch (pauseMenuSelection) {
                    case 0: // Reprendre
                        currentGameState = GameState.PLAYING;
                        break;
                    case 1: // Options
                        previousStateBeforeOptions = GameState.PAUSED;
                        currentGameState = GameState.OPTIONS;
                        currentOptionSelection = 0;
                        isRemappingKey = false;
                        break;
                    case 2: // Menu Principal
                        // No high score check here as game is paused, not finished.
                        currentGameState = GameState.MAIN_MENU;
                        mainMenuSelection = 0;
                        break;
                    case 3: // Quitter
                        System.exit(0);
                        break;
                }
            } else if (keyCode == KeyEvent.VK_ESCAPE) { // Resume game with ESC from pause
                currentGameState = GameState.PLAYING;
            }
        }

        private void handleOptionsInput(int keyCode) {
            if (isRemappingKey) {
                if (keyCode != KeyEvent.VK_ESCAPE && keyCode != KeyEvent.VK_ENTER) { // Any other key
                    switch (keyToRemapIndex) {
                        case 0: player1UpKey = keyCode; break;
                        case 1: player1DownKey = keyCode; break;
                        case 2: player2UpKey = keyCode; break;
                        case 3: player2DownKey = keyCode; break;
                    }
                    isRemappingKey = false;
                } else if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_ENTER) { // Cancel remapping with ESC or ENTER
                    isRemappingKey = false;
                }
            } else { // Not remapping a key, navigate options
                if (keyCode == KeyEvent.VK_UP) {
                    currentOptionSelection = (currentOptionSelection - 1 + OPTION_LABELS.length) % OPTION_LABELS.length;
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    currentOptionSelection = (currentOptionSelection + 1) % OPTION_LABELS.length;
                } else if (keyCode == KeyEvent.VK_ENTER) {
                    if (currentOptionSelection >= 0 && currentOptionSelection <= 3) { // Key remapping options
                        keyToRemapIndex = currentOptionSelection;
                        isRemappingKey = true;
                    } else if (currentOptionSelection == 4) { // Ball speed (Enter might cycle, or use Left/Right)
                         initialBallSpeed = (initialBallSpeed % MAX_BALL_SPEED) + MIN_BALL_SPEED; // Cycle through speeds
                    } else if (currentOptionSelection == 5) { // Paddle 1 color
                        paddle1Color = getNextColor(paddle1Color);
                    } else if (currentOptionSelection == 6) { // Paddle 2 color
                        paddle2Color = getNextColor(paddle2Color);
                    }
                } else if (keyCode == KeyEvent.VK_LEFT) {
                    if (currentOptionSelection == 4) { // Ball speed
                        initialBallSpeed = Math.max(MIN_BALL_SPEED, initialBallSpeed - 1);
                    } else if (currentOptionSelection == 5) { // Paddle 1 color
                        paddle1Color = getPreviousColor(paddle1Color);
                    } else if (currentOptionSelection == 6) { // Paddle 2 color
                        paddle2Color = getPreviousColor(paddle2Color);
                    }
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                     if (currentOptionSelection == 4) { // Ball speed
                        initialBallSpeed = Math.min(MAX_BALL_SPEED, initialBallSpeed + 1);
                    } else if (currentOptionSelection == 5) { // Paddle 1 color
                        paddle1Color = getNextColor(paddle1Color);
                    } else if (currentOptionSelection == 6) { // Paddle 2 color
                        paddle2Color = getNextColor(paddle2Color);
                    }
                }
                else if (keyCode == KeyEvent.VK_ESCAPE) {
                    currentGameState = previousStateBeforeOptions; // Return to Main Menu or Pause Menu
                    // Reset selections for those menus if needed
                    if (currentGameState == GameState.MAIN_MENU) mainMenuSelection = 0;
                    if (currentGameState == GameState.PAUSED) pauseMenuSelection = 0;
                    isRemappingKey = false; // Ensure remapping is cancelled
                }
            }
        }

        private void handleGameOverInput(int keyCode) {
            processPendingHighScore(); // Process any pending high score first

            if (keyCode == KeyEvent.VK_ENTER) {
                startNewGame();
            } else if (keyCode == KeyEvent.VK_ESCAPE) {
                currentGameState = GameState.MAIN_MENU;
                mainMenuSelection = 0;
            }
        }

        private void handleShowHighScoresInput(int keyCode) {
            if (keyCode == KeyEvent.VK_ESCAPE) {
                currentGameState = GameState.MAIN_MENU;
                mainMenuSelection = 0;
            }
        }


        @Override
        public void keyReleased(KeyEvent e) {
            int keyCode = e.getKeyCode();
            // Only handle releases if in PLAYING state to prevent interference
            if (currentGameState == GameState.PLAYING) {
                if (keyCode == player1UpKey) p1UpPressed = false;
                if (keyCode == player1DownKey) p1DownPressed = false;
                if (keyCode == player2UpKey) p2UpPressed = false;
                if (keyCode == player2DownKey) p2DownPressed = false;
            }
        }

        @Override
        public void keyTyped(KeyEvent e) { /* Not used, but required by KeyListener */ }
    }
}

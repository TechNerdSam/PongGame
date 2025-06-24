// PongGame.java
// This file contains the main class for the game, which initializes the window and the game panel.
// The game panel (GamePanel) is now a static nested class within PongGame.

import javax.swing.JFrame; // For creating the game window / Pour créer la fenêtre du jeu
import javax.swing.JPanel; // For creating a drawing panel / Pour créer un panneau de dessin
import javax.swing.JOptionPane; // For input dialog boxes / Pour les boîtes de dialogue d'entrée
import java.awt.Dimension; // For defining window dimensions / Pour définir les dimensions de la fenêtre
import java.awt.Toolkit; // For getting screen dimensions / Pour obtenir les dimensions de l'écran
import java.awt.Color; // For defining colors / Pour définir les couleurs
import java.awt.Font; // For defining fonts / Pour définir les polices
import java.awt.Graphics; // For basic drawing operations / Pour les opérations de dessin de base
import java.awt.Graphics2D; // For more advanced 2D drawing operations / Pour les opérations de dessin 2D avancées
import java.awt.RenderingHints; // To improve rendering quality (anti-aliasing) / Pour améliorer la qualité du rendu (anti-aliasing)
import java.awt.event.KeyEvent; // For handling keyboard events / Pour gérer les événements clavier
import java.awt.event.KeyListener; // Interface for listening to keyboard events / Interface pour écouter les événements clavier
import java.awt.RadialGradientPaint; // For radial gradients / Pour les dégradés radiaux
import java.awt.MultipleGradientPaint.CycleMethod; // For gradient cycle methods / Pour les méthodes de cycle de dégradé
import java.awt.geom.Point2D; // For 2D points / Pour les points 2D
import java.awt.GradientPaint; // For linear gradients / Pour les dégradés linéaires
import java.awt.AlphaComposite; // For fading effects / Pour les effets de fondu

// Imports for high score persistence / Importations pour la persistance des meilleurs scores
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
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Main class for the Pong game application.
 * It initializes the JFrame window and contains the GamePanel class,
 * which handles the core game logic and rendering.
 * Designed to be compatible with Java 6 for broader reach.
 * <p>
 * Classe principale de l'application du jeu Pong.
 * Elle initialise la fenêtre JFrame et contient la classe GamePanel,
 * qui gère la logique de jeu et le rendu.
 * Conçue pour être compatible avec Java 6 pour une plus large portée.
 */
public class PongGame {

    // --- Constants for Window Dimensions and File Paths ---
    // --- Constantes pour les Dimensions de la Fenêtre et les Chemins de Fichiers ---
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final String HIGH_SCORE_FILENAME = "pong_highscores.dat";
    private static final Logger LOGGER = Logger.getLogger(PongGame.class.getName());

    /**
     * Main entry point of the application.
     * Sets up the game window (JFrame) and starts the game panel.
     * <p>
     * Point d'entrée principal de l'application.
     * Configure la fenêtre de jeu (JFrame) et démarre le panneau de jeu.
     *
     * @param args Command line arguments (not used here). / Arguments de ligne de commande (non utilisés ici).
     */
    public static void main(String[] args) {
        // Initialize the graphical display module (JFrame)
        // Initialiser le module d'affichage graphique (JFrame)
        JFrame frame = new JFrame("Pong en Java 6 avec Highscores"); // UI Title in French / Titre de l'interface utilisateur en français
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false); // Window is not resizable / La fenêtre n'est pas redimensionnable

        // Create and add the game panel to the frame
        // Créer et ajouter le panneau de jeu au cadre
        GamePanel gamePanel = new GamePanel(WINDOW_WIDTH, WINDOW_HEIGHT, HIGH_SCORE_FILENAME);
        frame.add(gamePanel);

        frame.pack(); // Sizes the frame so that all its contents are at or above their preferred sizes
                      // Ajuste la taille du cadre pour que son contenu ait sa taille préférée ou plus
        // Center the window on the screen
        // Centrer la fenêtre sur l'écran
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - WINDOW_WIDTH) / 2;
        int y = (screenSize.height - WINDOW_HEIGHT) / 2;
        frame.setLocation(x, y);

        frame.setVisible(true); // Make the frame visible / Rendre le cadre visible
        gamePanel.startGameLoop(); // Start the game loop after the panel is visible and added
                                   // Démarrer la boucle de jeu après que le panneau soit visible et ajouté
    }

    /**
     * Represents an entry in the high score table.
     * Implements Serializable for persistence and Comparable for sorting by score in descending order.
     * <p>
     * Représente une entrée dans la table des meilleurs scores.
     * Implémente Serializable pour la persistance et Comparable pour le tri par score décroissant.
     */
    static class HighScoreEntry implements Serializable, Comparable<HighScoreEntry> {
        private static final long serialVersionUID = 1L; // For serialization compatibility / Pour la compatibilité de sérialisation
        private final String playerName; // (EN) Player's name. (FR) Nom du joueur.
        private final int score; // (EN) Player's score. (FR) Score du joueur.

        /**
         * Constructs a new HighScoreEntry.
         * <p>
         * Construit une nouvelle entrée de meilleur score.
         *
         * @param playerName (EN) The name of the player. (FR) Le nom du joueur.
         * @param score      (EN) The score achieved by the player. (FR) Le score obtenu par le joueur.
         */
        public HighScoreEntry(String playerName, int score) {
            this.playerName = playerName;
            this.score = score;
        }

        /**
         * Returns the player's name.
         * <p>
         * Retourne le nom du joueur.
         *
         * @return (EN) The player's name. (FR) Le nom du joueur.
         */
        public String getPlayerName() {
            return playerName;
        }

        /**
         * Returns the player's score.
         * <p>
         * Retourne le score du joueur.
         *
         * @return (EN) The player's score. (FR) Le score du joueur.
         */
        public int getScore() {
            return score;
        }

        /**
         * Compares this HighScoreEntry with another for sorting.
         * Sorts by score in descending order (higher scores come first).
         * <p>
         * Compare cette entrée de meilleur score avec une autre pour le tri.
         * Trie par score décroissant (les scores plus élevés viennent en premier).
         *
         * @param other (EN) The other HighScoreEntry to compare to. (FR) L'autre entrée de meilleur score à comparer.
         * @return (EN) A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object. (FR) Un entier négatif, zéro, ou un entier positif si cet objet est inférieur, égal ou supérieur à l'objet spécifié.
         */
        @Override
        public int compareTo(HighScoreEntry other) {
            return Integer.compare(other.score, this.score); // Descending order / Ordre décroissant
        }

        /**
         * Returns a string representation of the HighScoreEntry.
         * <p>
         * Retourne une représentation textuelle de l'entrée de meilleur score.
         *
         * @return (EN) A string in the format "PlayerName : Score". (FR) Une chaîne de caractères au format "NomDuJoueur : Score".
         */
        @Override
        public String toString() {
            return playerName + " : " + score;
        }
    }

    /**
     * Represents a single particle for visual effects (e.g., sparks, explosions).
     * Particles have position, velocity, life, and a color, fading out as they die.
     * <p>
     * Représente une seule particule pour les effets visuels (par exemple, étincelles, explosions).
     * Les particules ont une position, une vitesse, une durée de vie et une couleur, s'estompant à mesure qu'elles disparaissent.
     */
    static class Particle {
        private float x, y; // (EN) Current position of the particle. (FR) Position actuelle de la particule.
        private float vx, vy; // (EN) Velocity components of the particle. (FR) Composantes de la vitesse de la particule.
        private int life; // (EN) Remaining life of the particle (in updates). (FR) Durée de vie restante de la particule (en mises à jour).
        private Color color; // (EN) Color of the particle. (FR) Couleur de la particule.
        private float originalLife; // (EN) Initial life for calculating fade. (FR) Durée de vie initiale pour le calcul du fondu.

        /**
         * Constructs a new Particle.
         * <p>
         * Construit une nouvelle particule.
         *
         * @param x     (EN) Initial X coordinate. (FR) Coordonnée X initiale.
         * @param y     (EN) Initial Y coordinate. (FR) Coordonnée Y initiale.
         * @param vx    (EN) Initial velocity in X direction. (FR) Vitesse initiale dans la direction X.
         * @param vy    (EN) Initial velocity in Y direction. (FR) Vitesse initiale dans la direction Y.
         * @param life  (EN) Initial life duration of the particle. (FR) Durée de vie initiale de la particule.
         * @param color (EN) Color of the particle. (FR) Couleur de la particule.
         */
        public Particle(float x, float y, float vx, float vy, int life, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.originalLife = life;
            this.color = color;
        }

        /**
         * Updates the particle's position and reduces its life.
         * <p>
         * Met à jour la position de la particule et réduit sa durée de vie.
         *
         * @return (EN) True if the particle is still alive, false otherwise. (FR) Vrai si la particule est toujours vivante, faux sinon.
         */
        public boolean update() {
            x += vx;
            y += vy;
            life--;
            return life > 0; // Return true if still alive / Retourne vrai si toujours vivante
        }

        /**
         * Draws the particle on the screen, fading it out as its life diminishes.
         * <p>
         * Dessine la particule à l'écran, en la faisant s'estomper à mesure que sa durée de vie diminue.
         *
         * @param g2d (EN) The Graphics2D context to draw on. (FR) Le contexte Graphics2D sur lequel dessiner.
         */
        public void draw(Graphics2D g2d) {
            float alpha = life / originalLife; // Calculate alpha for fade effect / Calculer l'alpha pour l'effet de fondu
            if (alpha < 0) alpha = 0; // Ensure alpha doesn't go negative / Assurer que l'alpha ne devient pas négatif
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * alpha)));
            g2d.fillOval((int) (x - 2), (int) (y - 2), 4, 4); // Draw a small circle / Dessiner un petit cercle
        }
    }

    /**
     * Represents a temporary power-up object that can appear in the game.
     * Power-ups have a type, position, and duration for their effects.
     * <p>
     * Représente un objet de power-up temporaire qui peut apparaître dans le jeu.
     * Les power-ups ont un type, une position et une durée pour leurs effets.
     */
    static class PowerUp {
        /**
         * Defines the different types of power-ups available in the game.
         * <p>
         * Définit les différents types de power-ups disponibles dans le jeu.
         */
        enum PowerUpType {
            PADDLE_ENLARGE, // (EN) Increases paddle size. (FR) Augmente la taille de la raquette.
            MULTI_BALL,     // (EN) Spawns additional balls. (FR) Fait apparaître des balles supplémentaires.
            BALL_SPEED_UP,  // (EN) Increases ball speed. (FR) Augmente la vitesse de la balle.
            STICKY_PADDLE   // (EN) Ball sticks to paddle for a moment. (FR) La balle adhère à la raquette un instant.
        }

        private PowerUpType type; // (EN) The type of this power-up. (FR) Le type de ce power-up.
        private int x, y; // (EN) Position of the power-up on the screen. (FR) Position du power-up à l'écran.
        private boolean active; // (EN) True if the power-up is still active on screen. (FR) Vrai si le power-up est toujours actif à l'écran.
        private long durationEndTime; // (EN) System time when the power-up effect ends. (FR) Heure système à laquelle l'effet du power-up se termine.
        private long spawnTime; // (EN) System time when the power-up was spawned. (FR) Heure système à laquelle le power-up a été généré.
        // MODIFICATION: Increased displayDuration for power-ups to stay longer
        // MODIFICATION: Augmentation de la displayDuration pour que les power-ups restent plus longtemps
        private static final int DISPLAY_DURATION_MS = 15000; // (EN) How long it stays on screen if not collected (ms). (FR) Combien de temps il reste à l'écran s'il n'est pas collecté (ms).


        /**
         * Constructs a new PowerUp at a specified position with a given type.
         * <p>
         * Construit un nouveau PowerUp à une position spécifiée avec un type donné.
         *
         * @param type (EN) The type of power-up. (FR) Le type de power-up.
         * @param x    (EN) X coordinate for spawning. (FR) Coordonnée X pour l'apparition.
         * @param y    (EN) Y coordinate for spawning. (FR) Coordonnée Y pour l'apparition.
         */
        public PowerUp(PowerUpType type, int x, int y) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.active = true;
            this.spawnTime = System.currentTimeMillis();
        }

        /**
         * Checks if the power-up has expired (either collected or timed out on screen).
         * <p>
         * Vérifie si le power-up a expiré (soit collecté, soit le temps écoulé à l'écran).
         *
         * @return (EN) True if expired, false otherwise. (FR) Vrai si expiré, faux sinon.
         */
        public boolean isExpired() {
            return !active || (System.currentTimeMillis() - spawnTime > DISPLAY_DURATION_MS && durationEndTime == 0);
        }

        /**
         * Activates the power-up effect, setting its duration.
         * Once activated, the power-up is no longer visible on screen.
         * <p>
         * Active l'effet du power-up, en définissant sa durée.
         * Une fois activé, le power-up n'est plus visible à l'écran.
         *
         * @param durationMs (EN) The duration of the effect in milliseconds. (FR) La durée de l'effet en millisecondes.
         */
        public void activate(long durationMs) {
            this.active = false; // It's collected, no longer on screen / Il est collecté, plus à l'écran
            this.durationEndTime = System.currentTimeMillis() + durationMs;
        }

        /**
         * Checks if the power-up's effect is currently active.
         * <p>
         * Vérifie si l'effet du power-up est actuellement actif.
         *
         * @return (EN) True if the effect is active, false otherwise. (FR) Vrai si l'effet est actif, faux sinon.
         */
        public boolean isEffectActive() {
            return durationEndTime > 0 && System.currentTimeMillis() < durationEndTime;
        }

        /**
         * Draws the power-up on the screen if it is active.
         * <p>
         * Dessine le power-up à l'écran s'il est actif.
         *
         * @param g2d (EN) The Graphics2D context to draw on. (FR) Le contexte Graphics2D sur lequel dessiner.
         */
        public void draw(Graphics2D g2d) {
            if (!active) return; // Only draw if active on screen / Dessiner uniquement si actif à l'écran

            // (EN) Simple rendering for now, could be an image or more complex shape.
            // (FR) Rendu simple pour l'instant, pourrait être une image ou une forme plus complexe.
            g2d.setColor(Color.YELLOW);
            g2d.fillRect(x, y, 20, 20); // Placeholder square / Carré de remplacement

            g2d.setColor(Color.BLACK);
            String text = "";
            switch (type) {
                case PADDLE_ENLARGE:
                    text = "L";
                    break;
                case MULTI_BALL:
                    text = "M";
                    break;
                case BALL_SPEED_UP:
                    text = "S";
                    break;
                case STICKY_PADDLE:
                    text = "T";
                    break;
            }
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString(text, x + 5, y + 15);
        }
    }


    /**
     * Main game panel for Pong.
     * Manages game logic, graphics rendering, and user interactions.
     * Implements Runnable for the game loop and KeyListener for input.
     * <p>
     * Panneau de jeu principal pour Pong.
     * Gère la logique du jeu, le rendu graphique et les interactions utilisateur.
     * Implémente Runnable pour la boucle de jeu et KeyListener pour l'entrée.
     */
    static class GamePanel extends JPanel implements Runnable, KeyListener {

        // --- Game States ---
        // --- États du Jeu ---
        private enum GameState {
            MAIN_MENU,       // (EN) Main menu. (FR) Menu principal.
            PLAYING,         // (EN) Game in progress. (FR) Jeu en cours.
            OPTIONS,         // (EN) Options menu. (FR) Menu des options.
            PAUSED,          // (EN) Game is paused. (FR) Jeu en pause.
            GAME_OVER,       // (EN) Game has ended. (FR) Jeu terminé.
            SHOW_HIGHSCORES  // (EN) High scores display. (FR) Affichage des meilleurs scores.
        }
        private GameState currentGameState; // (EN) The current state of the game. (FR) L'état actuel du jeu.
        private GameState previousStateBeforeOptions; // (EN) Stores state to return to after options. (FR) Stocke l'état à restaurer après les options.

        // --- Game Modes ---
        // --- Modes de Jeu ---
        private enum GameMode {
            PLAYER_VS_PLAYER, // (EN) Two human players. (FR) Deux joueurs humains.
            PLAYER_VS_AI      // (EN) One human player vs. AI. (FR) Un joueur humain contre l'IA.
        }
        private GameMode currentGameMode; // (EN) The currently selected game mode. (FR) Le mode de jeu actuellement sélectionné.

        // --- Initialization and Configuration Variables ---
        // --- Variables d'Initialisation et de Configuration ---
        private final int panelWidth; // (EN) Width of the game panel. (FR) Largeur du panneau de jeu.
        private final int panelHeight; // (EN) Height of the game panel. (FR) Hauteur du panneau de jeu.
        private Thread gameThread; // (EN) Thread for the game loop. (FR) Thread pour la boucle de jeu.
        private volatile boolean isRunning; // (EN) Flag to control game loop execution. (FR) Drapeau pour contrôler l'exécution de la boucle de jeu.
        private Random random; // (EN) Random number generator for game elements. (FR) Générateur de nombres aléatoires pour les éléments du jeu.

        // --- Game Logic Constants and Variables ---
        // --- Constantes et Variables de Logique de Jeu ---
        private static final int BALL_RADIUS = 10; // (EN) Radius of the ball. (FR) Rayon de la balle.
        private int initialBallSpeed = 3; // (EN) Initial speed of the ball, adjustable. (FR) Vitesse initiale de la balle, ajustable.

        // Paddle properties / Propriétés des raquettes
        private int paddle1Y, paddle2Y; // (EN) Y-coordinates of paddle 1 and paddle 2. (FR) Coordonnées Y des raquettes 1 et 2.
        private static final int PADDLE_WIDTH = 15; // (EN) Fixed width of the paddles. (FR) Largeur fixe des raquettes.
        private static final int PADDLE_HEIGHT = 100; // (EN) Fixed initial height of the paddles. (FR) Hauteur initiale fixe des raquettes.
        private int paddle1Height = PADDLE_HEIGHT; // (EN) Current height of paddle 1 (can change with power-ups). (FR) Hauteur actuelle de la raquette 1 (peut changer avec les power-ups).
        private int paddle2Height = PADDLE_HEIGHT; // (EN) Current height of paddle 2 (can change with power-ups). (FR) Hauteur actuelle de la raquette 2 (peut changer avec les power-ups).
        private static final int PADDLE_SPEED = 7; // (EN) Movement speed of the paddles. (FR) Vitesse de déplacement des raquettes.

        // Score / Score
        private int player1Score = 0; // (EN) Score for player 1. (FR) Score du joueur 1.
        private int player2Score = 0; // (EN) Score for player 2. (FR) Score du joueur 2.
        private static final int MAX_SCORE_TO_WIN = 5; // (EN) Score needed to win the game. (FR) Score nécessaire pour gagner la partie.

        // Input flags / Drapeaux d'entrée
        private boolean p1UpPressed, p1DownPressed; // (EN) Flags for player 1 paddle movement. (FR) Drapeaux pour le mouvement de la raquette du joueur 1.
        private boolean p2UpPressed, p2DownPressed; // (EN) Flags for player 2 paddle movement. (FR) Drapeaux pour le mouvement de la raquette du joueur 2.

        // Menu and UI variables / Variables de menu et d'interface utilisateur
        private int mainMenuSelection = 0; // (EN) Current selected option in main menu. (FR) Option actuellement sélectionnée dans le menu principal.
        private String gameOverMessage = ""; // (EN) Message displayed on game over screen. (FR) Message affiché à l'écran de fin de jeu.
        private long gameOverTime; // (EN) Timestamp for game over, used for fade effect. (FR) Horodatage de fin de jeu, utilisé pour l'effet de fondu.

        private int pauseMenuSelection = 0; // (EN) Current selected option in pause menu. (FR) Option actuellement sélectionnée dans le menu pause.
        private long pauseStartTime; // (EN) Timestamp for pause, used for fade effect. (FR) Horodatage de la pause, utilisé pour l'effet de fondu.

        // Configurable options variables / Variables d'options configurables
        private Color paddle1Color = Color.WHITE; // (EN) Color of player 1's paddle. (FR) Couleur de la raquette du joueur 1.
        private Color paddle2Color = Color.WHITE; // (EN) Color of player 2's paddle. (FR) Couleur de la raquette du joueur 2.

        // Default key bindings / Liaisons de touches par défaut
        private int player1UpKey = KeyEvent.VK_W;
        private int player1DownKey = KeyEvent.VK_S;
        private int player2UpKey = KeyEvent.VK_UP;
        private int player2DownKey = KeyEvent.VK_DOWN;

        private int currentOptionSelection = 0; // (EN) Currently selected option in options menu. (FR) Option actuellement sélectionnée dans le menu des options.
        private boolean isRemappingKey = false; // (EN) Flag if a key is currently being remapped. (FR) Drapeau si une touche est en cours de remappage.
        private int keyToRemapIndex = 0; // (EN) Index of the key being remapped (0-3). (FR) Index de la touche en cours de remappage (0-3).

        // Constants for color options / Constantes pour les options de couleur
        private static final Color[] AVAILABLE_COLORS = {
                Color.WHITE, Color.BLUE, Color.RED, Color.GREEN,
                Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.ORANGE
        };
        // Labels for options menu / Étiquettes pour le menu des options
        private static final String[] OPTION_LABELS = {
                "Joueur 1 - Haut: ", "Joueur 1 - Bas: ",
                "Joueur 2 - Haut: ", "Joueur 2 - Bas: ",
                "Vitesse Balle: ", "Couleur Raquette 1: ", "Couleur Raquette 2: ",
                "Difficulte IA: "
        };
        private static final int MIN_BALL_SPEED = 1; // (EN) Minimum ball speed. (FR) Vitesse minimale de la balle.
        private static final int MAX_BALL_SPEED = 10; // (EN) Maximum ball speed. (FR) Vitesse maximale de la balle.

        // AI Difficulty Levels / Niveaux de Difficulté de l'IA
        private enum AIDifficulty {
            EASY,     // (EN) Easy difficulty. (FR) Difficulté facile.
            MEDIUM,   // (EN) Medium difficulty. (FR) Difficulté moyenne.
            HARD,     // (EN) Hard difficulty. (FR) Difficulté difficile.
            IMPOSSIBLE // (EN) Impossible difficulty (perfect AI). (FR) Difficulté impossible (IA parfaite).
        }
        private AIDifficulty aiDifficulty = AIDifficulty.MEDIUM; // (EN) Default AI difficulty. (FR) Difficulté de l'IA par défaut.

        // --- High Score Variables ---
        // --- Variables de Meilleurs Scores ---
        private List<HighScoreEntry> highScores; // (EN) List of high score entries. (FR) Liste des entrées de meilleurs scores.
        private static final int MAX_HIGHSCORES_TO_DISPLAY = 7; // (EN) Max number of high scores to show. (FR) Nombre max de meilleurs scores à afficher.
        private final String highScoreFilename; // (EN) Filename for high score persistence. (FR) Nom de fichier pour la persistance des meilleurs scores.
        private boolean highScorePendingCheck = false; // (EN) Flag if a new high score needs checking. (FR) Drapeau si un nouveau meilleur score doit être vérifié.
        private int scoreToPotentiallyRecord = 0; // (EN) Score to record if it's a high score. (FR) Score à enregistrer si c'est un meilleur score.
        private int winningPlayerForHighScore = 0; // (EN) Player who won for high score context. (FR) Joueur qui a gagné pour le contexte du meilleur score.

        // --- Particle System Variables ---
        // --- Variables du Système de Particules ---
        private List<Particle> particles; // (EN) List of active particles. (FR) Liste des particules actives.

        // --- Power-Up System Variables ---
        // --- Variables du Système de Power-Up ---
        private List<PowerUp> activePowerUps; // (EN) List of power-ups currently on screen. (FR) Liste des power-ups actuellement à l'écran.
        private static final long POWER_UP_SPAWN_INTERVAL_MIN = 15000; // (EN) Min time between power-up spawns (ms). (FR) Temps min entre les apparitions de power-ups (ms).
        private static final long POWER_UP_SPAWN_INTERVAL_MAX = 30000; // (EN) Max time between power-up spawns (ms). (FR) Temps max entre les apparitions de power-ups (ms).
        private long nextPowerUpSpawnTime; // (EN) System time for the next power-up spawn. (FR) Heure système pour la prochaine apparition de power-up.

        // --- Active Power-Up Effects (for players) ---
        // --- Effets de Power-Up Actifs (pour les joueurs) ---
        private static final long POWER_UP_EFFECT_DURATION_MS = 10000; // (EN) Duration of a power-up effect in milliseconds. (FR) Durée d'un effet de power-up en millisecondes.
        private long p1PaddleEnlargeEndTime = 0; // (EN) Time when P1 paddle enlarge effect ends. (FR) Heure de fin de l'effet d'agrandissement de la raquette P1.
        private long p2PaddleEnlargeEndTime = 0; // (EN) Time when P2 paddle enlarge effect ends. (FR) Heure de fin de l'effet d'agrandissement de la raquette P2.
        private long p1StickyPaddleEndTime = 0; // (EN) Time when P1 sticky paddle effect ends. (FR) Heure de fin de l'effet de raquette collante P1.
        private long p2StickyPaddleEndTime = 0; // (EN) Time when P2 sticky paddle effect ends. (FR) Heure de fin de l'effet de raquette collante P2.
        private int originalPaddleHeight; // (EN) Stores the original paddle height for power-up reset. (FR) Stocke la hauteur originale de la raquette pour la réinitialisation après power-up.

        // --- Multi-Ball System ---
        // --- Système Multi-Balles ---
        private List<Ball> balls; // (EN) List of all active balls in the game. (FR) Liste de toutes les balles actives dans le jeu.

        /**
         * Represents a single ball in the game.
         * Balls have position, velocity, and properties for specific power-up effects.
         * <p>
         * Représente une seule balle dans le jeu.
         * Les balles ont une position, une vitesse et des propriétés pour des effets de power-up spécifiques.
         */
        private class Ball {
            private int x, y; // (EN) Current position of the ball. (FR) Position actuelle de la balle.
            private int vx, vy; // (EN) Velocity components of the ball. (FR) Composantes de la vitesse de la balle.
            private boolean isSticky = false; // (EN) True if ball is currently sticky to a paddle. (FR) Vrai si la balle est actuellement collante à une raquette.
            private long stickyReleaseTime = 0; // (EN) Time when sticky effect wears off or ball released. (FR) Heure à laquelle l'effet collant se dissipe ou la balle est relâchée.

            /**
             * Constructs a new Ball with specified position and velocity.
             * <p>
             * Construit une nouvelle balle avec une position et une vitesse spécifiées.
             *
             * @param x  (EN) Initial X coordinate. (FR) Coordonnée X initiale.
             * @param y  (EN) Initial Y coordinate. (FR) Coordonnée Y initiale.
             * @param vx (EN) Initial velocity in X direction. (FR) Vitesse initiale dans la direction X.
             * @param vy (EN) Initial velocity in Y direction. (FR) Vitesse initiale dans la direction Y.
             */
            public Ball(int x, int y, int vx, int vy) {
                this.x = x;
                this.y = y;
                this.vx = vx;
                this.vy = vy;
            }
        }

        /**
         * Constructs the GamePanel.
         * Initializes game dimensions, input listeners, high scores, and default game state.
         * <p>
         * Construit le GamePanel.
         * Initialise les dimensions du jeu, les écouteurs d'entrée, les meilleurs scores et l'état de jeu par défaut.
         *
         * @param width         (EN) The width of the game panel. (FR) La largeur du panneau de jeu.
         * @param height        (EN) The height of the game panel. (FR) La hauteur du panneau de jeu.
         * @param highScoreFile (EN) The filename for high score persistence. (FR) Le nom de fichier pour la persistance des meilleurs scores.
         */
        public GamePanel(int width, int height, String highScoreFile) {
            this.panelWidth = width;
            this.panelHeight = height;
            this.highScoreFilename = highScoreFile;
            this.random = new Random();
            this.particles = new ArrayList<>();
            this.activePowerUps = new ArrayList<>();
            this.originalPaddleHeight = PADDLE_HEIGHT; // Assign the constant value / Assigner la valeur de la constante
            this.balls = new ArrayList<>();
            this.currentGameMode = GameMode.PLAYER_VS_AI; // Default mode on startup / Mode par défaut au démarrage

            setPreferredSize(new Dimension(panelWidth, panelHeight));
            setBackground(Color.BLACK); // Set background color / Définir la couleur de fond
            setFocusable(true); // Crucial for KeyListener to work / Crucial pour que KeyListener fonctionne
            addKeyListener(this); // Register key listener / Enregistrer l'écouteur de touches

            loadHighScores(); // Load high scores from file / Charger les meilleurs scores depuis le fichier
            this.currentGameState = GameState.MAIN_MENU; // Start in main menu / Commencer dans le menu principal

            setNextPowerUpSpawnTime(); // Initialize power-up spawn timer / Initialiser le minuteur d'apparition des power-ups
        }

        /**
         * Initializes or resets game elements to their starting positions and states for a new round.
         * Scores are not reset here.
         * <p>
         * Initialise ou réinitialise les éléments de jeu à leurs positions et états de départ pour une nouvelle manche.
         * Les scores ne sont pas réinitialisés ici.
         */
        private void initializeRound() {
            balls.clear(); // Clear existing balls / Effacer les balles existantes
            balls.add(new Ball(panelWidth / 2, panelHeight / 2, 0, 0)); // Add a single new ball at center / Ajouter une seule nouvelle balle au centre

            // Reset paddle heights (in case of power-up effects)
            // Réinitialiser les hauteurs des raquettes (en cas d'effets de power-up)
            paddle1Height = originalPaddleHeight;
            paddle2Height = originalPaddleHeight;
            p1PaddleEnlargeEndTime = 0;
            p2PaddleEnlargeEndTime = 0;
            p1StickyPaddleEndTime = 0;
            p2StickyPaddleEndTime = 0;

            // Randomize initial ball direction for the first ball
            // Randomiser la direction initiale de la première balle
            Ball mainBall = balls.get(0);
            double angle = random.nextDouble() * Math.PI / 2 - Math.PI / 4; // -45 to +45 degrees / -45 à +45 degrés
            if (random.nextBoolean()) angle += Math.PI; // Add 180 degrees for other side / Ajouter 180 degrés pour l'autre côté

            mainBall.vx = (int) (initialBallSpeed * Math.cos(angle));
            mainBall.vy = (int) (initialBallSpeed * Math.sin(angle));

            // Ensure ball is moving (prevent zero velocity at start)
            // S'assurer que la balle bouge (éviter une vitesse nulle au début)
            if (mainBall.vx == 0) mainBall.vx = (random.nextBoolean()) ? initialBallSpeed : -initialBallSpeed;
            if (mainBall.vy == 0) mainBall.vy = (random.nextBoolean()) ? initialBallSpeed : -initialBallSpeed;

            // Set paddles to center / Placer les raquettes au centre
            paddle1Y = panelHeight / 2 - paddle1Height / 2;
            paddle2Y = panelHeight / 2 - paddle2Height / 2;
        }

        /**
         * Resets scores and initializes the first round. Called when starting a new game.
         * <p>
         * Réinitialise les scores et initialise la première manche. Appelée au début d'une nouvelle partie.
         */
        private void startNewGame() {
            player1Score = 0;
            player2Score = 0;
            initializeRound();
            currentGameState = GameState.PLAYING;
            activePowerUps.clear(); // Clear power-ups for new game / Effacer les power-ups pour une nouvelle partie
            // lastPowerUpSpawnTime was removed as redundant, no need to set it here.
            // lastPowerUpSpawnTime a été supprimé car redondant, pas besoin de le définir ici.
            setNextPowerUpSpawnTime(); // Reset power-up spawn timer / Réinitialiser le minuteur d'apparition des power-ups
        }

        /**
         * Starts the game loop in a new thread.
         * <p>
         * Démarre la boucle de jeu dans un nouveau thread.
         */
        public void startGameLoop() {
            isRunning = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        /**
         * Stops the game loop gracefully.
         * This method attempts to join the game thread, waiting for it to terminate.
         * <p>
         * Arrête la boucle de jeu de manière élégante.
         * Cette méthode tente de joindre le thread de jeu, en attendant sa terminaison.
         */
        public void stopGameLoop() {
            isRunning = false; // Signal the loop to stop / Signaler à la boucle de s'arrêter
            if (gameThread != null) {
                try {
                    gameThread.join(1000); // Wait for the thread to die, with a timeout of 1 second
                                          // Attendre la terminaison du thread, avec un délai d'attente de 1 seconde
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Game thread interruption during stop", e); // Log interruption / Journaliser l'interruption
                    Thread.currentThread().interrupt(); // Preserve interrupt status / Préserver l'état d'interruption
                }
            }
        }

        /**
         * The main game loop, executed in a separate thread.
         * Manages game updates and rendering at a target FPS/UPS.
         * <p>
         * La boucle de jeu principale, exécutée dans un thread séparé.
         * Gère les mises à jour du jeu et le rendu à un FPS/UPS cible.
         */
        @Override
        public void run() {
            long lastTime = System.nanoTime();
            double amountOfTicks = 60.0; // Target 60 FPS / UPS / Cible 60 FPS / UPS
            double nsPerTick = 1000000000 / amountOfTicks; // Nanoseconds per tick / Nanosecondes par tick
            double delta = 0; // Time accumulated for updates / Temps accumulé pour les mises à jour
            long timer = System.currentTimeMillis(); // Timer for optional FPS/UPS display / Minuteur pour l'affichage optionnel FPS/UPS

            while (isRunning) {
                long now = System.nanoTime();
                delta += (now - lastTime) / nsPerTick;
                lastTime = now;

                boolean shouldRender = false;
                // Update game logic multiple times if necessary to catch up to target UPS
                // Mettre à jour la logique du jeu plusieurs fois si nécessaire pour atteindre l'UPS cible
                while (delta >= 1) {
                    updateGameLogic();
                    delta--;
                    shouldRender = true; // Render only after an update / Rendre uniquement après une mise à jour
                }

                // Small sleep to free up CPU and prevent busy-waiting
                // Petite pause pour libérer le CPU et éviter l'attente active
                try {
                    Thread.sleep(2); // Sleep for 2 milliseconds / Dormir 2 millisecondes
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Game loop sleep interrupted", e); // Log interruption / Journaliser l'interruption
                    Thread.currentThread().interrupt(); // Preserve interrupt status / Préserver l'état d'interruption
                    isRunning = false; // Stop if interrupted / Arrêter si interrompu
                }

                // Request a repaint if an update occurred
                // Demander un rafraîchissement si une mise à jour a eu lieu
                if (shouldRender) {
                    repaint(); // Calls paintComponent / Appelle paintComponent
                }

                // Optional: Print FPS and UPS once per second (for debugging)
                // Optionnel : Afficher le FPS et l'UPS une fois par seconde (pour le débogage)
                if (System.currentTimeMillis() - timer > 1000) {
                    timer += 1000;
                    // System.out.println("FPS: " + frames + " UPS: " + updates); // For debugging / Pour le débogage
                    // frames = 0; // Reset for next second / Réinitialiser pour la seconde suivante
                    // updates = 0; // Reset for next second / Réinitialiser pour la seconde suivante
                }
            }
        }

        /**
         * Contains the core game logic updates per tick.
         * Handles paddle movement, ball movement, collisions, power-ups, and scoring.
         * <p>
         * Contient les mises à jour de la logique de jeu par tick.
         * Gère le mouvement des raquettes, le mouvement de la balle, les collisions, les power-ups et le score.
         */
        private void updateGameLogic() {
            if (currentGameState != GameState.PLAYING) {
                return; // Only update game logic when playing / Mettre à jour la logique de jeu uniquement en mode jeu
            }

            // --- Update Paddle Heights and Sticky Effects if power-up expired ---
            // --- Mettre à jour les hauteurs des raquettes et les effets collants si le power-up a expiré ---
            handlePowerUpDurationEnd(System.currentTimeMillis());

            // --- Move Paddles ---
            // --- Déplacer les Raquettes ---
            movePlayer1Paddle();
            movePlayer2PaddleOrAI();

            // Clamp paddles to screen bounds / Limiter les raquettes aux bords de l'écran
            paddle1Y = Math.max(0, Math.min(paddle1Y, panelHeight - paddle1Height));
            paddle2Y = Math.max(0, Math.min(paddle2Y, panelHeight - paddle2Height));

            // --- Update Particles ---
            // --- Mettre à Jour les Particules ---
            updateParticles();

            // --- Power-Up Spawning and Expiration ---
            // --- Apparition et Expiration des Power-Ups ---
            managePowerUpSpawnsAndExpiration();

            // --- Move Balls and Check Collisions ---
            // --- Déplacer les Balles et Vérifier les Collisions ---
            handleBallMovementAndCollisions();

            // If all balls are gone and game not over, start new round (e.g., after all multi-balls score)
            // Si toutes les balles sont parties et que le jeu n'est pas terminé, démarrer une nouvelle manche
            if (balls.isEmpty() && currentGameState == GameState.PLAYING) {
                initializeRound();
            }
        }

        /**
         * Handles the expiration of power-up effects for paddles.
         * <p>
         * Gère l'expiration des effets de power-up pour les raquettes.
         *
         * @param currentTime (EN) The current system time in milliseconds. (FR) L'heure système actuelle en millisecondes.
         */
        private void handlePowerUpDurationEnd(long currentTime) {
            // Player 1 Paddle Enlarge effect / Effet d'agrandissement de la raquette du joueur 1
            if (p1PaddleEnlargeEndTime > 0 && currentTime >= p1PaddleEnlargeEndTime) {
                paddle1Height = originalPaddleHeight;
                p1PaddleEnlargeEndTime = 0;
            }
            // Player 2 Paddle Enlarge effect / Effet d'agrandissement de la raquette du joueur 2
            if (p2PaddleEnlargeEndTime > 0 && currentTime >= p2PaddleEnlargeEndTime) {
                paddle2Height = originalPaddleHeight;
                p2PaddleEnlargeEndTime = 0;
            }
            // Player 1 Sticky Paddle effect / Effet de raquette collante du joueur 1
            if (p1StickyPaddleEndTime > 0 && currentTime >= p1StickyPaddleEndTime) {
                // If a ball is still sticky, un-stick it and give it a push
                // Si une balle est toujours collante, la décoller et lui donner une poussée
                for (Ball ball : balls) {
                    if (ball.isSticky) {
                        ball.isSticky = false;
                        ball.vx = initialBallSpeed; // Push away from paddle / Pousser loin de la raquette
                        if (ball.vy == 0) ball.vy = random.nextBoolean() ? initialBallSpeed : -initialBallSpeed; // Ensure vertical movement / Assurer un mouvement vertical
                    }
                }
                p1StickyPaddleEndTime = 0;
            }
            // Player 2 Sticky Paddle effect / Effet de raquette collante du joueur 2
            if (p2StickyPaddleEndTime > 0 && currentTime >= p2StickyPaddleEndTime) {
                for (Ball ball : balls) {
                    if (ball.isSticky) {
                        ball.isSticky = false;
                        ball.vx = -initialBallSpeed; // Push away from paddle / Pousser loin de la raquette
                        if (ball.vy == 0) ball.vy = random.nextBoolean() ? initialBallSpeed : -initialBallSpeed;
                    }
                }
                p2StickyPaddleEndTime = 0;
            }
        }

        /**
         * Handles Player 1 paddle movement based on input flags.
         * <p>
         * Gère le mouvement de la raquette du joueur 1 en fonction des drapeaux d'entrée.
         */
        private void movePlayer1Paddle() {
            if (p1UpPressed) paddle1Y -= PADDLE_SPEED;
            if (p1DownPressed) paddle1Y += PADDLE_SPEED;
        }

        /**
         * Handles Player 2 paddle movement (human or AI) based on the current game mode.
         * <p>
         * Gère le mouvement de la raquette du joueur 2 (humain ou IA) en fonction du mode de jeu actuel.
         */
        private void movePlayer2PaddleOrAI() {
            if (currentGameMode == GameMode.PLAYER_VS_AI) {
                moveAIPaddle(); // AI controls paddle 2 / L'IA contrôle la raquette 2
            } else { // GameMode.PLAYER_VS_PLAYER
                if (p2UpPressed) paddle2Y -= PADDLE_SPEED;
                if (p2DownPressed) paddle2Y += PADDLE_SPEED;
            }
        }

        /**
         * Updates the state of all active particles, removing expired ones.
         * <p>
         * Met à jour l'état de toutes les particules actives, en supprimant celles qui ont expiré.
         */
        private void updateParticles() {
            for (int i = 0; i < particles.size(); i++) {
                if (!particles.get(i).update()) { // If particle is dead / Si la particule est morte
                    particles.remove(i); // Remove it / La supprimer
                    i--; // Adjust index due to removal / Ajuster l'index en raison de la suppression
                }
            }
        }

        /**
         * Manages the spawning and expiration of power-ups on the screen.
         * <p>
         * Gère l'apparition et l'expiration des power-ups à l'écran.
         */
        private void managePowerUpSpawnsAndExpiration() {
            // Spawn Power-Up if conditions met (time and no active power-ups)
            // Faire apparaître un Power-Up si les conditions sont remplies (temps et aucun power-up actif)
            if (System.currentTimeMillis() >= nextPowerUpSpawnTime && activePowerUps.isEmpty()) {
                spawnPowerUp();
                setNextPowerUpSpawnTime(); // Schedule next spawn / Planifier la prochaine apparition
            }

            // Update & Check Power-Ups for on-screen expiration
            // Mettre à jour et vérifier les Power-Ups pour l'expiration à l'écran
            for (int i = 0; i < activePowerUps.size(); i++) {
                PowerUp pu = activePowerUps.get(i);
                if (pu.isExpired()) { // If power-up on screen has expired / Si le power-up à l'écran a expiré
                    activePowerUps.remove(i);
                    i--;
                }
            }
        }


        /**
         * Handles movement, collisions (walls, paddles, power-ups), and scoring for all active balls.
         * <p>
         * Gère le mouvement, les collisions (murs, raquettes, power-ups) et le score pour toutes les balles actives.
         */
        private void handleBallMovementAndCollisions() {
            List<Ball> ballsToRemove = new ArrayList<>(); // Balls that scored or need removal / Balles qui ont marqué ou doivent être supprimées
            List<Ball> ballsToAdd = new ArrayList<>(); // New balls from multi-ball power-up / Nouvelles balles du power-up multi-balles

            for (Ball ball : balls) {
                // Handle sticky ball logic / Gérer la logique de la balle collante
                if (ball.isSticky) {
                    // Position ball on paddle / Positionner la balle sur la raquette
                    if (ball.x < panelWidth / 2) { // Sticky to P1 / Collante à P1
                        ball.x = PADDLE_WIDTH + BALL_RADIUS;
                        ball.y = paddle1Y + paddle1Height / 2;
                    } else { // Sticky to P2 / Collante à P2
                        ball.x = panelWidth - PADDLE_WIDTH - BALL_RADIUS;
                        ball.y = paddle2Y + paddle2Height / 2;
                    }
                    // Check if sticky effect duration has passed / Vérifier si la durée de l'effet collant est passée
                    if (ball.stickyReleaseTime > 0 && System.currentTimeMillis() >= ball.stickyReleaseTime) {
                        ball.isSticky = false;
                        // Give it an initial push away from the paddle / Lui donner une poussée initiale loin de la raquette
                        if (ball.x < panelWidth / 2) ball.vx = initialBallSpeed;
                        else ball.vx = -initialBallSpeed;
                        // Ensure it moves vertically if it was previously still (e.g. at collision point)
                        // S'assurer qu'elle bouge verticalement si elle était auparavant immobile (par exemple, au point de collision)
                        if (ball.vy == 0) ball.vy = random.nextBoolean() ? initialBallSpeed : -initialBallSpeed;
                        ball.stickyReleaseTime = 0;
                    }
                } else {
                    // Move ball based on velocity / Déplacer la balle en fonction de la vitesse
                    ball.x += ball.vx;
                    ball.y += ball.vy;
                }

                // Ball collision with top/bottom walls / Collision de la balle avec les murs supérieurs/inférieurs
                if (ball.y - BALL_RADIUS < 0) {
                    ball.vy *= -1;
                    ball.y = BALL_RADIUS; // Correct position to prevent sticking / Corriger la position pour éviter de coller
                    addParticles(ball.x, ball.y, 5, Color.BLUE); // Add particle effect / Ajouter un effet de particule
                } else if (ball.y + BALL_RADIUS > panelHeight) {
                    ball.vy *= -1;
                    ball.y = panelHeight - BALL_RADIUS; // Correct position / Corriger la position
                    addParticles(ball.x, ball.y, 5, Color.BLUE); // Add particle effect / Ajouter un effet de particule
                }

                // Ball collision with left paddle (Player 1)
                // Collision de la balle avec la raquette gauche (Joueur 1)
                if (ball.vx < 0 && // Ball moving left / Balle se déplaçant à gauche
                        ball.x - BALL_RADIUS <= PADDLE_WIDTH && // Ball x is at or behind paddle front / La position x de la balle est au niveau ou derrière le devant de la raquette
                        ball.x - BALL_RADIUS > 0 && // Ball is not beyond the paddle's back edge (prevent sticking)
                                                    // La balle n'est pas au-delà du bord arrière de la raquette (pour éviter de coller)
                        ball.y + BALL_RADIUS >= paddle1Y &&
                        ball.y - BALL_RADIUS <= paddle1Y + paddle1Height) {

                    if (p1StickyPaddleEndTime > 0) { // If sticky power-up active for P1 / Si le power-up collant est actif pour P1
                        ball.isSticky = true;
                        ball.stickyReleaseTime = System.currentTimeMillis() + 1000; // Stick for 1 second / Coller pendant 1 seconde
                    } else {
                        ball.vx *= -1; // Reverse horizontal velocity / Inverser la vitesse horizontale
                        // Add slight angle based on where it hits the paddle (center hit -> less angle, edge hit -> more angle)
                        // Ajouter un léger angle en fonction de l'endroit où elle touche la raquette (centre -> moins d'angle, bord -> plus d'angle)
                        double hitFactor = (ball.y - paddle1Y) / (double) paddle1Height; // 0.0 top, 1.0 bottom / 0.0 haut, 1.0 bas
                        ball.vy = (int) (initialBallSpeed * 1.5 * (hitFactor - 0.5)); // Adjust vertical velocity / Ajuster la vitesse verticale
                    }
                    ball.x = PADDLE_WIDTH + BALL_RADIUS; // Correct position to avoid sticking / Corriger la position pour éviter de coller
                    addParticles(ball.x - BALL_RADIUS, ball.y, 10, Color.WHITE); // Add particle effect / Ajouter un effet de particule
                }

                // Ball collision with right paddle (Player 2)
                // Collision de la balle avec la raquette droite (Joueur 2)
                if (ball.vx > 0 && // Ball moving right / Balle se déplaçant à droite
                        ball.x + BALL_RADIUS >= panelWidth - PADDLE_WIDTH && // Ball x is at or beyond paddle front / La position x de la balle est au niveau ou au-delà du devant de la raquette
                        ball.x + BALL_RADIUS < panelWidth && // Ball is not beyond the paddle's back edge / La balle n'est pas au-delà du bord arrière de la raquette
                        ball.y + BALL_RADIUS >= paddle2Y &&
                        ball.y - BALL_RADIUS <= paddle2Y + paddle2Height) {

                    if (p2StickyPaddleEndTime > 0) { // If sticky power-up active for P2 / Si le power-up collant est actif pour P2
                        ball.isSticky = true;
                        ball.stickyReleaseTime = System.currentTimeMillis() + 1000;
                    } else {
                        ball.vx *= -1; // Reverse horizontal velocity / Inverser la vitesse horizontale
                        double hitFactor = (ball.y - paddle2Y) / (double) paddle2Height;
                        ball.vy = (int) (initialBallSpeed * 1.5 * (hitFactor - 0.5));
                    }
                    ball.x = panelWidth - PADDLE_WIDTH - BALL_RADIUS; // Correct position / Corriger la position
                    addParticles(ball.x + BALL_RADIUS, ball.y, 10, Color.WHITE); // Add particle effect / Ajouter un effet de particule
                }

                // Ball collision with power-ups / Collision de la balle avec les power-ups
                // Iterate on a copy to avoid ConcurrentModificationException if power-ups are removed during iteration
                // Itérer sur une copie pour éviter ConcurrentModificationException si les power-ups sont supprimés pendant l'itération
                for (PowerUp pu : new ArrayList<>(activePowerUps)) {
                    // Check for collision with power-up bounding box
                    // Vérifier la collision avec la boîte englobante du power-up
                    if (pu.active && ball.x + BALL_RADIUS > pu.x && ball.x - BALL_RADIUS < pu.x + 20 &&
                            ball.y + BALL_RADIUS > pu.y && ball.y - BALL_RADIUS < pu.y + 20) {
                        applyPowerUpEffect(pu.type, ball, ballsToAdd); // Apply effect / Appliquer l'effet
                        pu.active = false; // Power-up collected / Power-up collecté
                        addParticles(pu.x + 10, pu.y + 10, 20, Color.GREEN); // Explosion of particles / Explosion de particules
                    }
                }

                // Scoring logic / Logique de score
                if (ball.x < 0) { // Player 2 scores / Le joueur 2 marque
                    player2Score++;
                    addParticles(panelWidth / 2, panelHeight / 2, 50, Color.GREEN); // Large score particle effect / Grand effet de particule de score
                    ballsToRemove.add(ball); // Mark ball for removal / Marquer la balle pour suppression
                    // Only check game end if this was the last ball, or if only one ball left after scoring
                    // Vérifier la fin du jeu uniquement si c'était la dernière balle, ou s'il ne reste qu'une seule balle après avoir marqué
                    if (balls.size() - ballsToRemove.size() == 0) checkGameEndOrNextRound();
                } else if (ball.x > panelWidth) { // Player 1 scores / Le joueur 1 marque
                    player1Score++;
                    addParticles(panelWidth / 2, panelHeight / 2, 50, Color.GREEN);
                    ballsToRemove.add(ball);
                    if (balls.size() - ballsToRemove.size() == 0) checkGameEndOrNextRound();
                }
            }

            // Remove and add balls after iterating to avoid ConcurrentModificationException
            // Supprimer et ajouter des balles après l'itération pour éviter ConcurrentModificationException
            balls.removeAll(ballsToRemove);
            balls.addAll(ballsToAdd);
        }

        /**
         * Implements the AI logic for paddle 2.
         * The AI predicts the ball's trajectory and adjusts its paddle position,
         * with adjustable difficulty levels affecting reaction time and accuracy.
         * <p>
         * Implémente la logique de l'IA pour la raquette 2.
         * L'IA prédit la trajectoire de la balle et ajuste la position de sa raquette,
         * avec des niveaux de difficulté ajustables affectant le temps de réaction et la précision.
         */
        private void moveAIPaddle() {
            if (balls.isEmpty()) return; // No ball to track / Aucune balle à suivre

            Ball mainBall = balls.get(0); // AI focuses on the first ball (can be improved for multiple balls)
                                         // L'IA se concentre sur la première balle (peut être amélioré pour plusieurs balles)
            int targetY = mainBall.y - paddle2Height / 2; // Default target: ball's current Y / Cible par défaut : Y actuel de la balle
            int paddleCenter = paddle2Y + paddle2Height / 2; // Current center of AI paddle / Centre actuel de la raquette de l'IA

            // AI Prediction - simple linear prediction
            // Prédiction de l'IA - prédiction linéaire simple
            // Predict where the ball will be when it reaches the paddle X position
            // Prédire où sera la balle lorsqu'elle atteindra la position X de la raquette
            double predictX = panelWidth - PADDLE_WIDTH - BALL_RADIUS; // Target X for collision / Cible X pour la collision
            double timeToHitPaddle = (predictX - mainBall.x) / mainBall.vx; // Time steps until collision / Étapes de temps jusqu'à la collision

            if (mainBall.vx > 0 && timeToHitPaddle > 0) { // Ball moving towards AI paddle and will reach it
                                                          // Balle se déplaçant vers la raquette de l'IA et l'atteindra
                // Adjust targetY based on anticipated ball Y position
                // Ajuster targetY en fonction de la position Y anticipée de la balle
                int predictedBallY = (int) (mainBall.y + mainBall.vy * timeToHitPaddle);
                targetY = predictedBallY - paddle2Height / 2; // Target center of paddle to hit ball center / Cible le centre de la raquette pour frapper le centre de la balle

                // Introduce "errors" and reaction time based on difficulty
                // Introduire des "erreurs" et un temps de réaction basés sur la difficulté
                double reactionDelayThreshold = 0; // Likelihood of skipping an update / Probabilité de sauter une mise à jour
                double inaccuracyFactor = 0; // How much off-center the AI aims / Dans quelle mesure l'IA vise mal

                switch (aiDifficulty) {
                    case EASY:
                        reactionDelayThreshold = 15 + random.nextInt(10); // Slower reaction (higher threshold) / Réaction plus lente (seuil plus élevé)
                        inaccuracyFactor = 0.4 + random.nextDouble() * 0.4; // More off-target aiming / Ciblage plus imprécis
                        break;
                    case MEDIUM:
                        reactionDelayThreshold = 5 + random.nextInt(5);
                        inaccuracyFactor = 0.1 + random.nextDouble() * 0.2;
                        break;
                    case HARD:
                        reactionDelayThreshold = 1 + random.nextInt(2);
                        inaccuracyFactor = random.nextDouble() * 0.05; // Almost perfect / Presque parfait
                        break;
                    case IMPOSSIBLE:
                        reactionDelayThreshold = 0; // Instant reaction / Réaction instantanée
                        inaccuracyFactor = 0; // Perfect aiming / Visée parfaite
                        break;
                }

                // Simulate reaction delay: AI might skip reacting in some frames
                // Simuler un délai de réaction : l'IA pourrait sauter la réaction dans certaines images
                if (random.nextInt(60) < reactionDelayThreshold) { // (EN) 60 updates per second, higher threshold means more skips.
                                                                  // (FR) 60 mises à jour par seconde, un seuil plus élevé signifie plus de sauts.
                    return; // AI doesn't react this tick / L'IA ne réagit pas à ce tick
                }

                // Apply inaccuracy: offset the target position slightly
                // Appliquer l'imprécision : décaler légèrement la position cible
                targetY += (random.nextBoolean() ? 1 : -1) * paddle2Height * inaccuracyFactor;
                // Clamp targetY to prevent aiming outside screen bounds
                // Limiter targetY pour éviter de viser en dehors des limites de l'écran
                targetY = Math.max(0, Math.min(targetY, panelHeight - paddle2Height));

            } else { // Ball moving away or no clear prediction, return to center
                     // Balle s'éloignant ou pas de prédiction claire, revenir au centre
                targetY = panelHeight / 2 - paddle2Height / 2;
            }

            // Move paddle towards target Y position
            // Déplacer la raquette vers la position Y cible
            if (paddleCenter < targetY) {
                paddle2Y += Math.min(PADDLE_SPEED, targetY - paddle2Y); // Move at PADDLE_SPEED, but not past target
                                                                       // Se déplacer à PADDLE_SPEED, mais pas au-delà de la cible
            } else if (paddleCenter > targetY) {
                paddle2Y -= Math.min(PADDLE_SPEED, paddle2Y - targetY);
            }
        }

        /**
         * Adds a specified number of particles at a given location with a base color.
         * Particles are given random velocities and limited lifespans.
         * <p>
         * Ajoute un nombre spécifié de particules à un endroit donné avec une couleur de base.
         * Les particules reçoivent des vitesses aléatoires et des durées de vie limitées.
         *
         * @param x         (EN) X coordinate for particle spawn. (FR) Coordonnée X pour l'apparition des particules.
         * @param y         (EN) Y coordinate for particle spawn. (FR) Coordonnée Y pour l'apparition des particules.
         * @param count     (EN) Number of particles to add. (FR) Nombre de particules à ajouter.
         * @param baseColor (EN) The base color of the particles. (FR) La couleur de base des particules.
         */
        private void addParticles(int x, int y, int count, Color baseColor) {
            for (int i = 0; i < count; i++) {
                float angle = (float) (random.nextDouble() * 2 * Math.PI); // Random direction / Direction aléatoire
                float speed = 0.5f + random.nextFloat() * 2; // Random speed / Vitesse aléatoire
                int life = 20 + random.nextInt(30); // Random lifespan / Durée de vie aléatoire
                particles.add(new Particle(x, y, (float) (speed * Math.cos(angle)), (float) (speed * Math.sin(angle)), life, baseColor));
            }
        }

        /**
         * Sets the time for the next power-up to spawn based on a random interval.
         * <p>
         * Définit l'heure de la prochaine apparition de power-up en fonction d'un intervalle aléatoire.
         */
        private void setNextPowerUpSpawnTime() {
            nextPowerUpSpawnTime = System.currentTimeMillis() + POWER_UP_SPAWN_INTERVAL_MIN +
                                   random.nextInt((int) (POWER_UP_SPAWN_INTERVAL_MAX - POWER_UP_SPAWN_INTERVAL_MIN));
        }

        /**
         * Spawns a new power-up at a random, easy-to-reach central location.
         * <p>
         * Fait apparaître un nouveau power-up à un emplacement central aléatoire et facile d'accès.
         */
        private void spawnPowerUp() {
            // MODIFICATION: Place power-ups horizontally at the center for easier access
            // MODIFICATION: Placer les power-ups horizontalement au centre pour un accès plus facile
            int puX = panelWidth / 2 - 10; // Center of the screen, adjusted for power-up width (20)
                                           // Centre de l'écran, ajusté pour la largeur du power-up (20)
            int puY = random.nextInt(panelHeight - 40) + 20; // Vertical range remains broad / La plage verticale reste large

            PowerUp.PowerUpType[] types = PowerUp.PowerUpType.values(); // Get all power-up types / Obtenir tous les types de power-up
            PowerUp.PowerUpType type = types[random.nextInt(types.length)]; // Select a random type / Sélectionner un type aléatoire

            activePowerUps.add(new PowerUp(type, puX, puY)); // Add to active power-ups / Ajouter aux power-ups actifs
        }

        /**
         * Applies the effect of a collected power-up to the game state.
         * <p>
         * Applique l'effet d'un power-up collecté à l'état du jeu.
         *
         * @param type       (EN) The type of power-up collected. (FR) Le type de power-up collecté.
         * @param ball       (EN) The ball that collected the power-up. (FR) La balle qui a collecté le power-up.
         * @param ballsToAdd (EN) List to add new balls to for multi-ball effect. (FR) Liste pour ajouter de nouvelles balles pour l'effet multi-balles.
         */
        private void applyPowerUpEffect(PowerUp.PowerUpType type, Ball ball, List<Ball> ballsToAdd) {
            // MODIFICATION: Increased power-up effect duration
            // MODIFICATION: Augmentation de la durée de l'effet du power-up
            long duration = POWER_UP_EFFECT_DURATION_MS; // Use constant for duration / Utiliser la constante pour la durée

            switch (type) {
                case PADDLE_ENLARGE:
                    if (ball.vx < 0) { // Player 1 collected it / Le joueur 1 l'a collecté
                        paddle1Height = originalPaddleHeight * 2;
                        p1PaddleEnlargeEndTime = System.currentTimeMillis() + duration;
                    } else { // Player 2 collected it / Le joueur 2 l'a collecté
                        paddle2Height = originalPaddleHeight * 2;
                        p2PaddleEnlargeEndTime = System.currentTimeMillis() + duration;
                    }
                    break;
                case MULTI_BALL:
                    // Create 2 new balls with slight variations in velocity
                    // Créer 2 nouvelles balles avec de légères variations de vitesse
                    ballsToAdd.add(new Ball(ball.x, ball.y, -ball.vx, ball.vy + (random.nextInt(3) - 1)));
                    ballsToAdd.add(new Ball(ball.x, ball.y, ball.vx, -ball.vy + (random.nextInt(3) - 1)));
                    break;
                case BALL_SPEED_UP:
                    for (Ball b : balls) { // Affect all active balls / Affecter toutes les balles actives
                        b.vx = (int) (b.vx * 1.2); // Increase speed by 20% / Augmenter la vitesse de 20%
                        b.vy = (int) (b.vy * 1.2);
                    }
                    break;
                case STICKY_PADDLE:
                    if (ball.vx < 0) { // Player 1 collected / Le joueur 1 l'a collecté
                        p1StickyPaddleEndTime = System.currentTimeMillis() + duration;
                    } else { // Player 2 collected / Le joueur 2 l'a collecté
                        p2StickyPaddleEndTime = System.currentTimeMillis() + duration;
                    }
                    break;
            }
        }

        /**
         * Checks if the game has ended based on scores, and transitions to GAME_OVER state or starts a new round.
         * <p>
         * Vérifie si le jeu est terminé en fonction des scores, et passe à l'état GAME_OVER ou démarre une nouvelle manche.
         */
        private void checkGameEndOrNextRound() {
            if (player1Score >= MAX_SCORE_TO_WIN) {
                gameOverMessage = "Joueur 1 GAGNE !"; // UI Text in French / Texte UI en français
                winningPlayerForHighScore = 1;
                scoreToPotentiallyRecord = player1Score;
                highScorePendingCheck = true;
                currentGameState = GameState.GAME_OVER;
                gameOverTime = System.currentTimeMillis(); // Start timer for fade effect / Démarrer le minuteur pour l'effet de fondu
            } else if (player2Score >= MAX_SCORE_TO_WIN) {
                gameOverMessage = "Joueur 2 GAGNE !"; // UI Text in French / Texte UI en français
                winningPlayerForHighScore = 2;
                scoreToPotentiallyRecord = player2Score;
                highScorePendingCheck = true;
                currentGameState = GameState.GAME_OVER;
                gameOverTime = System.currentTimeMillis(); // Start timer for fade effect / Démarrer le minuteur pour l'effet de fondu
            } else {
                initializeRound(); // Start next round if no winner / Démarrer la manche suivante si pas de gagnant
            }
        }

        /**
         * Handles the high score check and prompts the player to enter their name if a new high score is achieved.
         * This is typically called when transitioning from the GAME_OVER state.
         * <p>
         * Gère la vérification des meilleurs scores et invite le joueur à entrer son nom si un nouveau meilleur score est atteint.
         * Ceci est généralement appelé lors de la transition depuis l'état GAME_OVER.
         */
        private void processPendingHighScore() {
            if (highScorePendingCheck) {
                if (isHighScore(scoreToPotentiallyRecord)) {
                    promptAndSaveHighScore(scoreToPotentiallyRecord, winningPlayerForHighScore);
                }
                highScorePendingCheck = false; // Reset flag / Réinitialiser le drapeau
                winningPlayerForHighScore = 0; // Reset player / Réinitialiser le joueur
                scoreToPotentiallyRecord = 0; // Reset score / Réinitialiser le score
            }
        }

        /**
         * Paints the component, directing drawing based on the current game state.
         * This method is called automatically by Swing when the component needs to be redrawn.
         * <p>
         * Peint le composant, dirigeant le dessin en fonction de l'état de jeu actuel.
         * Cette méthode est appelée automatiquement par Swing lorsque le composant doit être redessiné.
         *
         * @param g (EN) The Graphics object to protect graphics states. (FR) L'objet Graphics pour protéger les états graphiques.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Clears the panel (fills with background color) / Efface le panneau (remplit avec la couleur de fond)
            Graphics2D g2d = (Graphics2D) g; // Cast to Graphics2D for advanced features / Caster en Graphics2D pour les fonctionnalités avancées

            // Enable anti-aliasing for smoother graphics and text rendering
            // Activer l'anti-aliasing pour un rendu graphique et textuel plus lisse
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY); // High rendering quality / Haute qualité de rendu

            // Draw Dynamic Background (subtle animated stars)
            // Dessiner le Fond Dynamique (étoiles animées subtiles)
            drawBackground(g2d);

            // Draw elements based on current game state
            // Dessiner les éléments en fonction de l'état de jeu actuel
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
                    drawGameElements(g2d); // Draw game state underneath / Dessiner l'état du jeu en dessous
                    drawPauseMenu(g2d);
                    break;
                case GAME_OVER:
                    drawGameElements(g2d); // Optionally draw final game state / Optionnellement dessiner l'état final du jeu
                    drawGameOverScreen(g2d);
                    break;
                case SHOW_HIGHSCORES:
                    drawHighScoresScreen(g2d);
                    break;
            }
            // Draw Particles over everything else for visual effects
            // Dessiner les Particules par-dessus tout le reste pour les effets visuels
            for (Particle p : new ArrayList<>(particles)) { // Iterate on copy to avoid ConcurrentModification issues
                                                            // Itérer sur une copie pour éviter les problèmes de modification concurrente
                p.draw(g2d);
            }
            // g2d.dispose(); // Dispose of graphics context when done in a paint cycle - usually handled by Swing
        }

        /**
         * Draws the dynamic background elements (e.g., twinkling stars).
         * <p>
         * Dessine les éléments de fond dynamiques (par exemple, des étoiles scintillantes).
         *
         * @param g2d (EN) The Graphics2D context to draw on. (FR) Le contexte Graphics2D sur lequel dessiner.
         */
        private void drawBackground(Graphics2D g2d) {
            // Fill background with a very dark gray / Remplir le fond avec un gris très foncé
            g2d.setColor(Color.DARK_GRAY.darker().darker());
            g2d.fillRect(0, 0, panelWidth, panelHeight);

            // Draw subtle, fading stars for dynamic effect
            // Dessiner des étoiles subtiles et s'estompant pour un effet dynamique
            Random bgRand = new Random(12345L); // Seed for consistent star positions across repaints
                                               // Graine pour des positions d'étoiles cohérentes entre les rafraîchissements
            for (int i = 0; i < 100; i++) { // Draw 100 stars / Dessiner 100 étoiles
                int starX = bgRand.nextInt(panelWidth);
                int starY = bgRand.nextInt(panelHeight);
                // Pulsating alpha effect based on current time
                // Effet alpha pulsant basé sur l'heure actuelle
                int alpha = 50 + (int) (100 * Math.abs(Math.sin(System.currentTimeMillis() / 1000.0 + i)));
                g2d.setColor(new Color(255, 255, 255, alpha)); // White stars with varying opacity / Étoiles blanches avec une opacité variable
                g2d.fillRect(starX, starY, 2, 2); // Small square for a star / Petit carré pour une étoile
            }
        }

        /**
         * Draws all main game elements: balls, paddles, center line, and scores.
         * <p>
         * Dessine tous les éléments principaux du jeu : balles, raquettes, ligne centrale et scores.
         *
         * @param g2d (EN) The Graphics2D context to draw on. (FR) Le contexte Graphics2D sur lequel dessiner.
         */
        private void drawGameElements(Graphics2D g2d) {
            // --- Draw balls ---
            // --- Dessiner les balles ---
            for (Ball ball : balls) {
                Point2D center = new Point2D.Float(ball.x, ball.y);
                float radius = BALL_RADIUS;
                float[] dist = {0.0f, 1.0f}; // Gradient distribution / Distribution du dégradé
                Color[] colors = {Color.CYAN, Color.BLUE}; // Inner to outer color for ball / Couleur intérieure à extérieure pour la balle

                // If ball is sticky, change its color to indicate effect
                // Si la balle est collante, changer sa couleur pour indiquer l'effet
                if (ball.isSticky) {
                    colors[0] = Color.RED;
                    colors[1] = Color.ORANGE;
                }
                // Apply radial gradient paint for a spherical look
                // Appliquer un dégradé radial pour un aspect sphérique
                RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors, CycleMethod.NO_CYCLE);
                g2d.setPaint(p);
                g2d.fillOval(ball.x - BALL_RADIUS, ball.y - BALL_RADIUS, BALL_RADIUS * 2, BALL_RADIUS * 2);
            }

            // --- Draw paddles with linear gradients for depth ---
            // --- Dessiner les raquettes avec des dégradés linéaires pour la profondeur ---
            // Paddle 1 (left) / Raquette 1 (gauche)
            GradientPaint gp1 = new GradientPaint(0, paddle1Y, paddle1Color.darker().darker(),
                                                  PADDLE_WIDTH, paddle1Y, paddle1Color.brighter().brighter());
            g2d.setPaint(gp1);
            g2d.fillRect(0, paddle1Y, PADDLE_WIDTH, paddle1Height);

            // Paddle 2 (right) / Raquette 2 (droite)
            GradientPaint gp2 = new GradientPaint(panelWidth - PADDLE_WIDTH, paddle2Y, paddle2Color.brighter().brighter(),
                                                  panelWidth, paddle2Y, paddle2Color.darker().darker());
            g2d.setPaint(gp2);
            g2d.fillRect(panelWidth - PADDLE_WIDTH, paddle2Y, PADDLE_WIDTH, paddle2Height);

            // --- Draw center line (dashed effect) ---
            // --- Dessiner la ligne centrale (effet pointillé) ---
            g2d.setColor(Color.GRAY.darker());
            for (int i = 0; i < panelHeight; i += 20) { // Draw dashed line / Dessiner une ligne pointillée
                g2d.fillRect(panelWidth / 2 - 2, i, 4, 10);
            }

            // --- Draw scores ---
            // --- Dessiner les scores ---
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 40)); // Large, bold font for scores / Grande police grasse pour les scores
            g2d.drawString(String.valueOf(player1Score), panelWidth / 2 - 80, 50); // Player 1 score on left / Score joueur 1 à gauche
            g2d.drawString(String.valueOf(player2Score), panelWidth / 2 + 40, 50); // Player 2 score on right / Score joueur 2 à droite

            // --- Draw active Power-Ups on screen ---
            // --- Dessiner les Power-Ups actifs à l'écran ---
            for (PowerUp pu : activePowerUps) {
                pu.draw(g2d);
            }
        }

        /**
         * Draws the main menu screen with game title and selectable options.
         * <p>
         * Dessine l'écran du menu principal avec le titre du jeu et les options sélectionnables.
         *
         * @param g2d (EN) The Graphics2D context to draw on. (FR) Le contexte Graphics2D sur lequel dessiner.
         */
        private void drawMainMenu(Graphics2D g2d) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Impact", Font.BOLD, 90)); // Large, impactful font for title / Grande police percutante pour le titre
            String title = "PONG"; // UI Text
            int titleWidth = g2d.getFontMetrics().stringWidth(title); // Get width for centering / Obtenir la largeur pour le centrage
            g2d.drawString(title, (panelWidth - titleWidth) / 2, panelHeight / 4); // Draw title / Dessiner le titre

            // Updated menu options for game modes (PvP and PvAI)
            // Options de menu mises à jour pour les modes de jeu (PvP et PvAI)
            String[] menuOptions = {"Jouer (1 vs 1)", "Jouer (vs IA)", "Highscores", "Options", "Quitter"}; // UI Text in French / Texte UI en français
            g2d.setFont(new Font("Arial", Font.PLAIN, 36)); // Slightly larger font for options / Police légèrement plus grande pour les options

            // Draw menu options, highlighting the currently selected one
            // Dessiner les options du menu, en surlignant celle qui est actuellement sélectionnée
            for (int i = 0; i < menuOptions.length; i++) {
                String optionText = menuOptions[i];
                int optionWidth = g2d.getFontMetrics().stringWidth(optionText);
                int yPos = panelHeight / 2 + i * 60; // More spacing between options / Plus d'espacement entre les options
                if (i == mainMenuSelection) {
                    g2d.setColor(Color.YELLOW); // Highlight selected option / Surligner l'option sélectionnée
                } else {
                    g2d.setColor(Color.WHITE.darker()); // Subtle difference for unselected options / Différence subtile pour les options non sélectionnées
                }
                g2d.drawString(optionText, (panelWidth - optionWidth) / 2, yPos);
            }
        }

        // --- HIGHSCORE METHODS ---
        // --- MÉTHODES DES MEILLEURS SCORES ---

        /**
         * Loads high scores from a file.
         * Handles file existence, empty files, and potential corruption.
         * Uses try-with-resources for safe stream management.
         * <p>
         * Charge les meilleurs scores depuis un fichier.
         * Gère l'existence du fichier, les fichiers vides et la corruption potentielle.
         * Utilise try-with-resources pour une gestion sécurisée des flux.
         */
        @SuppressWarnings("unchecked") // Suppress warning for unsafe cast of readObject()
                                       // Supprimer l'avertissement pour le cast non sûr de readObject()
        private void loadHighScores() {
            File file = new File(highScoreFilename);
            // If file doesn't exist or is empty, initialize an empty list
            // Si le fichier n'existe pas ou est vide, initialiser une liste vide
            if (!file.exists() || file.length() == 0) {
                highScores = new ArrayList<>();
                return;
            }

            try (FileInputStream fis = new FileInputStream(file);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {

                Object rawObject = ois.readObject();
                // Ensure the read object is a List and contains HighScoreEntry objects
                // S'assurer que l'objet lu est une List et contient des objets HighScoreEntry
                if (rawObject instanceof List<?>) {
                    List<?> rawList = (List<?>) rawObject;
                    highScores = new ArrayList<>();
                    for (Object item : rawList) {
                        if (item instanceof HighScoreEntry) {
                            highScores.add((HighScoreEntry) item);
                        } else {
                            // Log warning if unexpected object type found
                            // Journaliser l'avertissement si un type d'objet inattendu est trouvé
                            LOGGER.log(Level.WARNING, "Non-HighScoreEntry item found in high scores file. Item: " + item);
                            // Optionally, clear and start fresh if corruption is severe / Optionnellement, effacer et repartir à zéro si la corruption est grave
                        }
                    }
                    // Trim high scores if too many are loaded (e.g., from an older file)
                    // Tronquer les meilleurs scores si trop sont chargés (par exemple, depuis un ancien fichier)
                    if (highScores.size() > MAX_HIGHSCORES_TO_DISPLAY) {
                        Collections.sort(highScores); // Sort to remove lowest scores / Trier pour supprimer les scores les plus bas
                        while (highScores.size() > MAX_HIGHSCORES_TO_DISPLAY) {
                           highScores.remove(highScores.size() - 1); // Remove lowest / Supprimer le plus bas
                        }
                    }
                } else {
                    // Log error if file format is unexpected
                    // Journaliser l'erreur si le format du fichier est inattendu
                    LOGGER.log(Level.WARNING, "High score file format error: Expected List, found " + rawObject.getClass().getName());
                    highScores = new ArrayList<>(); // Start with an empty list on format error / Commencer avec une liste vide en cas d'erreur de format
                }
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Error loading high scores (class not found)", e);
                highScores = new ArrayList<>(); // Fallback to empty list / Revenir à une liste vide
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "I/O error loading high scores", e);
                highScores = new ArrayList<>(); // Fallback to empty list / Revenir à une liste vide
            } catch (ClassCastException e) {
                LOGGER.log(Level.SEVERE, "Error loading high scores (incorrect object type in file)", e);
                highScores = new ArrayList<>(); // Fallback to empty list / Revenir à une liste vide
            }

            // Ensure highScores is never null and is sorted
            // S'assurer que highScores n'est jamais nul et est trié
            if (highScores == null) {
                highScores = new ArrayList<>();
            }
            Collections.sort(highScores); // Sorts by score descending (due to Comparable implementation)
                                          // Trie par score décroissant (en raison de l'implémentation de Comparable)
        }

        /**
         * Saves the current list of high scores to a file.
         * Uses try-with-resources for safe stream management.
         * <p>
         * Sauvegarde la liste actuelle des meilleurs scores dans un fichier.
         * Utilise try-with-resources pour une gestion sécurisée des flux.
         */
        private void saveHighScores() {
            try (FileOutputStream fos = new FileOutputStream(highScoreFilename);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(highScores); // Write the entire list / Écrire toute la liste
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "I/O error saving high scores", e); // Log error / Journaliser l'erreur
            }
        }

        /**
         * Checks if a given score qualifies as a high score.
         * A score is a high score if there's space in the list or if it's better than the lowest existing high score.
         * <p>
         * Vérifie si un score donné est un meilleur score.
         * Un score est un meilleur score s'il y a de la place dans la liste ou s'il est supérieur au plus bas score existant.
         *
         * @param score (EN) The score to check. (FR) Le score à vérifier.
         * @return (EN) True if it's a high score, false otherwise. (FR) Vrai si c'est un meilleur score, faux sinon.
         */
        private boolean isHighScore(int score) {
            if (score <= 0) return false; // Don't record zero or negative scores / Ne pas enregistrer les scores nuls ou négatifs
            if (highScores.size() < MAX_HIGHSCORES_TO_DISPLAY) {
                return true; // Always a high score if there's space / Toujours un meilleur score s'il y a de la place
            }
            // Otherwise, check if better than the lowest current high score
            // Sinon, vérifier s'il est meilleur que le plus bas score actuel
            return score > highScores.get(highScores.size() - 1).getScore();
        }

        /**
         * Adds a new high score entry to the list, maintaining sorting and max display count.
         * <p>
         * Ajoute une nouvelle entrée de meilleur score à la liste, en maintenant le tri et le nombre maximal d'affichage.
         *
         * @param playerName (EN) The name of the player. (FR) Le nom du joueur.
         * @param score      (EN) The score achieved. (FR) Le score obtenu.
         */
        private void addHighScore(String playerName, int score) {
            highScores.add(new HighScoreEntry(playerName, score)); // Add new entry / Ajouter une nouvelle entrée
            Collections.sort(highScores); // Sorts by score descending / Trie par score décroissant
            // Remove the lowest score if list is too long (beyond max display limit)
            // Supprimer le score le plus bas si la liste est trop longue (au-delà de la limite d'affichage maximale)
            while (highScores.size() > MAX_HIGHSCORES_TO_DISPLAY) {
                highScores.remove(highScores.size() - 1);
            }
            saveHighScores(); // Persist changes to file / Persister les changements dans le fichier
        }

        /**
         * Prompts the player to enter their name for a new high score and saves it.
         * Includes basic input sanitization and length truncation.
         * <p>
         * Invite le joueur à entrer son nom pour un nouveau meilleur score et l'enregistre.
         * Inclut une sanitisation de base de l'entrée et une troncation de la longueur.
         *
         * @param scoreToSave  (EN) The score to be saved. (FR) Le score à sauvegarder.
         * @param playerNumber (EN) The player number who achieved the score (for messaging). (FR) Le numéro du joueur qui a obtenu le score (pour le message).
         */
        private void promptAndSaveHighScore(int scoreToSave, int playerNumber) {
            String promptMessage = "Joueur " + playerNumber + ", vous avez un score de " + scoreToSave + "!\n" + // UI Text
                                   "Entrez votre pseudo (max 10 caractères alphanumériques):"; // UI Text
            String playerName = JOptionPane.showInputDialog(this, promptMessage, "Nouveau Highscore!", JOptionPane.PLAIN_MESSAGE); // UI Text

            if (playerName != null) { // If input was not cancelled / Si l'entrée n'a pas été annulée
                playerName = playerName.trim(); // Remove leading/trailing whitespace / Supprimer les espaces avant/arrière
                if (!playerName.isEmpty()) {
                    playerName = playerName.replaceAll("[^a-zA-Z0-9]", ""); // Keep only alphanumeric characters / Garder uniquement les caractères alphanumériques
                    if (playerName.length() > 10) {
                        playerName = playerName.substring(0, 10); // Truncate to 10 characters / Tronquer à 10 caractères
                    }
                    if (playerName.isEmpty()) { // If name becomes empty after sanitization / Si le nom devient vide après la sanitisation
                        playerName = "Anonyme" + playerNumber; // Default to anonymous / Par défaut à anonyme
                    }
                    addHighScore(playerName, scoreToSave); // Add and save the high score / Ajouter et sauvegarder le meilleur score
                } else {
                    LOGGER.info("Empty player name, score not saved."); // Log if name is empty / Journaliser si le nom est vide
                }
            } else {
                LOGGER.info("Player name input cancelled, score not saved."); // Log if input cancelled / Journaliser si l'entrée est annulée
            }
        }

        /**
         * Draws the high scores screen, displaying the top scores.
         * <p>
         * Dessine l'écran des meilleurs scores, affichant les scores les plus élevés.
         *
         * @param g2d (EN) The Graphics2D context to draw on. (FR) Le contexte Graphics2D sur lequel dessiner.
         */
        private void drawHighScoresScreen(Graphics2D g2d) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            String title = "MEILLEURS SCORES"; // UI Text
            int titleWidth = g2d.getFontMetrics().stringWidth(title);
            g2d.drawString(title, (panelWidth - titleWidth) / 2, 80);

            g2d.setFont(new Font("Monospaced", Font.PLAIN, 28)); // Monospaced font for alignment / Police monospace pour l'alignement
            int startY = 150;
            int lineHeight = 35; // Vertical spacing between entries / Espacement vertical entre les entrées

            if (highScores.isEmpty()) {
                String noScoresMsg = "Aucun score enregistré."; // UI Text
                g2d.drawString(noScoresMsg, panelWidth / 2 - g2d.getFontMetrics().stringWidth(noScoresMsg) / 2, startY);
            } else {
                // Headers for the high score table / En-têtes pour la table des meilleurs scores
                String header = String.format("%-4s %-12s %5s", "RANG", "PSEUDO", "SCORE"); // UI Text
                g2d.setColor(Color.YELLOW); // Highlight headers / Surligner les en-têtes
                g2d.drawString(header, panelWidth / 4, startY);
                g2d.drawLine(panelWidth / 4, startY + 10, panelWidth - panelWidth / 4, startY + 10); // Underline header / Souligner l'en-tête
                g2d.setColor(Color.WHITE); // Reset color for entries / Réinitialiser la couleur pour les entrées

                // Draw each high score entry
                // Dessiner chaque entrée de meilleur score
                for (int i = 0; i < highScores.size(); i++) {
                    HighScoreEntry entry = highScores.get(i);
                    String rankStr = (i + 1) + "."; // Rank number / Numéro de rang
                    String scoreLine = String.format("%-4s %-12s %5d",
                                                     rankStr,
                                                     entry.getPlayerName(),
                                                     entry.getScore());
                    g2d.drawString(scoreLine, panelWidth / 4, startY + (i + 1) * lineHeight + 10);
                }
            }

            // Instructions to return to main menu
            // Instructions pour revenir au menu principal
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            String returnMsg = "Appuyez sur ESC pour revenir au menu principal"; // UI Text
            int returnMsgWidth = g2d.getFontMetrics().stringWidth(returnMsg);
            g2d.drawString(returnMsg, (panelWidth - returnMsgWidth) / 2, panelHeight - 50);
        }

        /**
         * Draws the options menu, allowing configuration of game settings and key bindings.
         * <p>
         * Dessine le menu des options, permettant la configuration des paramètres du jeu et des liaisons de touches.
         *
         * @param g2d (EN) The Graphics2D context to draw on. (FR) Le contexte Graphics2D sur lequel dessiner.
         */
        private void drawOptionsMenu(Graphics2D g2d) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            String title = "OPTIONS"; // UI Text
            int titleWidth = g2d.getFontMetrics().stringWidth(title);
            g2d.drawString(title, (panelWidth - titleWidth) / 2, 80);

            g2d.setFont(new Font("Arial", Font.PLAIN, 25));
            int startY = 150;
            int lineHeight = 40;

            // Draw each option label and its current value
            // Dessiner chaque étiquette d'option et sa valeur actuelle
            for (int i = 0; i < OPTION_LABELS.length; i++) {
                String label = OPTION_LABELS[i];
                String value = "";
                // Determine the current value for each option
                // Déterminer la valeur actuelle pour chaque option
                switch (i) {
                    case 0: value = KeyEvent.getKeyText(player1UpKey); break;
                    case 1: value = KeyEvent.getKeyText(player1DownKey); break;
                    case 2: value = KeyEvent.getKeyText(player2UpKey); break;
                    case 3: value = KeyEvent.getKeyText(player2DownKey); break;
                    case 4: value = String.valueOf(initialBallSpeed); break;
                    case 5: value = getColorName(paddle1Color); break;
                    case 6: value = getColorName(paddle2Color); break;
                    case 7: value = aiDifficulty.toString(); break;
                }

                if (i == currentOptionSelection) {
                    g2d.setColor(Color.YELLOW); // Highlight selected option / Surligner l'option sélectionnée
                    if (isRemappingKey && (i >= 0 && i <= 3)) { // If remapping a key, show prompt
                                                               // Si on remappe une touche, afficher l'invite
                        value = "Appuyez sur une touche..."; // UI Text
                    }
                } else {
                    g2d.setColor(Color.WHITE);
                }
                g2d.drawString(label + value, panelWidth / 4, startY + i * lineHeight);
            }
            // Instructions for navigation
            // Instructions pour la navigation
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            String returnMsg = "Appuyez sur ESC pour revenir. Utilisez GAUCHE/DROITE pour changer les valeurs."; // UI Text
            int returnMsgWidth = g2d.getFontMetrics().stringWidth(returnMsg);
            g2d.drawString(returnMsg, (panelWidth - returnMsgWidth) / 2, panelHeight - 50);
        }

        /**
         * Returns the French name of a given Color object.
         * <p>
         * Retourne le nom français d'un objet Color donné.
         *
         * @param color (EN) The Color object. (FR) L'objet Color.
         * @return (EN) The French name of the color. (FR) Le nom français de la couleur.
         */
        private String getColorName(Color color) {
            if (Color.WHITE.equals(color)) return "Blanc";
            if (Color.BLUE.equals(color)) return "Bleu";
            if (Color.RED.equals(color)) return "Rouge";
            if (Color.GREEN.equals(color)) return "Vert";
            if (Color.YELLOW.equals(color)) return "Jaune";
            if (Color.CYAN.equals(color)) return "Cyan";
            if (Color.MAGENTA.equals(color)) return "Magenta";
            if (Color.ORANGE.equals(color)) return "Orange";
            return "Inconnu"; // Default for unsupported colors / Par défaut pour les couleurs non prises en charge
        }

        /**
         * Returns the next color in the `AVAILABLE_COLORS` array for cycling through color options.
         * <p>
         * Retourne la couleur suivante dans le tableau `AVAILABLE_COLORS` pour faire défiler les options de couleur.
         *
         * @param currentColor (EN) The current color. (FR) La couleur actuelle.
         * @return (EN) The next color in the sequence. (FR) La couleur suivante dans la séquence.
         */
        private Color getNextColor(Color currentColor) {
            for (int i = 0; i < AVAILABLE_COLORS.length; i++) {
                if (AVAILABLE_COLORS[i].equals(currentColor)) {
                    return AVAILABLE_COLORS[(i + 1) % AVAILABLE_COLORS.length];
                }
            }
            return Color.WHITE; // Fallback / Repli
        }

        /**
         * Returns the previous color in the `AVAILABLE_COLORS` array for cycling through color options.
         * <p>
         * Retourne la couleur précédente dans le tableau `AVAILABLE_COLORS` pour faire défiler les options de couleur.
         *
         * @param currentColor (EN) The current color. (FR) La couleur actuelle.
         * @return (EN) The previous color in the sequence. (FR) La couleur précédente dans la séquence.
         */
         private Color getPreviousColor(Color currentColor) {
            for (int i = 0; i < AVAILABLE_COLORS.length; i++) {
                if (AVAILABLE_COLORS[i].equals(currentColor)) {
                    return AVAILABLE_COLORS[(i - 1 + AVAILABLE_COLORS.length) % AVAILABLE_COLORS.length];
                }
            }
            return Color.WHITE; // Fallback / Repli
        }

        /**
         * Returns the next AI difficulty level in the enum.
         * <p>
         * Retourne le niveau de difficulté de l'IA suivant dans l'énumération.
         *
         * @param current (EN) The current AI difficulty. (FR) La difficulté actuelle de l'IA.
         * @return (EN) The next AI difficulty level. (FR) Le niveau de difficulté de l'IA suivant.
         */
        private AIDifficulty getNextAIDifficulty(AIDifficulty current) {
            AIDifficulty[] difficulties = AIDifficulty.values();
            int nextOrdinal = (current.ordinal() + 1) % difficulties.length;
            return difficulties[nextOrdinal];
        }

        /**
         * Returns the previous AI difficulty level in the enum.
         * <p>
         * Retourne le niveau de difficulté de l'IA précédent dans l'énumération.
         *
         * @param current (EN) The current AI difficulty. (FR) La difficulté actuelle de l'IA.
         * @return (EN) The previous AI difficulty level. (FR) Le niveau de difficulté de l'IA précédent.
         */
         private AIDifficulty getPreviousAIDifficulty(AIDifficulty current) {
            AIDifficulty[] difficulties = AIDifficulty.values();
            int prevOrdinal = (current.ordinal() - 1 + difficulties.length) % difficulties.length;
            return difficulties[prevOrdinal];
        }

        /**
         * Draws the game over screen with a fading overlay and options to restart or return to main menu.
         * <p>
         * Dessine l'écran de fin de jeu avec un calque de fondu et des options pour redémarrer ou revenir au menu principal.
         *
         * @param g2d (EN) The Graphics2D context to draw on. (FR) Le contexte Graphics2D sur lequel dessiner.
         */
        private void drawGameOverScreen(Graphics2D g2d) {
            // Calculate alpha for fading in the overlay / Calculer l'alpha pour le fondu du calque
            long elapsedTime = System.currentTimeMillis() - gameOverTime;
            float alpha = Math.min(1f, elapsedTime / 1000f); // Fade in over 1 second / Fondu en 1 seconde

            // Apply alpha composite for transparency / Appliquer le composite alpha pour la transparence
            g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2d.setColor(new Color(0, 0, 0, 180)); // Semi-transparent dark overlay / Calque sombre semi-transparent
            g2d.fillRect(0, 0, panelWidth, panelHeight); // Cover the entire panel / Couvrir tout le panneau

            g2d.setColor(Color.RED); // Set color for game over message / Définir la couleur pour le message de fin de jeu
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

            g2d.setComposite(AlphaComposite.SrcOver.derive(1f)); // Reset alpha composite to full opacity / Réinitialiser le composite alpha à une opacité totale
        }

        /**
         * Draws the pause menu with a fading overlay and options to resume, go to options, or return to main menu.
         * <p>
         * Dessine le menu de pause avec un calque de fondu et des options pour reprendre, aller aux options ou revenir au menu principal.
         *
         * @param g2d (EN) The Graphics2D context to draw on. (FR) Le contexte Graphics2D sur lequel dessiner.
         */
        private void drawPauseMenu(Graphics2D g2d) {
            // Calculate alpha for fading in the overlay / Calculer l'alpha pour le fondu du calque
            long elapsedTime = System.currentTimeMillis() - pauseStartTime;
            float alpha = Math.min(1f, elapsedTime / 500f); // Fade in over 0.5 second / Fondu en 0,5 seconde

            // Apply alpha composite for transparency / Appliquer le composite alpha pour la transparence
            g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2d.setColor(new Color(0, 0, 0, 180)); // Semi-transparent dark overlay / Calque sombre semi-transparent
            g2d.fillRect(0, 0, panelWidth, panelHeight); // Cover the entire panel / Couvrir tout le panneau

            g2d.setColor(Color.WHITE); // Set color for pause title / Définir la couleur pour le titre de pause
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            String title = "PAUSE"; // UI Text
            int titleWidth = g2d.getFontMetrics().stringWidth(title);
            g2d.drawString(title, (panelWidth - titleWidth) / 2, panelHeight / 4);

            String[] pauseOptions = {"Reprendre", "Options", "Menu Principal", "Quitter"}; // UI Text
            g2d.setFont(new Font("Arial", Font.PLAIN, 30));

            // Draw pause menu options, highlighting the selected one
            // Dessiner les options du menu pause, en surlignant celle qui est sélectionnée
            for (int i = 0; i < pauseOptions.length; i++) {
                String optionText = pauseOptions[i];
                int optionWidth = g2d.getFontMetrics().stringWidth(optionText);
                int yPos = panelHeight / 2 + i * 50;
                if (i == pauseMenuSelection) {
                    g2d.setColor(Color.YELLOW); // Highlight selected option / Surligner l'option sélectionnée
                } else {
                    g2d.setColor(Color.WHITE);
                }
                g2d.drawString(optionText, (panelWidth - optionWidth) / 2, yPos);
            }
            g2d.setComposite(AlphaComposite.SrcOver.derive(1f)); // Reset alpha composite / Réinitialiser le composite alpha
        }

        /**
         * Handles keyboard key press events based on the current game state.
         * Dispatches the event to specific handlers for each state.
         * <p>
         * Gère les événements d'appui sur les touches du clavier en fonction de l'état de jeu actuel.
         * Distribue l'événement à des gestionnaires spécifiques pour chaque état.
         *
         * @param e (EN) The KeyEvent generated by the key press. (FR) L'événement KeyEvent généré par l'appui sur la touche.
         */
        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode(); // Get the key code of the pressed key / Obtenir le code de la touche appuyée

            // Dispatch to appropriate handler based on current game state
            // Distribuer au gestionnaire approprié en fonction de l'état de jeu actuel
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

        /**
         * Handles key input events when the game is in the main menu state.
         * Allows navigation and selection of menu options.
         * <p>
         * Gère les événements d'entrée clavier lorsque le jeu est dans l'état du menu principal.
         * Permet la navigation et la sélection des options de menu.
         *
         * @param keyCode (EN) The key code of the pressed key. (FR) Le code de la touche appuyée.
         */
        private void handleMainMenuInput(int keyCode) {
            // Options array to match menu display / Tableau d'options pour correspondre à l'affichage du menu
            String[] menuOptions = {"Jouer (1 vs 1)", "Jouer (vs IA)", "Highscores", "Options", "Quitter"};
            if (keyCode == KeyEvent.VK_UP) {
                mainMenuSelection = (mainMenuSelection - 1 + menuOptions.length) % menuOptions.length; // Move selection up / Déplacer la sélection vers le haut
            } else if (keyCode == KeyEvent.VK_DOWN) {
                mainMenuSelection = (mainMenuSelection + 1) % menuOptions.length; // Move selection down / Déplacer la sélection vers le bas
            } else if (keyCode == KeyEvent.VK_ENTER) {
                // Perform action based on selected option
                // Effectuer une action en fonction de l'option sélectionnée
                switch (mainMenuSelection) {
                    case 0: // "Jouer (1 vs 1)" - Player vs Player mode
                            // "Jouer (1 vs 1)" - Mode Joueur contre Joueur
                        currentGameMode = GameMode.PLAYER_VS_PLAYER;
                        startNewGame();
                        break;
                    case 1: // "Jouer (vs IA)" - Player vs AI mode
                            // "Jouer (vs IA)" - Mode Joueur contre IA
                        currentGameMode = GameMode.PLAYER_VS_AI;
                        startNewGame();
                        break;
                    case 2: // "Highscores"
                        currentGameState = GameState.SHOW_HIGHSCORES;
                        break;
                    case 3: // "Options"
                        previousStateBeforeOptions = GameState.MAIN_MENU; // Store current state for return / Stocker l'état actuel pour le retour
                        currentGameState = GameState.OPTIONS;
                        currentOptionSelection = 0; // Reset option selection / Réinitialiser la sélection d'option
                        isRemappingKey = false; // Ensure no key remapping is active / S'assurer qu'aucun remappage de touche n'est actif
                        break;
                    case 4: // "Quitter" - Exit application
                            // "Quitter" - Quitter l'application
                        System.exit(0);
                        break;
                }
            }
        }

        /**
         * Handles key input events when the game is in the playing state.
         * Controls paddle movement and pause functionality.
         * <p>
         * Gère les événements d'entrée clavier lorsque le jeu est en mode de jeu.
         * Contrôle le mouvement de la raquette et la fonctionnalité de pause.
         *
         * @param keyCode (EN) The key code of the pressed key. (FR) Le code de la touche appuyée.
         */
        private void handlePlayingInput(int keyCode) {
            // Player 1 input handling / Gestion de l'entrée du joueur 1
            if (keyCode == player1UpKey) p1UpPressed = true;
            if (keyCode == player1DownKey) p1DownPressed = true;

            // Player 2 input handling (only if in Player vs Player mode)
            // Gestion de l'entrée du joueur 2 (uniquement en mode Joueur contre Joueur)
            if (currentGameMode == GameMode.PLAYER_VS_PLAYER) {
                if (keyCode == player2UpKey) p2UpPressed = true;
                if (keyCode == player2DownKey) p2DownPressed = true;
            }

            // Pause game with ESCAPE key / Mettre le jeu en pause avec la touche ÉCHAP
            if (keyCode == KeyEvent.VK_ESCAPE) {
                currentGameState = GameState.PAUSED;
                pauseMenuSelection = 0; // Reset pause menu selection / Réinitialiser la sélection du menu pause
                pauseStartTime = System.currentTimeMillis(); // Start timer for fade effect / Démarrer le minuteur pour l'effet de fondu
            }
        }

        /**
         * Handles key input events when the game is in the paused state.
         * Allows navigation and selection in the pause menu.
         * <p>
         * Gère les événements d'entrée clavier lorsque le jeu est en pause.
         * Permet la navigation et la sélection dans le menu de pause.
         *
         * @param keyCode (EN) The key code of the pressed key. (FR) Le code de la touche appuyée.
         */
        private void handlePausedInput(int keyCode) {
            String[] pauseOptions = {"Reprendre", "Options", "Menu Principal", "Quitter"}; // UI Text
            if (keyCode == KeyEvent.VK_UP) {
                pauseMenuSelection = (pauseMenuSelection - 1 + pauseOptions.length) % pauseOptions.length; // Move selection up / Déplacer la sélection vers le haut
            } else if (keyCode == KeyEvent.VK_DOWN) {
                pauseMenuSelection = (pauseMenuSelection + 1) % pauseOptions.length; // Move selection down / Déplacer la sélection vers le bas
            } else if (keyCode == KeyEvent.VK_ENTER) {
                // Perform action based on selected option
                // Effectuer une action en fonction de l'option sélectionnée
                switch (pauseMenuSelection) {
                    case 0: // Reprendre - Resume game / Reprendre le jeu
                        currentGameState = GameState.PLAYING;
                        break;
                    case 1: // Options - Go to options menu / Aller au menu des options
                        previousStateBeforeOptions = GameState.PAUSED;
                        currentGameState = GameState.OPTIONS;
                        currentOptionSelection = 0;
                        isRemappingKey = false;
                        break;
                    case 2: // Menu Principal - Return to main menu / Retourner au menu principal
                        // No high score check here as game is paused, not finished (score is preserved for game continuation).
                        // Pas de vérification des meilleurs scores ici car le jeu est en pause, non terminé (le score est conservé pour la suite du jeu).
                        currentGameState = GameState.MAIN_MENU;
                        mainMenuSelection = 0;
                        break;
                    case 3: // Quitter - Exit application / Quitter l'application
                        System.exit(0);
                        break;
                }
            } else if (keyCode == KeyEvent.VK_ESCAPE) { // Resume game with ESC from pause / Reprendre le jeu avec ÉCHAP depuis la pause
                currentGameState = GameState.PLAYING;
            }
        }

        /**
         * Handles key input events when the game is in the options menu state.
         * Allows navigation, changing settings, and remapping keys.
         * <p>
         * Gère les événements d'entrée clavier lorsque le jeu est dans l'état du menu des options.
         * Permet la navigation, le changement des paramètres et le remappage des touches.
         *
         * @param keyCode (EN) The key code of the pressed key. (FR) Le code de la touche appuyée.
         */
        private void handleOptionsInput(int keyCode) {
            if (isRemappingKey) { // If a key remapping is in progress
                                  // Si un remappage de touche est en cours
                // Any key pressed (except ESC/ENTER) will be the new key binding
                // Toute touche pressée (sauf ÉCHAP/ENTRÉE) sera la nouvelle liaison de touche
                if (keyCode != KeyEvent.VK_ESCAPE && keyCode != KeyEvent.VK_ENTER) {
                    switch (keyToRemapIndex) {
                        case 0: player1UpKey = keyCode; break;
                        case 1: player1DownKey = keyCode; break;
                        case 2: player2UpKey = keyCode; break;
                        case 3: player2DownKey = keyCode; break;
                    }
                    isRemappingKey = false; // Stop remapping / Arrêter le remappage
                } else if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_ENTER) { // Cancel remapping with ESC or ENTER
                                                                                           // Annuler le remappage avec ÉCHAP ou ENTRÉE
                    isRemappingKey = false;
                }
            } else { // Not remapping a key, navigate options or change values
                     // Pas de remappage de touche, naviguer dans les options ou changer les valeurs
                if (keyCode == KeyEvent.VK_UP) {
                    currentOptionSelection = (currentOptionSelection - 1 + OPTION_LABELS.length) % OPTION_LABELS.length; // Move selection up / Déplacer la sélection vers le haut
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    currentOptionSelection = (currentOptionSelection + 1) % OPTION_LABELS.length; // Move selection down / Déplacer la sélection vers le bas
                } else if (keyCode == KeyEvent.VK_ENTER) {
                    // Enter key pressed on a selectable option
                    // Touche Entrée appuyée sur une option sélectionnable
                    if (currentOptionSelection >= 0 && currentOptionSelection <= 3) { // Key remapping options
                                                                                    // Options de remappage de touches
                        keyToRemapIndex = currentOptionSelection;
                        isRemappingKey = true; // Start key remapping mode / Démarrer le mode de remappage de touche
                    } else if (currentOptionSelection == 4) { // Ball speed / Vitesse de la balle
                         initialBallSpeed = (initialBallSpeed % MAX_BALL_SPEED) + MIN_BALL_SPEED; // Cycle through speeds / Faire défiler les vitesses
                    } else if (currentOptionSelection == 5) { // Paddle 1 color / Couleur de la raquette 1
                        paddle1Color = getNextColor(paddle1Color);
                    } else if (currentOptionSelection == 6) { // Paddle 2 color / Couleur de la raquette 2
                        paddle2Color = getNextColor(paddle2Color);
                    } else if (currentOptionSelection == 7) { // AI Difficulty / Difficulté de l'IA
                        aiDifficulty = getNextAIDifficulty(aiDifficulty);
                    }
                } else if (keyCode == KeyEvent.VK_LEFT) { // Adjust value left (e.g., decrease speed, previous color)
                                                          // Ajuster la valeur vers la gauche (par exemple, diminuer la vitesse, couleur précédente)
                    if (currentOptionSelection == 4) { // Ball speed / Vitesse de la balle
                        initialBallSpeed = Math.max(MIN_BALL_SPEED, initialBallSpeed - 1);
                    } else if (currentOptionSelection == 5) { // Paddle 1 color / Couleur de la raquette 1
                        paddle1Color = getPreviousColor(paddle1Color);
                    } else if (currentOptionSelection == 6) { // Paddle 2 color / Couleur de la raquette 2
                        paddle2Color = getPreviousColor(paddle2Color);
                    } else if (currentOptionSelection == 7) { // AI Difficulty / Difficulté de l'IA
                        aiDifficulty = getPreviousAIDifficulty(aiDifficulty);
                    }
                } else if (keyCode == KeyEvent.VK_RIGHT) { // Adjust value right (e.g., increase speed, next color)
                                                           // Ajuster la valeur vers la droite (par exemple, augmenter la vitesse, couleur suivante)
                     if (currentOptionSelection == 4) { // Ball speed / Vitesse de la balle
                        initialBallSpeed = Math.min(MAX_BALL_SPEED, initialBallSpeed + 1);
                    } else if (currentOptionSelection == 5) { // Paddle 1 color / Couleur de la raquette 1
                        paddle1Color = getNextColor(paddle1Color);
                    } else if (currentOptionSelection == 6) { // Paddle 2 color / Couleur de la raquette 2
                        paddle2Color = getNextColor(paddle2Color);
                    } else if (currentOptionSelection == 7) { // AI Difficulty / Difficulté de l'IA
                        aiDifficulty = getNextAIDifficulty(aiDifficulty);
                    }
                }
                else if (keyCode == KeyEvent.VK_ESCAPE) { // Return to previous state (Main Menu or Paused)
                                                          // Revenir à l'état précédent (Menu Principal ou En Pause)
                    currentGameState = previousStateBeforeOptions;
                    // Reset selections for those menus if needed
                    // Réinitialiser les sélections pour ces menus si nécessaire
                    if (currentGameState == GameState.MAIN_MENU) mainMenuSelection = 0;
                    if (currentGameState == GameState.PAUSED) pauseMenuSelection = 0;
                    isRemappingKey = false; // Ensure remapping is cancelled / S'assurer que le remappage est annulé
                }
            }
        }

        /**
         * Handles key input events when the game is in the game over state.
         * Allows restarting the game or returning to the main menu.
         * <p>
         * Gère les événements d'entrée clavier lorsque le jeu est dans l'état de fin de jeu.
         * Permet de redémarrer le jeu ou de revenir au menu principal.
         *
         * @param keyCode (EN) The key code of the pressed key. (FR) Le code de la touche appuyée.
         */
        private void handleGameOverInput(int keyCode) {
            processPendingHighScore(); // Process any pending high score first / Traiter tout meilleur score en attente en premier

            if (keyCode == KeyEvent.VK_ENTER) {
                startNewGame(); // Start a new game / Démarrer une nouvelle partie
            } else if (keyCode == KeyEvent.VK_ESCAPE) {
                currentGameState = GameState.MAIN_MENU; // Go to main menu / Aller au menu principal
                mainMenuSelection = 0; // Reset main menu selection / Réinitialiser la sélection du menu principal
            }
        }

        /**
         * Handles key input events when the high scores screen is displayed.
         * Allows returning to the main menu.
         * <p>
         * Gère les événements d'entrée clavier lorsque l'écran des meilleurs scores est affiché.
         * Permet de revenir au menu principal.
         *
         * @param keyCode (EN) The key code of the pressed key. (FR) Le code de la touche appuyée.
         */
        private void handleShowHighScoresInput(int keyCode) {
            if (keyCode == KeyEvent.VK_ESCAPE) {
                currentGameState = GameState.MAIN_MENU; // Go to main menu / Aller au menu principal
                mainMenuSelection = 0; // Reset main menu selection / Réinitialiser la sélection du menu principal
            }
        }

        /**
         * Handles keyboard key release events.
         * Resets input flags for paddle movement when keys are released.
         * <p>
         * Gère les événements de relâchement des touches du clavier.
         * Réinitialise les drapeaux d'entrée pour le mouvement de la raquette lorsque les touches sont relâchées.
         *
         * @param e (EN) The KeyEvent generated by the key release. (FR) L'événement KeyEvent généré par le relâchement de la touche.
         */
        @Override
        public void keyReleased(KeyEvent e) {
            int keyCode = e.getKeyCode();
            // Only handle releases if in PLAYING state to prevent interference with menus
            // Gérer les relâchements uniquement en mode JEU pour éviter les interférences avec les menus
            if (currentGameState == GameState.PLAYING) {
                if (keyCode == player1UpKey) p1UpPressed = false;
                if (keyCode == player1DownKey) p1DownPressed = false;
                // Only process player 2 release input if in PLAYER_VS_PLAYER mode
                // Traiter l'entrée de relâchement du joueur 2 uniquement en mode Joueur contre Joueur
                if (currentGameMode == GameMode.PLAYER_VS_PLAYER) {
                    if (keyCode == player2UpKey) p2UpPressed = false;
                    if (keyCode == player2DownKey) p2DownPressed = false;
                }
            }
        }

        /**
         * Required by KeyListener interface, but not used in this implementation.
         * <p>
         * Requise par l'interface KeyListener, mais non utilisée dans cette implémentation.
         *
         * @param e (EN) The KeyEvent generated. (FR) L'événement KeyEvent généré.
         */
        @Override
        public void keyTyped(KeyEvent e) { /* Not used, but required by KeyListener */ }
    }
}
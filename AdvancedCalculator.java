import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import net.objecthunter.exp4j.ExpressionBuilder;

public class AdvancedCalculator extends JFrame {
    private JTextField display;
    private StringBuilder input = new StringBuilder();

    // Standard Professional Colors from your current layout
    private final Color BG = new Color(33, 33, 33);
    private final Color LCD = new Color(220, 230, 220);
    private final Color NUM_BTN = new Color(66, 66, 66);
    private final Color OP_BTN = new Color(255, 152, 0); // Orange
    private final Color FUNC_BTN = new Color(50, 50, 50); // Dark Function button
    private final Color PRIMARY_YELLOW = new Color(255, 200, 0); // Shift
    private final Color PRIMARY_PURPLE = new Color(150, 100, 255); // Alpha

    // Grid: 7 rows x 5 columns
    // Operator Names replaced with actual keyboard symbols
    private String[][] buttons = {
        {"SHIFT", "ALPHA", "MODE", "log", "ln"},
        {"sin", "cos", "tan", "x^2", "sqrt"},
        {"(", ")", "hyp", "abs", "Exp"},
        {"7", "8", "9", "DEL", "AC"},
        {"4", "5", "6", "*", "/"}, // MUL -> *, DIV -> /
        {"1", "2", "3", "+", "-"}, // ADD -> +, SUB -> -
        {"0", ".", "Ans", "PI", "="}
    };

    public AdvancedCalculator() {
        setTitle("Engineering Calc v4.0 (Animated)");
        setSize(480, 720); // Slightly increased for animation breathing room
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BG);

        // Display Area
        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Monospaced", Font.BOLD, 42));
        display.setBackground(LCD);
        display.setForeground(Color.BLACK);
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        add(display, BorderLayout.NORTH);

        // Button Panel
        JPanel panel = new JPanel(new GridLayout(7, 5, 8, 8));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        for (String[] row : buttons) {
            for (String text : row) {
                JButton btn = createAnimatedButton(text);
                panel.add(btn);
            }
        }

        add(panel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Factory method to create a button with animations and exact same style.
     */
    private JButton createAnimatedButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14)); // Slightly larger font, still clear
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        
        // --- 1. DEFINE EXACT SAME STYLE ---
        Color originalBg;
        if (text.matches("[0-9.]")) originalBg = NUM_BTN;
        else if (text.equals("AC") || text.equals("DEL")) originalBg = OP_BTN;
        else if (text.equals("SHIFT")) originalBg = PRIMARY_YELLOW;
        else if (text.equals("ALPHA")) { 
            originalBg = PRIMARY_PURPLE;
            btn.setForeground(Color.WHITE);
        }
        else originalBg = FUNC_BTN;
        
        btn.setBackground(originalBg);
        btn.setOpaque(true);
        btn.setBorderPainted(false); // Clean physical look

        // --- 2. ADD LOGIC LISTENER ---
        btn.addActionListener(e -> processInput(text));

        // --- 3. ADD FADE-IN GLOW ANIMATION ---
        // Animation variables (graphics/engineering concept)
        Color glowColor = Color.WHITE;
        btn.addMouseListener(new MouseAdapter() {
            private Timer animTimer;
            private float alpha = 0.0f;
            private boolean entering = false;

            @Override
            public void mouseEntered(MouseEvent e) {
                entering = true;
                if(animTimer != null) animTimer.stop();
                animTimer = new Timer(5, e2 -> {
                    alpha += 0.05f; // Faster fade in
                    if(alpha >= 0.5f) { // Caps glow at 50%
                        alpha = 0.5f;
                        ((Timer)e2.getSource()).stop();
                    }
                    Color interpolatedColor = interpolate(originalBg, glowColor, alpha);
                    btn.setBackground(interpolatedColor);
                });
                animTimer.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                entering = false;
                if(animTimer != null) animTimer.stop();
                animTimer = new Timer(10, e2 -> {
                    alpha -= 0.03f; // Slower fade out
                    if(alpha <= 0.0f) {
                        alpha = 0.0f;
                        ((Timer)e2.getSource()).stop();
                        btn.setBackground(originalBg);
                    } else {
                        Color interpolatedColor = interpolate(originalBg, glowColor, alpha);
                        btn.setBackground(interpolatedColor);
                    }
                });
                animTimer.start();
            }
            
            // Subtle change on press for better feedback
            @Override
            public void mousePressed(MouseEvent e) {
                 btn.setBackground(interpolate(originalBg, Color.BLACK, 0.2f));
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if(entering) {
                    btn.setBackground(interpolate(originalBg, glowColor, alpha));
                } else {
                    btn.setBackground(originalBg);
                }
            }
        });

        return btn;
    }

    /**
     * Color Interpolation helper function.
     * Computes (1-t)*c1 + t*c2 smoothly across RGB channels.
     */
    private static Color interpolate(Color c1, Color c2, float t) {
        float r = c1.getRed() / 255f + t * (c2.getRed() / 255f - c1.getRed() / 255f);
        float g = c1.getGreen() / 255f + t * (c2.getGreen() / 255f - c1.getGreen() / 255f);
        float b = c1.getBlue() / 255f + t * (c2.getBlue() / 255f - c1.getBlue() / 255f);
        return new Color(r, g, b);
    }

    /**
     * Core logic for input processing. Kept identical to v4.0.
     */
    private void processInput(String text) {
        switch (text) {
            case "AC": input.setLength(0); break;
            case "DEL": if (input.length() > 0) input.setLength(input.length() - 1); break;
            case "=": calculate(); return;
            // Standard Keyboard symbols used in calculate() for matching
            case "+": input.append("+"); break;
            case "-": input.append("-"); break;
            case "*": input.append("*"); break;
            case "/": input.append("/"); break;
            case "x^2": input.append("^2"); break;
            case "sqrt": input.append("sqrt("); break;
            case "PI": input.append("pi"); break;
            case "SHIFT": case "ALPHA": case "MODE": break; // Non-functional
            case "hyp": case "abs": default: 
                if(text.matches("sin|cos|tan|log|ln|abs|hyp")) input.append(text + "(");
                else input.append(text);
                break;
        }
        display.setText(input.toString());
    }

    /**
     * Identical calculation logic.
     */
    private void calculate() {
        try {
            String expr = input.toString();
            if (expr.isEmpty()) return;
            // net.objecthunter.exp4j does the heavy lifting
            double result = new ExpressionBuilder(expr).build().evaluate();
            String out = String.valueOf(result);
            if (out.endsWith(".0")) out = out.substring(0, out.length() - 2);
            display.setText(out);
            input.setLength(0);
            input.append(out);
        } catch (Exception e) {
            display.setText("Error");
            input.setLength(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdvancedCalculator::new);
    }
}
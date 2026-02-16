package game;

import javax.swing.*;
import java.awt.*;
import java.util.Stack;

public class Board extends JPanel {

    private JButton[][] squares = new JButton[8][8];
    private boolean whiteTurn = true;
    private JButton selected = null;
    private int selRow, selCol;

    private Stack<Move> history = new Stack<>();

    private String[][] board = {
            {"BR","BN","BB","BQ","BK","BB","BN","BR"},
            {"BP","BP","BP","BP","BP","BP","BP","BP"},
            {"","","","","","","",""},
            {"","","","","","","",""},
            {"","","","","","","",""},
            {"","","","","","","",""},
            {"WP","WP","WP","WP","WP","WP","WP","WP"},
            {"WR","WN","WB","WQ","WK","WB","WN","WR"}
    };

    public Board() {
        setLayout(new GridLayout(8, 8));
        setPreferredSize(new Dimension(640, 640));
        initBoard();
    }

    private void initBoard() {
        Font font = new Font("Arial", Font.BOLD, 18);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                JButton btn = new JButton(board[r][c]);
                btn.setFont(font);
                btn.setFocusPainted(false);
                colorSquare(btn, r, c);

                int row = r, col = c;
                btn.addActionListener(e -> handleClick(btn, row, col));

                squares[r][c] = btn;
                add(btn);
            }
        }
    }

    private void handleClick(JButton btn, int r, int c) {

        if (selected == null) {
            if (!btn.getText().isEmpty() && isCorrectTurn(btn.getText())) {
                selected = btn;
                selRow = r;
                selCol = c;
                btn.setBackground(Color.YELLOW);
            }
            return;
        }

        if (btn == selected) {
            resetColors();
            selected = null;
            return;
        }

        if (isLegalMove(selRow, selCol, r, c)) {
            makeMove(selRow, selCol, r, c);
            whiteTurn = !whiteTurn;

            if (isCheckmate(whiteTurn)) {
                JOptionPane.showMessageDialog(this,
                        (whiteTurn ? "White" : "Black") + " is CHECKMATED!");
            }
        }

        resetColors();
        selected = null;
    }

    private boolean isCorrectTurn(String piece) {
        return (whiteTurn && piece.startsWith("W")) ||
                (!whiteTurn && piece.startsWith("B"));
    }

    private boolean isLegalMove(int sr, int sc, int tr, int tc) {

        String piece = board[sr][sc];
        String target = board[tr][tc];

        if (!target.isEmpty() && target.charAt(0) == piece.charAt(0))
            return false;

        int dr = tr - sr;
        int dc = tc - sc;

        switch (piece.charAt(1)) {
            case 'P': return pawnMove(piece, sr, sc, tr, tc);
            case 'R': return straightMove(sr, sc, tr, tc);
            case 'B': return diagonalMove(sr, sc, tr, tc);
            case 'Q': return straightMove(sr, sc, tr, tc) || diagonalMove(sr, sc, tr, tc);
            case 'N': return Math.abs(dr * dc) == 2;
            case 'K': return Math.max(Math.abs(dr), Math.abs(dc)) == 1;
        }
        return false;
    }

    private boolean pawnMove(String p, int sr, int sc, int tr, int tc) {
        int dir = p.startsWith("W") ? -1 : 1;

        if (sc == tc && board[tr][tc].isEmpty())
            return tr - sr == dir;

        if (Math.abs(tc - sc) == 1 && tr - sr == dir && !board[tr][tc].isEmpty())
            return true;

        return false;
    }

    private boolean straightMove(int sr, int sc, int tr, int tc) {
        if (sr != tr && sc != tc) return false;

        int dr = Integer.compare(tr, sr);
        int dc = Integer.compare(tc, sc);

        int r = sr + dr, c = sc + dc;
        while (r != tr || c != tc) {
            if (!board[r][c].isEmpty()) return false;
            r += dr;
            c += dc;
        }
        return true;
    }

    private boolean diagonalMove(int sr, int sc, int tr, int tc) {
        if (Math.abs(tr - sr) != Math.abs(tc - sc)) return false;

        int dr = Integer.compare(tr, sr);
        int dc = Integer.compare(tc, sc);

        int r = sr + dr, c = sc + dc;
        while (r != tr) {
            if (!board[r][c].isEmpty()) return false;
            r += dr;
            c += dc;
        }
        return true;
    }

    private void makeMove(int sr, int sc, int tr, int tc) {
        history.push(new Move(sr, sc, tr, tc, board[tr][tc]));

        board[tr][tc] = board[sr][sc];
        board[sr][sc] = "";

        squares[tr][tc].setText(board[tr][tc]);
        squares[sr][sc].setText("");
    }

    private boolean isCheckmate(boolean white) {
        int kingR = -1, kingC = -1;
        String king = white ? "WK" : "BK";

        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (board[r][c].equals(king)) {
                    kingR = r;
                    kingC = c;
                }

        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (!board[r][c].isEmpty() &&
                        board[r][c].charAt(0) != king.charAt(0))
                    if (isLegalMove(r, c, kingR, kingC))
                        return true;

        return false;
    }

    public void undoMove() {
        if (history.isEmpty()) return;

        Move m = history.pop();
        board[m.sr][m.sc] = board[m.tr][m.tc];
        board[m.tr][m.tc] = m.captured;

        squares[m.sr][m.sc].setText(board[m.sr][m.sc]);
        squares[m.tr][m.tc].setText(board[m.tr][m.tc]);

        whiteTurn = !whiteTurn;
    }

    private void resetColors() {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                colorSquare(squares[r][c], r, c);
    }

    private void colorSquare(JButton b, int r, int c) {
        if ((r + c) % 2 == 0)
            b.setBackground(new Color(240, 217, 181));
        else
            b.setBackground(new Color(181, 136, 99));
    }

    private static class Move {
        int sr, sc, tr, tc;
        String captured;

        Move(int sr, int sc, int tr, int tc, String cap) {
            this.sr = sr; this.sc = sc;
            this.tr = tr; this.tc = tc;
            this.captured = cap;
        }
    }
}

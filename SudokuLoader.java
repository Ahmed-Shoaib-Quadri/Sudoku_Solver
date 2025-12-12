import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class SudokuLoader {
    JFrame f;
    private final JProgressBar progressBar;
    private final ImageIcon image;
    private final JLabel label;

    public SudokuLoader() {
        f = new JFrame();
        progressBar = new JProgressBar();

        ImageIcon tmpIcon;
        try {
            tmpIcon = new ImageIcon(new ImageIcon("Sudoku.png")
                    .getImage().getScaledInstance(700, 624, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            tmpIcon = new ImageIcon();
            e.printStackTrace();
        }
        image = tmpIcon;
        label = new JLabel(image);

        f.setLayout(null);
        f.setBounds(100, 50, 700, 700);
        f.setTitle("Sudoku Loading...");

        f.add(label);
        f.add(progressBar);

        label.setBounds(0, 0, 700, 624);

        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setBounds(0, 625, 700, 50);
        progressBar.setBackground(Color.BLACK);
        progressBar.setForeground(Color.GREEN);
        progressBar.setFont(new Font("MV Boli", Font.BOLD, 25));

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);

        startLoadingWorker();
    }

    private void startLoadingWorker() {
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i <= 100; i++) {
                    Thread.sleep(15);
                    publish(i);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int val = chunks.get(chunks.size() - 1);
                progressBar.setValue(val);
            }

            @Override
            protected void done() {
                progressBar.setValue(100);
                progressBar.setString("Done !");
                f.dispose();
                javax.swing.SwingUtilities.invokeLater(() -> new Sudoku());
            }
        };

        worker.execute();
    }
}

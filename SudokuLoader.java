import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class SudokuLoader {
    JFrame f;
    JProgressBar progressBar;
    ImageIcon image;
    JLabel label;

    private int counter=0;
    SudokuLoader(){
        f=new JFrame();
        progressBar = new JProgressBar();
        image = new ImageIcon(new ImageIcon("Sudoku.png").getImage().getScaledInstance(700, 624, Image.SCALE_SMOOTH));
        label = new JLabel(image);

        f.setLayout(null);
        f.setBounds(100,50,700,700);
        f.setTitle("Sudoku Loading...");
        
        f.add(label);
        f.add(progressBar);

        label.setBounds(0,0,700,624);
        

        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setBounds(0,625,700,50);
        progressBar.setBackground(Color.black);
        progressBar.setForeground(Color.green);
        progressBar.setFont(new Font("MV Boli",Font.BOLD,25));

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);

        fill();
    }

    private void fill(){
        while(counter<=100){
            try{
                Thread.sleep(15);
            }catch(InterruptedException ine){
                ine.printStackTrace();
            }
            progressBar.setValue(counter++);
        }
        progressBar.setString("Done !");
    }
}

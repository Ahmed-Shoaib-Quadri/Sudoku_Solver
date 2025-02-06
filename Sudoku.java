//importing all necessary AWT files and Swing files
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Sudoku implements KeyListener,ActionListener{
    //GUI Component variables
    private JFrame frame;
    private Label headingLabel;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem loadPreviousState;
    private JMenuItem saveCurrentState;
    private Label l2;
    private Label downLabel;
    private JButton solveButton;
    private JButton refreshButton;
    private JPanel p;
    private static JTextField[][] t;

    //Counter variables
    private int i;
    private int j;
    private int k=0;
    private int m=0;
    private int keyX=0;
    private int keyY=0;

    //Board to solve sudoku
    private static int[][] checkboard=new int[9][9];

    //Constructor
    Sudoku(){
        frame= new JFrame();
        headingLabel= new Label("           Sudoku Solver");
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        loadPreviousState = new JMenuItem("Load State");
        saveCurrentState = new JMenuItem("Save State");
        l2= new Label("* Use \"Arrow Keys\" to navigate and fill the cells*");
        solveButton= new JButton("Solve");
        refreshButton = new JButton("Refresh");
        p= new JPanel();
        t=new JTextField[9][9];
        downLabel = new Label();

        frame.setLayout(null);
        frame.setBounds(100,50,700,700); 
        frame.getContentPane().setBackground(new Color(0, 39, 58));
        frame.setTitle("Sudoku Solver");

        frame.add(headingLabel);
        frame.add(menuBar);
        menuBar.add(fileMenu);
        fileMenu.add(loadPreviousState);
        fileMenu.add(saveCurrentState);
        frame.add(l2);
        frame.add(p);
        frame.add(solveButton);
        frame.add(refreshButton);
        frame.add(downLabel);

        headingLabel.setBounds(0,0,700,60);
        menuBar.setBounds(0,61,700,25);
        l2.setBounds(100,90,700,50);
        p.setBounds(160,150,274,274);
        solveButton.setBounds(190,470,90,30);
        refreshButton.setBounds(310,470,90,30);
        downLabel.setBounds(20,600,700,50);

        headingLabel.setBackground(new Color(0, 65, 98));
        headingLabel.setFont(new Font("Consolas",Font.ITALIC,45));
        headingLabel.setForeground(new Color(255,255,255));
        
        loadPreviousState.setToolTipText("Loads the Previous State...");
        saveCurrentState.setToolTipText("Saves the Current State...");

        loadPreviousState.addActionListener(this);
        saveCurrentState.addActionListener(this);

        l2.setFont(new Font("Consolas",Font.ITALIC,22));
        l2.setForeground(new Color(250,250,250));
        
        p.setBackground(Color.BLACK);
        p.setLayout(null);

        solveButton.setBackground(Color.GREEN);
        refreshButton.setBackground(Color.red);
        refreshButton.setForeground(Color.white);

        solveButton.addActionListener(this);
        refreshButton.addActionListener(this);

        downLabel.setForeground(Color.white);
        downLabel.setFont(new Font("Consolas",Font.ITALIC,20));

        //Placing TextFields over the Panel (81 TextFields)
        for(i=0;i<9;i++){
            if(i%3==0&&i!=0)
                {m=m+2;}
                k=0;
            for(j=0;j<9;j++){
                t[i][j]=new JTextField();
                t[i][j].setFont(new Font("Consolas",Font.BOLD,20));
                t[i][j].addKeyListener(this);
                p.add(t[i][j]);
                if(j%3==0&&j!=0)
                {k=k+2;}
                t[i][j].setBounds(j*30+k,i*30+m,30,30);
            }
        }
        //------------------------------------------------------------//

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }//End of Constructor

    //Overriding All Methods of KeyListener
    @Override
   public void keyTyped(KeyEvent k){
    downLabel.setText("Key Typed is : "+k.getKeyChar());
   }

   @Override
   public void keyReleased(KeyEvent k){
    //...This function is left empty...
   }

   @Override
   public void keyPressed(KeyEvent k){
    int x=k.getKeyCode();

    switch(x){
        case KeyEvent.VK_RIGHT:
        if(keyY == 8)
         keyY=-1;
        t[keyX][++keyY].requestFocus();break;
        case KeyEvent.VK_LEFT:
        if(keyY == 0)
         keyY=9;
        t[keyX][--keyY].requestFocus();break;
        case KeyEvent.VK_UP:
        if(keyX == 0)
         keyX=9;
        t[--keyX][keyY].requestFocus();break;
        case KeyEvent.VK_DOWN:
        if(keyX == 8)
         keyX=-1;
        t[++keyX][keyY].requestFocus();break;
        default:
        break;
    }
   }

   //Overriding method of ActionListener
   @Override
   public void actionPerformed(ActionEvent ae){
    if(ae.getSource()==solveButton){
        try{for(i=0;i<9;i++){
            for(j=0;j<9;j++){
                if(t[i][j].getText().equals("")){
                    checkboard[i][j]=0;
                }
                else{
                    t[i][j].setBackground(new Color(200,200,200));
                    checkboard[i][j]=Integer.parseInt(t[i][j].getText());
                }
            }
        }
         helper(checkboard,0,0);
         display();
    }catch(NumberFormatException ne){
                        JOptionPane.showMessageDialog(frame,"Enter only integer numbers !\nRe-enter :( ");
                        refreshBoard();
    }

    }else if(ae.getSource()==refreshButton){
         refreshBoard();
    }else if(ae.getSource()==loadPreviousState){
        char x;
       try{
        BufferedReader br = new BufferedReader(new FileReader("Sudoku.txt"));
        for(i=0;i<9;i++){
            for(j=0;j<9;j++){
                if((x=(char)br.read())=='0'){
                    t[i][j].setText("");
                }
                else{
                    t[i][j].setText(x+"");
                }
                br.read();
            }
            br.read();
        }
        br.close();
        downLabel.setText("Previous State Loaded...");
       }catch(IOException ie){
        ie.printStackTrace();
       }
    }else if(ae.getSource()==saveCurrentState){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter("Sudoku.txt"));
            for(i=0;i<9;i++){
            for(j=0;j<9;j++){
                if(t[i][j].getText().equals("")){
                    checkboard[i][j]=0;
                    bw.write(checkboard[i][j]+" ");
                }
                else{
                    checkboard[i][j]=Integer.parseInt(t[i][j].getText());
                    bw.write(checkboard[i][j]+" ");
                }
            }
            bw.write("\n");
        }
        bw.close();
        downLabel.setText("Current State Saved...");
        }catch(IOException ie ){
            ie.printStackTrace();
        }
    }
}

//Method to refresh the board 
private void refreshBoard(){
for(i=0;i<9;i++){
            for(j=0;j<9;j++){
                t[i][j].setText("");
                t[i][j].setBackground(Color.WHITE);
                t[i][j].setForeground(Color.BLACK);
            }
        }
}

//Method to display the Solved Sudoku
public static void display(){
    for(int i=0;i<9;i++){
        for(int j=0;j<9;j++){
            t[i][j].setText(String.valueOf(checkboard[i][j]));
        }
     }
}



//Method to check wether a number is safe to place in that particular cell or not.
private static boolean isSafe(int[][] board,int row,int col,int number){
    for(int i=0;i<board.length;i++){
        if(board[i][col]==number){
            return false;
        }
    }
    for(int j=0;j<board.length;j++){
        if(board[row][j]==number){
            return false;
        }
    }
    int sr = 3*(row/3);
    int sc = 3*(col/3);
    for(int i=sr;i<sr+3;i++){
        for(int j=sc;j<sc+3;j++){
            if(board[i][j]==number){
                return false;
            }
        }
    }
    return true;
}

//Program to recursively solve sudoku using backtracking concept
private static boolean helper(int[][] board,int row,int col ){
    if(row==board.length){
        return true;
    }

    int nrow=0;
    int ncol=0;
    if(col==board.length-1){
        nrow=row+1;
        ncol=0;
    }else{
        nrow=row;
        ncol=col+1;
    }

    if(board[row][col]!=0){
        if(helper(board,nrow,ncol)){
            return true;}
        }else{
            for(int i=1;i<=9;i++){
                if(isSafe(board,row,col,i)){
                    board[row][col]=i;
                    t[row][col].setForeground(Color.RED);
                    if(helper(board,nrow,ncol))
                     return true;
                    else
                     board[row][col]=0;
                }
            }
        }
    return false;
}//End of helper method

}//End of class
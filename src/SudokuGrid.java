import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SudokuGrid extends JPanel
{
   
   JTextField[] textField = new JTextField[81];
   
   public SudokuGrid(){
       setLayout(new GridLayout(10,10));
       setBackground(Color.darkGray);
       setBorder(BorderFactory.createLineBorder(Color.gray, 3));
       for(int i = 0; i < 81; i++){
           textField[i] = new JTextField(1);
           add(textField[i]);
        }
       
       
        JButton b1 = new JButton ("Solve");
        ButtonListener listener = new ButtonListener();
        add(b1);
        b1.addActionListener(listener);
       
        JButton b2 = new JButton ("Clear");
        ButtonListener2 listenerC = new ButtonListener2();
        add(b2);
        b2.addActionListener(listenerC);
       
        JButton b3 = new JButton ("Brute");
        ButtonListener3 listenerB = new ButtonListener3();
        add(b3);
        b3.addActionListener(listenerB);
       
   }
   private class ButtonListener implements ActionListener
   {
       public void actionPerformed(ActionEvent event){
            //collect the text from textfields
           String State = "";
           for(int i = 0; i < 81; i++){
               if(textField[i].getText().isEmpty())
                   State +="_";
               else
                   State += textField[i].getText();
           }
            //call the solver
           SudokuSolver sudoku = new SudokuSolver();
           //receive the answer from the solver
           String answer = sudoku.solve(State); 
           
           //update the textfields.
           for(int i = 0; i < answer.length(); i++)
               if(State.charAt(i) == '_'){
                   textField[i].setForeground(Color.blue);
                   textField[i].setText(answer.substring(i,i+1));
               }
       }
   }
    private class ButtonListener2 implements ActionListener
    {
        public void actionPerformed(ActionEvent event){
            
            for(int i = 0; i < 81; i++){
                textField[i].setForeground(Color.black);
                textField[i].setText("");
            }
            
        }
    }
    private class ButtonListener3 implements ActionListener
    {
        public void actionPerformed(ActionEvent event){
            //collect the text from textfields
            String State = "";
            for(int i = 0; i < 81; i++){
                if(textField[i].getText().isEmpty())
                    State +="_";
                else
                    State += textField[i].getText();
            }
            //call the solver
            SudokuSolver sudoku = new SudokuSolver();
            String answer = sudoku.solve(State);
            for(int i = 0; i < answer.length(); i++)
                if(State.charAt(i) == '_'){
                    textField[i].setForeground(Color.blue);
                    textField[i].setText(answer.substring(i,i+1));
                }
        }
    }
    
}

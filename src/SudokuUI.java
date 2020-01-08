import java.awt.*;
import javax.swing.*;

public class SudokuUI
{
     public static void main(String[] args){
         JFrame frame = new JFrame("Sudoku Solver");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.getContentPane().add(new SudokuGrid());
         
         frame.pack();
         frame.setVisible(true);
         frame.setSize(500, 500);
     }
}
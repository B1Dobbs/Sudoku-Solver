import java.sql.Struct;
import java.util.*;

public class SudokuSolver {
    
    /**
   * 'main' function for solving sudoku
   * @param stupidState
   * @return the solution in string format 
   */
    public String solve(String stupidState){
       
            //Convert the string state to something more useful
            Box [][] smartState2 = convertToSmart(stupidState);

            //Reduce the domain of the state using Arc Consistency
            Box [][] ac3Solution = AC3(smartState2);

            //Solve the remaining state
            String solution = "";
            if(solve(0,0,ac3Solution)){
                solution = convertToStupid(ac3Solution);
            }
            return solution;
    }

    /**
   * AC3 function for Arc Consistency
   * @param state the state of the board initially
   * @return a 2D array of the board 
   */
    public Box [][] AC3(Box [][] state) {
        
        Box [][] temp = state;

        //Get the initial arcs between each box 
        Queue<Arc> queue = getArcsForState(state);

        //While the arcs to check are not empty, remove inconsistencies
        while(!queue.isEmpty()){
            Arc arc = queue.remove();
            if(removeInconsistenValues(arc)){
            
                //Assign the value if its the only one avaiable for the first Box in the arc
                if(arc.domain1.possible.size() == 1){
                    temp[arc.domain1.x][arc.domain1.y].value = arc.domain1.possible.get(0);
                }

                 //Assign the value if its the only one avaiable for the second Box in the arc
                if(arc.domain2.possible.size() == 1){
                    temp[arc.domain2.x][arc.domain2.y].value = arc.domain2.possible.get(0);
                }

                //Something changed at this point so re-evaluate the board
                //**Can't just add neighbors here because its cascading**
                queue.addAll(getArcsForState(temp));
            }
        }

        return temp;

    }

    /**
   * Remove any inconsistent values in an arc
   * @param arc relation between two boxes
   * @return true if something was removed
   */
    public boolean removeInconsistenValues(Arc arc){
        boolean removed = false;
        ArrayList<Integer> Xi = arc.domain1.possible; //3, 5, 6
        ArrayList<Integer> Xj = arc.domain2.possible; //3

        //Find consistency from Xi -> Xj
        for(int i = 0; i < Xi.size(); i++){
            //if there doesn't exist a value where d1 != d2, the remove that value from d1
            if(Xj.contains(Xi.get(i)) && Xj.size() == 1){
                //System.out.println("***** Removing ******");
                //System.out.println(Xi.toString());
                Xi.remove(Xi.get(i));
                removed = true;
                //System.out.println(arc.toString());
            }
        }
        
        return removed;
    }

    /**
   * Solve function for remaining state
   * @param i x coordinate
   * @param j y coordinate
   * @param state the state of the board
   * @return true if it was solved
   */
    public boolean solve(int i, int j, Box [][] state) {

        //Check if we've gone through the whole puzzle
        if (i == 9) {
            i = 0;
            if (++j == 9) 
                return true; 
        }

        //if there is a value assigned for the box, move to the next
        if (state[i][j].value != -1) 
            return solve(i+1,j,state);
        
        //For each domain (1-9)
        for (int val = 1; val <= 9; val++) {
            //make the valid move
            if (checkValidMove(i,j,val,state)) {  
                state[i][j].value = val;   
            //Solve the rest    
            if (solve(i+1,j,state))  
                return true;
            }
        }

        //Undo move
        state[i][j].value = -1; 

        return false;
    }

    /**
   * Finds the possibilities for a box based on neighbors
   * @param box 
   * @param state the current state
   */
    public void findPossibleForBox(Box box, Box[][] state){

        for(int i = 0; i < 9; i++){
            if(state[box.x][i].value != -1)
                box.possible.remove(new Integer(state[box.x][i].value));
            if(state[i][box.y].value != -1)
                box.possible.remove(new Integer(state[i][box.y].value));
        }

        //Check box
        int col = ((box.x % 3) == 0) ? box.x : box.x - (box.x % 3);
        int row = ((box.y % 3) == 0) ? box.y : box.y - (box.y % 3);
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                if (state[col+i][row+j].value != -1)
                    box.possible.remove(new Integer(state[col+i][row+j].value));
            }
        }

    }

    /**
   * Determines if a relation between two boxes already exists in the list
   * This relation goes one way, A -> B can exist and so can B -> A
   * @param one box one
   * @param two box two
   * @return returns true if the connection already exists
   */
    public Boolean doesExist(Queue<Arc> arcs, Box one, Box two){

        //Don't add if itself
        if(one.x == two.x && one.y == two.y){
            return true;
        }

        //Check the all connections one way
        for(Arc arc : arcs){
            if(arc.domain1.x == one.x && arc.domain1.y == one.y){
                if(arc.domain2.x == two.x && arc.domain2.y == two.y){
                    return true;
                }
            }
                
        }
            
        return false;
    }

     /**
   * Finds all the possible connections between the boxes on the board
   * @param state the current state
   * @return a queue with all of the connections
   */
    public Queue<Arc> getArcsForState(Box [][] state){
        Queue<Arc> arcs = new LinkedList<Arc>();

        //Iterate through every box
        for(int x = 0; x < 9; x++){ 
            for(int y = 0; y < 9; y++){
                //System.out.println("State: (" + x + "," + y + ") =" + state[x][y]);
                if(state[x][y].value == -1){
                    //Get states of neighbors if empty
                    Box currentBox = state[x][y];
                    arcs.addAll(getNeighborArcs(currentBox, state));
                }
            }
        }
        return arcs;
    }

    /**
   * Finds all of the connections for an individual box
   * @param box the box to find neighbors
   * @param state the current state
   * @return a queue with all of the connections for the box
   */
    public Queue<Arc> getNeighborArcs(Box box, Box [][] state){
        Queue<Arc> neighbors = new LinkedList<Arc>();
        //Check row and column
        for(int i = 0; i < 9; i++){
            Box row = state[box.x][i];
            if(row.value == -1 && !doesExist(neighbors, box, row)){ 
                //Add box if its empty and not already accounted for
                //System.out.println("Adding: (" + box.x + "," + i + ") =" + state[box.x][i]);
                neighbors.add(new Arc(box, row));
            }
            Box col = state[i][box.y];
            if(col.value == -1 && !doesExist(neighbors, box, col)){
                //Add box if its empty and not already accounted for
               // System.out.println("Adding: (" + i + "," + box.y + ") =" + state[i][box.y]);
                neighbors.add(new Arc(box, col));
            }
        }

        //Check the box its in
        int col = ((box.x % 3) == 0) ? box.x : box.x - (box.x % 3);
        int row = ((box.y % 3) == 0) ? box.y : box.y - (box.y % 3);
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                Box local = state[col+i][row+j];
                if (state[col+i][row+j].value == -1 && !doesExist(neighbors, box, local)){ 
                    //Add box if its empty and not already accounted for
                    //System.out.println("Adding: (" + (col + i) + "," + (row + j) + ") =" + state[col+i][row+j]);
                    neighbors.add(new Arc(box, local));
                }
            }
        }

        return neighbors;
    }

    /**
   * Checks if the potential move is valid
   * @param x x index
   * @param y y index
   * @param num the potential value
   * @param state the current state
   * @return true if the move is valid
   */
    public Boolean checkValidMove(int x, int y, int num, Box [][] state){

        //Check row and column
        for(int i = 0; i < 9; i++){
            if(num == state[x][i].value)
                return false;
            if(num == state[i][y].value)
                return false;
        }

        //Check box
        int col = ((x % 3) == 0) ? x : x - (x % 3);
	    int row = ((y % 3) == 0) ? y : y - (y % 3);
        for (int i = 0; i < 3; ++i){
            for (int j = 0; j < 3; j++){
                if (num == state[col+i][row+j].value)
                    return false;
            }
        }

        return true;
    }

    /**
   * Converts the state to something useful
   * @param stupidState the current state
   * @return a 2D array of Box objects in to represent the board
   */
    public Box [][] convertToSmart(String stupidState){
        Box [][] smartState = new Box [9][9];

        int index = 0;
        //Initialize the board
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
                smartState[i][j] = new Box(i, j);
                if(stupidState.substring(index, index + 1).equals("_")){
                    smartState[i][j].value = -1;
                }
                else{
                    smartState[i][j].value = Integer.parseInt(stupidState.substring(index, index + 1));
                }
                //System.out.print(smartState[i][j].value + " ");
                index++;
            }
            //System.out.println();
        }

        //Set possible values for each box
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
                if(smartState[i][j].value == -1){
                    findPossibleForBox(smartState[i][j], smartState);
                    //System.out.println("X: " + smartState[i][j].x + " Y: " + smartState[i][j].y + " Poss: " + smartState[i][j].possible.toString());
                }
            }
        }

        return smartState;
    }

    /**
   * Converts the state back to a string
   * @param smartState the state for the solved puzzle
   * @return a string for the 'compressed' state
   */
    public String convertToStupid(Box [][] smartState){
        String stupidState = "";
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){

                if(smartState[i][j].value == -1){
                    stupidState += "_";
                }
                else{
                    stupidState += Integer.toString(smartState[i][j].value);
                }
            }
        }

        return stupidState;
    }

    //Class to hold values of a box
    class Box{
        public int x;
        public int y;
        public int value;
        public ArrayList<Integer> possible = new ArrayList<Integer>();

        public Box(int x, int y){
            this.x = x;
            this.y = y;
            for(int i = 0; i < 9; i++){
                possible.add(i + 1);
            }
        }

        public String toString(){
            String arcString = "";
            arcString += "X: " + x + " Y: " + y + " Poss: " + possible.toString();
            return arcString;
        }
    }

    //Class to hold two Boxes to represent a connection
    class Arc{
        public Box domain1;
        public Box domain2;

        public Arc(Box one, Box two){
            domain1 = one;
            domain2 = two;
        }

        public String toString(){
            return domain1.toString() + " -> " + domain2.toString();
        }
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.controller;

import edu.oswego.cs.CPSLab.AutomotiveCPS.gui.Parameter;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.Block;
import edu.oswego.cs.CPSLab.AutomotiveCPS.utilities.Customized2DArray;
import java.util.List;

/**
 *
 * @author HN
 */
public class MapDAO {
    private List<Block> tracks;
    private Customized2DArray array;
    private String[][] board;

    public List<Block> getTracks() {
        return tracks;
    }
    public Customized2DArray getArray() {
        return array;
    }
    public String[][] getBoard() { return board; }
    public Integer[] startingLocation;

    public void setTracks(List<Block> tracks) {
        this.tracks = tracks;
        this.setArray();
        this.setBoard();       
    }

    private void setArray() {
        if (tracks==null)
            return;
        
        int i = 0;
        int j = 0;
        
        /**
         * Direction: Vertical of Horizontal
         * True: West->East or East->West
         * False: North->South or South->North.
         */
        boolean direction = true;
        
        /**
         * Positive of next index
         * True: West->East or North->South
         * False: East->West or South->North.
         */
        boolean positive = true;
        
        this.array = new Customized2DArray();

        for (Block track : this.tracks){
            int pieceId = track.getPieceId();

            /**
             * START piece.
             */
            if(Parameter.START_PIECE.contains(pieceId)){
                this.array.add(Parameter.START_FINISH,i,j,track);
                if (track.getReverse()){
                    positive = !positive;
                    j--; 
                }
                else{
                    j++ ;
                }
                continue;
            }
            
            /**
             * FINISH piece.
             */
            if(Parameter.FINISH_PIECE.contains(pieceId)){
                break;
            }
            
            /**
             * STRAIGHT piece.
             */
            if(Parameter.STRAIGHT_PIECE.contains(pieceId)){
                if (direction){
                    //horizontal
                    this.array.add(Parameter.STRAIGHT_HORIZONTAL,i,j,track);
                    if(positive){
                        j++;
                    }
                    else{
                        j--;
                    }
                }
                else{
                    this.array.add(Parameter.STRAIGHT_VERTICAL,i,j,track);
                    if(positive){
                        i++;
                    }
                    else{
                        i--;
                    }
                }
                continue;
            }
            
            /**
             * CURVED piece.
             */
            if(Parameter.CURVED_PIECE.contains(pieceId)){
                if(direction && positive){
                    if(track.getReverse()){
                        this.array.add(Parameter.CURVED_SOUTH_WEST,i,j,track);
                        positive = true;
                        direction = false;
                        i++;
                    }
                    else{
                        this.array.add(Parameter.CURVED_NORTH_WEST,i,j,track);
                        positive = false;
                        direction = false;
                        i--;
                    }                    
                }
                else if(direction && !positive){
                    if(track.getReverse()){
                        this.array.add(Parameter.CURVED_NORTH_EAST,i,j,track);
                        positive = false;
                        direction = false;
                        i--;
                    }
                    else{
                        this.array.add(Parameter.CURVED_SOUTH_EAST,i,j,track);
                        positive = true;
                        direction = false;
                        i++;
                    }                   
                }
                else if(!direction && positive){
                    if(track.getReverse()){
                        this.array.add(Parameter.CURVED_NORTH_WEST,i,j,track);
                        positive = false;
                        direction = true;
                        j--;
                    }
                    else{
                        this.array.add(Parameter.CURVED_NORTH_EAST,i,j,track);
                        positive = true;
                        direction = true;
                        j++;
                    }
                }
                else if(!direction && !positive){
                    if(track.getReverse()){
                        this.array.add(Parameter.CURVED_SOUTH_EAST,i,j,track);
                        positive = true;
                        direction = true;
                        j++;
                    }
                    else{
                        this.array.add(Parameter.CURVED_SOUTH_WEST,i,j,track);
                        positive = false;
                        direction = true;
                        j--;
                    }
                }
                continue;
            }
            if(Parameter.INTERSECTION_PIECE.contains(pieceId)){
                this.array.add(Parameter.INTERSECTION,i,j,track);
                if (direction){
                    if(positive){
                        j++;
                    }
                    else{
                        j--;
                    }
                }
                else{
                    if(positive){
                        i++;
                    }
                    else{
                        i--;
                    }
                }
                continue;
            }
        }
    }
   
    private void setBoard(){
        if (this.array==null)
            return;
        board = this.array.convertToArray();
    }
    
    public void printBoard(){
        if (this.board==null){
            System.out.println("null");
            return;
        }
        for (String[] row: this.board){
            for (String p:row){
                System.out.print(p+"\t");
            }
            System.out.print("\n");
        }
    }
    
    public Block getBlock(int i, int j){
        return this.array.getBlock(i, j);
    }
    
    public boolean containsBlock(Block block){
        return this.tracks.contains(block);
    }

    public void addStartingLocation(Integer[] location) {this.startingLocation = location; }
    
}

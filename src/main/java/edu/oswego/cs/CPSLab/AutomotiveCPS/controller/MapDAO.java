/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.controller;

import edu.oswego.cs.CPSLab.AutomotiveCPS.gui.Parameter;
import Utilities.ArrayMap;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.Block;
import java.util.List;

/**
 *
 * @author HN
 */
public class MapDAO {
    private List<Block> tracks;
    private ArrayMap array;
    private String[][] board;

    public List<Block> getTracks() {
        return tracks;
    }
    public ArrayMap getArray() {
        return array;
    }
    public String[][] getBoard() {
        return board;
    }

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
        
        this.array = new ArrayMap();
        
        for (Block track : this.tracks){
            int pieceId = track.getPieceId();
            
            /**
             * START piece.
             */
            if(Parameter.START_PIECE.contains(pieceId)){
                this.array.add(Parameter.START_FINISH,i,j);
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
                    this.array.add(Parameter.STRAIGHT_HORIZONTAL,i,j);
                    if(positive){
                        j++;
                    }
                    else{
                        j--;
                    }
                }
                else{
                    this.array.add(Parameter.STRAIGHT_VERTICAL,i,j);
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
                        this.array.add(Parameter.CURVED_SOUTH_WEST,i,j);
                        positive = true;
                        direction = false;
                        i++;
                    }
                    else{
                        this.array.add(Parameter.CURVED_NORTH_WEST,i,j);
                        positive = false;
                        direction = false;
                        i--;
                    }                    
                }
                else if(direction && !positive){
                    if(track.getReverse()){
                        this.array.add(Parameter.CURVED_NORTH_EAST,i,j);
                        positive = false;
                        direction = false;
                        i--;
                    }
                    else{
                        this.array.add(Parameter.CURVED_SOUTH_EAST,i,j);
                        positive = true;
                        direction = false;
                        i++;
                    }                   
                }
                else if(!direction && positive){
                    if(track.getReverse()){
                        this.array.add(Parameter.CURVED_NORTH_WEST,i,j);
                        positive = false;
                        direction = true;
                        j--;
                    }
                    else{
                        this.array.add(Parameter.CURVED_NORTH_EAST,i,j);
                        positive = true;
                        direction = true;
                        j++;
                    }
                }
                else if(!direction && !positive){
                    if(track.getReverse()){
                        this.array.add(Parameter.CURVED_SOUTH_EAST,i,j);
                        positive = true;
                        direction = true;
                        j++;
                    }
                    else{
                        this.array.add(Parameter.CURVED_SOUTH_WEST,i,j);
                        positive = false;
                        direction = true;
                        j--;
                    }
                }
                continue;
            }
            if(Parameter.INTERSECTION_PIECE.contains(pieceId)){
                this.array.add(Parameter.INTERSECTION,i,j);
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
    
    
}

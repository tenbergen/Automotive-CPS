/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.controller;

import Utilities.ArrayMap;
import edu.oswego.cs.CPSLab.AutomotiveCPS.gui.Parameter;
import de.adesso.anki.AnkiConnector;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.SdkModeMessage;
import de.adesso.anki.messages.SetSpeedMessage;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.Block;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author HN
 */
public class DrawMapTestProgram {
    
    List<Block> tracks = new ArrayList<>();
    String[][] board;
          
    public void addTracks(){
        tracks.add(new Block(33,false));
        tracks.add(new Block(23,false));
        tracks.add(new Block(18,false));
        tracks.add(new Block(36,false));
        tracks.add(new Block(18,false));
        tracks.add(new Block(17,false));
        tracks.add(new Block(34,false));
    }
    public void setBoard(){
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
        
        ArrayMap arrayMap = new ArrayMap();
        
        for (Block track : tracks){
            int pieceId = track.getPieceId();
            
            /**
             * START piece.
             */
            if(Parameter.START_PIECE.contains(pieceId)){
                arrayMap.add(Parameter.START_FINISH,i,j);
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
                    arrayMap.add(Parameter.STRAIGHT_HORIZONTAL,i,j);
                    if(positive){
                        j++;
                    }
                    else{
                        j--;
                    }
                }
                else{
                    arrayMap.add(Parameter.STRAIGHT_VERTICAL,i,j);
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
                        arrayMap.add(Parameter.CURVED_SOUTH_WEST,i,j);
                        positive = true;
                        direction = false;
                        i++;
                    }
                    else{
                        arrayMap.add(Parameter.CURVED_NORTH_WEST,i,j);
                        positive = false;
                        direction = false;
                        i--;
                    }                    
                }
                else if(direction && !positive){
                    if(track.getReverse()){
                        arrayMap.add(Parameter.CURVED_NORTH_EAST,i,j);
                        positive = false;
                        direction = false;
                        i--;
                    }
                    else{
                        arrayMap.add(Parameter.CURVED_SOUTH_EAST,i,j);
                        positive = true;
                        direction = false;
                        i++;
                    }                   
                }
                else if(!direction && positive){
                    if(track.getReverse()){
                        arrayMap.add(Parameter.CURVED_NORTH_WEST,i,j);
                        positive = false;
                        direction = true;
                        j--;
                    }
                    else{
                        arrayMap.add(Parameter.CURVED_NORTH_EAST,i,j);
                        positive = true;
                        direction = true;
                        j++;
                    }
                }
                else if(!direction && !positive){
                    if(track.getReverse()){
                        arrayMap.add(Parameter.CURVED_SOUTH_EAST,i,j);
                        positive = true;
                        direction = true;
                        j++;
                    }
                    else{
                        arrayMap.add(Parameter.CURVED_SOUTH_WEST,i,j);
                        positive = false;
                        direction = true;
                        j--;
                    }
                }
                continue;
            }
            if(Parameter.INTERSECTION_PIECE.contains(pieceId)){
                arrayMap.add(Parameter.INTERSECTION,i,j);
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
        
        board = arrayMap.convertToArray();
        
        for (String[] row: board){
            for (String p:row){
                System.out.print(p+"\t");
            }
            System.out.print("\n");
        }
        
        
    }
    
    public static void main(String[] args) throws IOException{
        
        /***
        * GUI Track -- 33 -- false
        * GUI Track -- 23 -- false
        * GUI Track -- 18 -- false
        * GUI Track -- 36 -- false
        * GUI Track -- 18 -- false
        * GUI Track -- 17 -- false
        * GUI Track -- 34 -- false
        ***/
        //DrawMapTestProgram program = new DrawMapTestProgram();
        //program.addTracks();
        //program.setBoard();
        
        System.out.println("Launching connector...");
        AnkiConnector anki = new AnkiConnector("192.168.1.101", 5000);
        System.out.print("...looking for cars...");
        List<Vehicle> vehicles = anki.findVehicles();

        if (vehicles.isEmpty()) {
            System.out.println(" NO CARS FOUND. I guess that means we're done.");

        } else {
            System.out.println(" FOUND " + vehicles.size() + " CARS! They are:");

            Iterator<Vehicle> iter = vehicles.iterator();
            while (iter.hasNext()) {
                Vehicle v = iter.next();
                System.out.println("   " + v);
                System.out.println("      ID: " + v.getAdvertisement().getIdentifier());
                System.out.println("      Model: " + v.getAdvertisement().getModel());
                System.out.println("      Model ID: " + v.getAdvertisement().getModelId());
                System.out.println("      Product ID: " + v.getAdvertisement().getProductId());
                System.out.println("      Address: " + v.getAddress());
                System.out.println("      Color: " + v.getColor());
                System.out.println("      charging? " + v.getAdvertisement().isCharging());
                v.connect();
                v.sendMessage(new SdkModeMessage());
                v.sendMessage(new SetSpeedMessage(500, 100));
            }
        }
        
    }
}

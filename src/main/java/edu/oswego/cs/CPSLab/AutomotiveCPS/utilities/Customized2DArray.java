/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.utilities;

import edu.oswego.cs.CPSLab.AutomotiveCPS.gui.Parameter;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.Block;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author HN
 */
public class Customized2DArray {
    
    private List<String> keys = new ArrayList<>();
    private List<Block> blocks = new ArrayList<>();
    private List<String> data = new ArrayList<String>();
    private List<Integer> row = new ArrayList<Integer>();
    private List<Integer> col = new ArrayList<Integer>();
    
    private boolean normalize = false;
    
    public void add(String c, int i, int j, Block block){
        this.data.add(c);
        this.row.add(i);
        this.col.add(j);
        this.blocks.add(block);
    }
    
    public Block getBlock(int i, int j){
        int index = this.keys.indexOf(i+"//"+j);
        return this.blocks.get(index);
    }
    
    public void print(){
        int n = this.data.size();
        for (int i=0;i<n;i++){
            System.out.println(this.row.get(i)+","+this.col.get(i)+"="+this.data.get(i));
        }
    }
    
    public void normalizeIndex(){
        int i_min = this.minRowIndex();
        int i_gap = (i_min<0)?-i_min:0;
        
        int j_min = this.minColumnIndex();
        int j_gap = (j_min<0)?-j_min:0;
        
        for(int k=0;k<this.data.size();k++){
            int nx = this.row.get(k)+i_gap;
            int ny = this.col.get(k)+j_gap;
            this.row.set(k, nx);
            this.col.set(k, ny);
            this.keys.add(k,nx+"//"+ny);
        }      
        this.normalize = true;
        
        System.out.println("Row: "+this.row.toString());
        System.out.println("Col: "+this.col.toString());
    }
    public String[][] convertToArray(){
        if (!this.normalize){
            normalizeIndex();
        }
        
        String[][] array = new String[this.maxRowIndex()+1][this.maxColumnIndex()+1];
        
        for(int k=0;k<this.data.size();k++){
            array[this.row.get(k)][this.col.get(k)] = this.data.get(k);
        }
      
        return array;
    }
    
    public int maxRowIndex(){
        int i_max = 0;
        for(int i:this.row){
            if (i>i_max)
                i_max = i;              
        }
        return i_max;
    }
    
    public int minRowIndex(){
        int i_min = 0;
        for(int i:this.row){
            if (i<i_min)
                i_min = i;              
        }
        return i_min;
    }
    
    public int maxColumnIndex(){
        int j_max = 0;
        for(int j:this.col){
            if (j>j_max)
                j_max = j;              
        }
        return j_max;
    }
    
    public int minColumnIndex(){
        int j_min = 0;
        for(int j:this.col){
            if (j<j_min)
                j_min = j;              
        }
        return j_min;
    }

    public List<String> getKeys() { return keys; }

}

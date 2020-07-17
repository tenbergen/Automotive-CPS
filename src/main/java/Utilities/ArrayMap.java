/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author HN
 */
public class ArrayMap {
    
    private List<String> data = new ArrayList<String>();
    private List<Integer> row = new ArrayList<Integer>();
    private List<Integer> col = new ArrayList<Integer>();
    
    public ArrayMap(){
    }
    
    public void add(String c, int i, int j){
        this.data.add(c);
        this.row.add(i);
        this.col.add(j);
    }
    
    public String get(int i, int j){
        int index_row = this.row.indexOf(i);
        int index_col = this.col.indexOf(j);
        if (index_row!=index_col)
            return null;
        return this.data.get(index_row);
    }
    
    public void print(){
        int n = this.data.size();
        for (int i=0;i<n;i++){
            System.out.println(this.row.get(i)+","+this.col.get(i)+"="+this.data.get(i));
        }
    }
    
    public String[][] convertToArray(){
        int i_min = this.minRowIndex();
        int i_gap = (i_min<0)?-i_min:0;
        
        int j_min = this.minColumnIndex();
        int j_gap = (j_min<0)?-j_min:0;
        
        /*System.out.println("i_max: "+this.maxRowIndex());
        System.out.println("i_min: "+this.minRowIndex());
        System.out.println("i_gap: "+i_gap);
        
        System.out.println("j_max: "+this.maxColumnIndex());
        System.out.println("j_min: "+this.minColumnIndex());        
        System.out.println("j_gap: "+j_gap);*/
        
        String[][] array = new String[this.maxRowIndex()+i_gap+1][this.maxColumnIndex()+j_gap+1];
        
        for(int k=0;k<this.data.size();k++){
            array[this.row.get(k)+i_gap][this.col.get(k)+j_gap] = this.data.get(k);
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
}

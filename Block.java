/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.oswego.cs.CPSLab.anki;

import de.adesso.anki.roadmap.Section;
import de.adesso.anki.roadmap.roadpieces.Roadpiece;

/**
 *
 * @author notebook
 */
public class Block {

        private Integer a;
        private Boolean b;
        private Roadpiece c;
        private Section d;

        public Block(Integer a, Boolean b) {
            this.a = a;
            this.b = b;
        }

        public Integer getPieceId() {
            return a;
        }
        
        public Boolean getReverse() {
            return b;
        }
        
        public Roadpiece getPiece() {
            return c;
        }
        
        public Section getSection() {
            return d;
        }
        
        public boolean isCorrespondingPiece(Integer a, Boolean b) {
            return this.a == a && this.b == b;
        }

        public void assignPiece(Roadpiece c) {
            this.c = c;
        }

        public void assignSection(Section d) {
            this.d = d;
        }
        
        public void assignPieceId(Integer a) {
            this.a = a;
        }

        public String toString() {
            return "(" + a + "," + b + "," + c + "," + d + ")";
        }
    }
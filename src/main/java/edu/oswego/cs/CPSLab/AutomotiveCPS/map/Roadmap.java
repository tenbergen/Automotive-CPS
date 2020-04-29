/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.map;

import de.adesso.anki.roadmap.Section;
import java.util.ArrayList;

/**
 *
 * @author notebook
 */
public class Roadmap {

        private ArrayList<Block> roadmap;

        public Roadmap() {
            roadmap = new ArrayList<>();
        }

        public void add(Block block) {
            roadmap.add(block);
        }

        public Block get(int index) {
            return roadmap.get(index);
        }
        
        public int indexOf(int pieceId) {
            for (Block block : roadmap) {
                if (pieceId == block.getPieceId()) {
                    return roadmap.indexOf(block);
                }
            }
            return -1;
        }

        public int size() {
            return roadmap.size();
        }

        public boolean member(Integer pieceId, Boolean reverse) {
            for (Block block : roadmap) {
                if (pieceId == block.getPieceId() && reverse == block.getReverse()) {
                    return true;
                }
            }
            return false;
        }

        public Section lookup(Integer pieceId, Boolean reverse) {
            for (Block block : roadmap) {
                if (pieceId == block.getPieceId() && reverse == block.getReverse()) {
                    return block.getSection();
                }
            }
            return null;
        }
        
        public boolean duplicate(Integer pieceId) {
            int counter = 0;
            for (Block block : roadmap) {
                if (block.getPieceId() == pieceId) {
                    counter++;
                }
            }
            if (counter == 1) {
                return false;
            } else {
                return true;
            }
        }
        
        public int getBySection(Section s) {
            for (Block block : roadmap) {
                if (block.getSection() == s) {
                    return roadmap.indexOf(block);
                }
            }
            return -1;
        }
    }
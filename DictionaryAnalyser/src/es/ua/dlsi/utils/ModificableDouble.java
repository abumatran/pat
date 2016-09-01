/**************************************************************************
 DictionaryAnalyser - Package based in DixTools and created to provide a set
               of tools that ease the addition of new entries to dictionaries
               and helps to analyse the dictionaries.

 Copyright (C) 2011-2012 Universitat d'Alacant [www.ua.es]

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package es.ua.dlsi.utils;

/**
 * This class represents contains a double variable which can be modified. It is
 * thought for using it in Lists or Maps.
 * @author Miquel Esplà Gomis
 * @version 1.0
 */

public class ModificableDouble{
    /** Double value */
    double value;

    /**
     * Default constructor of the class.
     */
    public ModificableDouble() {
        this.value = 0.0;
    }

    /**
     * Method that returns the value of the double value.
     * @return Returns the value of the double object.
     */
    public double getValue() {
        return value;
    }

    /**
     * Method that asigns a new value to the object.
     * @param value New value for the object.
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Method that implements the add operation.
     * @param value Value which will be added to the current value of the object.
     */
    public void add(double value){
        this.value+=value;
    }

    /**
     * Method that increments (+1) the current value of the object.
     */
    public void increment(){
        this.value++;
    }
}

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

package es.ua.dlsi.id3;

import es.ua.dlsi.utils.CmdLineParser;

/**
 * Main class that reads the instances from a text file and builds an ID3 tree
 * by generating the output in dot format.
 * @author Miquel Esplà Gomis
 */
public class BuildTreeFromFile {

    /**
     * Main method that process the input file and outputs the ID3 tree in dot
     * format. This method reads from command line the path to the input file
     * (option -i or --instances-file) and output a dot file containing the tree.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option oinstances = parser.addStringOption('i',"instances-file");
        
        try{
            parser.parse(args);
        }
        catch(CmdLineParser.IllegalOptionValueException e){
            System.err.println(e);
            System.exit(-1);
        }
        catch(CmdLineParser.UnknownOptionException e){
            System.err.println(e);
            System.exit(-1);
        }

        String instances=(String)parser.getOptionValue(oinstances,null);
        
        if(instances==null){
            System.err.println("A instances file must be defined (option -i)");
            System.exit(-1);
        }

        InstanceCollection records;

        // read in all our data
        records=new InstanceCollection();
        records.buildInstances(instances);

        Tree t = new Tree(records);

        t.Print(null);
    }
}

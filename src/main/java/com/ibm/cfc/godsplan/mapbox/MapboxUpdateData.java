/* _______________________________________________________ {COPYRIGHT-TOP} _____
 * IBM Confidential
 * IBM Lift CLI Source Materials
 *
 * (C) Copyright IBM Corp. 2017  All Rights Reserved.
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 * _______________________________________________________ {COPYRIGHT-END} _____*/

package com.ibm.cfc.godsplan.mapbox;

/**
 *
 */
public class MapboxUpdateData
{
   private final String dataset;
   private final String tilesetName;
   private final String tileset;

   /**
    * @param dataset 
    * @param tilesetName 
    * @param tileset 
    */
   public MapboxUpdateData(String dataset, String tileset, String tilesetName)
   {
      this.dataset = dataset;
      this.tilesetName = tilesetName;
      this.tileset = tileset;
   }

   /**
    * @return dataset
    */
   public String getDataset()
   {
      return this.dataset;
   }

   /**
    * @return tileset name
    */
   public String getTilesetName()
   {
      return this.tilesetName;
   }

   /**
    * @return tileset
    */
   public String getTileset()
   {
      return this.tileset;
   }
}

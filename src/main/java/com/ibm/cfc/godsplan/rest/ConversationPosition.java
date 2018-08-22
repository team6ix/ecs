/* _______________________________________________________ {COPYRIGHT-TOP} _____
 * IBM Confidential
 * IBM Lift CLI Source Materials
 *
 * (C) Copyright IBM Corp. 2018  All Rights Reserved.
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 * _______________________________________________________ {COPYRIGHT-END} _____*/

package com.ibm.cfc.godsplan.rest;

/**
 * an enum to denote the important conversation positions we need to keep track of.
 */
public enum ConversationPosition
{

   /***/
   ADDRESS("node_1_1534430223721"),
   /***/
   ADDRESS_CONFIRMATION("test2"),
   /***/
   OTHER("");

   private final String nodeID;

   private ConversationPosition(String watsonAssistantNodeID)
   {
      this.nodeID = watsonAssistantNodeID;
   }

   /**
    * @param nodeID
    * @return ConversationPosition that matches the nodeID
    */
   public static ConversationPosition getPosition(String nodeID)
   {
      ConversationPosition pos = ConversationPosition.OTHER;
      for (ConversationPosition position : ConversationPosition.values())
      {
         if (nodeID.contains(position.getNodeID()))
         {
            pos = position;
            break;
         }
      }
      return pos;
   }

   /**
    * @return this {@link ConversationPosition} nodeID
    */
   public String getNodeID()
   {
      return nodeID;
   }

}

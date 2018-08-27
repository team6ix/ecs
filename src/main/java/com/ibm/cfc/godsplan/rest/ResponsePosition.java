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
 * an enum to denote the important conversation positions we need to keep track
 * of.
 */
public enum ResponsePosition
{
	/** first response */
	ASSISTANCE_CONFIRMATION("Welcome"),
	/** second response */
	ADDRESS_INPUT("node_1_1534430223721"),
	/** third response */
	ADDRESS_CONFIRMATION("node_2_1534964907188"),
	/** fourth response */
	INJURY_CONFIRMATION("node_4_1534430386010"),
	/***/
	HAS_SPACE_IN_VEHICLE("node_4_1534459146359"),
	/***/
	HAS_VEHICLE("node_2_1534459022264"),
	/***/
	OTHER("");

	private final String nodeID;

	private ResponsePosition(String watsonAssistantNodeID)
	{
		this.nodeID = watsonAssistantNodeID;
	}

	/**
	 * @param nodeID
	 * @return ConversationPosition that matches the nodeID
	 */
	public static ResponsePosition getPosition(String nodeID)
	{
		ResponsePosition pos = ResponsePosition.OTHER;
		for (ResponsePosition position : ResponsePosition.values())
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
	 * @return this {@link ResponsePosition} nodeID
	 */
	public String getNodeID()
	{
		return nodeID;
	}

}

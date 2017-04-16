package Agent.src.Add;

import java.io.BufferedReader;
import java.util.ArrayList;

import java.util.Arrays;

import java.util.HashMap;

import java.util.HashSet;

import java.util.LinkedList;

import java.util.Map;

import java.util.Queue;

import java.util.Random;

import java.util.Set;

import java.util.logging.Level;

import java.util.logging.Logger;


import se.sics.isl.transport.Transportable;

import se.sics.tasim.aw.Agent;

import se.sics.tasim.aw.Message;

import se.sics.tasim.props.SimulationStatus;

import se.sics.tasim.props.StartInfo;

import tau.tac.adx.ads.properties.AdType;

import tau.tac.adx.demand.CampaignStats;

import tau.tac.adx.devices.Device;

import tau.tac.adx.props.AdxBidBundle;

import tau.tac.adx.props.AdxQuery;

import tau.tac.adx.props.PublisherCatalog;

import tau.tac.adx.props.PublisherCatalogEntry;

import tau.tac.adx.props.ReservePriceInfo;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.demand.AdNetBidMessage;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.CampaignReport;
import tau.tac.adx.report.demand.CampaignReportKey;
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReport;
import tau.tac.adx.report.publisher.AdxPublisherReport;
import tau.tac.adx.report.publisher.AdxPublisherReportEntry;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.BankStatus;


public class ReadUserData {
	Map<AudienceOrientation , Double> publisherAudienceOrientation; //The string is a publisher's(web site's) name
	
	public ReadUserData(){
		readPublisherOrientationData();
	}

	//maybe we'll have to change the code to the names in the game

	void readPublisherOrientationData(){
		publisherAudienceOrientation = new HashMap<AudienceOrientation , Double>();

		publisherAudienceOrientation.put(new AudienceOrientation("Yahoo" , MarketSegment.FEMALE) , 0.504);
		publisherAudienceOrientation.put(new AudienceOrientation("Yahoo" , MarketSegment.MALE) , 0.496);
		publisherAudienceOrientation.put(new AudienceOrientation("Yahoo" , MarketSegment.YOUNG) , 0.46);
		publisherAudienceOrientation.put(new AudienceOrientation("Yahoo" , MarketSegment.OLD) , 0.54);
		publisherAudienceOrientation.put(new AudienceOrientation("Yahoo" , MarketSegment.LOW_INCOME) , 0.8);
		publisherAudienceOrientation.put(new AudienceOrientation("Yahoo" , MarketSegment.HIGH_INCOME) , 0.2);

		publisherAudienceOrientation.put(new AudienceOrientation("CNN" , MarketSegment.FEMALE) , 0.514);
		publisherAudienceOrientation.put(new AudienceOrientation("CNN" , MarketSegment.MALE) , 0.486);
		publisherAudienceOrientation.put(new AudienceOrientation("CNN" , MarketSegment.YOUNG) , 0.43);
		publisherAudienceOrientation.put(new AudienceOrientation("CNN" , MarketSegment.OLD) , 0.57);
		publisherAudienceOrientation.put(new AudienceOrientation("CNN" , MarketSegment.LOW_INCOME) , 0.75);
		publisherAudienceOrientation.put(new AudienceOrientation("CNN" , MarketSegment.HIGH_INCOME) , 0.25);

		publisherAudienceOrientation.put(new AudienceOrientation("NY Times" , MarketSegment.FEMALE) , 0.524);
		publisherAudienceOrientation.put(new AudienceOrientation("NY Times" , MarketSegment.MALE) , 0.476);
		publisherAudienceOrientation.put(new AudienceOrientation("NY Times" , MarketSegment.YOUNG) , 0.41);
		publisherAudienceOrientation.put(new AudienceOrientation("NY Times" , MarketSegment.OLD) , 0.59);
		publisherAudienceOrientation.put(new AudienceOrientation("NY Times" , MarketSegment.LOW_INCOME) , 0.73);
		publisherAudienceOrientation.put(new AudienceOrientation("NY Times" , MarketSegment.HIGH_INCOME) , 0.27);

		publisherAudienceOrientation.put(new AudienceOrientation("Hfngtn" , MarketSegment.FEMALE) , 0.534);
		publisherAudienceOrientation.put(new AudienceOrientation("Hfngtn" , MarketSegment.MALE) , 0.466);
		publisherAudienceOrientation.put(new AudienceOrientation("Hfngtn" , MarketSegment.YOUNG) , 0.43);
		publisherAudienceOrientation.put(new AudienceOrientation("Hfngtn" , MarketSegment.OLD) , 0.57);
		publisherAudienceOrientation.put(new AudienceOrientation("Hfngtn" , MarketSegment.LOW_INCOME) , 0.74);
		publisherAudienceOrientation.put(new AudienceOrientation("Hfngtn" , MarketSegment.HIGH_INCOME) , 0.26);
//-----------
		publisherAudienceOrientation.put(new AudienceOrientation("MSN" , MarketSegment.FEMALE) , 0.524);
		publisherAudienceOrientation.put(new AudienceOrientation("MSN" , MarketSegment.MALE) , 0.446);
		publisherAudienceOrientation.put(new AudienceOrientation("MSN" , MarketSegment.YOUNG) , 0.43);
		publisherAudienceOrientation.put(new AudienceOrientation("MSN" , MarketSegment.OLD) , 0.57);
		publisherAudienceOrientation.put(new AudienceOrientation("MSN" , MarketSegment.LOW_INCOME) , 0.76);
		publisherAudienceOrientation.put(new AudienceOrientation("MSN" , MarketSegment.HIGH_INCOME) , 0.24);

		publisherAudienceOrientation.put(new AudienceOrientation("Fox" , MarketSegment.FEMALE) , 0.514);
		publisherAudienceOrientation.put(new AudienceOrientation("Fox" , MarketSegment.MALE) , 0.486);
		publisherAudienceOrientation.put(new AudienceOrientation("Fox" , MarketSegment.YOUNG) , 0.41);
		publisherAudienceOrientation.put(new AudienceOrientation("Fox" , MarketSegment.OLD) , 0.59);
		publisherAudienceOrientation.put(new AudienceOrientation("Fox" , MarketSegment.LOW_INCOME) , 0.72);
		publisherAudienceOrientation.put(new AudienceOrientation("Fox" , MarketSegment.HIGH_INCOME) , 0.28);

		publisherAudienceOrientation.put(new AudienceOrientation("Amazon" , MarketSegment.FEMALE) , 0.524);
		publisherAudienceOrientation.put(new AudienceOrientation("Amazon" , MarketSegment.MALE) , 0.416);
		publisherAudienceOrientation.put(new AudienceOrientation("Amazon" , MarketSegment.YOUNG) , 0.41);
		publisherAudienceOrientation.put(new AudienceOrientation("Amazon" , MarketSegment.OLD) , 0.59);
		publisherAudienceOrientation.put(new AudienceOrientation("Amazon" , MarketSegment.LOW_INCOME) , 0.77);
		publisherAudienceOrientation.put(new AudienceOrientation("Amazon" , MarketSegment.HIGH_INCOME) , 0.23);

		publisherAudienceOrientation.put(new AudienceOrientation("Ebay" , MarketSegment.FEMALE) , 0.514);
		publisherAudienceOrientation.put(new AudienceOrientation("Ebay" , MarketSegment.MALE) , 0.486);
		publisherAudienceOrientation.put(new AudienceOrientation("Ebay" , MarketSegment.YOUNG) , 0.41);
		publisherAudienceOrientation.put(new AudienceOrientation("Ebay" , MarketSegment.OLD) , 0.59);
		publisherAudienceOrientation.put(new AudienceOrientation("Ebay" , MarketSegment.LOW_INCOME) , 0.77);
		publisherAudienceOrientation.put(new AudienceOrientation("Ebay" , MarketSegment.HIGH_INCOME) , 0.23);

		publisherAudienceOrientation.put(new AudienceOrientation("Wal-Mart" , MarketSegment.FEMALE) , 0.544);
		publisherAudienceOrientation.put(new AudienceOrientation("Wal-Mart" , MarketSegment.MALE) , 0.456);
		publisherAudienceOrientation.put(new AudienceOrientation("Wal-Mart" , MarketSegment.YOUNG) , 0.39);
		publisherAudienceOrientation.put(new AudienceOrientation("Wal-Mart" , MarketSegment.OLD) , 0.61);
		publisherAudienceOrientation.put(new AudienceOrientation("Wal-Mart" , MarketSegment.LOW_INCOME) , 0.75);
		publisherAudienceOrientation.put(new AudienceOrientation("Wal-Mart" , MarketSegment.HIGH_INCOME) , 0.25);

		publisherAudienceOrientation.put(new AudienceOrientation("Target" , MarketSegment.FEMALE) , 0.544);
		publisherAudienceOrientation.put(new AudienceOrientation("Target" , MarketSegment.MALE) , 0.456);
		publisherAudienceOrientation.put(new AudienceOrientation("Target" , MarketSegment.YOUNG) , 0.44);
		publisherAudienceOrientation.put(new AudienceOrientation("Target" , MarketSegment.OLD) , 0.56);
		publisherAudienceOrientation.put(new AudienceOrientation("Target" , MarketSegment.LOW_INCOME) , 0.72);
		publisherAudienceOrientation.put(new AudienceOrientation("Target" , MarketSegment.HIGH_INCOME) , 0.28);

		publisherAudienceOrientation.put(new AudienceOrientation("BestBuy" , MarketSegment.FEMALE) , 0.524);
		publisherAudienceOrientation.put(new AudienceOrientation("BestBuy" , MarketSegment.MALE) , 0.476);
		publisherAudienceOrientation.put(new AudienceOrientation("BestBuy" , MarketSegment.YOUNG) , 0.41);
		publisherAudienceOrientation.put(new AudienceOrientation("BestBuy" , MarketSegment.OLD) , 0.59);
		publisherAudienceOrientation.put(new AudienceOrientation("BestBuy" , MarketSegment.LOW_INCOME) , 0.725);
		publisherAudienceOrientation.put(new AudienceOrientation("BestBuy" , MarketSegment.HIGH_INCOME) , 0.275);

		publisherAudienceOrientation.put(new AudienceOrientation("Sears" , MarketSegment.FEMALE) , 0.534);
		publisherAudienceOrientation.put(new AudienceOrientation("Sears" , MarketSegment.MALE) , 0.466);
		publisherAudienceOrientation.put(new AudienceOrientation("Sears" , MarketSegment.YOUNG) , 0.38);
		publisherAudienceOrientation.put(new AudienceOrientation("Sears" , MarketSegment.OLD) , 0.62);
		publisherAudienceOrientation.put(new AudienceOrientation("Sears" , MarketSegment.LOW_INCOME) , 0.7);
		publisherAudienceOrientation.put(new AudienceOrientation("Sears" , MarketSegment.HIGH_INCOME) , 0.3);

		publisherAudienceOrientation.put(new AudienceOrientation("WebMD" , MarketSegment.FEMALE) , 0.544);
		publisherAudienceOrientation.put(new AudienceOrientation("WebMD" , MarketSegment.MALE) , 0.456);
		publisherAudienceOrientation.put(new AudienceOrientation("WebMD" , MarketSegment.YOUNG) , 0.4);
		publisherAudienceOrientation.put(new AudienceOrientation("WebMD" , MarketSegment.OLD) , 0.6);
		publisherAudienceOrientation.put(new AudienceOrientation("WebMD" , MarketSegment.LOW_INCOME) , 0.725);
		publisherAudienceOrientation.put(new AudienceOrientation("WebMD" , MarketSegment.HIGH_INCOME) , 0.275);

		publisherAudienceOrientation.put(new AudienceOrientation("EHow" , MarketSegment.FEMALE) , 0.524);
		publisherAudienceOrientation.put(new AudienceOrientation("EHow" , MarketSegment.MALE) , 0.476);
		publisherAudienceOrientation.put(new AudienceOrientation("EHow" , MarketSegment.YOUNG) , 0.41);
		publisherAudienceOrientation.put(new AudienceOrientation("EHow" , MarketSegment.OLD) , 0.59);
		publisherAudienceOrientation.put(new AudienceOrientation("EHow" , MarketSegment.LOW_INCOME) , 0.77);
		publisherAudienceOrientation.put(new AudienceOrientation("EHow" , MarketSegment.HIGH_INCOME) , 0.23);

		publisherAudienceOrientation.put(new AudienceOrientation("Ask" , MarketSegment.FEMALE) , 0.514);
		publisherAudienceOrientation.put(new AudienceOrientation("Ask" , MarketSegment.MALE) , 0.486);
		publisherAudienceOrientation.put(new AudienceOrientation("Ask" , MarketSegment.YOUNG) , 0.39);
		publisherAudienceOrientation.put(new AudienceOrientation("Ask" , MarketSegment.OLD) , 0.61);
		publisherAudienceOrientation.put(new AudienceOrientation("Ask" , MarketSegment.LOW_INCOME) , 0.78);
		publisherAudienceOrientation.put(new AudienceOrientation("Ask" , MarketSegment.HIGH_INCOME) , 0.22);

		publisherAudienceOrientation.put(new AudienceOrientation("TripAdvicor" , MarketSegment.FEMALE) , 0.534);
		publisherAudienceOrientation.put(new AudienceOrientation("TripAdvicor" , MarketSegment.MALE) , 0.466);
		publisherAudienceOrientation.put(new AudienceOrientation("TripAdvicor" , MarketSegment.YOUNG) , 0.42);
		publisherAudienceOrientation.put(new AudienceOrientation("TripAdvicor" , MarketSegment.OLD) , 0.58);
		publisherAudienceOrientation.put(new AudienceOrientation("TripAdvicor" , MarketSegment.LOW_INCOME) , 0.725);
		publisherAudienceOrientation.put(new AudienceOrientation("TripAdvicor" , MarketSegment.HIGH_INCOME) , 0.275);

		publisherAudienceOrientation.put(new AudienceOrientation("CNet" , MarketSegment.FEMALE) , 0.494);
		publisherAudienceOrientation.put(new AudienceOrientation("CNet" , MarketSegment.MALE) , 0.506);
		publisherAudienceOrientation.put(new AudienceOrientation("CNet" , MarketSegment.YOUNG) , 0.43);
		publisherAudienceOrientation.put(new AudienceOrientation("CNet" , MarketSegment.OLD) , 0.57);
		publisherAudienceOrientation.put(new AudienceOrientation("CNet" , MarketSegment.LOW_INCOME) , 0.745);
		publisherAudienceOrientation.put(new AudienceOrientation("CNet" , MarketSegment.HIGH_INCOME) , 0.255);

		publisherAudienceOrientation.put(new AudienceOrientation("Weather" , MarketSegment.FEMALE) , 0.524);
		publisherAudienceOrientation.put(new AudienceOrientation("Weather" , MarketSegment.MALE) , 0.476);
		publisherAudienceOrientation.put(new AudienceOrientation("Weather" , MarketSegment.YOUNG) , 0.41);
		publisherAudienceOrientation.put(new AudienceOrientation("Weather" , MarketSegment.OLD) , 0.59);
		publisherAudienceOrientation.put(new AudienceOrientation("Weather" , MarketSegment.LOW_INCOME) , 0.72);
		publisherAudienceOrientation.put(new AudienceOrientation("Weather" , MarketSegment.HIGH_INCOME) , 0.28);
	}

	
	public double getUserOrientation(String publisher , Set<MarketSegment> ms){
		double prob = 1.0;
		for(MarketSegment segment : ms){
			prob *= publisherAudienceOrientation.get(new AudienceOrientation(publisher , segment));
		}
		return prob;
	}
	
	private class AudienceOrientation{
		String publisherName;
		MarketSegment segment;
		
		public AudienceOrientation(String publisher , MarketSegment segment){
			this.publisherName = publisher;
			this.segment = segment;
		}
		@Override
		public boolean equals(Object other){
			if(other == null){return false;}
			if(other == this){return true;}
			if(!(other instanceof AudienceOrientation)){return false;}
			AudienceOrientation otherObj = (AudienceOrientation) other;
			if(!otherObj.publisherName.equals(this)){return false;}
			if(!otherObj.segment.equals(this)){return false;}
			return true;
		}
	}
	
}

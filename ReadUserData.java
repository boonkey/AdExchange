
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
	Map<String , Map<MarketSegment , Double>> publisherAudienceOrientation; //The string is a publisher's(web site's) name
	
	public ReadUserData(){
		readPublisherOrientationData();
	}

	//maybe we'll have to change the code to the names in the game

	void readPublisherOrientationData(){
		publisherAudienceOrientation = new HashMap<String , Map<MarketSegment , Double>>();

		Map<MarketSegment , Double> yahooMap = new HashMap<MarketSegment , Double>();
		yahooMap.put(MarketSegment.FEMALE , 0.504);
		yahooMap.put(MarketSegment.MALE , 0.496);
		yahooMap.put(MarketSegment.YOUNG , 0.46);
		yahooMap.put(MarketSegment.OLD , 0.54);
		yahooMap.put(MarketSegment.LOW_INCOME , 0.8);
		yahooMap.put(MarketSegment.HIGH_INCOME , 0.2);

		publisherAudienceOrientation.put("yahoo", yahooMap);

		Map<MarketSegment , Double> cnnMap = new HashMap<MarketSegment , Double>();
		cnnMap.put(MarketSegment.FEMALE , 0.514);
		cnnMap.put(MarketSegment.MALE , 0.486);
		cnnMap.put(MarketSegment.YOUNG , 0.43);
		cnnMap.put(MarketSegment.OLD , 0.57);
		cnnMap.put(MarketSegment.LOW_INCOME , 0.75);
		cnnMap.put(MarketSegment.HIGH_INCOME , 0.25);
		publisherAudienceOrientation.put("cnn", cnnMap);
		
		Map<MarketSegment , Double> nyTimesMap = new HashMap<MarketSegment , Double>();
		nyTimesMap.put(MarketSegment.FEMALE , 0.524);
		nyTimesMap.put(MarketSegment.MALE , 0.476);
		nyTimesMap.put(MarketSegment.YOUNG , 0.41);
		nyTimesMap.put(MarketSegment.OLD , 0.59);
		nyTimesMap.put(MarketSegment.LOW_INCOME , 0.73);
		nyTimesMap.put(MarketSegment.HIGH_INCOME , 0.27);
		publisherAudienceOrientation.put("nyt", nyTimesMap);
		
		Map<MarketSegment , Double> hfngtnMap = new HashMap<MarketSegment , Double>();		
		hfngtnMap.put(MarketSegment.FEMALE , 0.534);
		hfngtnMap.put(MarketSegment.MALE , 0.466);
		hfngtnMap.put(MarketSegment.YOUNG , 0.43);
		hfngtnMap.put(MarketSegment.OLD , 0.57);
		hfngtnMap.put(MarketSegment.LOW_INCOME , 0.74);
		hfngtnMap.put(MarketSegment.HIGH_INCOME , 0.26);
		publisherAudienceOrientation.put("hfn", hfngtnMap);

		Map<MarketSegment , Double> msnMap = new HashMap<MarketSegment , Double>();		
		msnMap.put(MarketSegment.FEMALE , 0.524);
		msnMap.put(MarketSegment.MALE , 0.446);
		msnMap.put(MarketSegment.YOUNG , 0.43);
		msnMap.put(MarketSegment.OLD , 0.57);
		msnMap.put(MarketSegment.LOW_INCOME , 0.76);
		msnMap.put(MarketSegment.HIGH_INCOME , 0.24);
		publisherAudienceOrientation.put("msn", msnMap);
		
		Map<MarketSegment , Double> foxMap = new HashMap<MarketSegment , Double>();		
		foxMap.put(MarketSegment.FEMALE , 0.514);
		foxMap.put(MarketSegment.MALE , 0.486);
		foxMap.put(MarketSegment.YOUNG , 0.41);
		foxMap.put(MarketSegment.OLD , 0.59);
		foxMap.put(MarketSegment.LOW_INCOME , 0.72);
		foxMap.put(MarketSegment.HIGH_INCOME , 0.28);
		publisherAudienceOrientation.put("fox", foxMap);
		
		Map<MarketSegment , Double> amazonMap = new HashMap<MarketSegment , Double>();		
		amazonMap.put(MarketSegment.FEMALE , 0.524);
		amazonMap.put(MarketSegment.MALE , 0.416);
		amazonMap.put(MarketSegment.YOUNG , 0.41);
		amazonMap.put(MarketSegment.OLD , 0.59);
		amazonMap.put(MarketSegment.LOW_INCOME , 0.77);
		amazonMap.put(MarketSegment.HIGH_INCOME , 0.23);
		publisherAudienceOrientation.put("amazon", amazonMap);

		Map<MarketSegment , Double> ebayMap = new HashMap<MarketSegment , Double>();		
		ebayMap.put(MarketSegment.FEMALE , 0.514);
		ebayMap.put(MarketSegment.MALE , 0.486);
		ebayMap.put(MarketSegment.YOUNG , 0.41);
		ebayMap.put(MarketSegment.OLD , 0.59);
		ebayMap.put(MarketSegment.LOW_INCOME , 0.77);
		ebayMap.put(MarketSegment.HIGH_INCOME , 0.23);
		publisherAudienceOrientation.put("ebay", ebayMap);
		
		Map<MarketSegment , Double> walmartMap = new HashMap<MarketSegment , Double>();		
		walmartMap.put(MarketSegment.FEMALE , 0.544);
		walmartMap.put(MarketSegment.MALE , 0.456);
		walmartMap.put(MarketSegment.YOUNG , 0.39);
		walmartMap.put(MarketSegment.OLD , 0.61);
		walmartMap.put(MarketSegment.LOW_INCOME , 0.75);
		walmartMap.put(MarketSegment.HIGH_INCOME , 0.25);
		publisherAudienceOrientation.put("walmart", walmartMap);

		Map<MarketSegment , Double> targetMap = new HashMap<MarketSegment , Double>();		
		targetMap.put(MarketSegment.FEMALE , 0.544);
		targetMap.put(MarketSegment.MALE , 0.456);
		targetMap.put(MarketSegment.YOUNG , 0.44);
		targetMap.put(MarketSegment.OLD , 0.56);
		targetMap.put(MarketSegment.LOW_INCOME , 0.72);
		targetMap.put(MarketSegment.HIGH_INCOME , 0.28);
		publisherAudienceOrientation.put("target", targetMap);

		Map<MarketSegment , Double> bestbuyMap = new HashMap<MarketSegment , Double>();		
		bestbuyMap.put(MarketSegment.FEMALE , 0.524);
		bestbuyMap.put(MarketSegment.MALE , 0.476);
		bestbuyMap.put(MarketSegment.YOUNG , 0.41);
		bestbuyMap.put(MarketSegment.OLD , 0.59);
		bestbuyMap.put(MarketSegment.LOW_INCOME , 0.725);
		bestbuyMap.put(MarketSegment.HIGH_INCOME , 0.275);
		publisherAudienceOrientation.put("bestbuy", bestbuyMap);

		Map<MarketSegment , Double> searsMap = new HashMap<MarketSegment , Double>();		
		searsMap.put(MarketSegment.FEMALE , 0.534);
		searsMap.put(MarketSegment.MALE , 0.466);
		searsMap.put(MarketSegment.YOUNG , 0.38);
		searsMap.put(MarketSegment.OLD , 0.62);
		searsMap.put(MarketSegment.LOW_INCOME , 0.7);
		searsMap.put(MarketSegment.HIGH_INCOME , 0.3);
		publisherAudienceOrientation.put("sears", searsMap);

		Map<MarketSegment , Double> webmdMap = new HashMap<MarketSegment , Double>();		
		webmdMap.put(MarketSegment.FEMALE , 0.544);
		webmdMap.put(MarketSegment.MALE , 0.456);
		webmdMap.put(MarketSegment.YOUNG , 0.4);
		webmdMap.put(MarketSegment.OLD , 0.6);
		webmdMap.put(MarketSegment.LOW_INCOME , 0.725);
		webmdMap.put(MarketSegment.HIGH_INCOME , 0.275);
		publisherAudienceOrientation.put("webmd", webmdMap);

		Map<MarketSegment , Double> ehowMap = new HashMap<MarketSegment , Double>();
		ehowMap.put(MarketSegment.FEMALE , 0.524);
		ehowMap.put(MarketSegment.MALE , 0.476);
		ehowMap.put(MarketSegment.YOUNG , 0.41);
		ehowMap.put(MarketSegment.OLD , 0.59);
		ehowMap.put(MarketSegment.LOW_INCOME , 0.77);
		ehowMap.put(MarketSegment.HIGH_INCOME , 0.23);
		publisherAudienceOrientation.put("ehow", ehowMap);

		Map<MarketSegment , Double> askMap = new HashMap<MarketSegment , Double>();
		askMap.put(MarketSegment.FEMALE , 0.514);
		askMap.put(MarketSegment.MALE , 0.486);
		askMap.put(MarketSegment.YOUNG , 0.39);
		askMap.put(MarketSegment.OLD , 0.61);
		askMap.put(MarketSegment.LOW_INCOME , 0.78);
		askMap.put(MarketSegment.HIGH_INCOME , 0.22);
		publisherAudienceOrientation.put("ask", askMap);

		Map<MarketSegment , Double> tripAdvisorMap = new HashMap<MarketSegment , Double>();
		tripAdvisorMap.put(MarketSegment.FEMALE , 0.534);
		tripAdvisorMap.put(MarketSegment.MALE , 0.466);
		tripAdvisorMap.put(MarketSegment.YOUNG , 0.42);
		tripAdvisorMap.put(MarketSegment.OLD , 0.58);
		tripAdvisorMap.put(MarketSegment.LOW_INCOME , 0.725);
		tripAdvisorMap.put(MarketSegment.HIGH_INCOME , 0.275);
		publisherAudienceOrientation.put("tripadvisor", tripAdvisorMap);

		Map<MarketSegment , Double> cnetMap = new HashMap<MarketSegment , Double>();
		cnetMap.put(MarketSegment.FEMALE , 0.494);
		cnetMap.put(MarketSegment.MALE , 0.506);
		cnetMap.put(MarketSegment.YOUNG , 0.43);
		cnetMap.put(MarketSegment.OLD , 0.57);
		cnetMap.put(MarketSegment.LOW_INCOME , 0.745);
		cnetMap.put(MarketSegment.HIGH_INCOME , 0.255);
		publisherAudienceOrientation.put("cnet", cnetMap);

		Map<MarketSegment , Double> weatherMap = new HashMap<MarketSegment , Double>();		
		weatherMap.put(MarketSegment.FEMALE , 0.524);
		weatherMap.put(MarketSegment.MALE , 0.476);
		weatherMap.put(MarketSegment.YOUNG , 0.41);
		weatherMap.put(MarketSegment.OLD , 0.59);
		weatherMap.put(MarketSegment.LOW_INCOME , 0.72);
		weatherMap.put(MarketSegment.HIGH_INCOME , 0.28);
		publisherAudienceOrientation.put("weather", weatherMap);
	}

	
	public double getUserOrientation(String publisher , Set<MarketSegment> ms){
		if(publisher == null || ms == null){ return 0.0; }
		if(!publisherAudienceOrientation.containsKey(publisher)){ return 0.0; }

		double prob = 1.0;
		for(MarketSegment segment : ms){
			prob *= (publisherAudienceOrientation.get(publisher)).get(segment);
		}
		return prob;
	}
		
}

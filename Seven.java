

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import tau.tac.adx.props.ReservePriceType;
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

import Agent.src.Add.ReadUserData;//TODO - correct the import
import Agent.src.*;//TODO - correct the import
//import CampaignData;//TODO - correct the import
//import CampaignEngine;//TODO - correct the import



/**
 * 
 * @author Mariano Schain
 * Test plug-in
 * 
 */
public class Seven extends Agent {

	private final Logger log = Logger
			.getLogger(Seven.class.getName());

	/*
	 * Basic simulation information. An agent should receive the {@link
	 * StartInfo} at the beginning of the game or during recovery.
	 */
	@SuppressWarnings("unused")
	private StartInfo startInfo;

	/**
	 * Messages received:
	 * 
	 * We keep all the {@link CampaignReport campaign reports} delivered to the
	 * agent. We also keep the initialization messages {@link PublisherCatalog}
	 * and {@link InitialCampaignMessage} and the most recent messages and
	 * reports {@link CampaignOpportunityMessage}, {@link CampaignReport}, and
	 * {@link AdNetworkDailyNotification}.
	 */
	private final Queue<CampaignReport> campaignReports;
	private PublisherCatalog publisherCatalog;
	private InitialCampaignMessage initialCampaignMessage;
	private AdNetworkDailyNotification adNetworkDailyNotification;

	/*
	 * The addresses of server entities to which the agent should send the daily
	 * bids data
	 */
	private String demandAgentAddress;
	private String adxAgentAddress;

	/*
	 * we maintain a list of queries - each characterized by the web site (the
	 * publisher), the device type, the ad type, and the user market segment
	 */
	private AdxQuery[] queries;

	/**
	 * Information regarding the latest campaign opportunity announced
	 */
	private CampaignData pendingCampaign;

	/**
	 * We maintain a collection (mapped by the campaign id) of the campaigns won
	 * by our agent.
	 */
	private Map<Integer, CampaignData> myCampaigns;
	//Added by Daniel - no point in keeping the total data about the campaigns we won, as we'll use only the data of the active campaigns

	//Added by Daniel
	/**
	 * We maintain a collection (mapped by the campaign id) of the active
	 * campaigns won by our agent.
	 */
	private Map<Integer, CampaignData> myActiveCampaigns;

	/**
	 * We maintain a collection (mapped by the campaign id) of the active
	 * campaigns that are currently run by someone (us included).
	 */
	private Map<Integer, CampaignData> activeCampaigns;

	/**
	 * Our agent's current quality score
	 */
	double qualityScore;

	/**
	 * Class with which we save the probabilities that a user that enters a specific publisher's website
	 * is part of a specific marketsegment.
	 */
	private ReadUserData userData;
	
	/**
	 * The factor with which we are going to multiply our bids on
	 * campaigns in case we have critical campaigns.
	 */	
	private double criticalFactor;

	/**
	 * We maintain a collection (mapped by the publisher name) of the probabilities
	 * that a random user will enter the publisher's website.
	 */
	private Map<String , Double> publisherPopularityMap;


	/*
	 * the bidBundle to be sent daily to the AdX
	 */
	private AdxBidBundle bidBundle;

	/*
	 * The current bid level for the user classification service
	 */
	private double ucsBid;

	/*
	 * The targeted service level for the user classification service
	 */
	private double ucsTargetLevel;
	
	/* Added by Katrin
	 * 
	 * The last UCS bid that is not 0.
	 */
	private double lastUcsBid;
	
	/* Added by Katrin
	 * 
	 * The last UCS level that is not 0.
	 */
	private double lastUcsLevel;

	/*
	 * current day of simulation
	 */
	private int day;
	private String[] publisherNames;
	private CampaignData currCampaign;

	public Seven() {
		campaignReports = new LinkedList<CampaignReport>();
	}

	@Override
	protected void messageReceived(Message message) {
		try {
			Transportable content = message.getContent();

			// log.fine(message.getContent().getClass().toString());

			if (content instanceof InitialCampaignMessage) {
				handleInitialCampaignMessage((InitialCampaignMessage) content);
			} else if (content instanceof CampaignOpportunityMessage) {
				handleICampaignOpportunityMessage((CampaignOpportunityMessage) content);
			} else if (content instanceof CampaignReport) {
				handleCampaignReport((CampaignReport) content);
			} else if (content instanceof AdNetworkDailyNotification) {
				handleAdNetworkDailyNotification((AdNetworkDailyNotification) content);
			} else if (content instanceof AdxPublisherReport) {
				handleAdxPublisherReport((AdxPublisherReport) content);
			} else if (content instanceof SimulationStatus) {
				handleSimulationStatus((SimulationStatus) content);
			} else if (content instanceof PublisherCatalog) {
				handlePublisherCatalog((PublisherCatalog) content);
			} else if (content instanceof AdNetworkReport) {
				handleAdNetworkReport((AdNetworkReport) content);
			} else if (content instanceof StartInfo) {
				handleStartInfo((StartInfo) content);
			} else if (content instanceof BankStatus) {
				handleBankStatus((BankStatus) content);
			} else if(content instanceof CampaignAuctionReport) {
				hadnleCampaignAuctionReport((CampaignAuctionReport) content);
			} else if (content instanceof ReservePriceInfo) {
				//((ReservePriceInfo)content).getReservePriceType();
			} else {
				System.out.println("UNKNOWN Message Received: " + content);
			}

		} catch (NullPointerException e) {
			this.log.log(Level.SEVERE,
					"Exception thrown while trying to parse message." + e);
			return;
		}
	}

	private void hadnleCampaignAuctionReport(CampaignAuctionReport content) {
		// ignoring - this message is obsolete
	}

	private void handleBankStatus(BankStatus content) {
		System.out.println("Day " + day + " :" + content.toString());
	}

	/**
	 * Processes the start information.
	 * 
	 * @param startInfo
	 *            the start information.
	 */
	protected void handleStartInfo(StartInfo startInfo) {
		this.startInfo = startInfo;
	}

	/**
	 * Process the reported set of publishers
	 * 
	 * @param publisherCatalog
	 */
	private void handlePublisherCatalog(PublisherCatalog publisherCatalog) {
		this.publisherCatalog = publisherCatalog;
		generateAdxQuerySpace();
		getPublishersNames();

	}

	/**
	 * On day 0, a campaign (the "initial campaign") is allocated to each
	 * competing agent. The campaign starts on day 1. The address of the
	 * server's AdxAgent (to which bid bundles are sent) and DemandAgent (to
	 * which bids regarding campaign opportunities may be sent in subsequent
	 * days) are also reported in the initial campaign message
	 */
	private void handleInitialCampaignMessage(
			InitialCampaignMessage campaignMessage) {
		System.out.println(campaignMessage.toString());

		day = 0;

		initialCampaignMessage = campaignMessage;
		demandAgentAddress = campaignMessage.getDemandAgentAddress();
		adxAgentAddress = campaignMessage.getAdxAgentAddress();

		CampaignData campaignData = new CampaignData(initialCampaignMessage);
		campaignData.setBudget(initialCampaignMessage.getBudgetMillis()/1000.0);
		currCampaign = campaignData;
		genCampaignQueries(currCampaign);

		/*
		 * The initial campaign is already allocated to our agent so we add it
		 * to our allocated-campaigns list.
		 */
		System.out.println("Day " + day + ": Allocated campaign - " + campaignData);
		myCampaigns.put(initialCampaignMessage.getId(), campaignData);
		//added by Daniel
		myActiveCampaigns.put(initialCampaignMessage.getId(), campaignData);
		activeCampaigns.put(initialCampaignMessage.getId(), campaignData);
		
	}

	/**
	 * On day n ( > 0) a campaign opportunity is announced to the competing
	 * agents. The campaign starts on day n + 2 or later and the agents may send
	 * (on day n) related bids (attempting to win the campaign). The allocation
	 * (the winner) is announced to the competing agents during day n + 1.
	 */
	private void handleICampaignOpportunityMessage(
			CampaignOpportunityMessage com) {

		day = com.getDay();

		pendingCampaign = new CampaignData(com);
		System.out.println("Day " + day + ": Campaign opportunity - " + pendingCampaign);

		/*
		 * The campaign requires com.getReachImps() impressions. The competing
		 * Ad Networks bid for the total campaign Budget (that is, the ad
		 * network that offers the lowest budget gets the campaign allocated).
		 * The advertiser is willing to pay the AdNetwork at most 1$ CPM,
		 * therefore the total number of impressions may be treated as a reserve
		 * (upper bound) price for the auction.
		 */

		//long cmpimps = com.getReachImps();
		//Added by Daniel -> call to Oriel's method for the bid for the campaign
		long cmpBidMillis = CampaignEngine.CalcPayment(pendingCampaign ,myActiveCampaigns , activeCampaigns , day , qualityScore);
		pendingCampaign.setPromisedPayment((double) cmpBidMillis);
		
		System.out.println("Day " + day + ": Campaign total budget bid (millis): " + cmpBidMillis);

		/*
		 * Adjust ucs bid s.t. target level is achieved. Note: The bid for the
		 * user classification service is piggybacked
		 */

		if (adNetworkDailyNotification != null) {
			double ucsLevel = adNetworkDailyNotification.getServiceLevel();
			
			//Added By Katrin
			
			if(null != myActiveCampaigns && myActiveCampaigns.isEmpty()){
				if (ucsBid != 0) {
					lastUcsBid = ucsBid;
					lastUcsLevel = ucsLevel;
					ucsBid = 0;
				}
			}
			else{
				if (ucsBid == 0){
					ucsBid = calcUCS(lastUcsBid,lastUcsLevel);
				}
				else {
					ucsBid = calcUCS(ucsBid,ucsLevel);
				}	
			}
			
			System.out.println("Day " + day + ": ucs level reported: " + ucsLevel);
		} else {
			System.out.println("Day " + day + ": Initial ucs bid is " + ucsBid);
		}

		/* Note: Campaign bid is in millis */
		AdNetBidMessage bids = new AdNetBidMessage(ucsBid, pendingCampaign.getId(), cmpBidMillis);
		sendMessage(demandAgentAddress, bids);
	}
	
	//Added by Katrin
	private double calcUCS(double bid, double level){
		double newUcsBid;
		if (level > consts.secondPlace){
			newUcsBid = bid/(1+consts.pi);
		}
		else if (level > consts.thirdPlace){
			newUcsBid = bid;
		}
		else if (level > consts.fifthPlace) {
			newUcsBid = bid*(1+consts.pi);
		}
		else {
			newUcsBid = bid*(1+consts.sigma);
		}
		return newUcsBid;
	}

	/**
	 * On day n ( > 0), the result of the UserClassificationService and Campaign
	 * auctions (for which the competing agents sent bids during day n -1) are
	 * reported. The reported Campaign starts in day n+1 or later and the user
	 * classification service level is applicable starting from day n+1.
	 */
	private void handleAdNetworkDailyNotification(
			AdNetworkDailyNotification notificationMessage) {

		adNetworkDailyNotification = notificationMessage;

		System.out.println("Day " + day + ": Daily notification for campaign "
				+ adNetworkDailyNotification.getCampaignId());

		String campaignAllocatedTo = " allocated to "
				+ notificationMessage.getWinner();

		if ((pendingCampaign.getId() == adNetworkDailyNotification.getCampaignId())
				&& (notificationMessage.getCostMillis() != 0)) {

			/* add campaign to list of won campaigns */
			pendingCampaign.setBudget(notificationMessage.getCostMillis()/1000.0);
			currCampaign = pendingCampaign;
			genCampaignQueries(currCampaign);
			myCampaigns.put(pendingCampaign.getId(), pendingCampaign);

			campaignAllocatedTo = " WON at cost (Millis)"
					+ notificationMessage.getCostMillis();
		}
		qualityScore = notificationMessage.getQualityScore();
		System.out.println("Day " + day + ": " + campaignAllocatedTo
				+ ". UCS Level set to " + notificationMessage.getServiceLevel()
				+ " at price " + notificationMessage.getPrice()
				+ " Quality Score is: " + notificationMessage.getQualityScore());
	}

	/**
	 * The SimulationStatus message received on day n indicates that the
	 * calculation time is up and the agent is requested to send its bid bundle
	 * to the AdX.
	 */
	private void handleSimulationStatus(SimulationStatus simulationStatus) {
		System.out.println("Day " + day + " : Simulation Status Received");
		sendBidAndAds();
		System.out.println("Day " + day + " ended. Starting next day");
		++day;
	}

	/**
	 * 
	 */
	protected void sendBidAndAds() {

		bidBundle = new AdxBidBundle();

		/*
		 * Note: bidding per 1000 imps (CPM) - no more than average budget
		 * revenue per imp
		 */

		double rbid;

		/*
		 * add bid entries w.r.t. each active campaign with remaining contracted
		 * impressions.
		 */
		
		//These campaigns aren't interesting anymore, remove them
		 removeNonactiveCampaigns();

		resetCriticalParameter();
		
		/* Check the current rating situation and decide how critical it is to improve it
		 * i.e. ask ourselves: "are we willing to lose money on campaigns to improve our
		 * rating?"
		 */
		if(ratingImprovementCrucial()){defineCrucialCampaigns();}

		//Decide weight of a campaign
		Map <Set<MarketSegment> , Set<CampaignData>> campaignOverlaps = new HashMap<Set<MarketSegment> , Set<CampaignData>>();		
		Map <CampaignData , Integer> campaignWeights = new HashMap<CampaignData,Integer>();

		for(CampaignData campaign : myActiveCampaigns.values()){
			for(Set<MarketSegment> segment: SubMarketSegment(campaign.getTargetSegment())){
				Set<CampaignData> set = campaignOverlaps.get(segment);
				if(set == null){
					set = new HashSet<CampaignData>();
				}
				set.add(campaign);
				campaignOverlaps.put(segment, set);
				campaignWeights.put(campaign, 1);
			}
		}

		for(CampaignData campaign : myActiveCampaigns.values()){
			boolean competition = false;
			int weight = 1;
			double totalReachLeft = 0;
			for(Set<MarketSegment> segment : SubMarketSegment(campaign.getTargetSegment())){
				for(CampaignData camp : campaignOverlaps.get(segment)){
					totalReachLeft += (camp.competitionImpsToGo()/(camp.getdayEnd() - day +1));
				}
				if(campaignOverlaps.get(segment).size() > 2){competition = true;}
			}

			if(competition){
				weight = (int)((campaign.competitionImpsToGo()/(campaign.getdayEnd() - day +1)* 10.0)/totalReachLeft);
				campaignWeights.put(campaign, weight);
			}
			
			if(campaign.isCritical()){//put a lot on the critical, divide weight among the others
				campaignWeights.put(campaign, 10 * weight);
			}
		}	

		//Actually bid for the campaign
		for(CampaignData campaign: myActiveCampaigns.values()){

			rbid = campaignProfitability(campaign);

			int entCount = 0;

			for (AdxQuery query : campaign.getCampaignQueries()) {
				double bid = rbid;
				
				/*
				 * among matching entries with the same campaign id, the AdX
				 * randomly chooses an entry according to the designated
				 * weight. by setting a constant weight 1, we create a
				 * uniform probability over active campaigns(irrelevant because we are bidding only on one campaign)
				 */
				if (query.getDevice() == Device.pc) {
					if (query.getAdType() == AdType.text) {
						entCount++;
					} else {
						entCount ++;
						bid *= campaign.getVideoCoef();
					}
				} else {
					if (query.getAdType() == AdType.text) {
						entCount++;
						bid *= campaign.getMobileCoef();
					} else {
						entCount ++;
						bid *= (campaign.getVideoCoef() + campaign.getMobileCoef());
					}
				}

				double publisherProfitProbability = userData.getUserOrientation(query.getPublisher(), campaign.getTargetSegment()) * publisherPopularityMap.get(query.getPublisher());
				if(query.getMarketSegments().size() == 0 && publisherProfitProbability > 0.6 && (!campaign.isCritical())){//TODO - Dan - define probability parameter (maybe change 0.6 to something else)
					/* Unknown market segment, and the campaign isn't critical, so we're willing
					 * to pay for it only if we're fairly sure who's the user
					 */
					bidBundle.addQuery(query, bid , new Ad(null),
							campaign.getId(), 1);
				}
				else if(campaign.isCritical() && query.getMarketSegments().size() == 0 && publisherProfitProbability > 0.5){
					//Unknown market segment, but the campaign is critical, so we're willing to pay more money for it
					bidBundle.addQuery(query, bid , new Ad(null),
							campaign.getId(), 1);
				}
				else{
					//We know which market segment the user belongs to, bid the usual bid
					bidBundle.addQuery(query, bid , new Ad(null),
						campaign.getId(), campaignWeights.get(campaign));
				}
			}


			double impressionLimit = campaign.impsTogo();//TODO - overachiever - maybe implement it here
			double budgetLimit = campaign.getBudget();
			if(!campaign.isCritical()){
				bidBundle.setCampaignDailyLimit(campaign.getId(),
						(int) impressionLimit, budgetLimit);
			}
			else{
				bidBundle.setCampaignDailyLimit(campaign.getId(),
						(int) impressionLimit, budgetLimit * criticalFactor);	
			}

			System.out.println("Day " + day + ": Updated " + entCount
					+ " Bid Bundle entries for Campaign id " + campaign.getId());
		}
		if (bidBundle != null) {
			System.out.println("Day " + day + ": Sending BidBundle");
			sendMessage(adxAgentAddress, bidBundle);
		}
	}

	/**
	 * Campaigns performance w.r.t. each allocated campaign
	 */
	private void handleCampaignReport(CampaignReport campaignReport) {

		campaignReports.add(campaignReport);

		/*
		 * for each campaign, the accumulated statistics from day 1 up to day
		 * n-1 are reported
		 */
		for (CampaignReportKey campaignKey : campaignReport.keys()) {
			int cmpId = campaignKey.getCampaignId();
			CampaignStats cstats = campaignReport.getCampaignReportEntry(
					campaignKey).getCampaignStats();
			myCampaigns.get(cmpId).setStats(cstats);

			System.out.println("Day " + day + ": Updating campaign " + cmpId + " stats: "
					+ cstats.getTargetedImps() + " tgtImps "
					+ cstats.getOtherImps() + " nonTgtImps. Cost of imps is "
					+ cstats.getCost());
		}
	}

	/**
	 * Users and Publishers statistics: popularity and ad type orientation
	 */
	private void handleAdxPublisherReport(AdxPublisherReport adxPublisherReport) {
		System.out.println("Publishers Report: ");
		for (PublisherCatalogEntry publisherKey : adxPublisherReport.keys()) {
			AdxPublisherReportEntry entry = adxPublisherReport
					.getEntry(publisherKey);
					
			publisherPopularityMap.put(entry.getPublisherName(), ((double)entry.getPopularity())/10000.0);

			System.out.println(entry.toString());
			entry.getAdTypeOrientation();

		}
	}

	/**
	 * 
	 * @param AdNetworkReport
	 */
	private void handleAdNetworkReport(AdNetworkReport adnetReport) {

		System.out.println("Day " + day + " : AdNetworkReport");
		/*
		 * for (AdNetworkKey adnetKey : adnetReport.keys()) {
		 * 
		 * double rnd = Math.random(); if (rnd > 0.95) { AdNetworkReportEntry
		 * entry = adnetReport .getAdNetworkReportEntry(adnetKey);
		 * System.out.println(adnetKey + " " + entry); } }
		 */
	}

	@Override
	protected void simulationSetup() {

		day = 0;
		bidBundle = new AdxBidBundle();

		/* initial bid between 0.1 and 0.2 */
		//ucsBid = 0.1 + random.nextDouble()/10.0;
		
		//Added by Katrin
		ucsBid = consts.initialUcs;

		myCampaigns = new HashMap<Integer, CampaignData>();
		//Added by Daniel
		myActiveCampaigns = new HashMap<Integer, CampaignData>();
		activeCampaigns = new HashMap<Integer, CampaignData>();
		userData = new ReadUserData();
		criticalFactor = consts.initialCriticalFactor;
		publisherPopularityMap = new HashMap<String , Double>();
		qualityScore = 1.0;

		
		log.fine("AdNet " + getName() + " simulationSetup");
	}

	@Override
	protected void simulationFinished() {
		campaignReports.clear();
		bidBundle = null;
	}

	/**
	 * A user visit to a publisher's web-site results in an impression
	 * opportunity (a query) that is characterized by the the publisher, the
	 * market segment the user may belongs to, the device used (mobile or
	 * desktop) and the ad type (text or video).
	 * 
	 * An array of all possible queries is generated here, based on the
	 * publisher names reported at game initialization in the publishers catalog
	 * message
	 */
	private void generateAdxQuerySpace() {
		if (publisherCatalog != null && queries == null) {
			Set<AdxQuery> querySet = new HashSet<AdxQuery>();

			/*
			 * for each web site (publisher) we generate all possible variations
			 * of device type, ad type, and user market segment
			 */
			for (PublisherCatalogEntry publisherCatalogEntry : publisherCatalog) {
				String publishersName = publisherCatalogEntry
						.getPublisherName();
				for (MarketSegment userSegment : MarketSegment.values()) {
					Set<MarketSegment> singleMarketSegment = new HashSet<MarketSegment>();
					singleMarketSegment.add(userSegment);

					querySet.add(new AdxQuery(publishersName,
							singleMarketSegment, Device.mobile, AdType.text));

					querySet.add(new AdxQuery(publishersName,
							singleMarketSegment, Device.pc, AdType.text));

					querySet.add(new AdxQuery(publishersName,
							singleMarketSegment, Device.mobile, AdType.video));

					querySet.add(new AdxQuery(publishersName,
							singleMarketSegment, Device.pc, AdType.video));

				}

				/**
				 * An empty segments set is used to indicate the "UNKNOWN"
				 * segment such queries are matched when the UCS fails to
				 * recover the user's segments.
				 */
				querySet.add(new AdxQuery(publishersName,
						new HashSet<MarketSegment>(), Device.mobile,
						AdType.video));
				querySet.add(new AdxQuery(publishersName,
						new HashSet<MarketSegment>(), Device.mobile,
						AdType.text));
				querySet.add(new AdxQuery(publishersName,
						new HashSet<MarketSegment>(), Device.pc, AdType.video));
				querySet.add(new AdxQuery(publishersName,
						new HashSet<MarketSegment>(), Device.pc, AdType.text));
			}
			queries = new AdxQuery[querySet.size()];
			querySet.toArray(queries);
		}
	}

	/*
	 * generates an array of the publishers names
	 */
	private void getPublishersNames() {
		if (null == publisherNames && publisherCatalog != null) {
			ArrayList<String> names = new ArrayList<String>();
			for (PublisherCatalogEntry pce : publisherCatalog) {
				names.add(pce.getPublisherName());
			}

			publisherNames = new String[names.size()];
			names.toArray(publisherNames);
		}
	}
	/*
	 * generates the campaign queries relevant for the specific campaign, and assign them as the campaign's campaignQueries field 
	 */
	private void genCampaignQueries(CampaignData campaignData) {
		Set<AdxQuery> campaignQueriesSet = new HashSet<AdxQuery>();
		for (String PublisherName : publisherNames) {
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.getTargetSegment(), Device.mobile, AdType.text));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.getTargetSegment(), Device.mobile, AdType.video));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.getTargetSegment(), Device.pc, AdType.text));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.getTargetSegment(), Device.pc, AdType.video));

			//--------------------------added by Daniel ------------------------
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					new HashSet<MarketSegment>(), Device.mobile,
					AdType.video));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					new HashSet<MarketSegment>(), Device.mobile,
					AdType.text));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					new HashSet<MarketSegment>(), Device.pc, AdType.video));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					new HashSet<MarketSegment>(), Device.pc, AdType.text));
			//------------------------------------------------------------------

		}

		campaignData.campaignQueries = new AdxQuery[campaignQueriesSet.size()];
		campaignQueriesSet.toArray(campaignData.campaignQueries);
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!"+Arrays.toString(campaignData.campaignQueries)+"!!!!!!!!!!!!!!!!");


	}

	//Added by Daniel
	//-----------------------------------------------------------------------------------------

	/**
	 * This method goes over the active campaign maps (myActiveCampaigns and activeCampaigns)
	 * and removes the ones that aren't active anymore (whether they reached the goal impression
	 * amount or they reached their last day).
	 */
	private void removeNonactiveCampaigns(){
		int dayBiddingFor = day + 1;
		for(CampaignData campaign: activeCampaigns.values()){
			if(dayBiddingFor > campaign.getdayEnd()){
				activeCampaigns.remove(campaign.getId());
				myActiveCampaigns.remove(campaign.getId());
			}
		}
		for(CampaignData campaign: activeCampaigns.values()){
			if(campaign.impsTogo() == 0){
				myActiveCampaigns.remove(campaign.getId());
			}
		}
	}

	
	/**
	 * This method returns a Set of all of the triplets of market segments (which are sets themselves),
	 * that if we merge them we'll recieve the original narketSegment recieved by the method.
	 * Returns null, if the input marketSegment is null. 
	 */
	private Set<Set<MarketSegment>> SubMarketSegment(Set<MarketSegment> marketSegment){
		if(marketSegment == null) return null;

		Set<Set<MarketSegment>> subSegments = new HashSet<Set<MarketSegment>>();

		for(Set<MarketSegment> subSet: MarketSegment.marketSegments()){
			if(subSet.size() != 3){continue;}
			
			if(isContained(marketSegment , subSet)){ subSegments.add(subSet); }
		}
		return subSegments;
	}

	/**
	 * This method returns true iff the "containee" set of market segments
	 * is fully contained by the "container" set of market segments.
	 * Returns false if either on of the is null.
	 */
	private boolean isContained(Set<MarketSegment> container , Set<MarketSegment> containee){
		if(container == null || containee == null){ return false; }
		for(MarketSegment seg : container){
			if(!containee.contains(seg)){ return false; }
		}
		return true;
	}
	
	/**
	 * This method returns the basic bid we're going to offer upon an impression opportunity of a given campaign
	 * on a given day by different parameter (the math is explained in the final report). //TODO - add explanation to the math in the final report
	 */
	private double campaignProfitability(CampaignData campaign){
		double marketSegmentSize = (double) MarketSegment.marketSegmentSize(campaign.getTargetSegment());
		double impressionsAchieved = campaign.stats.getTargetedImps();
		double leftImpressionsToAchieve = Math.min(consts.impressionOpertunitiesPerUserPerDay * marketSegmentSize, campaign.impsTogo());//TODO campaign. imps left to win
		double badLeftImpressionsToAchieve =  Math.max(consts.impressionOpertunitiesPerUserPerDay * marketSegmentSize, campaign.impsTogo());
		double catastropheImpressionsToAchieve = Math.max(consts.impressionOpertunitiesPerUserPerDay * marketSegmentSize * campaign.getCampaignLength(), campaign.impsTogo());
		
		double currentReach = campaign.impsTogo()/(((double)(campaign.getdayEnd() - day + 1)) * (MarketSegment.usersInMarketSegments().get(campaign.getTargetSegment())));
		
		double payment = 0;
		
		/**
		 * Either first 5 days so the competition is fierce so bidding aggressively
		 * Or the campaign isn't considered critical, but still needs to hurry up to complete
		 * the campaign.
		 */
		if((!campaign.isCritical()) && (day <= 5 || currentReach < 0.9 * campaign.getNeededReach())){
			payment = calcGainedPercentage(campaign , catastropheImpressionsToAchieve , impressionsAchieved);
		}

		 //bad stuff going on - but this campaign is not the one I'm going to waste money on
		else if((!campaign.isCritical()) && ((currentReach < campaign.getNeededReach() && currentReach > 0.9 * campaign.getNeededReach()) || campaign.getCampaignLength() < 5)){
			payment = calcGainedPercentage(campaign , badLeftImpressionsToAchieve , impressionsAchieved);
		}
		
		//default situation - all is well
		else if(!campaign.isCritical() && currentReach > campaign.getNeededReach()){
			payment = calcGainedPercentage(campaign , leftImpressionsToAchieve , impressionsAchieved);
		}
		
		else{//The campaign is considered critical in improving our rating
			payment = criticalFactor * (calcGainedPercentage(campaign , catastropheImpressionsToAchieve , impressionsAchieved));
		}
		
		/**
		 * Multiply by 1000 because we're paying in CPMs (and not for single impressions)
		 * and also normalize by the ratio (what we're going to get paid for 100% reach / amount of impressions for 100% reach) 
		 */
		return payment * 1000.0 * (campaign.getPromisedPayment()/((double)campaign.getReachImps()));
	}

	/**
	 * This method calculates how much of the campaign (in percent) we'll complete per single
	 * impression if we add "tooAdd" impressions to the "current" impressions we already achieved
	 * during the run of the campaign.
	 */
	private double calcGainedPercentage(CampaignData campaign , double toAdd , double current){
		return (1.0/toAdd)*(2.0/consts.a)*(Math.atan(consts.a*((toAdd + current)/campaign.getNeededReach())-consts.b) - Math.atan(consts.a*(current/campaign.getNeededReach())-consts.b));
	}

	/**
	 * This method resets the critical parameter of all our active campaigns to false.
	 */
	private void resetCriticalParameter(){
		for(CampaignData campaign: myActiveCampaigns.values()){
			if(!campaign.isCritical()){continue;}
			if(campaign.impsTogo()/((double)(campaign.getdayEnd() - day + 1) * (MarketSegment.usersInMarketSegments().get(campaign.getTargetSegment()))) < 0.9/*maybe change this*/ * campaign.getNeededReach()){
				criticalFactor *= 1.05;
			}
			else{
				criticalFactor /= 1.05;//TODO - Dan - maybe change this factor
			}

			campaign.setCriticalParameter(false);
		}
		criticalFactor = Math.max(criticalFactor , 1.1);//TODO - Dan - maybe change the minimal value of the critical parameter
	}


	/**
	 * We'll want to find the easiest campaigns through which we can boost our quality score (or "rating" as
	 * specified in other parts of the code). This method returns a linked HashMap in which the campaigns are
	 * sorted in regards of their potential to boost our quality score.
	 */
	private static LinkedHashMap<Integer, CampaignData> sortCampaignsByComparator(Map<Integer, CampaignData> unsortMap){

		List<Entry<Integer, CampaignData>> list = new LinkedList<Entry<Integer, CampaignData>>(unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<Integer, CampaignData>>(){
			public int compare(Entry<Integer, CampaignData> o1, Entry<Integer, CampaignData> o2){
				CampaignData c1 = o1.getValue();
				CampaignData c2 = o2.getValue();
				if(c1.getdayEnd() != c2.getdayEnd()){
					return ((int)(c1.getdayEnd() - c2.getdayEnd()));
				}
				else{
					return (c1.impsTogo())/(c1.getdayEnd() - day + 1) - (c2.impsTogo())/(c2.getdayEnd() - day + 1);
				}
			}//TODO
		});

		// Maintaining insertion order with the help of LinkedList
		Map<Integer, CampaignData> sortedMap = new LinkedHashMap<Integer, CampaignData>();
		for (Entry<Integer, CampaignData> entry : list){
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return (LinkedHashMap<Integer, CampaignData>) sortedMap;
	}


	
	/**
	 * This method defines how critical it is for us to improve our quality score
	 * according to how advanced we're in the game.
	 */
	boolean ratingImprovementCrucial(){//TODO - Dan, maybe add some more else-ifs
		if(qualityScore <= 1.0 && day <=20){return true;}
		else if(qualityScore <= 0.95 && day <=40){return true;}
		else if(qualityScore <= 0.80 && day <=50){return true;}
		return false;
	}//TODO - see what Oriel did in the implementation - He's the one to define how critical the rating is!!!!! (change this comment before submission)

	
	/**
	 * If called, this method sets the critical parameter to at most 2 campaigns to true
	 * and its their job to save our quality score.
	 */
	void defineCrucialCampaigns(){
		if(myActiveCampaigns.isEmpty()){ return; }

		LinkedHashMap<Integer, CampaignData> list = sortCampaignsByComparator(myActiveCampaigns);
		int i = 0;
		Iterator<CampaignData> iter = list.values().iterator();
		while(iter.hasNext()){
			if( (i > myActiveCampaigns.size() / 5 && i > 6) || i > 2){break;}
			CampaignData campaign = iter.next();
			campaign.setCriticalParameter(true);
			i++;
		}
	}
	
	//------------------------------------------------------------------------------------------
	//remark - the CampaignData class was removed to a separate file so that other class, besides this one could use it as well
}

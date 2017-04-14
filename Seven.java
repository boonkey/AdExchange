

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

import Agent.src.Add.ReadUserData;


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
	private Map<Integer, CampaignData> myActiveCampaigns;
	private Map<Integer, CampaignData> activeCampaigns;
	double qualityScore = 1.0;
	private static double a = 4.08577;
	private static double b = 3.08577;
	private static double impressionOpertunitiesPerUserPerDay = 1.42753;
	private ReadUserData userData;
	private double criticalFactor;
	private Map<String , Double> publisherPopularityMap;

	//Added by Katrin
	//UCS parameters
	private static double pi = 0.2;
	private static double sigma = 0.3;
	private static double initialUcs = 0.15; 

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

		Random random = new Random();
		long cmpimps = com.getReachImps();
		long cmpBidMillis = random.nextInt((int)cmpimps);

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
		AdNetBidMessage bids = new AdNetBidMessage(ucsBid, pendingCampaign.id, cmpBidMillis);
		sendMessage(demandAgentAddress, bids);
	}
	
	//Added by Katrin
	private double calcUCS(double bid, double level){
		double newUcsBid;
		if (level > 0.9){
			newUcsBid = bid/(1+pi);
		}
		else if (level > 0.81){
			newUcsBid = bid;
		}
		else if (level > 0.6561) {
			newUcsBid = bid*(1+pi);
		}
		else {
			newUcsBid = bid*(1+sigma);
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

		if ((pendingCampaign.id == adNetworkDailyNotification.getCampaignId())
				&& (notificationMessage.getCostMillis() != 0)) {

			/* add campaign to list of won campaigns */
			pendingCampaign.setBudget(notificationMessage.getCostMillis()/1000.0);
			currCampaign = pendingCampaign;
			genCampaignQueries(currCampaign);
			myCampaigns.put(pendingCampaign.id, pendingCampaign);

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
			for(Set<MarketSegment> segment: SubMarketSegment(campaign.targetSegment)){
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
			for(Set<MarketSegment> segment : SubMarketSegment(campaign.targetSegment)){
				if(campaignOverlaps.get(segment).size() > 2){competition = true;}
			}

			if(competition){
				campaignWeights.put(campaign, (int) (campaign.competitionImpsToGo()/(campaign.dayEnd - day +1)* 10.0));
			}
			
			if(campaign.critical){//put a lot on the critical, divide weight among the others
				campaignWeights.put(campaign, 10 * weight);
			}
		}	

		//Actually bid for the campaign
		for(CampaignData campaign: myActiveCampaigns.values()){

			rbid = campaignProfitability(campaign);

			int entCount = 0;

			for (AdxQuery query : campaign.campaignQueries) {
				double bid = rbid;
				if (campaign.impsTogo() - entCount > 0) {

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
							bid *= campaign.videoCoef;
						}
					} else {
						if (query.getAdType() == AdType.text) {
							entCount++;
							bid *= campaign.mobileCoef;
						} else {
							entCount ++;
							bid *= (campaign.videoCoef + campaign.mobileCoef);
						}
					}

					double publisherProfitProbability = userData.getUserOrientation(query.getPublisher(), campaign.targetSegment) * publisherPopularityMap.get(query.getPublisher());
					if(query.getMarketSegments().size() == 0 && publisherProfitProbability > 0.6 && (!campaign.critical)){//TODO - define probability parameter
						//Unknown market segment, but the campaign is critical, so we're willing to pay more money for it
						bidBundle.addQuery(query, bid , new Ad(null),
								campaign.id, 1);
					}
					else if(campaign.critical && query.getMarketSegments().size() == 0 && publisherProfitProbability > 0.5){
						//Unknown market segment, but the campaign is critical, so we're willing to pay more money for it
						bidBundle.addQuery(query, bid , new Ad(null),
								campaign.id, 1);
					}
					else{
						//We know which market segment the user belongs to, bid the usual bid
						bidBundle.addQuery(query, bid , new Ad(null),
								campaign.id, campaignWeights.get(campaign));
					}
				}


				double impressionLimit = campaign.impsTogo();//TODO - overachiever - maybe implement it here
				double budgetLimit = campaign.budget;
				if(!campaign.critical){
					bidBundle.setCampaignDailyLimit(campaign.id,
							(int) impressionLimit, budgetLimit);
				}
				else{
					bidBundle.setCampaignDailyLimit(campaign.id,
							(int) impressionLimit, budgetLimit * criticalFactor);	
				}

				System.out.println("Day " + day + ": Updated " + entCount
						+ " Bid Bundle entries for Campaign id " + campaign.id);
			}
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
		Random random = new Random();

		day = 0;
		bidBundle = new AdxBidBundle();

		/* initial bid between 0.1 and 0.2 */
		//ucsBid = 0.1 + random.nextDouble()/10.0;
		
		//Added by Katrin
		ucsBid = initialUcs;

		myCampaigns = new HashMap<Integer, CampaignData>();
		//Added by Daniel
		myActiveCampaigns = new HashMap<Integer, CampaignData>();
		activeCampaigns = new HashMap<Integer, CampaignData>();
		userData = new ReadUserData();
		criticalFactor = 1.2;
		publisherPopularityMap = new HashMap<String , Double>();

		
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
					campaignData.targetSegment, Device.mobile, AdType.text));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.mobile, AdType.video));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.pc, AdType.text));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.pc, AdType.video));

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
	private void removeNonactiveCampaigns(){
		int dayBiddingFor = day + 1;
		for(CampaignData campaign: activeCampaigns.values()){
			if(dayBiddingFor > campaign.dayEnd){
				activeCampaigns.remove(campaign.id);
				myActiveCampaigns.remove(campaign.id);
			}
		}
		for(CampaignData campaign: activeCampaigns.values()){
			if(campaign.impsTogo() == 0){
				myActiveCampaigns.remove(campaign.id);
			}
		}
	}

	private Set<Set<MarketSegment>> SubMarketSegment(Set<MarketSegment> marketSegment){
		if(marketSegment == null) return null;

		Set<Set<MarketSegment>> subSegments = new HashSet<Set<MarketSegment>>();
		boolean flag;

		for(Set<MarketSegment> marketSegmentSet: MarketSegment.marketSegments()){
			if(marketSegmentSet.size() != 3){continue;}
			flag = true;
			for(MarketSegment segment: marketSegment){
				if(!marketSegmentSet.contains(segment)){
					flag = false;
					break;
				}
			}
			if(flag){ subSegments.add(marketSegmentSet); }
		}

		return subSegments;
	}


	private double campaignProfitability(CampaignData campaign){
		double marketSegmentSize = (double) MarketSegment.marketSegmentSize(campaign.targetSegment);
		double impressionsAchieved = campaign.stats.getTargetedImps();
		double leftImpressionsToAchieve = Math.min(impressionOpertunitiesPerUserPerDay * marketSegmentSize, campaign.impsTogo());//TODO campaign. imps left to win
		double badLeftImpressionsToAchieve =  Math.max(impressionOpertunitiesPerUserPerDay * marketSegmentSize, campaign.impsTogo());
		double catastropheImpressionsToAchieve = Math.max(impressionOpertunitiesPerUserPerDay * marketSegmentSize * ((int)(campaign.dayEnd - day +1)), campaign.impsTogo());
		
		double currentReach = campaign.impsTogo()/(((double)(campaign.dayEnd - day + 1)) * (MarketSegment.usersInMarketSegments().get(campaign.targetSegment)));
		
		double payment = 0;
		
		//bad stuff going on //TODO - define a parameter through which we'll choose a specific campaign to be crucial for rating (has to be closest campaign to end for effectiveness
		if(day < 5 || currentReach < 0.9 * campaign.neededReach){
			payment = calcGainedPercentage(campaign , catastropheImpressionsToAchieve , impressionsAchieved);
		}	//these are the campaigns whose end's the nearest, everything went badly and its their responsibility to solve the issue

		else if((!campaign.critical) && ((currentReach < campaign.neededReach && currentReach > 0.9 * campaign.neededReach) || campaign.getCampaignLength() < 5)){//default situation - all is well
			payment = calcGainedPercentage(campaign , badLeftImpressionsToAchieve , impressionsAchieved);
		} //bad stuff going on - but this campaign is not the one I'm going to waste money on
		
		else if(!campaign.critical && currentReach > campaign.neededReach){
			payment = calcGainedPercentage(campaign , leftImpressionsToAchieve , impressionsAchieved);
		}
		else{//The campaign is considered critical in improving our rating
			payment = criticalFactor * (calcGainedPercentage(campaign , catastropheImpressionsToAchieve , impressionsAchieved));
		}
		
		return payment * 1000;//TODO - Multiply by 1000 because we're paying in CPMs 
	}

	private double calcGainedPercentage(CampaignData campaign , double toAdd , double current){
		return (1/toAdd)*(2/a)*(Math.atan(a*((toAdd + current)/campaign.neededReach)-b) - Math.atan(a*(current/campaign.neededReach)-b));
	}

	private void resetCriticalParameter(){
		for(CampaignData campaign: myActiveCampaigns.values()){
			if(!campaign.critical){continue;}
			if(campaign.impsTogo()/((double)(campaign.dayEnd - day + 1) * (MarketSegment.usersInMarketSegments().get(campaign.targetSegment))) < 0.9/*maybe change this*/ * campaign.neededReach){
				criticalFactor *= 1.05;
			}
			else{
				criticalFactor /= 1.05;//TODO - maybe change this factor
			}

			campaign.critical = false;
		}

	}


	private static LinkedHashMap<Integer, CampaignData> sortCampaignsByComparator(Map<Integer, CampaignData> unsortMap){

		List<Entry<Integer, CampaignData>> list = new LinkedList<Entry<Integer, CampaignData>>(unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<Integer, CampaignData>>(){
			public int compare(Entry<Integer, CampaignData> o1, Entry<Integer, CampaignData> o2){
				CampaignData c1 = o1.getValue();
				CampaignData c2 = o2.getValue();
				if(c1.dayEnd != c2.dayEnd){
					return ((int)(c1.dayEnd - c2.dayEnd));
				}
				else{
					return c1.getCampaignLength() * c1.impsTogo() - c2.getCampaignLength() * c2.impsTogo();
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


	
	
	boolean ratingImprovementCrucial(){
		if(day <= 5){return true;}
		else if(qualityScore <= 1.0 && day <=20){return true;}
		else if(qualityScore <= 0.95 && day <=40){return true;}
		else if(qualityScore <= 0.80 && day <=50){return true;}
		return false;
	}//TODO - see what Oriel did in the implementation - He's the one to define how critical the rating is!!!!! (change this comment before submission)

	
	private double CalcRatingSignificance(){
		/*
		 * Calculating a function that decides the importance of the rating
		 * depending on the current rating and the day.
		 * On a scale of 1 to 100. We decided that for the first 5 days are the
		 */
		double significance = -0.04198*((double)(day*day)) + 0.41975 *((double)(day)) + 98.95062;
		return significance / qualityScore;
	}

	void defineCrucialCampaigns(){
		LinkedHashMap<Integer, CampaignData> list = sortCampaignsByComparator(myActiveCampaigns);
		int i = 0;
		Iterator<CampaignData> iter = list.values().iterator();
		while(iter.hasNext()){
			if( i > myActiveCampaigns.size() / 5 || i > 1){break;}
			CampaignData campaign = iter.next();
			campaign.critical = true;
			i++;
		}
	}

	
	//------------------------------------------------------------------------------------------

	private class CampaignData {
		/* campaign attributes as set by server */
		Long reachImps;
		long dayStart;
		long dayEnd;
		Set<MarketSegment> targetSegment;
		double videoCoef;
		double mobileCoef;
		int id;
		private AdxQuery[] campaignQueries;//array of queries relvent for the campaign.

		/* campaign info as reported */
		CampaignStats stats;
		double budget;

		//Added by Daniel
		private double neededReach;
		private boolean critical;//Was the campaign chosen for improving our rating



		public CampaignData(InitialCampaignMessage icm) {
			reachImps = icm.getReachImps();
			dayStart = icm.getDayStart();
			dayEnd = icm.getDayEnd();
			targetSegment = icm.getTargetSegment();
			videoCoef = icm.getVideoCoef();
			mobileCoef = icm.getMobileCoef();
			id = icm.getId();

			stats = new CampaignStats(0, 0, 0);
			budget = 0.0;

			//Added by Daniel
			neededReach = reachImps/((this.getCampaignLength()) * (MarketSegment.usersInMarketSegments().get(targetSegment)));
		}

		public void setBudget(double d) {
			budget = d;
		}

		public CampaignData(CampaignOpportunityMessage com) {
			dayStart = com.getDayStart();
			dayEnd = com.getDayEnd();
			id = com.getId();
			reachImps = com.getReachImps();
			targetSegment = com.getTargetSegment();
			mobileCoef = com.getMobileCoef();
			videoCoef = com.getVideoCoef();
			stats = new CampaignStats(0, 0, 0);
			budget = 0.0;
		}

		@Override
		public String toString() {
			return "Campaign ID " + id + ": " + "day " + dayStart + " to "
					+ dayEnd + " " + targetSegment + ", reach: " + reachImps
					+ " coefs: (v=" + videoCoef + ", m=" + mobileCoef + ")";
		}
		//TODO - overachiever or just achieve to 100%. To be decided by the campaign's complexity
		int impsTogo() {
			return (int) Math.max(0 , 1.1 * reachImps - stats.getTargetedImps());//TODO
		}
		int competitionImpsToGo(){
			return (int) Math.max(0 , reachImps - stats.getTargetedImps());//TODO
		}

		void setStats(CampaignStats s) {
			stats.setValues(s);
		}

		public AdxQuery[] getCampaignQueries() {
			return campaignQueries;
		}

		public void setCampaignQueries(AdxQuery[] campaignQueries) {
			this.campaignQueries = campaignQueries;
		}

		public int getCampaignLength(){
			return ((int) this.dayEnd - (int)this.dayStart) + 1;
		}

	}

}

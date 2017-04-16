import java.util.Set;

import tau.tac.adx.demand.CampaignStats;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.InitialCampaignMessage;

public class consts {
	//SPEC parameters
	public static final Double pUserContinuation = 0.3;
	public static final Double pRandomCampaignAllocation = 0.36;
	public static final Integer maxUserDailyImpressions = 6;
	public static final Double initialReservePrice = 0.005;
	public static final Double reservePriceVariance = 0.02;
	public static final Double reservePriceLearningRate = 0.2;
	public static final Integer shortCampaignDuration = 3;
	public static final Integer mediumCampaignDuration = 5;
	public static final Integer longCampaignDuration = 10;
	public static final Double lowCampaignReachFactor = 0.2;
	public static final Double mediumCampaignReachFactor = 0.5;
	public static final Double highCampaignReachFactor = 0.8;
	public static final Double maxCampaignCostByImpression = 0.001;
	public static final Double minCampaignCostByImpression = 0.0001;
	public static final Double qualityRatingLearningRate = 0.6;
	public static final Integer gameLength = 60;
	public static final Integer realTimeSecondsPerDay = 10;
	public static final Double pUcsUserRevelation = 0.9;
	public static final Double initialDayClassificationAcc = 0.9;
		
	//campaingEngine parameters
	public static final Double CampaignEngine_m_lowerRatingLimit = 0.95;
	public static final Double CampaignEngine_panic_rating = 0.8;
	public static final Double CampaignEngine_upperALPHALimit = 1.15; 
	public static final Double CampaignEngine_initialBETAValue = 0.5;
	public static final Double CampaignEngine_Ggreed = 1.2; /* Ggreed>1 is a factor describing how greedy Los Capaors is when bidding for a contract. It is used to update the CI (competeing index). LC is more generous if Ggreed is set high.*/
	public static final Double CampaignEngine_privateValueUpperLimit = 1.5;
	public static final Double CampaignEngine_privateValueBottomLimit = 1.15;
	public static final Double CampaignEngine_lowerBidLimit = 300.0;
	public static final Double CampaignEngine_ALPHAPow = 1.0;
	public static final Double CampaignEngine_BETAPow = 1.0;
		
		
	//UCS parameters
	public static final Double initalUCSBid = 0.15;
	public static final Double GUCS = 0.2; /* 0.5<GUCS<1 means the percentage the bid in UCS auctions should be scaled up by in order to receive a higher UCS level */
	public static final Double GUCSPanic = 0.3;
	public static final Double minImp = 0.75;
		
	//BidBundle parameters
	public static final Double fixedBudgetPrecentege = 0.15;
	public static final Double panicDays = 0.33;
	public static final Double smallReach = 0.4;
	public static final Double tooLittleBudget = 0.2;
	public static final Integer panicRatio = 5;
	public static final Double budgetLimit = 1.3;
	public static final Double limit = 1.1;
	public static final Double limit2 = 1.5;
		
		
	//added by Daniel
	//bid bundle parameters
	public static double a = 4.08577;
	public static double b = 3.08577;
	public static double impressionOpertunitiesPerUserPerDay = 1.42753;
	public static double initialCriticalFactor = 1.2;
	
	//Added by Katrin
	//UCS parameters
	public static double pi = 0.2;
	public static double sigma = 0.3;
	public static double initialUcs = 0.15; 
	//Added by Daniel
	public static double secondPlace = 0.9;
	public static double thirdPlace = 0.81;
	public static double fifthPlace = 0.6561;
	
}

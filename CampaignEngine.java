import ext.consts;//TODO - correct the import

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.Math;

import tau.tac.adx.report.adn.MarketSegment;
import ext.CampaignData;

public class CampaignEngine {
		

		static int m_day;
		static List<CampaignData> m_TotalMarketActiveCampaignData;//market campaign data

		//campaign Data members
		static List<Set<MarketSegment>> basicSegmentsList;
		static CampaignData m_campaignData;
		static int m_lengthOfCampaign;
		static Set<MarketSegment> m_Segements;
		static Set<Set<MarketSegment>> m_powerSegments;	
		static Map<Integer,CampaignData> activecampaigns;

		//agent paramters (Beta and rating)
		static double BETA = consts.CampaignEngine_initialBETAValue; 	//competitiveness index 
		static double m_myRating;

		//last campaign suggestions
		static CampaignData m_lastSuggestionResultsResults;



		/*initiating class members for campaign
		 * calculate campaign offer
		 */
		public static long CalcPayment(CampaignData campaign,Map<Integer,CampaignData> ourActiveCampaigns,
			Map<Integer,CampaignData> MarketActiveCampaigns,int day, double rating){
			double segmentGrade=0.0;
			if ((MarketActiveCampaigns==null)||(MarketActiveCampaigns.size()==0)){//no active campaigns
				segmentGrade=campaign.getreachImps() / ((campaign.getdayEnd() - campaign.getdayStart() + 1) * MarketSegment.marketSegmentSize(campaign.getTargetSegment()));
			}
			else{//initializing parameters
				m_campaignData = campaign;
				m_lengthOfCampaign = (int) (m_campaignData.getdayEnd()- m_campaignData.getdayStart()+1);
				m_Segements = campaign.getTargetSegment();
				m_TotalMarketActiveCampaignData = ConvertToList(MarketActiveCampaigns);
				m_lastSuggestionResultsResults = lastCampaign;
				m_myRating = rating;
				m_day = day;
				activecampaigns = MarketActiveCampaigns;
				campaign.setSegmentSize();
				activecampaigns.put(campaign.getId(), campaign);
				
				//building segment set //might look nicer in a separate function...
				basicSegmentsList = new ArrayList<Set<MarketSegment>>();
				//adding {FEMALE, MALE}X{LOW_INCOME,HIGH_INCOME}x{YOUNG,OLD}
				basicSegmentsList.add(MarketSegment.compundMarketSegment3(MarketSegment.MALE,MarketSegment.LOW_INCOME, MarketSegment.YOUNG));
				basicSegmentsList.add(MarketSegment.compundMarketSegment3(MarketSegment.MALE,MarketSegment.LOW_INCOME, MarketSegment.OLD));
				basicSegmentsList.add(MarketSegment.compundMarketSegment3(MarketSegment.MALE,MarketSegment.HIGH_INCOME, MarketSegment.YOUNG));
				basicSegmentsList.add(MarketSegment.compundMarketSegment3(MarketSegment.MALE,MarketSegment.HIGH_INCOME, MarketSegment.OLD));
				basicSegmentsList.add((MarketSegment.compundMarketSegment3(MarketSegment.FEMALE,MarketSegment.LOW_INCOME,MarketSegment.YOUNG)));
				basicSegmentsList.add(MarketSegment.compundMarketSegment3(MarketSegment.FEMALE,MarketSegment.LOW_INCOME,MarketSegment.OLD));
				basicSegmentsList.add(MarketSegment.compundMarketSegment3(MarketSegment.FEMALE,MarketSegment.HIGH_INCOME,MarketSegment.YOUNG));
				basicSegmentsList.add(MarketSegment.compundMarketSegment3(MarketSegment.FEMALE,MarketSegment.HIGH_INCOME,MarketSegment.OLD));
				
				
				System.out.println("number of active campaign in the campaign engine is: " + activecampaigns.size());
				
				segmentGrade = SegmentTypeGrade();
			}
			System.out.println("pre reach impression: " + campaign.getreachImps());//pre sugg
			
			if (m_myRating == segmentGrade){
				return (long)(Math.floor(m_myRating*campaign.getreachImps()));
			}
			else if ((long)Math.ceil(segmentGrade * campaign.getreachImps()) < consts.CampaignEngine_lowerBidLimit){ //avoiding very low budget campaigns
				System.out.println("Seven campaign Bidder: avoiding very low budget campaigns, bidding maximum");
				return (long)(Math.floor(m_myRating * campaign.getreachImps())); 
			}
			else{
				return (long)(Math.ceil(segmentGrade * campaign.getreachImps()));
			}
		}



		/*the main calculation Function :

		 * calc the PI
		 * verify that the offer is in the limitation of the spec
		 * */
		private static double SegmentTypeGrade() {
			double minimumBid = 0.1/m_myRating;
			double ALPHA;
						
			if (m_day!=0){
				if (m_lastSuggestionResultsResults.IsWin() == true) //won the campaign
					if(m_lastSuggestionResultsResults.getBudget()!=(m_lastSuggestionResultsResults.getMyBid()/1000)){//won the campaign and not by random
						BETA = BETA*consts.CampaignEngine_Ggreed;
						System.out.println("Seven campaign Bidder: won last campaign, CI: " + BETA);
					}
				else{// we didn't win - market is getting more aggressive  
					BETA  = BETA/consts.CampaignEngine_Ggreed;
					System.out.println("Seven campaign Bidder: lost last campaign, CI : " + BETA);
				}
			}
			
			ALPHA = calcALPHA(m_Segements,m_campaignData.getdayStart(),m_campaignData.getdayEnd());
			
			double GAMMA = Math.pow(ALPHA, consts.CampaignEngine_ALPHAPow) * Math.pow(BETA, consts.CampaignEngine_BETAPow);//need to optimize powers aggressiveness vs desperation
			if (m_myRating < consts.CampaignEngine_panic_rating){ // case the Rating is very low - bid maximum 
				System.out.println("Seven campaign Bidder: Rating is at panic level, bidding maximum");
				return m_myRating;
			}
			else if(m_myRating < consts.CampaignEngine_m_lowerRatingLimit){ 
				System.out.println("Seven campaign Bidder: Rating is low but not critically low, bidding minimum");
				return minimumBid;
			}
			else if(ALPHA > consts.CampaignEngine_upperALPHALimit){ //case ALPHA is too high bid maximum
				System.out.println("Seven campaign Bidder: Internal parameter value is high, bidding maximum");
				return m_myRating;
			}
			else if(GAMMA >= consts.CampaignEngine_privateValueUpperLimit * minimumBid){ //case ALPHA*CI is too high bid maximum
				System.out.println("Seven campaign Bidder: Internal parameter value is middle, encreasing to maximum");
				return m_myRating;
			}
			else if(GAMMA < consts.CampaignEngine_privateValueUpperLimit * minimumBid && GAMMA >= consts.CampaignEngine_privateValueBottomLimit * minimumBid){ //case PI*CI is high but not too high, reduce it
				System.out.println("Seven campaign Bidder: Internal parameter value is in between, reducing close to minimum");
				return Math.max(minimumBid,Math.min(1.15 * minimumBid,m_myRating));
			}
			System.out.println("Seven bidding GAMMA: " + GAMMA + " ,bidding regular");
			return Math.max(minimumBid ,Math.min(GAMMA,m_myRating));

		}

		
		/*Calculates segments popularity (ALPHA) on some days range*/
		private static double calcALPHA(Set<MarketSegment> setMarketSegment, long day_start, long day_end){
			double ALPHA = 0;
			double duration = (double)day_end-day_start+1;
			
			for(Set<MarketSegment> basicSegment: basicSegmentsList){
				if (IsbasicSegmentinSetofSegments(setMarketSegment ,basicSegment)){
					for (long i = day_start ;i <= day_end ;++i){
						ALPHA = ALPHA + MarketSegment.marketSegmentSize(basicSegment) * dailyPopularity(basicSegment,i);
					}
				}
			}
			ALPHA = ALPHA / ((double)MarketSegment.marketSegmentSize(setMarketSegment) * duration);
			return ALPHA;
		}
		
		
		/*daily segment popularity*/
		private static double dailyPopularity(Set<MarketSegment> basicMarketSegment, long day){
			double popularityIndex = 0;
			CampaignData campaign;
			Set<MarketSegment> CampaignSegment; 
			long startDay,endDay;
			boolean dayWithinRange;
			
			for (Map.Entry<Integer, CampaignData> entry : activecampaigns.entrySet()){
				campaign = entry.getValue();
				CampaignSegment = campaign.getTargetSegment();
				startDay = campaign.getdayStart();
				endDay = campaign.getdayEnd();
				
				dayWithinRange = false;
				if (day >= startDay && day <= endDay){
					dayWithinRange = true;
				}
				if (dayWithinRange && IsbasicSegmentinSetofSegments(CampaignSegment,basicMarketSegment)){//relevant date + relevant segment
					popularityIndex = popularityIndex + ((double)campaign.getreachImps() / ((double)campaign.getSegmentSize() * ((double)endDay - startDay + 1)));// + reach/(size*lengthofcamp)
				}
			}
			return popularityIndex;
		}
		


  
	    		
		private static boolean IsbasicSegmentinSetofSegments(Set<MarketSegment> segments ,Set<MarketSegment> basicSegment) {
			for (MarketSegment s: segments){
				for (MarketSegment bs: basicSegment){
					if (s == bs){
						return true;
					}
				}
			}
			return false;
		}

		//up north, calculation part
		//down south, data structures
		
		private static List<CampaignData> ConvertToList(Map<Integer,CampaignData> CampDict){
			List<CampaignData> campaignDataList = new ArrayList<CampaignData>();
			for (Map.Entry<Integer, CampaignData> campaign : CampDict.entrySet())
			{
				campaignDataList.add(campaign.getValue());
			}
			return campaignDataList;

		}

			/*return true iff smallermarketsegment is contained in biggermarketsegment*/ //delete?
			private static boolean isSetContainedInSet(Set<MarketSegment> containerMarketSegments,Set<MarketSegment> containedmarketSegment) {
				MarketSegment[] biggerMarketSegment = containerMarketSegments.toArray(new MarketSegment[containerMarketSegments.size()]);
				MarketSegment[] smallerMarketSegment = containedmarketSegment.toArray(new MarketSegment[containedmarketSegment.size()]);
				boolean sum;
				for (MarketSegment contained: smallerMarketSegment){
					sum = false;
					for (MarketSegment container: biggerMarketSegment){
						if (container == contained){
							sum = true;
						}
					}
					if(!sum){
						return sum;
					}
				}
				return true;
			}
			
			/*basic, single day popularity of market segment //external usage
			 */
		    public static double pop(Set<MarketSegment> basicMarketSegment, long day, Map<Integer,CampaignData> activeCampaigns)
		    {
		        activecampaigns = activeCampaigns;
		        return dailyPopularity(basicMarketSegment,day);
		    }
			

}

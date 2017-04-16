import java.util.Set;

import tau.tac.adx.demand.CampaignStats;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.InitialCampaignMessage;

public class CampaignData {
	
		/* campaign attributes as set by server */
		long reachImps;
		long dayStart;
		long dayEnd;
		Set<MarketSegment> targetSegment;
		double videoCoef;
		double mobileCoef;
		int id;
		
		double campaignDifficulty;
		Integer SegmentSize;
		double MyBid;
		Boolean IsWin;
		Double MyRating;
		AdxQuery[] campaignQueries;//array of queries relvent for the campaign.
		CampaignStats stats;/* campaign info as reported */
		double budget;
		double remainingBudget;
		
		double impressionRatio;
		double currentRatio;
		double ratioTillFinish;

		//Added by Daniel
		private double neededReach;
		private boolean critical;//Was the campaign chosen for improving our rating
		private double promisedPayment;//What we'll get payed for 100% of the impressions needed for the campaign

		
		public CampaignData(InitialCampaignMessage icm) {
			dayStart = icm.getDayStart();
			dayEnd = icm.getDayEnd();
			id = icm.getId();
			reachImps = icm.getReachImps();
			targetSegment = icm.getTargetSegment();
			mobileCoef = icm.getMobileCoef();
			videoCoef = icm.getVideoCoef();
			stats = new CampaignStats(0, 0, 0);
			SegmentSize = 0;
			budget = 0.0;
			remainingBudget = 0.0;
			MyBid = 0.0;
			IsWin = false;
			MyRating = 0.0;
			impressionRatio = reachImps/(1+dayEnd-dayStart);
			currentRatio = 0;
			ratioTillFinish = reachImps/(1+dayEnd-dayStart);
			campaignDifficulty = (double)reachImps/((double)(1+dayEnd-dayStart)*(double)(MarketSegment.marketSegmentSize(targetSegment)));

			//Added by Daniel
			neededReach = ((double)reachImps)/(((double)this.getCampaignLength()) * ((double)MarketSegment.usersInMarketSegments().get(targetSegment)));
			critical = false;
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
			SegmentSize = 0;
			budget = 0.0;
			remainingBudget = 0.0;
			MyBid = 0.0;
			IsWin = false;
			MyRating = 0.0;
			impressionRatio = reachImps/(1+dayEnd-dayStart);
			currentRatio = 0;
			ratioTillFinish = reachImps/(1+dayEnd-dayStart);
			campaignDifficulty = (double)reachImps/((double)(1+dayEnd-dayStart)*(double)(MarketSegment.marketSegmentSize(targetSegment)));

			//Added by Daniel
			neededReach = ((double)reachImps)/(((double)this.getCampaignLength()) * ((double)MarketSegment.usersInMarketSegments().get(targetSegment)));
			critical = false;
		}

		@Override
		public String toString() {
			return "Campaign ID " + id + ": " + "day " + dayStart + " to "
					+ dayEnd + " " + targetSegment + ", reach: " + reachImps
					+ " coefs: (v=" + videoCoef + ", m=" + mobileCoef + ")";
		}

		/*========================================================
		 *						setters 
		 =======================================================*/

		public void setStats(CampaignStats s) {
			stats.setValues(s);
		}
		
		public void setCampaignQueries(AdxQuery[] campaignQueries) {
			this.campaignQueries = campaignQueries;
		}
		public void setMyBid(double bid) {
			MyBid = bid;
		}
		public void setMyRating(double rate) {
			MyRating = rate;
		}
		public void setIsWin(boolean win) {
			IsWin = win;
		}
		
		public void setBudget(double d) {
			budget = d;
		}
		
		public void setRemainingBudget(double d) {
			remainingBudget = d;
		}
		
		public void setSegmentSize() {
			this.SegmentSize = MarketSegment.marketSegmentSize(this.targetSegment);
		}
		
		public void setRatios(int day){
			if ((day>dayStart)&&(dayEnd>day)){
				currentRatio = stats.getTargetedImps()/(day-dayStart);
				ratioTillFinish = impsTogo()/daysTogo(day)+1;
			}
		}
		
		//Added by Daniel ----------------------------------------------
		public void setCriticalParameter(boolean newValue){
			critical = newValue;
		}
		
		public void setPromisedPayment(double newValue){
			promisedPayment = newValue;
		}
		//--------------------------------------------------------------
		
		
		/*========================================================
		 *						getters 
		 =======================================================*/
		 //Added by Daniel ------------------------------------------
		//Allow ourselves to overachieve
		int impsTogo() {
			return (int) Math.max(0 , 1.1 * reachImps - stats.getTargetedImps());//TODO - Dan - decide if 1.1 is ok or should we change it
		}
		//If we don't want to overachieve
		int competitionImpsToGo(){
			return (int) Math.max(0 , reachImps - stats.getTargetedImps());//TODO
		}
		
		boolean isCritical(){
			return critical;
		}
		
		double getNeededReach(){
			return neededReach;
		}
		
		public int getCampaignLength(){
			return ((int) this.dayEnd - (int)this.dayStart) + 1;
		}

		public double getPromisedPayment(){
			return promisedPayment;
		}
		//------------------------------------------------------------
		public int daysTogo(int day){
			return (int) dayEnd-day;
		}
		
		public AdxQuery[] getCampaignQueries() {
			return campaignQueries;
		}
		
		public long getdayStart() {
			return dayStart;
		}
		
		public long getdayEnd() {
			return dayEnd;
		}
				
		public Set<MarketSegment> getTargetSegment() {
			return targetSegment;
		}
		
		public int getId() {
			return id;
		}
		
		public double getVideoCoef() {
			return videoCoef;
		}
		
		public double getMobileCoef() {
			return mobileCoef;
		}
		
		public long getReachImps(){
			return reachImps;
		}
		
		public Integer getSegmentSize(){
			return SegmentSize;
		}		
		
		public double getMyBid(){
			return MyBid;
		}
		
		public boolean IsWin(){
			return IsWin;
		}
		
		public double getMyRating(){
			return MyRating;
		}
		
		public double getBudget() {
			return budget;
		}
		
		public double getRemainingBudget() {
			return remainingBudget;
		}
		
		public double getImpressionRatio() {
			return impressionRatio;
		}
		
		public double getCurrentRatio() {
			return currentRatio;
		}
		
		public double getRatioTillFinish() {
			return ratioTillFinish;
		}
		
		public double getCampaignDifficulty() {
			return campaignDifficulty;
		}
		
}
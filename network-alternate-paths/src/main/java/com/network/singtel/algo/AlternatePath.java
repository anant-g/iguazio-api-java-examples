package com.network.singtel.algo;

public class AlternatePath {

	private String id;
	private String sourceRouter;
	private String destRouter;
	private String alternateBestPath;
	private String currentBestPath;
	private boolean isLatencyCriteriaOK;
	private boolean isPktRateCriteriaOK;
	private boolean isUtilCriteriaOK;
	private boolean isPathFeasible;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSourceRouter() {
		return sourceRouter;
	}

	public void setSourceRouter(String sourceRouter) {
		this.sourceRouter = sourceRouter;
	}

	public String getDestRouter() {
		return destRouter;
	}

	public void setDestRouter(String destRouter) {
		this.destRouter = destRouter;
	}

	public String getAlternateBestPath() {
		return alternateBestPath;
	}

	public void setAlternateBestPath(String alternateBestPath) {
		this.alternateBestPath = alternateBestPath;
	}

	public String getCurrentBestPath() {
		return currentBestPath;
	}

	public void setCurrentBestPath(String currentBestPath) {
		this.currentBestPath = currentBestPath;
	}

	public boolean isLatencyCriteriaOK() {
		return isLatencyCriteriaOK;
	}

	public void setLatencyCriteriaOK(boolean isLatencyCriteriaOK) {
		this.isLatencyCriteriaOK = isLatencyCriteriaOK;
	}

	public boolean isPktRateCriteriaOK() {
		return isPktRateCriteriaOK;
	}

	public void setPktRateCriteriaOK(boolean isPktRateCriteriaOK) {
		this.isPktRateCriteriaOK = isPktRateCriteriaOK;
	}

	public boolean isUtilCriteriaOK() {
		return isUtilCriteriaOK;
	}

	public void setUtilCriteriaOK(boolean isUtilCriteriaOK) {
		this.isUtilCriteriaOK = isUtilCriteriaOK;
	}

	public boolean isPathFeasible() {
		return isPathFeasible;
	}

	public void setPathFeasible(boolean isPathFeasible) {
		this.isPathFeasible = isPathFeasible;
	}

}

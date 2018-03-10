package dk.kingu.shooting;

import java.nio.file.Path;
import java.util.Date;

/**
 * Class to carry the information associated with a result found in the filesystem 
 */
public class ResultFile {

	/** Path to the pdf file containing the result */
	private Path filePath;
	/** The name of the shooter */
	private String shooterName;
	/** The lane number the result was from */
	private int laneNumber;
	/** The time when the result was available (e.i. file creation time) */
	private Date resultDate;
	
	
	public ResultFile(Path filePath, String shooterName, int laneNumber, Date resultDate) {
		this.filePath = filePath;
		this.shooterName = shooterName;
		this.laneNumber = laneNumber;
		this.resultDate = resultDate;
	}


	public Path getFilePath() {
		return filePath;
	}


	public void setFilePath(Path filePath) {
		this.filePath = filePath;
	}


	public String getShooterName() {
		return shooterName;
	}


	public void setShooterName(String shooterName) {
		this.shooterName = shooterName;
	}


	public int getLaneNumber() {
		return laneNumber;
	}


	public void setLaneNumber(int laneNumber) {
		this.laneNumber = laneNumber;
	}


	public Date getResultDate() {
		return resultDate;
	}


	public void setResultDate(Date resultDate) {
		this.resultDate = resultDate;
	}
	
}

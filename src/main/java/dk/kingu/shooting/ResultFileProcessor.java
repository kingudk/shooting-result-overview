package dk.kingu.shooting;

import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultFileProcessor {
    private static Logger log = LoggerFactory.getLogger(ResultFileProcessor.class);

	final Path pdfFile;
	boolean processed = false;
	int laneNumber = 0;
	String shooterID = null;
	
	public ResultFileProcessor(Path pdfFile) {
		this.pdfFile = pdfFile;
	}
	
	public void process() {
        try (PDDocument document = PDDocument.load(pdfFile.toFile())) {
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition( true );
            Rectangle rect = new Rectangle( 10, 10, 200, 110 );
            stripper.addRegion( "class1", rect );
            PDPage firstPage = document.getPage(0);
            stripper.extractRegions( firstPage );
            
            String text = stripper.getTextForRegion( "class1" );
            String fragments[] = text.split("\n");
            
            if(fragments.length != 2) {
            	log.info("Didn't find two fragments in text in document {}", pdfFile);
            	shooterID = "Ukendt";
            	laneNumber = 0;
            } else {
                shooterID = fragments[0];
                laneNumber = extractLaneNumber(fragments[1]);
            }
            
        } catch (InvalidPasswordException e) {
			log.error("Error trying to read file {} (password protected?)", pdfFile, e);
		} catch (IOException e) {
		    log.error("IO failure processing file {}", pdfFile, e);
		}
		
		processed = true;
	}
	
	public static int extractLaneNumber(String laneNumberString) {
		int laneNumber;
        String frag = laneNumberString.trim();
		if(frag.matches("[^0-9]+")) {
			laneNumber = -1;
			log.info("Got a PDF file that did not have a parseable lane number line");
		} else {
	        if(frag.contains(" ")) {
	        	laneNumber = Integer.parseInt(frag.split(" ")[0]);
	        } else {
	        	laneNumber = Integer.parseInt(frag);
	        }              
	    }
        return laneNumber;
	}
	
	public int getLaneNumber() {
		if(!processed) {
			process();
		}
		
		return laneNumber;
	}
	
	public String getShooterID() {
		if(!processed) {
			process();
		}
		
		return shooterID;
	}
	
}

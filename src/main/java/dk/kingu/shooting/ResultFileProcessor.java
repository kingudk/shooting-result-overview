package dk.kingu.shooting;

import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class ResultFileProcessor {

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
            	System.out.println("Didn't find two fragments in text in document " + pdfFile);
            	shooterID = "Ukendt";
            	laneNumber = 0;
            } else {
                shooterID = fragments[0];
                String frag = fragments[1].trim();
                String numberString;
                if(frag.contains(" ")) {
                	numberString = frag.split(" ")[0];
                } else {
                	numberString = frag;
                }                
                laneNumber = Integer.parseInt(numberString);
            }
            
        } catch (InvalidPasswordException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		processed = true;
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

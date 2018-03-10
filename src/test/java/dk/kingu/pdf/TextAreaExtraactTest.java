package dk.kingu.pdf;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.testng.annotations.Test;

public class TextAreaExtraactTest {
	
	@Test
	public void extractTest() {
        //try (PDDocument document = PDDocument.load(new File(args[0])))
//        try (PDDocument document = PDDocument.load(	getClass().getClassLoader().getResourceAsStream("job_23-MSxpsPS.pdf")))
//        try (PDDocument document = PDDocument.load(	getClass().getClassLoader().getResourceAsStream("job_32-Untitled_Document_1.pdf")))
        try (PDDocument document = PDDocument.load(	getClass().getClassLoader().getResourceAsStream("smbprn.00000181_SiusData_-_TargetSheets-job_248.pdf")))

        
        {
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition( true );
            //Rectangle rect = new Rectangle( 10, 280, 275, 60 );
            Rectangle rect = new Rectangle( 10, 10, 200, 110 );
            stripper.addRegion( "class1", rect );
            PDPage firstPage = document.getPage(0);
            stripper.extractRegions( firstPage );
            System.out.println( "Text in the area:" + rect );
            System.out.println( stripper.getTextForRegion( "class1" ) );
            
            String text = stripper.getTextForRegion( "class1" );
            String fragments[] = text.split("\n");
            
            System.out.println("number of fragments: " + fragments.length);
            for(String f : fragments) {
            	System.out.println("fragment: '" + f + "'");
            }
            
        } catch (InvalidPasswordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void extractTest2() {
        //try (PDDocument document = PDDocument.load(new File(args[0])))
//        try (PDDocument doc = PDDocument.load(	getClass().getClassLoader().getResourceAsStream("job_23-MSxpsPS.pdf")))
        try (PDDocument doc = PDDocument.load(	getClass().getClassLoader().getResourceAsStream("smbprn.00000181_SiusData_-_TargetSheets-job_248.pdf")))
        
        {
        	// look at all the document information
        	PDDocumentInformation info = doc.getDocumentInformation();
        	PDDocumentCatalog cat = doc.getDocumentCatalog();

        	COSDictionary dict = cat.getPages().getCOSObject();
        	
        	Set l = dict.keySet();
        	for (Object o : l) {
        	    //System.out.println(o.toString() + " " + dict.getString(o));
        	    System.out.println(o.toString());
        	}

        	// look at the document catalog
        	System.out.println("Catalog:" + cat);

        	PDPageTree lp = cat.getPages();
        	
        	System.out.println("# Pages: " + lp.getCount());
        	PDPage page = lp.get(0);
        	System.out.println("Page: " + page);
        	System.out.println("\tCropBox: " + page.getCropBox());
        	System.out.println("\tMediaBox: " + page.getMediaBox());
        	System.out.println("\tResources: " + page.getResources());
        	System.out.println("\tRotation: " + page.getRotation());
        	System.out.println("\tArtBox: " + page.getArtBox());
        	System.out.println("\tBleedBox: " + page.getBleedBox());
        	System.out.println("\tContents: " + page.getContents());
        	System.out.println("\tTrimBox: " + page.getTrimBox());
        	List<PDAnnotation> la = page.getAnnotations();
        	System.out.println("\t# Annotations: " + la.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	


}




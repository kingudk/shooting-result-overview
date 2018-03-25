package dk.kingu.shooting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.Locale;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.themes.ValoTheme;

import pl.pdfviewer.PdfViewer;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
@Push
public class MyUI extends UI {

    private static Logger log = LoggerFactory.getLogger(MyUI.class);
    
    @Override
    protected void init(VaadinRequest vaadinRequest) {
        log.info("Initializing UI");
        Path watchFolder = getPDFDir();
        final String printerName = getPrinterName();
        UI ui = getUI();     
    	
    	final VerticalLayout layout = new VerticalLayout();
    	layout.setSizeFull();
    	layout.setMargin(false);
    	
    	final HorizontalLayout mainLayout = new HorizontalLayout();
    	mainLayout.setMargin(false);
    	mainLayout.setSizeFull();
    	final VerticalLayout resultChooserLayout = new VerticalLayout();
        Label headline = new Label("<b>Vælg resultat fra listen</b>", ContentMode.HTML);
        
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("da", "dk"));
        Grid<ResultFile> resultGrid  = new Grid<>();
        resultGrid.addColumn(ResultFile::getResultDate).setCaption("Dato").setRenderer(new DateRenderer(df));
        resultGrid.addColumn(ResultFile::getLaneNumber).setCaption("Bane #");
        resultGrid.addColumn(ResultFile::getShooterName).setCaption("Skytte");
        resultGrid.setSelectionMode(SelectionMode.SINGLE);
        
        ResultWatch watcher = new ResultWatch(watchFolder);
        watcher.watch();
        GridDataSink gridData = new GridDataSink(watcher, ui);
        resultGrid.setDataProvider(gridData.getDataProvider());
        resultGrid.setWidth("480px");
        resultGrid.setHeightByRows(20);
        
        final Button reloadPage = new Button("Gen-indlæs");
        reloadPage.setEnabled(true);

        resultChooserLayout.addComponents(headline, resultGrid, reloadPage);
        resultChooserLayout.setWidth("500px");
        mainLayout.addComponent(resultChooserLayout);
        
        VerticalLayout pdfContainer = new VerticalLayout(); 
        pdfContainer.setWidth("1300px");

        File pdf = new File("/tmp/Kontoplan.pdf");
        PdfViewer pdfViewer = new PdfViewer(pdf);
        pdfViewer.setDownloadBtnVisible(false);
        
        
        VerticalLayout buttonContainer = new VerticalLayout();
        buttonContainer.setWidth("300px");
        
        final Button print = new Button("Print");
        print.setEnabled(true);
        print.setStyleName(ValoTheme.BUTTON_PRIMARY);
        buttonContainer.addComponent(print);
   
        print.addClickListener(e -> {
        	Set<ResultFile> items = resultGrid.getSelectedItems();
        	ResultFile selectedFile = (ResultFile) items.toArray()[0];
        	File file = selectedFile.getFilePath().toFile();
        	boolean success;
			try {
				success = PrintUtils.printFile(file, printerName);
	        	if(success) {
	        		Notification notice = new Notification("Udskrift", "Udskrift sendt til printer", 
	        				Notification.Type.HUMANIZED_MESSAGE);
	        		notice.setDelayMsec(5000);
	        		notice.show(Page.getCurrent());
	        		log.info("Printjob sent to printer for file '{}'", file.toString());
	        	} else {
	        		Notification notice = new Notification("Udskrift", "Udskrift mislykkedes", 
	        				Notification.Type.ERROR_MESSAGE);
	        		notice.setDelayMsec(5000);
	        		log.warn("Failed to send print job to printer for file '{}'", file.toString());
	        	}
			} catch (IOException | InterruptedException ex) {
			    log.error("Error printing job for file '{}'", file.toString(), ex);
			}
			log.debug("Print button clicked for file: {}", selectedFile);
        });    
        
        reloadPage.addClickListener(e -> {
        	Page.getCurrent().reload();
        });
        
        pdfContainer.addComponent(pdfViewer);
        mainLayout.addComponent(pdfContainer);
        mainLayout.addComponent(buttonContainer);
        
        resultGrid.addSelectionListener( e -> {
        	Set<ResultFile> items = resultGrid.getSelectedItems();
        	ResultFile selectedFile = (ResultFile) items.toArray()[0];
        	pdfContainer.removeAllComponents();
        	File newpdf = selectedFile.getFilePath().toFile();
        	PdfViewer newpdfViewer = new PdfViewer(newpdf);
            newpdfViewer.setDownloadBtnVisible(false);
        	pdfContainer.addComponent(newpdfViewer);
        });
        
        layout.addComponents(mainLayout);
        setContent(layout);
    }
    
       
    private static String getPrinterName() {
    	String printerName = System.getProperty("PRINTER");
    	log.debug("Read PRINTER env got: '{}'", printerName);
    	
        if(printerName == null || printerName.isEmpty()) {
            log.error("PRINTER is not set, failing");
        	throw new RuntimeException("PRINTER was not set!");
        }
    	
    	return printerName;
    }
    
    private static Path getPDFDir() {
        String pdfDir = System.getProperty("PDF_DIR");
        log.debug("Read PDF_DIR env got: '{}'", pdfDir);

        if(pdfDir == null || pdfDir.isEmpty()) {
        	log.error("PDF_DIR is not set, failing");
        	throw new RuntimeException("PDF_DIR was not set!");
        }
        
        Path watchFolder = Paths.get(pdfDir);
        if(!(watchFolder.toFile().isDirectory() && watchFolder.toFile().canRead())) {
        	log.error("PDF_DIR ({}) was either not a directory or not readable, failing", pdfDir);
        	throw new RuntimeException("Could not read " + pdfDir + " !");
        }
        return watchFolder;
    }

    
    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}

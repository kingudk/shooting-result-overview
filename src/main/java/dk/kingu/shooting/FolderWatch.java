package dk.kingu.shooting;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.UI;

public class FolderWatch {

	WatchService watcher;
	WatchKey key;
	final Path watchedFolder;
	List<String> subject;
	ListDataProvider<String> listSink;
	UI ui;
	
	// TODO Figure out how to release resources when parent UI is done with them.
	
	
	public FolderWatch(Path watchedFolder, List<String> subject, ListDataProvider<String> sink, UI ui) {
		try {
			watcher = FileSystems.getDefault().newWatchService();
			this.watchedFolder = watchedFolder;
			System.out.println("Watching: " + watchedFolder);
			key = watchedFolder.register(watcher,
	                ENTRY_CREATE,
	                ENTRY_DELETE,
	                ENTRY_MODIFY);
			this.subject = subject;
			listSink = sink;
			this.ui = ui;
			
			listFiles(watchedFolder);
			
		} catch (IOException e) {
			System.err.println("Ran into an issuwatchedFoldere creating FolderWatch: " + e);
			throw new RuntimeException(e);
		}
	}
	
	
	public void watch() {
		Runnable watcher = new Watcher(/*notifier*/);
		
		Thread t = new Thread(watcher);
		t.setDaemon(true);
		t.start();
		
	}
	
	private List<String> listFiles(Path dir) {
		List<String> fileList = new ArrayList<>();
		List<File> files = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
		    for (Path file: stream) {
		    	if(!Files.isDirectory(file)) {
		    		fileList.add(file.getFileName().toString());
		    		files.add(file.toFile());
		    	}
		    }
		} catch (IOException | DirectoryIteratorException x) {
		    System.err.println(x);
		}
		Collections.sort(files, Comparator.comparing(File::lastModified));
		Collections.reverse(files);
		subject.clear();
		for(File f : files) {
			subject.add(f.toString());
		}
		
		ui.access(() -> listSink.refreshAll());
		return fileList;
	}
	
	
	private class Watcher implements Runnable {		

		@Override
		public void run() {
			
			while(true) {
			    WatchKey key;
			    try {
			        key = watcher.take();
			    } catch (InterruptedException x) {
			        return;
			    }

			    for (WatchEvent<?> event: key.pollEvents()) {
			        WatchEvent.Kind<?> kind = event.kind();

			        listFiles(watchedFolder);
			        
			        
			        if(kind == OVERFLOW) {
			        	System.out.println("Got overflow event");
			        	continue;
			        } else if(kind == ENTRY_CREATE) {
			        	System.out.println("Got new entry event");
			        } else if(kind == ENTRY_DELETE) {
			        	System.out.println("Got delete entry event");
			        } else if(kind == ENTRY_MODIFY) {
			        	System.out.println("Got modift entry event");
			        }
			        
			        WatchEvent<Path> ev = (WatchEvent<Path>)event;
			        Path filename = ev.context();
			        System.out.println("Event filename was: " + filename);
			        			        
			        if (kind == OVERFLOW) {
			            continue;
			        }
			    }

			    // Reset the key -- this step is critical if you want to
			    // receive further watch events.  If the key is no longer valid,
			    // the directory is inaccessible so exit the loop.
			    boolean valid = key.reset();
			    if (!valid) {
			        break;
			    }
			}			
		}
	}
	

}

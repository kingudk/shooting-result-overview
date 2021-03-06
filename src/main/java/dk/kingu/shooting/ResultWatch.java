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
import java.nio.file.attribute.BasicFileAttributeView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultWatch {
    private Logger log = LoggerFactory.getLogger(getClass());

	WatchService watcher;
	WatchKey key;
	final Path watchedFolder;
	Map<String, ResultFile> results;
	Set<ResultWatchSink> sinks = new HashSet<>();
	
	// TODO Figure out how to release resources when parent UI is done with them.
	
	
	public ResultWatch(Path watchedFolder) {
		try {
			results = new HashMap<>();
			watcher = FileSystems.getDefault().newWatchService();
			this.watchedFolder = watchedFolder;
			log.info("Watching: {}", watchedFolder);
			key = watchedFolder.register(watcher,
	                ENTRY_CREATE,
	                ENTRY_DELETE,
	                ENTRY_MODIFY);
			
			initialize();
		} catch (IOException e) {
			log.error("Ran into an issue when creating FolderWatch", e);
			throw new RuntimeException(e);
		}
	}
	
	private void initialize() {
		List<File> files = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(watchedFolder)) {
		    for (Path file : stream) {
		    	if(!Files.isDirectory(file)) {
		    		files.add(file.toFile());
		    	}
		    }
		} catch (IOException | DirectoryIteratorException e) {
		    log.error("Error initializing ResultWatch", e);
		}
		Collections.sort(files, Comparator.comparing(File::lastModified));
		Collections.reverse(files);
		
		for(File f: files) {
			addResult(f.toPath());
		}
	}
	
	
	public void watch() {
		Runnable watcher = new Watcher(/*notifier*/);
		
		Thread t = new Thread(watcher);
		t.setDaemon(true);
		t.start();
		
	}
	
	public void registerSink(ResultWatchSink sink) {
		sinks.add(sink);
	}
	
	public Collection<ResultFile> getResults() {
		return (Collection<ResultFile>) Collections.unmodifiableCollection(results.values());
	}
	
	protected void processNotifications() {
		for(ResultWatchSink sink : sinks) {
			sink.update();
		}
	}
	
	protected ResultFile buildResultFile(Path filename) throws IOException {
		Path fullPath = watchedFolder.resolve(filename);
		ResultFileProcessor rfp = new ResultFileProcessor(fullPath);
		Date fileDate = new Date(Files.getFileAttributeView(fullPath, BasicFileAttributeView.class).readAttributes().creationTime().toMillis());
		
		ResultFile rf = new ResultFile(fullPath,rfp.getShooterID(), rfp.laneNumber, fileDate);
		return rf;
	}
	
	protected void addResult(Path filename) {
		try {
			if(filename.toString().endsWith(".pdf")) {
				ResultFile result = buildResultFile(filename);
				results.put(filename.getFileName().toString(), result);
			} else {
				log.info("File ({}) did not end in .pdf, not processing", filename);
			}
		} catch (IOException e) {
			log.error("Failed to add result for file ({})", filename, e);
		}
	}
	
	protected void removeResult(Path filename) {
		results.remove(filename.getFileName().toString());
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
        
			        if(kind != OVERFLOW) {
			        	WatchEvent<Path> ev = (WatchEvent<Path>) event;
				        Path filename = ev.context();
				        
				        if(kind == ENTRY_CREATE) {
				        	log.debug("Got new entry event for file {}", filename);
				        } else if(kind == ENTRY_DELETE) {
				            log.debug("Got delete entry event for file {}", filename);
				        	removeResult(filename);
				        } else if(kind == ENTRY_MODIFY) {
				            log.debug("Got modift entry event for file {}", filename);
				        	addResult(filename);
				        }
			        }
			        processNotifications();
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

package dk.kingu.shooting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrintUtils {

	
	/**
	 * Simple method for submitting a print job for a specific file on a named printer. 
	 * The linux 'lp' command is used to submit the print job. 
	 * Errors is not well checked (data on stdin/stdout/stderr is not checked)
	 * @param file The file to print
	 * @param printerName The name of the CUPS printer
	 */
	public static boolean printFile(File file, String printerName) throws IOException, InterruptedException {
		
		List<String> command = new ArrayList<>();
		command.add("lp"); 
		command.add("-d");
		command.add(printerName);
		command.add(file.toString());
		
		Process process = new ProcessBuilder(command).start();
		int exitCode = process.waitFor();
			
		return exitCode == 0;
	}
}

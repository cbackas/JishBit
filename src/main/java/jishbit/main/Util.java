package jishbit.main;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

public class Util {
	
	public static File bothPath;

	static Optional<String> getBotToken() {
		try {
			File tokenFile = new File(bothPath, "token.txt");
			if(tokenFile.exists()) {
				String token = FileUtils.readFileToString(tokenFile, (String) null);
				if(!token.equalsIgnoreCase("TOKEN") && !token.isEmpty()) {
					return Optional.of(token);
				} else {
					return Optional.empty();
				}
			} else {
				FileUtils.writeStringToFile(tokenFile, "TOKEN", (String) null);
				return Optional.empty();
			}
		} catch(IOException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
	
}

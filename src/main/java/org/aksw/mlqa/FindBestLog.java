package org.aksw.mlqa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FindBestLog {
	public static void main(String[] args) throws IOException {
		Files.walk(Paths.get("src/main/resource")).peek(System.out::println);
	}
}

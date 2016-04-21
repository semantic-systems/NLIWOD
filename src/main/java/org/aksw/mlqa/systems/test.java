package org.aksw.mlqa.systems;

import java.io.IOException;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

public class test {
	public static void main(String[] args) {
		Asker webAsker = new Asker();
		try {
			System.out.println(webAsker.askSina("give me Apollo 14 astronauts"));
		} catch (IllegalStateException | IOException | URISyntaxException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

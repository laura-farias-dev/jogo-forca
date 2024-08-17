import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class Hangman {
	private int remainingTries;
	private int tries = 6;
	private List<String> words = Arrays.asList("gato", "cachorro", "pato", "carro", "navio");
	private String word;
	private String encodedWord;
	private HashSet<Character> guessedLetters = new HashSet<Character>();
	private HashSet<Character> wordLetters = new HashSet<Character>();
	
	public Hangman() {
		this.word = this.drawWord();
		this.wordLetters = word.chars().mapToObj(e -> (char) e).collect(Collectors.toCollection(HashSet::new));
		this.computeEncodedWord();
	}
	
	private String drawWord() {
		Random rand = new Random();
		String randomWord = words.get(rand.nextInt(words.size()));
		
		return randomWord;
	}

	private void computeEncodedWord() {
		StringBuilder encodedWord = new StringBuilder();
		
		for (char ch: word.toCharArray()) {
			if(guessedLetters.contains(ch)) {
				encodedWord.append(ch);
			} else {
				encodedWord.append("-");
			}
		}
		
		this.encodedWord = encodedWord.toString();
	}
	
	public void guess(String letterGuessed) {
		char letter = letterGuessed.charAt(0);
		guessedLetters.add(Character.toLowerCase(letter));
		this.computeEncodedWord();
		
		if(!wordLetters.contains(letter)) {
			--this.remainingTries;
		}
	}
	
	public boolean won() {
		return this.guessedLetters.containsAll(wordLetters);
	}
	
	public boolean lost() {
		return this.remainingTries <= 0;
	}
	
	public String getEncodedWord() {
		return this.encodedWord;
	}
}

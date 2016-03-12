package br.unime.dialoguetcc;


// This class contains a dictionary for replacement of words to allow slightly different questions (words) to be matched to the same keyword sets.

public class Normalizer {
	
	// Here is the list of words to be replaced in the original question. The first is the spoken word. The second is the word that will replace the first in the question.
	private static final String[][] dictionary = {
		{"quais", "qual"},
	};
	
	// Replaces the words found in the position in the first column of the dictionary array for the words found in the second column of the dictionary array
	public static String normalize(String sentence){
		String[] words = sentence.split("\\s+");
		for (int i = 0; i < words.length; i++) {
			for (String[] match : dictionary) {
				if(words[i].equals(match[0])){
					words[i] = match[1];
				}
			}
		}
		
		return implodeStringArray(words);
	}
	
	// Transforms an array of Strings in a single string with every word separated by a blank space
	public static String implodeStringArray(String[] sArray){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sArray.length; i++) {
			sb.append(sArray[i]);
			if(i != sArray.length - 1){
				sb.append(" ");
			}
		}
		return sb.toString();
	}

}

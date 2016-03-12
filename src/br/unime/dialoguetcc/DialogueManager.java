package br.unime.dialoguetcc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import android.content.Context;

public class DialogueManager {
	
	private Context context;
	
	private Map<Integer, String[]> knowledge; // Contains a map from the identifier of the response sets to each sets of keywords from questions
	private Map<Integer, String> complementEPMTrue; // Contains a map from the identifier of responses to a complement for some responses in case the user HAS visited the Espiral de Plantas Medicinais before.
	private Map<Integer, String> complementEPMFalse; // Contains a map from the identifier of responses to a complement for some responses in case the user HAS NOT visited the Espiral de Plantas Medicinais before.
	
	private Map<Integer, List<Integer>> history; // Contains all responses given before for each question
	
	private int lastResponse[] = {0,0}; // Keeps the last identifier of responses and the amount of time it was asked in a row.
	private boolean specialResponse = false;
	private boolean medPlantsSpiralVisited = false;
		
	private Random random;
	
	private static final int REPETITION_ALLOWED = 2;
	
	public DialogueManager(Context context){
		this.context = context;
		
		this.history = new HashMap<Integer, List<Integer>>();
		this.knowledge = new HashMap<Integer, String[]>();
		
		this.knowledge.put(R.array.own_name, new String[] {"qual", "seu", "nome"});
		this.knowledge.put(R.array.likes_banana, new String[] {"gosta", "banana"});
		this.knowledge.put(R.array.what_day, new String[] {"que", "dia", "é", "hoje"});
		this.knowledge.put(R.array.plants_names, new String[] {"qual", "nome", "plantas"});
		this.knowledge.put(R.array.chive_use, new String[] {"serve", "cebolinha"});
		this.knowledge.put(R.array.chive_dishes, new String[] {"prato", "cebolinha"});
		this.knowledge.put(R.array.basil_use, new String[] {"serve", "manjericão"});
		this.knowledge.put(R.array.basil_dishes, new String[] {"prato", "manjericão"});
		this.knowledge.put(R.array.parsley_use, new String[] {"serve", "salsa"});
		this.knowledge.put(R.array.parsley_dishes, new String[] {"prato", "salsa"});
		this.knowledge.put(R.array.cilantro_use, new String[] {"serve", "coentro"});
		this.knowledge.put(R.array.cilantro_dishes, new String[] {"prato", "coentro"});
		this.knowledge.put(R.array.rosemary_use, new String[] {"serve", "alecrim"});
		this.knowledge.put(R.array.rosemary_dishes, new String[] {"prato", "alecrim"});
		this.knowledge.put(R.array.oregano_use, new String[] {"serve", "orégano"});
		this.knowledge.put(R.array.oregano_dishes, new String[] {"prato", "orégano"});
		this.knowledge.put(R.array.good_for_health, new String[] {"faz", "bem", "saúde"});
		this.knowledge.put(R.array.most_delicious, new String[] {"mais", "gostosa"});
		this.knowledge.put(R.array.only_edible, new String[] {"só", "existe", "essa", "planta", "comestíve"});
		this.knowledge.put(R.array.buy_grocery_store, new String[] {"compra", "mercado"});
		this.knowledge.put(R.array.combine_plants, new String[] {"combina", "planta"});
		this.knowledge.put(R.array.eat_raw, new String[] {"come", "cru"});
		this.knowledge.put(R.array.eat_cooked, new String[] {"come", "cozid"});
		this.knowledge.put(R.array.eat_cooked_or_raw, new String[] {"cru", "ou", "cozid"});
		
		this.complementEPMTrue = new HashMap<Integer, String>();
		
		this.complementEPMTrue.put(R.array.basil_use, context.getString(R.string.basil_use_true));
		this.complementEPMTrue.put(R.array.cilantro_use, context.getString(R.string.cilantro_use_true));
		this.complementEPMTrue.put(R.array.rosemary_use, context.getString(R.string.rosemary_use_true));
		this.complementEPMTrue.put(R.array.good_for_health, context.getString(R.string.good_for_health_true));
		this.complementEPMTrue.put(R.array.only_edible, context.getString(R.string.only_edible_true));
		
		this.complementEPMFalse = new HashMap<Integer, String>();
		
		this.complementEPMFalse.put(R.array.basil_use, context.getString(R.string.basil_use_false));
		this.complementEPMFalse.put(R.array.cilantro_use, context.getString(R.string.cilantro_use_false));
		this.complementEPMFalse.put(R.array.rosemary_use, context.getString(R.string.rosemary_use_false));
		this.complementEPMFalse.put(R.array.good_for_health, context.getString(R.string.good_for_health_false));
		this.complementEPMFalse.put(R.array.only_edible, context.getString(R.string.only_edible_false));
	}
	
	// Search the knowledge array if there's a response that fits the speech string
	private int searchMatch(String speech){
		int matchedResponse;
		for (Entry<Integer,String[]> entry : this.knowledge.entrySet()) {
			if(containsAllKeywords(speech, entry.getValue())){
				matchedResponse = entry.getKey();
				return matchedResponse;
			}
		}
		return R.array.no_match;
	}
	
	// Check if the speech string contains all keywords for a response.
	private boolean containsAllKeywords(String speech, String[] keywords){
		boolean containsAllKeywords = true;
		
		for (String string : keywords) {
			if(!speech.contains(string)){
				containsAllKeywords = false;
				break;
			}
		}
		return containsAllKeywords;
	}
	
	// Get a random response from the response set, given the speech.
	public String getResponse(String speech){
		speech = Normalizer.normalize(speech);
		
		int resource = this.searchMatch(speech);
		this.specialResponse = false;
		
		String[] responseSet = this.context.getResources().getStringArray(resource);
		
		this.random = new Random();
		int index = this.random.nextInt(responseSet.length);
		
		if(resource == R.array.no_match){
			this.specialResponse = true;
			this.lastResponse[1] = 0; // Resets the count of last response 
		}
		
		// Updates the last response given or the count, except if it's no_match, no_alternatives or repetition
		if(!this.specialResponse){
			if(this.lastResponse[0] == resource){
				this.lastResponse[1]++;
			} else {
				this.lastResponse[0] = resource;
				this.lastResponse[1] = 1;
			}
		}
		
		// If the question matches the keyword for the same response set the specified number in REPETITION_ALLOWED times in a row, invoke the repetition responses
		if(this.lastResponse[1] > REPETITION_ALLOWED){
			resource = R.array.repetition;
			this.specialResponse = true;
			responseSet = this.context.getResources().getStringArray(resource);
		}
		
		// Check if one question was answered already
		if(this.history.containsKey(resource)){
			List<Integer> usedResponses = this.history.get(resource);
			// Get the response for no alternatives if all responses for the question were answered
			if(usedResponses.size() == responseSet.length){
				resource = R.array.no_alternative;
				this.specialResponse = true;
				responseSet = this.context.getResources().getStringArray(resource);
			} else {
				// If not all responses were used, get one that wasn't used yet and add it to the used list
				while(this.history.get(resource).contains(index)){
					index = this.random.nextInt(responseSet.length);
				}
				usedResponses.add(index);
			}
		} else {
			// If the question was never answered before, creates the response list and put the first selected response in the history (except if it's no_match, no_alternative or repetition)
			if(!this.specialResponse){
				List<Integer> indexes = new ArrayList<Integer>();
				indexes.add(index);
				this.history.put(resource, indexes);
			}
		}
		
		String response = responseSet[index];
		
		// Fill with the appropriate day name if the user asks what day is today
		if(resource == R.array.what_day){
			int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
			String weekday = this.context.getResources().getStringArray(R.array.weekdays)[day-1];
			response = String.format(response, weekday);
		}
		
		// Get the appropriate complement to the answer based on whether the user has visited another attraction
		String complement;
		if(this.medPlantsSpiralVisited){
			complement = this.complementEPMTrue.get(resource);
		} else {
			complement = this.complementEPMFalse.get(resource);
		}
		if(complement != null){
			response = response + " " + complement;
		}
		
		return response;
	}
	
	public void setMedPlantsSpiralVisited(boolean checked){
		this.medPlantsSpiralVisited = checked;
	}

}

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class FOLResolution {
	
	public static void main(String[] args) throws IOException {
		File outFile = new File("output.txt");
		FileWriter fileWriter = new FileWriter(outFile);
		File file = new File("/Users/laicunhao/eclipse-workspace/CS561HW3/src/input.txt");
		List<Map<String, List<Term>>> query = new ArrayList<>();
		List<Map<String, List<Term>>> kb = new ArrayList<>();
		
		Scanner scanner = new Scanner(file);
		int queryNnum = Integer.parseInt(scanner.next());
		processQuery(query, queryNnum, scanner);
		int kbNum = Integer.parseInt(scanner.next());
		scanner.nextLine();
		processkb(kb, kbNum, scanner);
		
		// for loop query; query come in  正常來說應該只有一條
		for(int i = 0; i < query.size(); i++) {
			Map<String, List<Term>> curQuery = query.get(i);
			Set<String> seen = new HashSet<>();
			String recordStr = substituteProcess(new HashMap<>(), curQuery);
			
			kb.add(curQuery);
			boolean findAnswer = resolution(kb, curQuery, seen, recordStr);
			kb.remove(kb.size() - 1);
			if(findAnswer == true) {
				fileWriter.write("TRUE");
			}
			else {
				fileWriter.write("FALSE");
			}
			if(i != query.size() - 1) {
				fileWriter.write("\n");
			}
		
		}
		fileWriter.close();
		
		int x = 0;
		System.out.println(x);
		
	}
	
	
	private static void processkb(List<Map<String, List<Term>>> kb, int kbNum, Scanner scanner) {
		for(int i = 0; i < kbNum; i++) {
			
			String rawKbSentence = scanner.nextLine();
			String implication = "=>";
			kb.add(new HashMap<>());
			// determine whether contain "=>"
			if(rawKbSentence.contains(implication) == true) {
				String[] spereateFrontBack = rawKbSentence.split(" => ");
				transformImplies(kb, i, spereateFrontBack[0], spereateFrontBack[1]);
			}
			else {
				processLiteral(kb.get(i), rawKbSentence);
			}

			
		}
	}
	
	// process the kb's terms
	private static void transformImplies(List<Map<String, List<Term>>> kb, int kbIndex ,String front, String back) {
		// process front  sperate all '&'
		String[] data = front.split(" & ");
		for(int i = 0;i < data.length; i++) {
			// we have to add negative to those predicate  Play(Peter, x, Lee)
			String cleanPredicate= addNegative(data[i]);
			processLiteral(kb.get(kbIndex), cleanPredicate);
		}
		// process back
		processLiteral(kb.get(kbIndex), back);
		
		
	}
	
	private static void processLiteral(Map<String, List<Term>> kb, String data) {
		// replace , ( )
		String replaceWhiteSpace = data.replaceAll("\\,|\\(|\\)", " ");
		String[] cleanData = replaceWhiteSpace.split("\\s+");
		
		// put the predicate first
		kb.put(cleanData[0], new ArrayList<>());
		List<Term> tmp = kb.get(cleanData[0]);
		
		// process term
		for(int j = 1; j < cleanData.length; j++) {
			
			Term queryTerm = new Term(cleanData[j]);
			// if term's first letter is lower case => variable otherwise constant 
			if(queryTerm.termName.charAt(0) - 'a' >= 0 && queryTerm.termName.charAt(0) - 'a' < 26) {
				queryTerm.setVariable();
			}
			else {
				queryTerm.setConstant();
			}
			tmp.add(queryTerm);
		}
		
	}
	
	/*
	 * adding negation
	 * return String
	 * */
	private static String addNegative(String data) {
		String newStr = "";
		if(data.charAt(0) == '~') {
			newStr = data.substring(1);
		}
		else {
			newStr = "~" + data;
		}
		return newStr;
	} 
	
	
	private static void processQuery(List<Map<String, List<Term>>> query, int queryNnum, Scanner scanner) {
		for(int i = 0; i < queryNnum; i++) {
			
			String rawQuery = scanner.next();
			
			String replaceWhiteSpace = rawQuery.replaceAll("\\,|\\(|\\)", " ");
			String[] data = replaceWhiteSpace.split("\\s+");
			System.out.println(data[0]);
			
			query.add(new HashMap<>());
			query.get(i).put(data[0], new ArrayList<>());
			for(int j = 1; j < data.length; j++) {
				
				Term queryTerm = new Term(data[j]);
				queryTerm.setConstant();
				
				query.get(i).get(data[0]).add(queryTerm);
			}			
		}
	}
	
	
 	
	private static boolean resolution(List<Map<String, List<Term>>> kb, Map<String, List<Term>> curQuery,Set<String> seen, String curQueryStr) {
		
		// avoid create a cycle
		if(seen.contains(curQueryStr)) {
			return false;
		}
		seen.add(curQueryStr);
		
		// for loop kb's sentence to find same predicates
		for(int j = 0; j < kb.size(); j++) {
			Map<String, List<Term>> potentialQuery = new HashMap<>();	
			// one sentence from kb
			Map<String, List<Term>> curKbSentence =  kb.get(j);
			makeCopyOfKbSentnece(potentialQuery, kb.get(j));
			
			boolean check = false;
			Map<String, Term> substitution =  new HashMap<>();
			// for loop a query's predicates, same sentence
			for(Map.Entry<String, List<Term>> queryEntry : curQuery.entrySet() ) {
				String curQueryPredicate = queryEntry.getKey();
				List<Term> curQueryTerms = queryEntry.getValue();
				// get this query's predicate and add negative
				// maybe here can do soome optomized
				String negatedQuery = addNegative(curQueryPredicate);
				
				// if predicates same do sth
				if( curKbSentence.containsKey(negatedQuery) ) {
					if(check == false) {
						check = true;
					}
					// check argument matched
					if(isArgsMatch(substitution, curQueryTerms, curKbSentence.get(negatedQuery))) {
						
						// if they are same we have to eliminate
						potentialQuery.remove(negatedQuery);
						// if the potentialQuery size equal to zeon. means that we find the answer!!
						if(potentialQuery.size() == 0) {
							return true;
						}
					}
					else {
						break;
					}
					
				}
				// collect those which are not same into next round query
				else {
					// assign a new address for list of terms whose predicate in query is not contained in KB 
					List<Term> copyCurTerms = makeCopyOfCurTerms(curQueryTerms);
					potentialQuery.put(curQueryPredicate, copyCurTerms );
				}				
				
			}
			if(check == true) {
				check = false;
				// after for loop we have to do substitution!!
				String nextQueryStr = substituteProcess(substitution,  potentialQuery);
				
				// here might have backtracking pattern
				
				// next add new sentence into orgriginalKB
				kb.add(potentialQuery);
				// next round
				boolean findResolution = resolution(kb, potentialQuery, seen, nextQueryStr);
				//remove new sentence
				kb.remove(kb.size() - 1);
				// if we get true then we could terminate
				if(findResolution == true) {
					return true;
				}
				
				
			}
			else {
				System.out.println("not Match");
				continue;
			}			
		}			
			
		
		
		
		return false;
	}
	/*
	 * A given predicate name will not appear with different number of arguments.
	 * Therefore, NOW, predicate name are same
	 * */
	private static boolean isArgsMatch( Map<String, Term> substitution, List<Term> queryArgs, List<Term> kbArgs) {
//		int potentialQuerySize = potentialQuery.size();
		for(int i = 0; i < queryArgs.size(); i++) {
			Term qArg = queryArgs.get(i);
			Term kbArg = kbArgs.get(i);
			
			if(qArg.constant && kbArg.constant && qArg.termName.equals(kbArg.termName) == false) {
				substitution.clear();
				return false;
			}
			else if(qArg.constant && kbArg.variable) {
				// 					variable   ,    cosntant 
				substitution.put(kbArg.termName, qArg);
//				potentialQuery.add(new HashMap<String, List<Term>>());
//				potentialQuery.get(potentialQuerySize).put(curPredicate, value)
			} 
			else if(qArg.variable && kbArg.constant) {
				substitution.put(qArg.termName, kbArg);
			}
			else if(qArg.variable && kbArg.variable && qArg.termName.equals(kbArg.termName) == false) {
				substitution.put(qArg.termName, kbArg);
			}
			// IMPORTANT I :: I think there is not exist "qArg.variable && kbArg.constant" this situation!!
			// IMPORTANT II:: Is it exist "qArg.variable && kbArg.variable" this situation????
		}
		return true;
	}
	
	
	// assign new address for potentialQuery
	private static void makeCopyOfKbSentnece(Map<String, List<Term>> potentialQuery , Map<String, List<Term>> curKbSentence ) {
		for(Map.Entry<String, List<Term>> m: curKbSentence.entrySet()) {
			List<Term> curTerms = makeCopyOfCurTerms(m.getValue());
			potentialQuery.put(m.getKey(), new ArrayList<>(curTerms));
		}
	}
	
	private static List<Term> makeCopyOfCurTerms(List<Term> curTerms) {
		List<Term> result = new ArrayList<>();
		for(Term term : curTerms) {
			Term tmp = new Term(term.termName);
			tmp.constant = term.constant;
			tmp.variable = term.variable;
			result.add(tmp);
		}
		return result;
	} 
	// impoortant!! I think after substitution may still have variable in sentence! 
	// something like Dog(Lucky)   Dog(y) v Owner(x, y)  in this case, we only can substitute x 
	private static String substituteProcess(Map<String, Term> substitution, Map<String, List<Term>> potentialQuery) {
		String cnfString = "";
		for(Map.Entry<String, List<Term>> entryPredicate: potentialQuery.entrySet()) {
			cnfString = cnfString + entryPredicate.getKey() + ",";
			for(Term t: entryPredicate.getValue()) {
				if(substitution.containsKey(t.termName) == true) {
					
					// substitute its status
					t.constant = substitution.get(t.termName).constant;
					t.variable = substitution.get(t.termName).variable;
					// substitute its name
					t.termName = substitution.get(t.termName).termName;
					
				}
				cnfString = cnfString + t.termName + ",";
			}
			
		}
		return cnfString;
		
	}
	
	
//	private static String toCNFProcess(Map<String, List<Term>> curQuery) {
//		
//		return "";
//	}
	
	
	public static class Predicate{
		String predicateName;
		List<Term> terms;
		public Predicate(String predicateName) {
			this.predicateName = predicateName;
			terms = new ArrayList<>();
		}
		private void addterms(Term t) {
			terms.add(t);
		}
	}
	
	public static class Term {
		String termName;
		boolean constant;
		boolean variable;
		public Term(String termName) {
			this.termName = termName;
		}
		
		private void setConstant() {
			constant = true;
			variable = false;
		}
		private void setVariable() {
			constant = false;
			variable = true;
		}
		
		private boolean equal(Term other) {
			if( (other.variable == true && variable == true) || (other.constant == true && constant == true && other.termName.equals(termName) == true) ) {
				return true;
			}
			return false;
		}
	}
	
}
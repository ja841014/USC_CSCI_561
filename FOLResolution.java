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
		List<List<Predicate>> query = new ArrayList<>();
		List<List<Predicate>> kb = new ArrayList<>();
		
		Scanner scanner = new Scanner(file);
		int queryNnum = Integer.parseInt(scanner.next());
		processQuery(query, queryNnum, scanner);
		int kbNum = Integer.parseInt(scanner.next());
		scanner.nextLine();
		processkb(kb, kbNum, scanner);
		
		Map<String, List<Integer>> tableMap = new HashMap<>();
		kbStats(kb, tableMap);			
//		
		
		// for loop query; query come in  正常來說應該只有一條
		for(int i = 0; i < query.size(); i++) {
			List<Predicate> curQuery = query.get(i);
			Set<String> seen = new HashSet<>();
	
			String recordStr = cleanUpNewQuery(curQuery);
			
			
			kb.add(curQuery);
			updateTable(tableMap, curQuery, kb.size());
			boolean findAnswer = resolution(kb, curQuery, seen, recordStr, tableMap);
			deleteTable(tableMap, curQuery);
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
	
	// 一條query一條ㄗ
	private static void processkb(List<List<Predicate>> kb, int kbNum, Scanner scanner) {
		for(int i = 0; i < kbNum; i++) {
			
			String rawKbSentence = scanner.nextLine();
			String implication = "=>";
			
			// determine whether contain "=>"
			if(rawKbSentence.contains(implication) == true) {
				String[] spereateFrontBack = rawKbSentence.split(" => ");
				transformImplies(kb, i, spereateFrontBack[0], spereateFrontBack[1]);
			}
			else {
				List<Predicate> CurKbSentence = new ArrayList<>();
				processLiteral(CurKbSentence, rawKbSentence, i);
				kb.add(CurKbSentence);
			}

			
		}
	}
	
	// process the kb's terms
	private static void transformImplies(List<List<Predicate>> kb, int kbIndex ,String front, String back) {
		// process front  sperate all '&'
		String[] data = front.split(" & ");
		List<Predicate> CurKbSentence = new ArrayList<>();
		for(int i = 0;i < data.length; i++) {
			// we have to add negative to those predicate  Play(Peter, x, Lee)
			String cleanPredicate= addNegative(data[i]);
			
			processLiteral(CurKbSentence, cleanPredicate, kbIndex);
		}
		// process back
		processLiteral(CurKbSentence, back, kbIndex);
		kb.add(CurKbSentence);
	}
	
	private static void processLiteral( List<Predicate> CurKbSentence, String data, int kbIndex) {
		// replace , ( )
		String replaceWhiteSpace = data.replaceAll("\\,|\\(|\\)", " ");
		String[] cleanData = replaceWhiteSpace.split("\\s+");
		
		// put the predicate first
		Predicate curPredicate = new Predicate(cleanData[0]);
//		kb.put(cleanData[0], new ArrayList<>());
		List<Term> tmp = curPredicate.terms;
		
		// process term
		for(int j = 1; j < cleanData.length; j++) {
			
			Term queryTerm = new Term(cleanData[j]);
			// if term's first letter is lower case => variable   otherwise constant 
			if(queryTerm.termName.charAt(0) - 'a' >= 0 && queryTerm.termName.charAt(0) - 'a' < 26) {
				queryTerm.setName(queryTerm.termName + kbIndex);
				queryTerm.setVariable();
			}
			else {
				queryTerm.setConstant();
			}
			tmp.add(queryTerm);
		}
		CurKbSentence.add(curPredicate);
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
	
	
	private static void processQuery(List<List<Predicate>> query, int queryNnum, Scanner scanner) {
		
		for(int i = 0; i < queryNnum; i++) {
			List<Predicate> predicatesList = new ArrayList<>();
			String rawQuery = scanner.next();
			
			String replaceWhiteSpace = rawQuery.replaceAll("\\,|\\(|\\)", " ");
			String[] data = replaceWhiteSpace.split("\\s+");
			Predicate curPre = new Predicate("~"+data[0]);
			
			
			System.out.println(data[0]);
			

			// process terms
			// when we read from file it always constant
			for(int j = 1; j < data.length; j++) {
				
				Term queryTerm = new Term(data[j]);
				queryTerm.setConstant();
				
				curPre.addterms(queryTerm);
			}		
			
			predicatesList.add(curPre);
			query.add(predicatesList);
		}
		
	}
	
	static int cnt = 0;
 	
	private static boolean resolution(List<List<Predicate>> kb, List<Predicate> curQuery, Set<String> seen, String curQueryStr, Map<String, List<Integer>> table) {
		cnt++;
		System.out.println(cnt);
//		for(List<Predicate> l: kb) {
//			for(Predicate y : l) {
//				System.out.print(y.predicateName + "(" );
//				for(Term t: y.terms) {
//					System.out.print(t.termName + "," );
//				}
//				System.out.print( ") " );
//			}
//			System.out.println();
//		}
//		System.out.println("******************************");
		
		// avoid create a cycle
		if(seen.contains(curQueryStr)) {
//			 System.out.println(curQueryStr);
			return false;
		}
		seen.add(curQueryStr);
		
		// loop query's predicate
		for(int i = 0; i < curQuery.size(); i++) {
			String curQueryPredicateStr = curQuery.get(i).predicateName;
			Predicate curQueryPredicate = curQuery.get(i);
			String negatedQuery = addNegative(curQueryPredicateStr);
			
			if(table.containsKey(negatedQuery) == true) {
				
				// loop the sentence which contain target string in kb
				// 這裏 table好像會變動
				List<Integer> copyOneList = makeCopyOfTableList(table.get(negatedQuery));
				for(Integer index : copyOneList) {
					
					List<Predicate> oneKbSentence = kb.get(index);
					
					// make a new Address
					List<Predicate> potentialQuery = makeCopyOfKbSentnece(oneKbSentence);
					
					// loop this kb sentence's predicate
					for(Predicate oneKbPredicate: potentialQuery) {
						if(oneKbPredicate.predicateName.equals(negatedQuery)) {
						Map<String, Term> substitution =  new HashMap<>();
						
							if(isArgsMatch(substitution, curQueryPredicate.terms, oneKbPredicate.terms )) {
//							
//							// remove 掉
//								potentialQuery.remove( oneKbPredicate );
//							
//							// add rest of the query predicate into it. except the i th predicate
//							// To prevent altering the original query value
//							// we will return a new address
								List<Predicate> newPotentialQuery = addRestOfQueryPredicate( potentialQuery, curQuery, i, oneKbPredicate);
//							// clean up the query sth like same or different
												
								if(newPotentialQuery.size() == 0) {
//									potentialQuery.add( oneKbPredicate );
									return true;
								}
								substituteProcess(substitution, newPotentialQuery);
								String visitedString =  cleanUpNewQuery(newPotentialQuery);
								
								for(Predicate y : newPotentialQuery) {
									System.out.print(y.predicateName + "(" );
									for(Term t: y.terms) {
										System.out.print(t.termName + "," );
									}
									System.out.print( ") " );
								}
								System.out.println();
								
								// update table
								updateTable(table, newPotentialQuery, kb.size());
								// next add new sentence into orgriginalKB
								kb.add(newPotentialQuery);
								
								// next round
								boolean findResolution = resolution(kb, newPotentialQuery, seen, visitedString, table);
								
								//remove new sentence
								kb.remove(kb.size() - 1);
								
								deleteTable(table, newPotentialQuery);
//								potentialQuery.add( oneKbPredicate );
								
								// if we get true then we could terminate
								if(findResolution == true) {
									return true;
								}
								
								
							}
//							else {
//								// maybe need use break not sure yet!
//								continue;
//							}
//						
						}
						
					}
					
				}				
			}
			
		}
//		seen.remove(curQueryStr);
		return false;
	}
	
	
	private static void deleteTable(Map<String, List<Integer>> table, List<Predicate> newPotentialQuery) {
		for(Predicate tmp: newPotentialQuery) {
			int size = table.get(tmp.predicateName).size();
			table.get(tmp.predicateName).remove(size - 1);
		}
	}
	
	private static void updateTable(Map<String, List<Integer>> table, List<Predicate> newPotentialQuery, int index) {
		for(Predicate tmp: newPotentialQuery) {
			if(table.containsKey(tmp.predicateName) == true) {
				table.get(tmp.predicateName).add(index);
			}
			else {
				table.put(tmp.predicateName, new ArrayList<>());
				table.get(tmp.predicateName).add(index);
			}
		}
	}
	
	private static void kbStats(List<List<Predicate>> kb, Map<String, List<Integer>> curSentenceStats) {
		for(int i = 0; i < kb.size(); i++) {
			List<Predicate> sentenceList = kb.get(i);
			for(Predicate p: sentenceList) {
				String pName = p.predicateName;
				if(curSentenceStats.containsKey(pName) == true) {
					curSentenceStats.get(pName).add(i);
				}
				else {
					curSentenceStats.put(pName, new ArrayList<>());
					curSentenceStats.get(pName).add(i);
				}
			}
		}

	}
	
	// before this do we need to variable standardize?
	private static List<Predicate>  addRestOfQueryPredicate(List<Predicate> potentialQuery, List<Predicate> curQuery, int index, Predicate oneKbPredicate) {
		List<Predicate> newPotentialQuery = new ArrayList<>();
		// copy a list pf predicate
		for(Predicate predicate : potentialQuery) {
			if(predicate == oneKbPredicate) {
				System.out.println("they are same");
				continue;
			}
			Predicate tmPredicate =  predicate.clonePredicate();
			newPotentialQuery.add(tmPredicate);
		}
		for(int i = 0; i < curQuery.size(); i++) {
			if(i == index) {
				continue;
			}
			else {
//				potentialQuery.add(curQuery.get(i));
				Predicate tmPredicate =  curQuery.get(i).clonePredicate();
				newPotentialQuery.add(tmPredicate);
			}
		}
		return newPotentialQuery;
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
	private static List<Predicate> makeCopyOfKbSentnece(List<Predicate> curKbSentence ) {
		List<Predicate> copyList = new ArrayList<>();
		for(Predicate m: curKbSentence ) {
			Predicate tmp = new Predicate(m.predicateName);
			tmp.terms = makeCopyOfCurTerms(m.terms);
			copyList.add(tmp);
		}
		return copyList;
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
	
	private static List<Integer> makeCopyOfTableList(List<Integer> table) {
		List<Integer> copy = new ArrayList<>();
		for(Integer i : table) {
			copy.add(i);
		}
		return copy;
	}
	// impoortant!! I think after substitution may still have variable in sentence! 
	// something like Dog(Lucky)   Dog(y) v Owner(x, y)  in this case, we only can substitute x 
	private static void substituteProcess(Map<String, Term> substitution, List<Predicate> potentialQuery) {		
		for(Predicate entryPredicate: potentialQuery) {
			
			for(Term t: entryPredicate.terms) {
				if(substitution.containsKey(t.termName) == true) {
					
					// substitute its status
					t.constant = substitution.get(t.termName).constant == true ? true : false;
					t.variable = substitution.get(t.termName).variable == true ? true : false;
					// substitute its name
					t.termName = substitution.get(t.termName).termName;
				}
				
				
			}
			
		}
		
	}
	
	
	private static String cleanUpNewQuery(List<Predicate> potentialQuery) {
		Map<String, Predicate> map = new HashMap<>();
		String cnfString = "";
		Set<Predicate> set = new HashSet<>();
		Set<Predicate> compensatePredicates = new HashSet<>();
		
		for(int i = 0; i < potentialQuery.size(); i++) {
			Predicate entryPredicate = potentialQuery.get(i);
			String clauseString = entryPredicate.predicateName + ",";
			
			for(Term t: entryPredicate.terms) {
				clauseString = clauseString + t.termName + ",";
			}
			String negeString = addNegative(clauseString);
			
			// same we need to eliminate
			if(map.containsKey(clauseString) ||  map.containsKey(negeString)) {
				if(map.containsKey(clauseString) ) {
					set.add(entryPredicate);
				}
				else {
					compensatePredicates.add(map.get(negeString));
					compensatePredicates.add(entryPredicate);
				}
				
			}
			else {
				map.put(clauseString, entryPredicate);
				cnfString = cnfString + clauseString;
			}
			
		}
		
		for(Predicate sPredicate : set) {
			potentialQuery.remove(sPredicate);
		}
		for(Predicate cPredicate: compensatePredicates) {
			potentialQuery.remove(cPredicate);
		}
		
		return cnfString;
	}

	
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
		
		private Predicate clonePredicate() {
			Predicate cloneP = new Predicate(predicateName);
			List<Term> cloneTerms = new ArrayList<>();
			for(Term t : terms) {
				Term cloneT = new Term(t.termName);
				cloneT.constant = t.constant;
				cloneT.variable = t.variable;
				cloneTerms.add(cloneT);
			}
			cloneP.terms = cloneTerms;
			return cloneP;
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
		
		private void setName(String name) {
			termName = name;
		}
		
//		private boolean equal(Term other) {
//			if( (other.variable == true && variable == true) || (other.constant == true && constant == true && other.termName.equals(termName) == true) ) {
//				return true;
//			}
//			return false;
//		}
		
		
	}
	
}
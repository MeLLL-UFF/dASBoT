/*
 * Copyright (C) 2012, Aline Paes, adapted from Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package br.uff.dl.rules.test;

//import java.io.File;

import org.dllearner.cli.CrossValidation;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.statistics.Stat;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
/**
 * Performs cross validation for the given problem. 
 * 
 * @author Jens Lehmann
 *
 */
public class CrossValidationFromFiles extends CrossValidation {

	protected Stat accuracyVal = new Stat();
	protected Stat fMeasureVal = new Stat();
	
	private AbstractCELA la;
	private PosNegLP lp;
	private AbstractReasonerComponent rs;
	private String prefixFile;
	
	
	public CrossValidationFromFiles(AbstractCELA la, PosNegLP lp, AbstractReasonerComponent rs, String prefixFile){
		this.la = la; // algorithm 
		this.lp = lp; // learning problem
		this.rs = rs;  // reasoner
		this.prefixFile = prefixFile; // prefix of file containing examples
		
	}
	
	public void runCVFromFiles(int folds, String nameOutput){		

		DecimalFormat df = new DecimalFormat();
		FileWriter output  = null;
		try{
			 output = new FileWriter(nameOutput);
		}catch (Exception e){
			System.err.println("Error when opening output file: " + e.getMessage());
		}
		
		//separar entre posonly e pos neg, por enquanto é só PosNeg
		for (int curFold = 1; curFold <= folds; curFold++){
			String exFileTr = prefixFile + String.valueOf(curFold) + ".trn";
						
			Set<Individual> posExamples = getPosExamples(exFileTr);
			Set<Individual> negExamples = getNegExamples(exFileTr);
			
			//System.out.println(posExamples);
			
			//Set<String> pos = Datastructures.individualSetToStringSet(posExamples);
			//Set<String> neg = Datastructures.individualSetToStringSet(negExamples);
			
			lp.setPositiveExamples(posExamples);
			lp.setNegativeExamples(negExamples);
			
			try {			
				lp.init();
				la.init();
			} catch (ComponentInitException e) {
				e.printStackTrace();
			}
				
					
			long algorithmStartTime = System.nanoTime();
			la.start();
			Description concept = la.getCurrentlyBestDescription();
			
			long algorithmDuration = System.nanoTime() - algorithmStartTime;
			runtime.addNumber(algorithmDuration/(double)1000000000);
			
			//log info
			outputWriter("#############");
			outputWriter(" *** Fold " + curFold);
			outputWriter("\n***** Best concept found: " + concept);
			
			// Header of output file
			outputHeader(output, curFold);
						
			//outputWriter("\n******* Training Results ");
			// commented in 09.01, repetido double[] measTrain = outputInfo(concept, posExamples, negExamples);
			double[] measTrain = outputInfo(concept, posExamples, negExamples, output, "Training");
			accuracyTraining.addNumber(measTrain[0]);
			fMeasureTraining.addNumber(measTrain[1]);

			
			//get test examples
			String exFileTs = prefixFile + String.valueOf(curFold) + ".tst";
			Set<Individual> posExamplesTst = getPosExamples(exFileTs);
			Set<Individual> negExamplesTst = getNegExamples(exFileTs);
			
			// write info for test examples
			//outputWriter("\n******* Training Results ");
			//double[] measTest = outputInfo(concept, posExamplesTst, negExamplesTst);
			double[] measTest = outputInfo(concept, posExamplesTst, negExamplesTst, output, "Test");
			accuracy.addNumber(measTest[0]);
			fMeasure.addNumber(measTest[1]);

			outputWriter("  runtime: " + df.format(algorithmDuration/(double)1000000000) + "s");
			
			//outputInstancesPerConceptTst(concept, posExamplesTst, negExamplesTst, curFold, output);
					
		}
		
		// write averages
		writeAveragesTrain(folds);
		writeAveragesTest(folds);
		
		try{
			output.close();
		}catch(IOException e){
			System.err.println("Error when closing file: " + e.getMessage());
		}
				
	}
	
	public void runCVFromFilesWithValSet(int folds, String nameOutput) {		

		DecimalFormat df = new DecimalFormat();
		FileWriter output  = null;
		try{
			 output = new FileWriter(nameOutput);
		}catch (Exception e){
			System.err.println("Error when opening output file: " + e.getMessage());
		}
		
		//separar entre posonly e pos neg, por enquanto é só PosNeg
		for (int fold = 1; fold <= folds; fold++){
			
			outputWriter("\n ###### Beginning fold " + fold);
			
			String exFileTr = prefixFile + String.valueOf(fold) + ".trn";
						
			Set<Individual> posExamples = getPosExamples(exFileTr);
			Set<Individual> negExamples = getNegExamples(exFileTr);
			
			//System.out.println(posExamples);
			
			//Set<String> pos = Datastructures.individualSetToStringSet(posExamples);
			//Set<String> neg = Datastructures.individualSetToStringSet(negExamples);
			
			lp.setPositiveExamples(posExamples);
			lp.setNegativeExamples(negExamples);
			
			try {			
				lp.init();
				la.init();
			} catch (ComponentInitException e) {
				e.printStackTrace();
			}
				
					
			long algorithmStartTime = System.nanoTime();
			la.start();
			Description concept = la.getCurrentlyBestDescription();
			
			long algorithmDuration = System.nanoTime() - algorithmStartTime;
			runtime.addNumber(algorithmDuration/(double)1000000000);
			
			// write info for training examples
			outputWriter(" ###### Training on fold " + fold);
			outputWriter(" Best concept found: " + concept);
			double[] measTrain = outputInfo(concept, posExamples, negExamples);
			accuracyTraining.addNumber(measTrain[0]);
			fMeasureTraining.addNumber(measTrain[1]);
			
			//get validation examples
			String exFileVal = prefixFile + String.valueOf(fold) + ".val";
			Set<Individual> posExamplesVal = getPosExamples(exFileVal);
			Set<Individual> negExamplesVal = getNegExamples(exFileVal);

			// write info for validation examples
			outputWriter("\n ###### Validation on fold " + fold);
			double[] measVal = outputInfo(concept, posExamplesVal, negExamplesVal);
			accuracyVal.addNumber(measVal[0]);
			fMeasureVal.addNumber(measVal[1]);
			
			outputInstancesPerConceptVal(concept, posExamplesVal, negExamplesVal, fold, output);			
			
			//get test examples
			String exFileTs = prefixFile + String.valueOf(fold) + ".tst";
			Set<Individual> posExamplesTst = getPosExamples(exFileTs);
			Set<Individual> negExamplesTst = getNegExamples(exFileTs);
			
			outputInstancesPerConceptTst(concept, posExamplesTst,negExamplesTst, fold, output);
			// write info for test examples
			outputWriter("\n ###### Testing on fold " + fold);
			double[] measTest = outputInfo(concept, posExamplesTst, negExamplesTst);
			accuracy.addNumber(measTest[0]);
			fMeasure.addNumber(measTest[1]);

			outputWriter("  runtime: " + df.format(algorithmDuration/(double)1000000000) + "s");
					
		}
		
		// write averages
		writeAveragesTrain(folds);
		writeAveragesVal(folds);
		writeAveragesTest(folds);
		
		try{
			output.close();
		}catch(IOException e){
			System.err.println("Error when closing file: " + e.getMessage());
		}
		
				
	}

	
	public void runCVInternalVal(int kFolds, int xFolds, String prefixFile, String measure, String nameOutput) {		

		DecimalFormat df = new DecimalFormat();
		FileWriter output = null;
		// open file to write description and examples for best internal fold
		try{
			output = new FileWriter(nameOutput);
		}catch (Exception e){
			System.err.println("Error when opening output file: " + e.getMessage());
		}
		
		//separar entre posonly e pos neg, por enquanto é só PosNeg
		int tfold = 0;
		// teste
		for (int kfold = 1; kfold <= kFolds; kfold++){
			outputWriter(" \n###### Beginning fold " + kfold);
			// treinamento com cada conjunto de treinamento, escolhe a melhor opção de acordo com o conjunto de validação
			int bestFold = 0;
			double bestMeasure = 0;
			Description bestDesc = null;
			runtime = new Stat();
			accuracyTraining = new Stat();
			fMeasureTraining = new Stat();
			accuracyVal = new Stat();
			fMeasureVal = new Stat();
			
			for (int xfold = 1; xfold <= xFolds; xfold++){
				outputWriter(" ###### Beginning fold " + xfold);
				tfold ++;
				
				// pq está indo para 11????
				String exFileTr = prefixFile + String.valueOf(tfold) + ".trn";
				Set<Individual> posExamples = getPosExamples(exFileTr);
				Set<Individual> negExamples = getNegExamples(exFileTr);
			
				////System.out.println(" pos examples no principal: " + posExamples);
			
				//Set<String> pos = Datastructures.individualSetToStringSet(posExamples);
				//Set<String> neg = Datastructures.individualSetToStringSet(negExamples);
			
				lp.setPositiveExamples(posExamples);
				lp.setNegativeExamples(negExamples);
			
				try {			
					lp.init();
					la.init();
				} catch (ComponentInitException e) {
					e.printStackTrace();
				}
				
					
				long algorithmStartTime = System.nanoTime();
				la.start();
				long algorithmDuration = System.nanoTime() - algorithmStartTime;
				runtime.addNumber(algorithmDuration/(double)1000000000);
			
				//List<Description> concepts = la.getCurrentlyBestDescriptions();
				Description concept = la.getCurrentlyBestDescription();
				
				// write info for training examples
				outputWriter(" ###### Training on external fold " + kfold + " and internal fold " + xfold);
				outputWriter(" Best concept found: " + concept);
				double[] measTrain = outputInfo(concept, posExamples, negExamples);
				accuracyTraining.addNumber(measTrain[0]);
				fMeasureTraining.addNumber(measTrain[1]);
			
				//get validation examples
				String exFileVal = prefixFile + String.valueOf(tfold) + ".val";
				Set<Individual> posExamplesVal = getPosExamples(exFileVal);
				Set<Individual> negExamplesVal = getNegExamples(exFileVal);
			
				// write info for validation examples
				outputWriter("###### Measures for validation set");
				double[] measVal = outputInfo(concept, posExamplesVal, negExamplesVal);
				accuracyVal.addNumber(measVal[0]);
				fMeasureVal.addNumber(measVal[1]);
			
				outputWriter("  runtime: " + df.format(algorithmDuration/(double)1000000000) + "s");
				
				//decide best internal fold
				//System.out.println("Measure fn: " + measure);
				//System.out.println("Best: " + bestMeasure);
				//System.out.println("measVal: " + measVal[0]);
				//System.out.println("measVal: " + measVal[1]);
				//System.out.println(measure.equals("accuracy"));
				//System.out.println("é maior? " + (measVal[0] > bestMeasure));
				if (measure.equals("fmeasure")){ 
					if (measVal[1] > bestMeasure){
						bestMeasure = measTrain[1];
						bestFold = tfold;
						bestDesc = concept; 
					}
				}
				else if (measure.equals("accuracy")){
						if (measVal[0] > bestMeasure){
							//System.out.println("Entrei aqui!!!!!");
							bestMeasure = measTrain[0];
							bestFold = tfold;
							bestDesc = concept;
						}	
				}
				
				
			} // fechando validação interna
			
			// escreve médias para treinamento e validação
			// write averages
			
			writeAveragesTrain(xFolds);
			writeAveragesVal(xFolds);			
				
			//escrever best conceitos com validação
			String bestFileVal = prefixFile + String.valueOf(bestFold) + ".val";
			Set<Individual> posExamplesBest = getPosExamples(bestFileVal);
			Set<Individual> negExamplesBest = getNegExamples(bestFileVal);
			outputInstancesPerConceptVal(bestDesc, posExamplesBest, negExamplesBest, kfold, output);
			
			
			// escrever valores para teste, com o melhor da validação interna
			outputWriter(" Test fold: " + kfold);
			outputWriter(" Test instances for best concept: ");
			String exFileTst = prefixFile + String.valueOf(kfold) + ".tst";
			Set<Individual> posExamplesTst = getPosExamples(exFileTst);
			Set<Individual> negExamplesTst = getNegExamples(exFileTst);
			
			double[] measTest = outputInfo(bestDesc, posExamplesTst, negExamplesTst);
			accuracy.addNumber(measTest[0]);
			fMeasure.addNumber(measTest[1]);
		} // fechando fold externo
		
		// escrever media para teste?
		writeAveragesTest(kFolds);
		
		try{
			output.close();
		}catch(IOException e){
			System.err.println("Error when closing file: " + e.getMessage());
		}
		
	} // fim metodo que faz validacao interna e externa

	
	public void writeAveragesTrain(int folds){
		DecimalFormat df = new DecimalFormat();
		
		outputWriter("");
		outputWriter("Finished " + folds + "-folds cross-validation.");
		outputWriter("runtime: " + statOutput(df, runtime, "s"));
		outputWriter("length: " + statOutput(df, length, ""));
		outputWriter("F-Measure on training set: " + statOutput(df, fMeasureTraining, "%"));		
		outputWriter("predictive accuracy on training set: " + statOutput(df, accuracyTraining, "%"));		
				
	}
	
	public void writeAveragesVal(int folds){
		DecimalFormat df = new DecimalFormat();
		
		outputWriter("");
		outputWriter("F-Measure on validation set: " + statOutput(df, fMeasureVal, "%"));
		outputWriter("predictive accuracy on validation set: " + statOutput(df, accuracyVal, "%"));
		
	}
	
	
	public void writeAveragesTest(int folds){
		DecimalFormat df = new DecimalFormat();
		
		outputWriter("");
		outputWriter("F-Measure on test set: " + statOutput(df, fMeasure, "%"));
		outputWriter("predictive accuracy on test set: " + statOutput(df, accuracy, "%"));
		
	}
	
	
	// escreve instâncias classificadas por cada conceito aprendido
	// usado quando existe mais de um conceito e PosOnly é usado
	public void outputInstancesPerConcepts(List<Description> concepts, Set<Individual> posInstances){
		for (Description concept : concepts){
			outputWriter("Concept: " + concept);
			LinkedList<Individual> tp = getClassifiedInstances(concept, posInstances);
			outputWriter("\nCorrectly classified Instances: " + tp);			
			outputWriter("\nTotal number of correctly classified Instances: " + tp.size());
		}
		
	}
	
	public void outputInstancesPerConcept(Description concept, Set<Individual> posInstances, Set<Individual>negInstances){
		outputWriter("Concept: " + concept);
		LinkedList<Individual> tp = getClassifiedInstances(concept, posInstances);
		outputWriter("\nCorrectly Classified Positive Instances: " + tp);
		LinkedList<Individual> fp = getClassifiedInstances(concept, negInstances);
		outputWriter("\nIncorrectly Classified Negative Instances: " + fp);
		
	}

	public void outputInstancesPerConceptTst(Description concept, Set<Individual> posInstances, Set<Individual>negInstances, int fold, FileWriter output){
		try{
			output.write(" ### Examples in Test Set:  ");
		} catch (IOException e){
			System.err.println("Error when writing to output best concept file: " + e.getMessage());
		}
		outputInstancesPerConceptVal(concept, posInstances, negInstances, fold, output);
	}
	
	// output concept and instances for a specified file
	public void outputInstancesPerConceptVal(Description concept, Set<Individual> posInstances, Set<Individual>negInstances, int fold, FileWriter output){
		LinkedList<Individual> tp = getClassifiedInstances(concept, posInstances);
		LinkedList<Individual> fp = getClassifiedInstances(concept, negInstances);
		
		try{
			output.write("################"+ "\n");
			output.write("Fold: " + String.valueOf(fold) + "\n");
			output.write("Best Description: " + concept + "\n");
			output.write("\nCorrectly Classified Positive Instances: " + tp + "\n");
			output.write("\nIncorrectly Classified Negative Instances: " + fp + "\n\n");
			output.write("Total positive instances: " + posInstances.size() + "\n");
			output.write("Total negative instances: " + negInstances.size() + "\n");
		} catch (Exception e){
			System.err.println("Error when writing to output best concept file: " + e.getMessage());
		}
	}

	
	public void outputInstancesPerConcepts(List<Description> concepts, Set<Individual> posInstances, Set<Individual>negInstances){
		for (Description concept : concepts){
			outputWriter("Concept: " + concept);
			LinkedList<Individual> tp = getClassifiedInstances(concept, posInstances);
			outputWriter("Correctly Classified Positive Instances: " + tp);
			LinkedList<Individual> fp = getClassifiedInstances(concept, negInstances);
			outputWriter("Incorrectly Classified Negative Instances: " + fp);
			
		}
		
	}

	// retorna instâncias classificadas pelo conceito
	public LinkedList<Individual> getClassifiedInstances(Description concept, Set<Individual> instances) {
		LinkedList<Individual> correct = new LinkedList<Individual>();
		for (Individual instance : instances){
			if (isClassifiedInstance(concept, instance)){
				correct.add(instance);
			} 
		}
		return correct;
		
	}	
	
	
	public boolean isClassifiedInstance(Description concept, Individual instance) {
		return rs.hasType(concept, instance);

	}
	
	// Lê exemplos positivos do arquivo. O arquivo foi escrito externamente, a partir de um outro conf
	// o formato do arquivo é 
	// prefixes = pref1:localname1, ..., prefN = localnameN
	// positiveExamples = prefX:ex1, ..., prefY:exm
	public Set<Individual> getPosExamples(String nameFile){
		Properties properties = new Properties();
		try {
			
		    properties.load(new FileInputStream(nameFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		String pref = properties.getProperty("prefixes");
		HashMap<String,String> prefixes = getPrefixes(pref);
		//System.out.println(" prefixes" + prefixes);
		String s = properties.getProperty("positiveExamples");
		//System.out.println("Pos examples: " + s);
		return getExamples(prefixes, s);
	}
	
	public Set<Individual> getNegExamples(String nameFile){
		Properties properties = new Properties();
		try {
			
		    properties.load(new FileInputStream(nameFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		String pref = properties.getProperty("prefixes");
		HashMap<String,String> prefixes = getPrefixes(pref);
		String s = properties.getProperty("negativeExamples");
		return getExamples(prefixes, s);
	}
	
	public HashMap<String,String> getPrefixes(String pref){
		String s[] = pref.split(",");
		HashMap<String,String> prefs = new HashMap<String,String>();
		for (String p : s){
			int index = p.indexOf(":");
			//index--;
			String p1 = p.substring(0, index);
			index ++;
			String p2 = p.substring(index);
			prefs.put(p1, p2);
		}
		return prefs;
	}
	
	public Set<Individual> getExamples(HashMap<String,String> prefs, String s){
			
		String instances[] = s.split(",");
		HashSet<Individual> instancesSet = new HashSet<Individual>();
		
		for (String instance : instances){
			String ex[] = instance.split(":");
			String prefEx = prefs.get(ex[0].trim());
			////System.out.println("pref vindo do exemplo: " + ex[0]);
			////System.out.println("pref vindo do hashmap: " + prefEx);
			String example = prefEx + ex[1];
			////System.out.println("Example: " + example);
			instancesSet.add(new Individual(example));
			
		}
		////System.out.println(instancesSet);
		return instancesSet;
		
	}
	
	public int getNumClassified(List<Description> concepts, Set<Individual> examples){
		int classified = 0;
		HashSet<Individual> localExs = new HashSet<Individual>(examples);
		
		for (Description concept: concepts){
			Set<Individual> covered = rs.hasType(concept,localExs);
			classified += covered.size();
			localExs.removeAll(covered);	
		}
		return classified;
	}
	
	public double[][] getConfusionTable(int nPos, int nNeg, int cPos, int iNeg){
		double[][] confTable = new double[2][2];
		//double[] confMatrix = {0,0,0,0};
		confTable[0][0] = cPos;        // true positive
		confTable[0][1] = nPos - cPos; // false negative
		confTable[1][0] = iNeg;        // false positive
		confTable[1][1] = nNeg - iNeg; // true negative
		return confTable;
	}
	
	public double getAccuracy(double[][] confTable){
		double correct = (confTable[0][0] + confTable[1][1]);
		double all = correct + confTable[0][1] + confTable[1][0];
		double acc = correct / all;
		return acc * 100;
	}
	
	public double getFmeasure(double[][] confTable){
		double precision = confTable[0][0] / ( confTable[0][0] + confTable[1][0] );
		double recall = confTable[0][0] / ( confTable[0][0] + confTable[0][1] );
		double f = 2 * (precision * recall) / (precision + recall) * 100;
		return f;
	}
	
	public double getPrecision(double[][] confTable){
		double precision = confTable[0][0] / ( confTable[0][0] + confTable[1][0] );
		return precision;
	}
	
	public double getRecall(double[][] confTable){
		double recall = confTable[0][0] / ( confTable[0][0] + confTable[0][1] );
		return recall;
	}
	
	public double[] outputInfo(Description concept, Set<Individual> posExamples, Set<Individual> negExamples){
		DecimalFormat df = new DecimalFormat();
		int nPos = posExamples.size();
		int nNeg = negExamples.size();
		
		LinkedList<Individual> listTP = getClassifiedInstances(concept, posExamples);
		LinkedList<Individual> listFP = getClassifiedInstances(concept, negExamples);
		
		int cPos = listTP.size();
		int iNeg = listFP.size();
		double[][] confTable = getConfusionTable(nPos, nNeg, cPos, iNeg);
		double acc = getAccuracy(confTable);
		double fmeasure = getFmeasure(confTable);
		
		
		//outputWriter("Number of positive examples: " + nPos + " positive examples and " + nNeg + " negative examples");
		outputWriter("Correctly Classified Positive Instances: " + listTP);
		outputWriter("Incorrectly Classified Negative Instances: " + listFP);
		outputWriter("********** Correctly classified " + confTable[0][0] + "of" + nPos + " positive examples\n");
		outputWriter("********** Incorrectly classified "+ confTable[1][0] + "of" + nNeg + " negative examples\n");
		outputWriter("**********  Accuracy: " + df.format(acc) + "% ");
		outputWriter("**********  FMeasure: " + df.format(fmeasure) + "% ");
		double[] out = {acc, fmeasure};
		return out;
	}
	
	// write info in output file
	public double[] outputInfo(Description concept, Set<Individual> posExamples, Set<Individual> negExamples, FileWriter output, String type){
		
		try{
			output.write(" ****** " + type + " Set\n\n");
		} catch (IOException e){
			System.err.println("Error when writing to output file: " + e.getMessage());
		}

		DecimalFormat df = new DecimalFormat();
		int nPos = posExamples.size();
		int nNeg = negExamples.size();
		
		LinkedList<Individual> listTP = getClassifiedInstances(concept, posExamples);
		LinkedList<Individual> listFP = getClassifiedInstances(concept, negExamples);
		
		int cPos = listTP.size();
		int iNeg = listFP.size();
		double[][] confTable = getConfusionTable(nPos, nNeg, cPos, iNeg);
		double acc = getAccuracy(confTable);
		double fmeasure = getFmeasure(confTable);
		double precision = getPrecision(confTable);
		double recall = getRecall(confTable);
		
		//outputWriter("Number of positive examples: " + nPos + " positive examples and " + nNeg + " negative examples");
		try{
			output.write("Best Description: " + concept + "\n");
			output.write("\nCorrectly Classified Positive Instances: " + listTP);
			output.write("\n\nIncorrectly Classified Negative Instances: " + listFP);
			output.write("\n\n********** Correctly classified " + confTable[0][0] + " of " + nPos + " positive examples\n");
			output.write("\n********** Incorrectly classified "+ confTable[1][0] + " of " + nNeg + " negative examples\n");
			output.write("\n**********  Accuracy: " + df.format(acc) + "% ");
			output.write("\n**********  FMeasure: " + df.format(fmeasure) + "% ");
			output.write("\n**********  Precision: " + df.format(precision));
			output.write("\n**********  Recall: " + df.format(recall) + "\n\n");
		} catch (IOException e){
			System.err.println("Error when writing info to output file: " + e.getMessage());
		}
		double[] out = {acc, fmeasure};
		return out;
	}
	
	// write header in output file
	void outputHeader(FileWriter output, int fold){
		try{
			
			output.write("\n################################\n");
			output.write("***Fold: " + fold+"\n\n");
			} catch (IOException e){
			System.err.println("Error when writing info to output file: " + e.getMessage());
		}
	}
	
	
	/*public int getCorrectPosClassified(AbstractReasonerComponent rs, List<Description> concepts, Set<Individual> posExamples){
		int correct = 0;
		for (Description concept: concepts){
			Set<Individual> covered = rs.hasType(concept, posExamples);
			correct += covered.size();
			posExamples.removeAll(covered);	
		}
		return correct;
	}
	
	public int getIncorrectNegClassified(AbstractReasonerComponent rs, List<Description> concepts, Set<Individual> negExamples){
		int incorrect = 0;
		for (Description concept: concepts){
			Set<Individual> covered = rs.hasType(concept, negExamples);
			incorrect += covered.size();
			negExamples.removeAll(covered);	
		}
		return incorrect;
	}*/
}	
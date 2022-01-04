/**
 * @author ES-SEBBAR Imane
 * @author EL MAROUNI Majda
 * */

package qengine_program;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.helpers.StatementPatternCollector;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;

/**
 * Programme simple lisant un fichier de requête et un fichier de données.
 * 
 * <p>
 * Les entrées sont données ici de manière statique,
 * à vous de programmer les entrées par passage d'arguments en ligne de commande comme demandé dans l'énoncé.
 * </p>
 * 
 * <p>
 * Le présent programme se contente de vous montrer la voie pour lire les triples et requêtes
 * depuis les fichiers ; ce sera à vous d'adapter/réécrire le code pour finalement utiliser les requêtes et interroger les données.
 * On ne s'attend pas forcémment à ce que vous gardiez la même structure de code, vous pouvez tout réécrire.
 * </p>
 * 
 * @author Olivier Rodriguez <olivier.rodriguez1@umontpellier.fr>
 */
final class Main {
	static final String baseURI = null;
	
	static int j; // compteur requetes juste pour l'ecriture

	static IndexationSPO spo = IndexationSPO.getSPOInstance();

	static Dictionnaire dictio = Dictionnaire.getDictioInstance();
	
	//static HashMap<Integer,Object> dictio_ ;
	//static ArrayList<ArrayList<Integer>> SPO_; 
	
	 /*static HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> OSP; 
	 static HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> PSO; 
	 static HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>POS; 
	 static HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> OPS;
	 static HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> SOP;*/


	/**
	 * Votre répertoire de travail où vont se trouver les fichiers à lire
	 */
	static final String workingDir = "data/";

	/**
	 * Fichier contenant les requêtes sparql
	 */
	static  String queryFile = workingDir + "STAR_ALL_workload.queryset";

	/**
	 * Fichier contenant des données rdf
	 */
	static  String dataFile = workingDir + "100K.nt";

	static  String outputFile =  "outputs\\results.txt";

	static int nbSol = 0;
	static long qTime;
	static long exTime;
	static long pTime;
	// ========================================================================

	/**
	 * Méthode utilisée ici lors du parsing de requête sparql pour agir sur l'objet obtenu.
	 */
	public static void processAQuery(ParsedQuery query) {
		List<StatementPattern> patterns = StatementPatternCollector.process(query.getTupleExpr());
		j++; // incrementation du compteur des requetes
		QueryProcess qp = new QueryProcess();  
		 qp.getResult(patterns);  // là ou se passe le processus
		 writes_results_into_file(qp.toString());
		 nbSol = qp.getNbSolutions();
	}
	

	/**
	 * Entrée du programme
	 */
	public static void main(String[] args) throws Exception {
		
		j = 0; // initialisation du compteur à 0
		
		HelpFormatter formatter = new HelpFormatter();
        
        Options options = configParameters();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
       
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Entrer les chemins demandés en utilisant les options : ", options);
            System.exit(1);
            return;
        }
        if (cmd.hasOption("help")) {
            System.out.println("Entrer les chemins en utilisant les options: " + options);
        }
        queryFile = cmd.getOptionValue("queries");
        dataFile = cmd.getOptionValue("data");
    	outputFile = cmd.getOptionValue("outputs");   
    	
    	System.out.println("\n \n { Début");
    	long startpTime = System.currentTimeMillis();
    	System.out.println("\tDébut du parsing des données: \n\t\tCréation du dictionnaire et indexation... \n");
    	parseData();
		long endpTime = System.currentTimeMillis();
		pTime = (endpTime - startpTime);
		System.out.println("\n\tFin du parsing des données: ");
    	System.out.println("\t\t[ Dictionary and Indexation Time: " +pTime + "ms ]"); 
		
    	long startqTime = System.currentTimeMillis();
    	System.out.println("\tDébut du parsing des requêtes : \n\t\tExécution des requêtes et récupération des résultats...");
		parseQueries();
		long endqTime = System.currentTimeMillis();
        qTime = (endqTime-startqTime);
    	System.out.println("\tFin du parsing des requêtes: ");
    	System.out.println("\t\t[ Queries Processing Time: " +qTime + "ms ]"); 
        
		long endTime = System.currentTimeMillis();
    	exTime = (endqTime - startpTime);
    	System.out.println("\tFin du programme: ");
    	System.out.println("\t\t[ Program Execution Time: " +exTime + "ms ]"); 
    	writes_stats_into_file();
    	//System.out.println("\n \tRecuperation du dictionnaire et de l'indexation ...");
    	//writes_dictioANDspo_into_file();
    	System.out.println(" Fin }");
    	System.out.println(">>> Les résultats des requêtes sont dans le fichier sufixé [_result] dans le dossier donné en outputs.");
    	//System.out.println(">>> Le dictionnaire et l'indexation SPO sont dans le fichier sufixe [_dictIndex] dans le dossier donné en outputs.");
    	System.out.println(">>> Des informations sont stockées dans le fichier suffixé  [_stats] dans le dossier donné en outputs.");
	}

	// ========================================================================

	/**
	 * Traite chaque requête lue dans {@link #queryFile} avec {@link #processAQuery(ParsedQuery)}.
	 */
	private static void parseQueries() throws FileNotFoundException, IOException {
		/**
		 * Try-with-resources
		 * 
		 * @see <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">Try-with-resources</a>
		 */
		/*
		 * On utilise un stream pour lire les lignes une par une, sans avoir à toutes les stocker
		 * entièrement dans une collection.
		 */
		try (Stream<String> lineStream = Files.lines(Paths.get(queryFile))) {
			SPARQLParser sparqlParser = new SPARQLParser();
			Iterator<String> lineIterator = lineStream.iterator();
			StringBuilder queryString = new StringBuilder();
			
			while (lineIterator.hasNext())
			/*
			 * On stocke plusieurs lignes jusqu'à ce que l'une d'entre elles se termine par un '}'
			 * On considère alors que c'est la fin d'une requête
			 */
			{
				String line = lineIterator.next();
				queryString.append(line);

				if (line.trim().endsWith("}")) {
					ParsedQuery query = sparqlParser.parseQuery(queryString.toString(), baseURI);

					processAQuery(query); // Traitement de la requête, à adapter/réécrire pour votre programme
					
					
					queryString.setLength(0); // Reset le buffer de la requête en chaine vide
				}
			}
		}
	}

	/**
	 * Traite chaque triple lu dans {@link #dataFile} avec {@link MainRDFHandler}.
	 */
	private static void parseData() throws FileNotFoundException, IOException {

		try (Reader dataReader = new FileReader(dataFile)) {
			// On va parser des données au format ntriples
			
			RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);

			// On utilise notre implémentation de handler
			
			MainRDFHandler handler = new MainRDFHandler();
			rdfParser.setRDFHandler(handler);
			
			// Parsing et traitement de chaque triple par le handler
			rdfParser.parse(dataReader, baseURI);
			
			/********** dictionnaire & Indexation **********/
			/** La création et l'ajout des données dans le dictionnaire se font dans la classe MainRDFHandler*/
			
			//dictio_ = dictio.getDictio(); //dictionnaire
			//SPO_ = spo.getSPO();  // Indexation 
			
			/********** Affichage **********/
			
			//dictio.print();
			//spo.print();

			
			/********** Les autres **********/
			/*SOP = SPO_bis.toSOP();
			OSP = SPO_bis.toOSP();
			PSO = SPO_bis.toPSO();
			POS = SPO_bis.toPOS();
			OPS = SPO_bis.toOPS();
			
			System.out.println("SOP : \n" );spo.print(SOP);
			System.out.println("OSP : \n" );spo.print(OSP);
			System.out.println("PSO : \n" );spo.print(PSO);
			System.out.println("POS : \n" );spo.print(POS);
			System.out.println("OPS : \n" );spo.print(OPS);*/
					

		}
	}
	
	public static void writes_results_into_file(String result){
        try {
            BufferedWriter fWriter = new BufferedWriter(new FileWriter(outputFile+"_results.txt",true));
            BufferedWriter bw = new BufferedWriter(fWriter);
            bw.write("Result query  "+ j+" : ");
            bw.newLine();
            bw.write(result);// ici tu mets ce que tu veux ecrire dans ton fichier
            bw.newLine();
            bw.close();
        }catch (IOException e) {
            System.out.print(e.getMessage());
        }
	}
	public static void writes_stats_into_file(){
		try {
            BufferedWriter fWriter = new BufferedWriter(new FileWriter(outputFile+"_stats.txt",true));
            BufferedWriter bw = new BufferedWriter(fWriter);
            bw.write("\t Total number of queries         : \t "+j);// ici tu mets ce que tu veux ecrire dans ton fichier
            bw.newLine();
            bw.write("\t Number of queries with solution : \t"+nbSol);// ici tu mets ce que tu veux ecrire dans ton fichier
            bw.newLine();
            bw.write("\t Queries process Time            : \t" +qTime + "  ms");
            bw.newLine();
            bw.write("\t Dictionary and Indexation Time  : \t" +pTime + "  ms");
            bw.newLine();
            bw.write("\t Total Program Execution Time    : \t" +exTime + "  ms");
            bw.newLine();           
            
            bw.close();
        }catch (IOException e) {
            System.out.print(e.getMessage());
        }
	}
	public static void writes_dictioANDspo_into_file(){
        try {
            BufferedWriter fWriter = new BufferedWriter(new FileWriter(outputFile+"_dictioANDspo.txt",true));
            BufferedWriter bw = new BufferedWriter(fWriter);
            bw.write(dictio.toString());
            bw.newLine();
            bw.write(spo.toString());
            bw.newLine();
            bw.close();
        }catch (IOException e) {
            System.out.print(e.getMessage());
        }
	}
	private static Options configParameters() {

		Option queriesOption = new Option("queries",true,"chemin/vers/le/fichier/des/requetes");
		Option dataOption = new Option("data",true,"chemin/vers/le/fichier/des/data");
		Option outputOption = new Option("outputs",true,"chemin/vers/le/fichier/des/outputs");
		outputOption.setRequired(true);
		dataOption.setRequired(true);
		queriesOption.setRequired(true);
        final Options options = new Options();
        options.addOption(queriesOption);
        options.addOption(dataOption);
        options.addOption(outputOption);
        
    

        return options;
    }
	
	
}

package com.karthik.web.nids.servlet;

import java.io.BufferedReader;
import java.io.FileReader;
import org.jgap.*;
import org.jgap.impl.*;
import org.jgap.audit.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class RuleMaker {

	static int src_bytes_a, src_bytes_b, src_bytes_c, src_bytes_d, dst_bytes_a,
			dst_bytes_b, dst_bytes_c, dst_bytes_d, count_a, count_b, count_c,
			count_d, srv_count_a, srv_count_b, srv_count_c, srv_count_d,
			fsrc_bytes, fdst_bytes, fcount, fsrv_count;

	static int max_src_bytes_lower = -1, max_src_bytes_upper = -1,
			max_dst_bytes_lower = -1, max_dst_bytes_upper = -1,
			max_count_lower = -1, max_count_upper = -1,
			max_srv_count_lower = -1, max_srv_count_upper = -1;
	static double max_fitness = 0;

	static int count_hash_src_bytes = 1;
	static int count_hash_dst_bytes = 1;
	static int count_hash_count = 1;
	static int count_hash_srv_count = 1;

	private static String CVS_REVISION = "$Revision: 1.25 $";
	private static int MAX_ALLOWED_EVOLUTIONS;
	private static String FILE_PATH = "/host/Users/Karthik/BTECH PROJECT/kdd_modified2.csv";
	static int flag = 0;
	static int attack;
	static String result_attack;

	public RuleMaker() {
		max_src_bytes_lower = -1;
		max_src_bytes_upper = -1;
		max_dst_bytes_lower = -1;
		max_dst_bytes_upper = -1;
		max_count_lower = -1;
		max_count_upper = -1;
		max_srv_count_lower = -1;
		max_srv_count_upper = -1;
		max_fitness = 0;

		count_hash_src_bytes = 1;
		count_hash_dst_bytes = 1;
		count_hash_count = 1;
		count_hash_srv_count = 1;

	}

	public Rule makeRules(int choice) throws Exception {

		// *************CUSTOMIZATION USING SPRING
		// FRAMEWORK************************************
		/*
		 * Resource resource = new ClassPathResource("applicationContext.xml");
		 * BeanFactory factory = new XmlBeanFactory(resource);
		 * 
		 * Customization customize = (Customization) factory
		 * .getBean("customizebean"); MAX_ALLOWED_EVOLUTIONS =
		 * customize.getMaxAllowedEvolutions(); CVS_REVISION =
		 * customize.getCVSRevision(); FILE_PATH = customize.getFilePath();
		 */
		// *****************************************************************

		// PRE-COMPUTATION STEP--------------------------

		/*
		 * Find the number of distinct values of src_bytes,dst_bytes,count and
		 * srv_count
		 */

		LookupTables.initialize();
		FileReader fr = new FileReader(FILE_PATH);
		BufferedReader br = new BufferedReader(fr);
		String line;

		while ((line = br.readLine()) != null) {
			String[] file_data = line.split(",");
			fsrc_bytes = Integer.parseInt(file_data[0]);
			fdst_bytes = Integer.parseInt(file_data[1]);
			fcount = Integer.parseInt(file_data[2]);
			fsrv_count = Integer.parseInt(file_data[3]);

			int temp = LookupTables.getHashValue("src_bytes", fsrc_bytes);
			LookupTables.setHashValue("src_bytes", fsrc_bytes, temp + 1);

			temp = LookupTables.getHashValue("dst_bytes", fdst_bytes);
			LookupTables.setHashValue("dst_bytes", fdst_bytes, temp + 1);

			temp = LookupTables.getHashValue("count", fcount);
			LookupTables.setHashValue("count", fcount, temp + 1);

			temp = LookupTables.getHashValue("srv_count", fsrv_count);
			LookupTables.setHashValue("srv_count", fsrv_count, temp + 1);

		}

		for (int i = 0; i < 70000; i++) {
			int temp = LookupTables.getHashValue("src_bytes", i);
			if (temp > 0) {
				LookupTables.setHashValue("src_bytes", i, count_hash_src_bytes);
				LookupTables.setLookupValue("src_bytes", count_hash_src_bytes,
						i);
				LookupTables.setFrequencyValue("src_bytes",
						count_hash_src_bytes, count_hash_src_bytes, temp);
				count_hash_src_bytes++;
			}
		}

		for (int i = 0; i < 300000; i++) {
			int temp = LookupTables.getHashValue("dst_bytes", i);
			if (temp > 0) {
				LookupTables.setHashValue("dst_bytes", i, count_hash_dst_bytes);
				LookupTables.setLookupValue("dst_bytes", count_hash_dst_bytes,
						i);
				LookupTables.setFrequencyValue("dst_bytes",
						count_hash_dst_bytes, count_hash_dst_bytes, temp);
				count_hash_dst_bytes++;
			}
		}

		for (int i = 0; i < 1000; i++) {
			int temp = LookupTables.getHashValue("count", i);
			if (temp > 0) {
				LookupTables.setHashValue("count", i, count_hash_count);
				LookupTables.setLookupValue("count", count_hash_count, i);
				LookupTables.setFrequencyValue("count", count_hash_count,
						count_hash_count, temp);
				count_hash_count++;
			}
		}

		for (int i = 0; i < 1000; i++) {
			int temp = LookupTables.getHashValue("srv_count", i);
			if (temp > 0) {
				LookupTables.setHashValue("srv_count", i, count_hash_srv_count);
				LookupTables.setLookupValue("srv_count", count_hash_srv_count,
						i);
				LookupTables.setFrequencyValue("srv_count",
						count_hash_srv_count, count_hash_srv_count, temp);
				count_hash_srv_count++;
			}
		}

		count_hash_src_bytes--;
		count_hash_dst_bytes--;
		count_hash_count--;
		count_hash_srv_count--;

		/*
		 * System.out.println(count_hash_src_bytes + " " + count_hash_dst_bytes
		 * + " " + count_hash_count + " " + count_hash_srv_count);
		 */

		LookupTables.sortArray("src_bytes", count_hash_src_bytes);
		LookupTables.sortArray("dst_bytes", count_hash_dst_bytes);
		LookupTables.sortArray("count", count_hash_count);
		LookupTables.sortArray("srv_count", count_hash_srv_count);

		for (int i = 1; i < count_hash_src_bytes; i++) {
			int freq = LookupTables.getFrequencyValue("src_bytes", i).frequency;
			int prev_value = LookupTables.getFrequencyValue("src_bytes", i - 1).value;
			int curr_value = LookupTables.getFrequencyValue("src_bytes", i).value;

			LookupTables.setSortedFrequencyValue("src_bytes", i, freq,
					curr_value + prev_value);

		}

		for (int i = 1; i < count_hash_dst_bytes; i++) {
			int freq = LookupTables.getFrequencyValue("dst_bytes", i).frequency;
			int prev_value = LookupTables.getFrequencyValue("dst_bytes", i - 1).value;
			int curr_value = LookupTables.getFrequencyValue("dst_bytes", i).value;

			LookupTables.setSortedFrequencyValue("dst_bytes", i, freq,
					curr_value + prev_value);

		}

		for (int i = 1; i < count_hash_count; i++) {
			int freq = LookupTables.getFrequencyValue("count", i).frequency;
			int prev_value = LookupTables.getFrequencyValue("count", i - 1).value;
			int curr_value = LookupTables.getFrequencyValue("count", i).value;

			LookupTables.setSortedFrequencyValue("count", i, freq, curr_value
					+ prev_value);

		}

		for (int i = 1; i < count_hash_srv_count; i++) {
			int freq = LookupTables.getFrequencyValue("srv_count", i).frequency;
			int prev_value = LookupTables.getFrequencyValue("srv_count", i - 1).value;
			int curr_value = LookupTables.getFrequencyValue("srv_count", i).value;

			LookupTables.setSortedFrequencyValue("srv_count", i, freq,
					curr_value + prev_value);

		}

		for (int i = 0; i < count_hash_src_bytes; i++) {
			System.out.println(i + " "
					+ LookupTables.getFrequencyValue("src_bytes", i).frequency
					+ " "
					+ LookupTables.getFrequencyValue("src_bytes", i).value);
		}

		// System.exit(0);

		/*
		 * The above precomputation is done to increase the probability of
		 * choosing the relevant values by the Genetic Algorithm.
		 */

		// --------------------END PRE-COMPUTATION STEP

		Configuration.reset();
		Configuration conf = new DefaultConfiguration();
		conf.setPreservFittestIndividual(true);

		FitnessFunction myFunc = new PacketFitnessFunction(flag, choice);
		flag = 1;
		conf.setFitnessFunction(myFunc);

		Gene[] sampleGenes = new Gene[17];

		/*************
		 * Chromosome Design for Implementing Fuzzy Logic***********
		 * 
		 * src_bytes parameters: a->150, b->200, c->300, d->400 dst_bytes
		 * parameters: a->1000, b->20000, c->25000, d-> 49000 count parameters:
		 * a->1, b->3, c->7, d->10 srv_count parameters: a->0, b->4, c->9, d->12
		 */

		sampleGenes[0] = new IntegerGene(conf, 1, 50); // src_bytes_a
		sampleGenes[1] = new IntegerGene(conf, 1,
				LookupTables.getFrequencyValue("src_bytes",
						count_hash_src_bytes - 1).value); // src_bytes_b
		sampleGenes[2] = new IntegerGene(conf, 1,
				LookupTables.getFrequencyValue("src_bytes",
						count_hash_src_bytes - 1).value); // src_bytes_c
		sampleGenes[3] = new IntegerGene(conf, 171, 220); // src_bytes_d

		sampleGenes[4] = new IntegerGene(conf, 1, 150); // dst_bytes_a
		sampleGenes[5] = new IntegerGene(conf, 1,
				LookupTables.getFrequencyValue("dst_bytes",
						count_hash_dst_bytes - 1).value); // dst_bytes_b
		sampleGenes[6] = new IntegerGene(conf, 1,
				LookupTables.getFrequencyValue("dst_bytes",
						count_hash_dst_bytes - 1).value); // dst_bytes_c
		sampleGenes[7] = new IntegerGene(conf, 451, 604); // dst_bytes_d

		sampleGenes[8] = new IntegerGene(conf, 1, 75); // count_a
		sampleGenes[9] = new IntegerGene(
				conf,
				1,
				LookupTables.getFrequencyValue("count", count_hash_count - 1).value); // count_b
		sampleGenes[10] = new IntegerGene(
				conf,
				1,
				LookupTables.getFrequencyValue("count", count_hash_count - 1).value); // count_c
		sampleGenes[11] = new IntegerGene(conf, 226, 294); // count_d

		sampleGenes[12] = new IntegerGene(conf, 1, 25); // srv_count_a
		sampleGenes[13] = new IntegerGene(conf, 1,
				LookupTables.getFrequencyValue("srv_count",
						count_hash_srv_count - 1).value); // srv_count_b
		sampleGenes[14] = new IntegerGene(conf, 1,
				LookupTables.getFrequencyValue("srv_count",
						count_hash_srv_count - 1).value); // srv_count_c
		sampleGenes[15] = new IntegerGene(conf, 61, 86); // srv_count_d
		sampleGenes[16] = new IntegerGene(conf);

		Chromosome sampleChromosome = new Chromosome(conf, sampleGenes);
		conf.setSampleChromosome(sampleChromosome);

		conf.setPopulationSize(1);

		PermutingConfiguration pconf = new PermutingConfiguration(conf);
		pconf.addGeneticOperatorSlot(new CrossoverOperator(conf));
		pconf.addGeneticOperatorSlot(new MutationOperator(conf));
		pconf.addNaturalSelectorSlot(new BestChromosomesSelector(conf));
		pconf.addNaturalSelectorSlot(new WeightedRouletteSelector(conf));
		pconf.addRandomGeneratorSlot(new StockRandomGenerator());
		RandomGeneratorForTesting rn = new RandomGeneratorForTesting();
		rn.setNextDouble(0.7d);
		rn.setNextInt(2);
		pconf.addRandomGeneratorSlot(rn);
		pconf.addRandomGeneratorSlot(new GaussianRandomGenerator());
		pconf.addFitnessFunctionSlot(new PacketFitnessFunction(flag, choice));
		Evaluator eval = new Evaluator(pconf);

		int permutation = 0;
		while (eval.hasNext()) {
			Genotype population = Genotype.randomInitialGenotype(eval.next());
			for (int run = 0; run < 10; run++) {
				for (int i = 0; i < MAX_ALLOWED_EVOLUTIONS; i++) {
					population.evolve();
				}

			}

			IChromosome bestSolutionSoFar = population.getFittestChromosome();
			IChromosome chrome = bestSolutionSoFar;

			PacketFitnessFunction.setChromosome(chrome);

			src_bytes_a = getValueAtGene(chrome, 0);
			src_bytes_b = getValueAtGene(chrome, 1);
			src_bytes_c = getValueAtGene(chrome, 2);
			src_bytes_d = getValueAtGene(chrome, 3);

			dst_bytes_a = getValueAtGene(chrome, 4);
			dst_bytes_b = getValueAtGene(chrome, 5);
			dst_bytes_c = getValueAtGene(chrome, 6);
			dst_bytes_d = getValueAtGene(chrome, 7);

			count_a = getValueAtGene(chrome, 8);
			count_b = getValueAtGene(chrome, 9);
			count_c = getValueAtGene(chrome, 10);
			count_d = getValueAtGene(chrome, 11);

			srv_count_a = getValueAtGene(chrome, 12);
			srv_count_b = getValueAtGene(chrome, 13);
			srv_count_c = getValueAtGene(chrome, 14);
			srv_count_d = getValueAtGene(chrome, 15);

			attack = getValueAtGene(chrome, 16);

			switch (attack) {
			case 1: {
				result_attack = "smurf";
				break;
			}
			case 2: {
				result_attack = "ipsweep";
				break;
			}
			case 3: {
				result_attack = "neptune";
				break;
			}
			case 4: {
				result_attack = "satan";
				break;
			}
			default: {
				result_attack = "unknown";
				break;
			}

			}

			System.out.println("");

			int temp;

			int sbl = LookupTables
					.getLookupValue(
							"src_bytes",
							LookupTables
									.getFrequencyValue(
											"src_bytes",
											(temp = LookupTables
													.ProbabilitySelector(
															"src_bytes",
															src_bytes_b)) < 0 ? (-temp - 1)
													: temp).frequency);
			int sbu = LookupTables
					.getLookupValue(
							"src_bytes",
							LookupTables
									.getFrequencyValue(
											"src_bytes",
											(temp = LookupTables
													.ProbabilitySelector(
															"src_bytes",
															src_bytes_c)) < 0 ? (-temp - 1)
													: temp).frequency);

			int dbl = LookupTables
					.getLookupValue(
							"dst_bytes",
							LookupTables
									.getFrequencyValue(
											"dst_bytes",
											(temp = LookupTables
													.ProbabilitySelector(
															"dst_bytes",
															dst_bytes_b)) < 0 ? (-temp - 1)
													: temp).frequency);

			int dbu = LookupTables
					.getLookupValue(
							"dst_bytes",
							LookupTables
									.getFrequencyValue(
											"dst_bytes",
											(temp = LookupTables
													.ProbabilitySelector(
															"dst_bytes",
															dst_bytes_c)) < 0 ? (-temp - 1)
													: temp).frequency);

			int cl = LookupTables
					.getLookupValue(
							"count",
							LookupTables
									.getFrequencyValue(
											"count",
											(temp = LookupTables
													.ProbabilitySelector(
															"count", count_b)) < 0 ? (-temp - 1)
													: temp).frequency);

			int cu = LookupTables
					.getLookupValue(
							"count",
							LookupTables
									.getFrequencyValue(
											"count",
											(temp = LookupTables
													.ProbabilitySelector(
															"count", count_c)) < 0 ? (-temp - 1)
													: temp).frequency);

			int srvcl = LookupTables
					.getLookupValue(
							"srv_count",
							LookupTables
									.getFrequencyValue(
											"srv_count",
											(temp = LookupTables
													.ProbabilitySelector(
															"srv_count",
															srv_count_b)) < 0 ? (-temp - 1)
													: temp).frequency);

			int srvcu = LookupTables
					.getLookupValue(
							"srv_count",
							LookupTables
									.getFrequencyValue(
											"srv_count",
											(temp = LookupTables
													.ProbabilitySelector(
															"srv_count",
															srv_count_c)) < 0 ? (-temp - 1)
													: temp).frequency);

			if (bestSolutionSoFar.getFitnessValue() > max_fitness) {
				max_fitness = bestSolutionSoFar.getFitnessValue();
				max_src_bytes_lower = sbl;
				max_src_bytes_upper = sbu;

				max_dst_bytes_lower = dbl;
				max_dst_bytes_upper = dbu;

				max_count_lower = cl;
				max_count_upper = cu;

				max_srv_count_lower = srvcl;
				max_srv_count_upper = srvcu;

			}

			System.out
					.println("***NEW RULE***:The current rule has a fitness value of "
							+ bestSolutionSoFar.getFitnessValue() + "\n");

			System.out.println("RULE: \nIf Source Bytes are between " + sbl
					+ " and " + sbu + ", \nDestination Bytes are between "
					+ dbl + " and " + dbu + ", \nCount is between " + cl
					+ " and " + cu + ", \nSrv count is between " + srvcl
					+ " and " + srvcu + ", it is a " + result_attack
					+ " attack.\n");

			System.out
					.println("*****************************************************************\n");

			permutation++;
		}

		System.out.println("Max fitness is:" + max_fitness);

		Rule r = new Rule();
		r.setRule(max_src_bytes_lower, max_dst_bytes_upper,
				max_dst_bytes_lower, max_dst_bytes_upper, max_count_lower,
				max_count_upper, max_srv_count_lower, max_srv_count_upper,
				max_fitness);

		return r;
	}

	public static int getValueAtGene(IChromosome chrome, int position) {
		Integer value = (Integer) chrome.getGene(position).getAllele();
		return value.intValue();
	}

}
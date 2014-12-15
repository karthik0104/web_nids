package com.karthik.web.nids.servlet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.jgap.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class PacketFitnessFunction extends FitnessFunction {

	int src_bytes_a, src_bytes_b, src_bytes_c, src_bytes_d, dst_bytes_a,
			dst_bytes_b, dst_bytes_c, dst_bytes_d, count_a, count_b, count_c,
			count_d, srv_count_a, srv_count_b, srv_count_c, srv_count_d,
			fsrc_bytes, fdst_bytes, fcount, fsrv_count, A, B, alpha, beta;
	double total;
	String fresult,
			FILE_PATH = "/host/Users/Karthik/BTECH PROJECT/kdd_modified2.csv";
	static int test_count;
	static IChromosome chrome;
	String attack_name;
	int attack_index;

	private static String CVS_REVISION = "$Revision: 1.25 $";

	public PacketFitnessFunction(int flag, int attack_index) {

		// *************SPRING FRAMEWORK************************************
		/*
		 * Resource resource = new ClassPathResource("applicationContext.xml");
		 * BeanFactory factory = new XmlBeanFactory(resource);
		 * 
		 * Customization customize = (Customization) factory
		 * .getBean("customizebean"); FILE_PATH = customize.getFilePath();
		 * CVS_REVISION = customize.getCVSRevision();
		 */
		this.attack_index = attack_index;

		switch (attack_index) {
		case 0: {
			attack_name = "smurf.";
			break;
		}
		case 1: {
			attack_name = "ipsweep.";
			break;
		}
		case 2: {
			attack_name = "neptune.";
			break;
		}
		case 3: {
			attack_name = "satan.";
			break;
		}

		}

		test_count = 0;

		// *****************************************************************

	}

	public static void setChromosome(IChromosome chrome_inst) {
		chrome = chrome_inst;
	}

	@Override
	protected double evaluate(IChromosome chrome) {
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

		chrome.getGene(16).setAllele(1 + attack_index);

		int temp;

		src_bytes_b = LookupTables.getFrequencyValue(
				"src_bytes",
				(temp = LookupTables.ProbabilitySelector("src_bytes",
						src_bytes_b)) < 0 ? (-temp - 1) : temp).frequency;
		src_bytes_c = LookupTables.getFrequencyValue(
				"src_bytes",
				(temp = LookupTables.ProbabilitySelector("src_bytes",
						src_bytes_c)) < 0 ? (-temp - 1) : temp).frequency;
		dst_bytes_b = LookupTables.getFrequencyValue(
				"dst_bytes",
				(temp = LookupTables.ProbabilitySelector("dst_bytes",
						dst_bytes_b)) < 0 ? (-temp - 1) : temp).frequency;
		dst_bytes_c = LookupTables.getFrequencyValue(
				"dst_bytes",
				(temp = LookupTables.ProbabilitySelector("dst_bytes",
						dst_bytes_b)) < 0 ? (-temp - 1) : temp).frequency;
		count_b = LookupTables
				.getFrequencyValue(
						"count",
						(temp = LookupTables.ProbabilitySelector("count",
								count_b)) < 0 ? (-temp - 1) : temp).frequency;
		count_c = LookupTables
				.getFrequencyValue(
						"count",
						(temp = LookupTables.ProbabilitySelector("count",
								count_c)) < 0 ? (-temp - 1) : temp).frequency;
		srv_count_b = LookupTables.getFrequencyValue(
				"srv_count",
				(temp = LookupTables.ProbabilitySelector("srv_count",
						srv_count_b)) < 0 ? (-temp - 1) : temp).frequency;
		srv_count_c = LookupTables.getFrequencyValue(
				"srv_count",
				(temp = LookupTables.ProbabilitySelector("srv_count",
						srv_count_c)) < 0 ? (-temp - 1) : temp).frequency;

		double fitness = 0;

		// *************The parameters of analysis**************************
		A = 0;
		B = 0;
		alpha = 0;
		beta = 0;
		// //////////////////////////////////////////////////////////////////

		FileReader fr;
		String line = "";
		try {
			fr = new FileReader(FILE_PATH);
			BufferedReader br = new BufferedReader(fr);

			while ((line = br.readLine()) != null) {
				double certainty;
				String[] packet_data = line.split(",");
				fsrc_bytes = Integer.parseInt(packet_data[0]);
				fdst_bytes = Integer.parseInt(packet_data[1]);
				fcount = Integer.parseInt(packet_data[2]);
				fsrv_count = Integer.parseInt(packet_data[3]);
				fresult = packet_data[4];
				total = 0;

				if (fresult.equals(attack_name))
					A++;
				else
					B++;

				for (int i = 0; i < 4; i++) {
					certainty = fuzzy(i);
					total = total + certainty;
				}

				if (total > (double) 2) { // Here I have taken 2.00 as threshold
											// value....Question is whether to
											// include
											// threshold also as a gene in the
											// chromosome...
					if (fresult.equals(attack_name))
						alpha++;
					else
						beta++;
				}

			}

			br.close();
			fr.close();

			fitness = ((double) alpha / (double) A - (double) beta / (double) B);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (fitness <= 0)
			fitness = -fitness;

		fitness *= 100;
		// System.out.println("Fitness:"+fitness);

		return fitness;
	}

	public static int getValueAtGene(IChromosome chrome, int position) {
		Integer value = (Integer) chrome.getGene(position).getAllele();
		return value.intValue();
	}

	// The Fuzzy Function......................

	public double fuzzy(int i) {
		double certainty = 0.0;
		switch (i) {
		case 0:
			int hash_fsrc_bytes = LookupTables.getHashValue("src_bytes",
					fsrc_bytes);
			if (src_bytes_b <= hash_fsrc_bytes
					&& hash_fsrc_bytes <= src_bytes_c)
				certainty = 1;
			else if (src_bytes_a < hash_fsrc_bytes
					&& hash_fsrc_bytes < src_bytes_b)
				certainty = (hash_fsrc_bytes - src_bytes_a)
						/ (src_bytes_b - src_bytes_a);
			else if (src_bytes_c < hash_fsrc_bytes
					&& hash_fsrc_bytes < src_bytes_d)
				certainty = (src_bytes_d - hash_fsrc_bytes)
						/ (src_bytes_d - src_bytes_c);
			else
				certainty = 0.0;

			break;
		case 1:
			int hash_fdst_bytes = LookupTables.getHashValue("dst_bytes",
					fdst_bytes);
			if (dst_bytes_b <= hash_fdst_bytes
					&& hash_fdst_bytes <= dst_bytes_c)
				certainty = 1;
			else if (dst_bytes_a < hash_fdst_bytes
					&& hash_fdst_bytes < dst_bytes_b)
				certainty = (hash_fdst_bytes - dst_bytes_a)
						/ (dst_bytes_b - dst_bytes_a);
			else if (dst_bytes_c < hash_fdst_bytes
					&& hash_fdst_bytes < dst_bytes_d)
				certainty = (dst_bytes_d - hash_fdst_bytes)
						/ (dst_bytes_d - dst_bytes_c);
			else
				certainty = 0.0;

			break;
		case 2:
			int hash_fcount = LookupTables.getHashValue("count", fcount);
			if (count_b <= hash_fcount && hash_fcount <= count_c)
				certainty = 1;
			else if (count_a < hash_fcount && hash_fcount < count_b)
				certainty = (hash_fcount - count_a) / (count_b - count_a);
			else if (count_c < hash_fcount && hash_fcount < count_d)
				certainty = (count_d - hash_fcount) / (count_d - count_c);
			else
				certainty = 0.0;

			break;
		case 3:
			int hash_fsrv_count = LookupTables.getHashValue("srv_count",
					fsrv_count);
			if ((srv_count_b <= hash_fsrv_count && hash_fsrv_count <= srv_count_c)
					|| (srv_count_c <= hash_fsrv_count && hash_fsrv_count <= srv_count_b))
				certainty = 1;
			else if (srv_count_a < hash_fsrv_count
					&& hash_fsrv_count < srv_count_b)
				certainty = (hash_fsrv_count - srv_count_a)
						/ (srv_count_b - srv_count_a);
			else if (srv_count_c < hash_fsrv_count
					&& hash_fsrv_count < srv_count_d)
				certainty = (srv_count_d - hash_fsrv_count)
						/ (srv_count_d - srv_count_c);
			else
				certainty = 0.0;

			break;
		}

		return certainty;
	}

}
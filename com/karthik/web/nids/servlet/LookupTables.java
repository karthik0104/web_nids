package com.karthik.web.nids.servlet;
import java.util.Arrays;
import java.util.Comparator;

public class LookupTables {

	static int[] hash_src_bytes;
	static int[] hash_dst_bytes;
	static int[] hash_count;
	static int[] hash_srv_count;

	static int[] lookup_src_bytes;
	static int[] lookup_dst_bytes;
	static int[] lookup_count;
	static int[] lookup_srv_count;

	static FrequencyClass[] freq_src_bytes;
	static FrequencyClass[] freq_dst_bytes;
	static FrequencyClass[] freq_count;
	static FrequencyClass[] freq_srv_count;

	static FrequencyClass[] sorted_freq_src_bytes;
	static FrequencyClass[] sorted_freq_dst_bytes;
	static FrequencyClass[] sorted_freq_count;
	static FrequencyClass[] sorted_freq_srv_count;

	public static void initialize() {
		hash_src_bytes = new int[70000];
		hash_dst_bytes = new int[300000];
		hash_count = new int[1000];
		hash_srv_count = new int[1000];

		lookup_src_bytes = new int[5000];
		lookup_dst_bytes = new int[5000];
		lookup_count = new int[5000];
		lookup_srv_count = new int[5000];
		
		freq_src_bytes = new FrequencyClass[5000];
		freq_dst_bytes = new FrequencyClass[5000];
		freq_count = new FrequencyClass[5000];
		freq_srv_count = new FrequencyClass[5000];

		for (int i = 0; i < 5000; i++) {
			freq_src_bytes[i] = new FrequencyClass();
			freq_dst_bytes[i] = new FrequencyClass();
			freq_count[i] = new FrequencyClass();
			freq_srv_count[i] = new FrequencyClass();
		}
	}

	// Getter and Setter methods for HashValues

	public static void setHashValue(String param, int i, int value) {
		if (param.equals("src_bytes"))
			hash_src_bytes[i] = value;
		else if (param.equals("dst_bytes"))
			hash_dst_bytes[i] = value;
		else if (param.equals("count"))
			hash_count[i] = value;
		else
			hash_srv_count[i] = value;
	}

	public static int getHashValue(String param, int i) {
		if (param.equals("src_bytes"))
			return hash_src_bytes[i];
		else if (param.equals("dst_bytes"))
			return hash_dst_bytes[i];
		else if (param.equals("count"))
			return hash_count[i];
		else
			return hash_srv_count[i];
	}

	// Getter and Setter methods for LookupValues

	public static void setLookupValue(String param, int i, int value) {
		if (param.equals("src_bytes"))
			lookup_src_bytes[i] = value;
		else if (param.equals("dst_bytes"))
			lookup_dst_bytes[i] = value;
		else if (param.equals("count"))
			lookup_count[i] = value;
		else
			lookup_srv_count[i] = value;
	}

	public static int getLookupValue(String param, int i) {
		if (param.equals("src_bytes"))
			return lookup_src_bytes[i];
		else if (param.equals("dst_bytes"))
			return lookup_dst_bytes[i];
		else if (param.equals("count"))
			return lookup_count[i];
		else
			return lookup_srv_count[i];
	}

	public static void setFrequencyValue(String param, int i, int freq,
			int value) {
		if (param.equals("src_bytes")) {
			freq_src_bytes[i].frequency = freq;
			freq_src_bytes[i].value = value;
		} else if (param.equals("dst_bytes")) {
			freq_dst_bytes[i].frequency = freq;
			freq_dst_bytes[i].value = value;
		} else if (param.equals("count")) {
			freq_count[i].frequency = freq;
			freq_count[i].value = value;
		} else {
			freq_srv_count[i].frequency = freq;
			freq_srv_count[i].value = value;
		}
	}

	public static FrequencyClass getFrequencyValue(String param, int i) {
		if (param.equals("src_bytes"))
			return sorted_freq_src_bytes[i];
		else if (param.equals("dst_bytes"))
			return sorted_freq_dst_bytes[i];
		else if (param.equals("count"))
			return sorted_freq_count[i];
		else
			return sorted_freq_srv_count[i];
	}

	public static void setSortedFrequencyValue(String param, int i, int freq,
			int value) {
		if (param.equals("src_bytes")) {
			sorted_freq_src_bytes[i].frequency = freq;
			sorted_freq_src_bytes[i].value = value;
		} else if (param.equals("dst_bytes")) {
			sorted_freq_dst_bytes[i].frequency = freq;
			sorted_freq_dst_bytes[i].value = value;
		} else if (param.equals("count")) {
			sorted_freq_count[i].frequency = freq;
			sorted_freq_count[i].value = value;
		} else {
			sorted_freq_srv_count[i].frequency = freq;
			sorted_freq_srv_count[i].value = value;
		}
	}

	public static void sortArray(String param, int n) {

		if (param.equals("src_bytes")) {

			sorted_freq_src_bytes = new FrequencyClass[n + 1];

			for (int i = 1; i <= n; i++) {
				sorted_freq_src_bytes[i] = new FrequencyClass();
				sorted_freq_src_bytes[i - 1] = freq_src_bytes[i];
			}

			Arrays.sort(sorted_freq_src_bytes,
					new Comparator<FrequencyClass>() {

						@Override
						public int compare(FrequencyClass o1, FrequencyClass o2) {
							// TODO Auto-generated method stub
							return o2.value - o1.value;
						}
					});
		} else if (param.equals("dst_bytes")) {

			sorted_freq_dst_bytes = new FrequencyClass[n + 1];

			for (int i = 1; i <= n; i++) {
				sorted_freq_dst_bytes[i] = new FrequencyClass();
				sorted_freq_dst_bytes[i - 1] = freq_dst_bytes[i];
			}

			Arrays.sort(sorted_freq_dst_bytes,
					new Comparator<FrequencyClass>() {

						@Override
						public int compare(FrequencyClass o1, FrequencyClass o2) {
							// TODO Auto-generated method stub
							return o2.value - o1.value;
						}
					});
		} else if (param.equals("count")) {

			sorted_freq_count = new FrequencyClass[n + 1];

			for (int i = 1; i <= n; i++) {
				sorted_freq_count[i] = new FrequencyClass();
				sorted_freq_count[i - 1] = freq_count[i];
			}

			Arrays.sort(sorted_freq_count, new Comparator<FrequencyClass>() {

				@Override
				public int compare(FrequencyClass o1, FrequencyClass o2) {
					// TODO Auto-generated method stub
					return o2.value - o1.value;
				}
			});
		} else {

			sorted_freq_srv_count = new FrequencyClass[n + 1];

			for (int i = 1; i <= n; i++) {
				sorted_freq_srv_count[i] = new FrequencyClass();
				sorted_freq_srv_count[i - 1] = freq_srv_count[i];
			}

			Arrays.sort(sorted_freq_srv_count,
					new Comparator<FrequencyClass>() {

						@Override
						public int compare(FrequencyClass o1, FrequencyClass o2) {
							// TODO Auto-generated method stub
							return o2.value - o1.value;
						}
					});
		}
	}

	public static int ProbabilitySelector(String param, int value) {
		FrequencyClass temp = new FrequencyClass();
		temp.value = value;
		int search = 0;

		if (param.equals("src_bytes")) {
			search = Arrays.binarySearch(sorted_freq_src_bytes, temp,
					new Comparator<FrequencyClass>() {

						@Override
						public int compare(FrequencyClass o1, FrequencyClass o2) {
							// TODO Auto-generated method stub
							return o1.value - o2.value;
						}

					});
		} else if (param.equals("dst_bytes")) {
			search = Arrays.binarySearch(sorted_freq_dst_bytes, temp,
					new Comparator<FrequencyClass>() {

						@Override
						public int compare(FrequencyClass o1, FrequencyClass o2) {
							// TODO Auto-generated method stub
							return o1.value - o2.value;
						}

					});
		} else if (param.equals("count")) {
			search = Arrays.binarySearch(sorted_freq_count, temp,
					new Comparator<FrequencyClass>() {

						@Override
						public int compare(FrequencyClass o1, FrequencyClass o2) {
							// TODO Auto-generated method stub
							return o1.value - o2.value;
						}

					});
		} else {
			search = Arrays.binarySearch(sorted_freq_srv_count, temp,
					new Comparator<FrequencyClass>() {

						@Override
						public int compare(FrequencyClass o1, FrequencyClass o2) {
							// TODO Auto-generated method stub
							return o1.value - o2.value;
						}

					});
		}

		return search;

	}

}
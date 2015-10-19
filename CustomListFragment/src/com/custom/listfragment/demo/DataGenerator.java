package com.custom.listfragment.demo;

public class DataGenerator {

	public static final int TITLE_COUNT = 20;
	public static final int DETAILS_COUNT = 50;
	
	static String[] dialogues = null;
	static String[] titles = null;
	
	public static String[] getTitleDataSet() {
		if(titles == null) {
			String[] arr = new String[TITLE_COUNT];
			for (int i = 0; i < TITLE_COUNT; i++) {
				arr[i] = "Title item " + i;
			}
			titles = arr;
		}
		return titles;
	}

	public static String[] getDetailsDataSet(int titleIndex) {
		if(dialogues == null) {
			initDetails();
		}
		String[] a = new String[dialogues.length];
		String[] titles = getTitleDataSet();
		for (int i = 0; i < dialogues.length; i++) {
			a[i] = "\n" + titles[titleIndex] + "\t " + dialogues[i];
		}
		return a;
	}
	
	private static void initDetails() {
		String[] arr = new String[DETAILS_COUNT];
		for (int i = 0; i < DETAILS_COUNT; i++) {
			arr[i] = " Detalis "+ i + "\n You are a beautiful girl ...\n";
		}
		dialogues = arr;
	}

	public static String getTitleItem(int index) {
		if(titles == null) {
			titles = getTitleDataSet();
		}
		return titles[index];
	}
	
	public static String getDetailsItem(int titleIndex, int detailsIndex) {
		if(dialogues == null) {
			dialogues = getDetailsDataSet(titleIndex);
		}
		return dialogues[detailsIndex];
	}
}

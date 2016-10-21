package com.zxz.utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MathUtil {

	public static void main(String[] args) {
		int[] array = {10,11,223};
		System.out.println(binarySearch(10, array));
	}
	
	/**
	 * ��һ�������У����һ����������ʹ����Ȼ����
	 * 
	 * @param array
	 * @param number
	 * @return
	 */
	public static int[] insertSortArray(int array[], int number) {
		int begin = 0;
		int end = array.length;
		boolean isEnd = false;
		int index = -1;
		int[] copyArray = new int[array.length + 1];
		if (number >= array[array.length - 1]) {
			index = array.length;
		} else if (number <= array[0]) {
			index = 0;
		} else {
			while (!isEnd) {
				int middle = (begin + end) / 2;
				int temp = array[middle];
				if (temp == number) {
					index = middle;
					isEnd = true;
				} else if (number > array[middle]
						&& number <= array[middle + 1]) {
					index = middle + 1;
					isEnd = true;
				} else if (number < array[middle]
						&& number >= array[middle - 1]) {
					index = middle;
					isEnd = true;
				} else if (number > array[middle]) {
					begin = middle;
				} else if (number < array[middle]) {
					end = middle;
				}
				if (begin >= end) {
					isEnd = true;
				}
			}
		}

		if (index == array.length) {
			for (int i = 0; i < array.length; i++) {
				copyArray[i] = array[i];
			}
			copyArray[array.length] = number;
		} else if (index == 0) {
			copyArray[0] = number;
			for (int i = 0; i < array.length; i++) {
				copyArray[i + 1] = array[i];
			}
		} else {
			for (int i = 0; i <= index - 1; i++) {
				copyArray[i] = array[i];
			}

			copyArray[index] = number;
			copyArray[index + 1] = array[index];
			for (int i = index + 1; i < array.length; i++) {
				copyArray[i + 1] = array[i];
			}
		}

		return copyArray;
	}

	/**
	 * ��һ�������У����һ����������ʹ����Ȼ����
	 * 
	 * @param array
	 * @param number
	 * @return
	 */
	public static int[] removeSortArray(int array[], int number) {
		int begin = 0;
		int end = array.length;
		boolean isEnd = false;
		int index = -1;
		int[] copyArray = new int[array.length + 1];
		if (number >= array[array.length - 1]) {
			index = array.length;
		} else if (number <= array[0]) {
			index = 0;
		} else {
			while (!isEnd) {
				int middle = (begin + end) / 2;
				int temp = array[middle];
				if (temp == number) {
					index = middle;
					isEnd = true;
				} else if (number > array[middle]
						&& number <= array[middle + 1]) {
					index = middle + 1;
					isEnd = true;
				} else if (number < array[middle]
						&& number >= array[middle - 1]) {
					index = middle;
					isEnd = true;
				} else if (number > array[middle]) {
					begin = middle;
				} else if (number < array[middle]) {
					end = middle;
				}
				if (begin >= end) {
					isEnd = true;
				}
			}
		}

		if (index == array.length) {
			for (int i = 0; i < array.length; i++) {
				copyArray[i] = array[i];
			}
			copyArray[array.length] = number;
		} else if (index == 0) {
			copyArray[0] = number;
			for (int i = 0; i < array.length; i++) {
				copyArray[i + 1] = array[i];
			}
		} else {
			for (int i = 0; i <= index - 1; i++) {
				copyArray[i] = array[i];
			}

			copyArray[index] = number;
			copyArray[index + 1] = array[index];
			for (int i = index + 1; i < array.length; i++) {
				copyArray[i + 1] = array[i];
			}
		}

		return copyArray;
	}

	public static int binarySearch(int des, int[] srcArray) {
		// ��һ��λ��.
		int low = 0;
		// ���λ��.���鳤��-1,��Ϊ�±��Ǵ�0��ʼ��.
		int high = srcArray.length - 1;
		// ��low"ָ��"��high���ظ���ʱ��.
		while (low <= high) {
			// �м�λ�ü���,low+ ���λ�ü�ȥ���λ��,����һλ,�൱�ڳ�2.Ҳ������(high+low)/2
			int middle = low + ((high - low) >> 1);
			// �����м�����ֽ����ж�,�Ƿ����,��ȵĻ��ͷ��ض�Ӧ�������±�.
			if (des == srcArray[middle]) {
				return middle;
				// ���С�ڵĻ����ƶ���߲��"ָ��"
			} else if (des < srcArray[middle]) {
				high = middle - 1;
				// �ƶ���͵�"ָ��"
			} else {
				low = middle + 1;
			}
		}
		return -1;
	}

	
	public static int binarySearch(int des, List<Integer> list) {
		Integer[] srcArray = list.toArray(new Integer [list.size()]);
		// ��һ��λ��.
		int low = 0;
		// ���λ��.���鳤��-1,��Ϊ�±��Ǵ�0��ʼ��.
		int high = srcArray.length - 1;
		// ��low"ָ��"��high���ظ���ʱ��.
		while (low <= high) {
			// �м�λ�ü���,low+ ���λ�ü�ȥ���λ��,����һλ,�൱�ڳ�2.Ҳ������(high+low)/2
			int middle = low + ((high - low) >> 1);
			// �����м�����ֽ����ж�,�Ƿ����,��ȵĻ��ͷ��ض�Ӧ�������±�.
			if (des == srcArray[middle]) {
				return middle;
				// ���С�ڵĻ����ƶ���߲��"ָ��"
			} else if (des < srcArray[middle]) {
				high = middle - 1;
				// �ƶ���͵�"ָ��"
			} else {
				low = middle + 1;
			}
		}
		return -1;
	}
	
	
	
	/**����ָ����Χ����
	 * @param begin
	 * @param end
	 * @return
	 */
	public static List<Integer> creatRandom(int begin,int end) {
		List<Integer> list = new LinkedList<Integer>();
		for (int i = begin; i <=end;i++) {
			list.add(i);
		}
		Collections.shuffle(list);
		return list;
	}
	
}

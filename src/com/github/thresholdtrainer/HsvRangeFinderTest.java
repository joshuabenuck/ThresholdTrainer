package com.github.thresholdtrainer;

import junit.framework.TestCase;

public class HsvRangeFinderTest extends TestCase {
	public void testComputeRanges() throws Exception {
		HsvRangeFinder rf = new HsvRangeFinder();
		rf.record(1, 5, 0);
		rf.record(2, 10, 0);
		rf.record(3, 11, 0);
		rf.record(3, 12, 0);
		assertEquals(1, rf.computeRanges(rf.hvalues).size());
		assertEquals(1.0, rf.computeRanges(rf.hvalues).get(0).min);
		assertEquals(3.0, rf.computeRanges(rf.hvalues).get(0).max);
		
		rf.record(7, 13, 0);
		assertEquals(1, rf.computeRanges(rf.hvalues).size());
		assertEquals(7.0, rf.computeRanges(rf.hvalues).get(0).max);

		rf.record(12, 14, 0);
		assertEquals(2, rf.computeRanges(rf.hvalues).size());
		assertEquals(12.0, rf.computeRanges(rf.hvalues).get(1).min);
		assertEquals(12.0, rf.computeRanges(rf.hvalues).get(1).max);
		
		assertEquals(2, rf.computeRanges(rf.svalues).size());
		assertEquals(1, rf.computeRanges(rf.vvalues).size());
	}
}

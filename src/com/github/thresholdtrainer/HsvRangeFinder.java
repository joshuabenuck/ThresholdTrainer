package com.github.thresholdtrainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class HsvRangeFinder {
	public class Range {
		public Double min, max;
	}
	public SortedSet<Double> hvalues = new TreeSet<Double>();
	public SortedSet<Double> svalues = new TreeSet<Double>();
	public SortedSet<Double> vvalues = new TreeSet<Double>();
	public void record(double h, double s, double v) {
		if (!hvalues.contains(h)) hvalues.add(h);
		if (!svalues.contains(h)) svalues.add(s);
		if (!vvalues.contains(h)) vvalues.add(v);
	}
	public List<Range> computeRanges(SortedSet<Double> values) {
		Double MAX_JUMP = 2.0;
		List<Range> ranges = new ArrayList<Range>();
		if (values.size() == 0) return ranges;
		Range r = new Range();
		r.min = values.first();
		r.max = values.first();
		ranges.add(r);
		for (Double d : values) {
			if (d - r.max >= MAX_JUMP) {
				r = new Range();
				r.min = d;
				r.max = d;
				ranges.add(r);
			}
			r.max = d;
		}
		return ranges;
	}
	public String persistValues(List<Double> hs, List<Double> ss, List<Double> vs) {
		StringBuilder out = new StringBuilder();
		out.append("{\n");
			out.append("\t\"h\" : ");
			out.append(persistValues(hs));
			out.append(",\n");

			out.append("\t\"s\" : ");
			out.append(persistValues(ss));
			out.append(",\n");

			out.append("\t\"v\" : ");
			out.append(persistValues(vs));
			out.append("\n");
		out.append("}\n");
		return out.toString();
	}
	public String persistValues(List<Double> values) {
		StringBuilder out = new StringBuilder();
		out.append("{\n");
		for (Double v : values) {
			out.append("\t\t");
			out.append(v);
			out.append(",");
		}
		if (values.size() > 0) out.deleteCharAt(out.length() - 1);
		out.append("}\n");
		return out.toString();
	}
}

package com.demandcube.githubflow.utils;

import java.util.Comparator;

import org.eclipse.egit.github.core.Issue;

public enum DateComparator2 implements Comparator<Issue> {
	CREATIONDATECOMPARATOR {
		@Override
		public int compare(Issue issue1, Issue issue2) {
			return issue1.getCreatedAt().compareTo(issue2.getCreatedAt());
		}
	},

	CLOSUREDATECOMPARATOR {
		@Override
		public int compare(Issue issue1, Issue issue2) {
			return issue1.getClosedAt().compareTo(issue2.getClosedAt());
		}
	};
}

package com.demandcube.githubflow.utils.predicates;

import java.util.Date;

import org.kohsuke.github.GHIssue;

import com.google.common.base.Predicate;

public class DatePredicates {
	static class ClosedAfterPredicate implements Predicate<GHIssue> {

		private Date date;

		public ClosedAfterPredicate(Date date) {
			super();
			this.date = date;
		}

		@Override
		public boolean apply(GHIssue issue) {
			return issue.getClosedAt().after(date);
		}
	}

	static class ClosedBeforePredicate implements Predicate<GHIssue> {

		private Date date;

		public ClosedBeforePredicate(Date date) {
			super();
			this.date = date;
		}

		@Override
		public boolean apply(GHIssue issue) {
			return issue.getClosedAt().before(date);
		}
	}
}

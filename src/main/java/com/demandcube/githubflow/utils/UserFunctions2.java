package com.demandcube.githubflow.utils;

import org.eclipse.egit.github.core.User;

import com.google.common.base.Function;
import com.google.common.base.Strings;

public enum UserFunctions2 implements Function<User, String> {

	NameFunction {

		@Override
		public String apply(User user) {
			String name = null;
			if (!Strings.isNullOrEmpty(user.getName())) {
				name = user.getName();
			}
			return name;
		}
	},

	EmailFunction {

		@Override
		public String apply(User user) {
			String address = null;
			if (!Strings.isNullOrEmpty(user.getEmail())
					&& user.getEmail().contains("@")) {
				address = user.getEmail();
			}
			return address;
		}
	}
}

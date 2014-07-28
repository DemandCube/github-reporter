package com.demandcube.githubflow;

import static com.demandcube.githubflow.utils.DateComparator.CLOSUREDATECOMPARATOR;
import static com.demandcube.githubflow.utils.DateComparator.CREATIONDATECOMPARATOR;
import static com.demandcube.githubflow.utils.UserFunctions.NameFunction;
import static com.demandcube.githubflow.utils.Utils.getStartOfPreviousWeek;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.base.Stopwatch.createUnstarted;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import com.demandcube.githubflow.utils.Emailer;
import com.demandcube.githubflow.utils.PropertyUtils;
import com.demandcube.githubflow.utils.UserFunctions;
import com.demandcube.githubflow.utils.Utils;
import com.demandcube.githubflow.utils.predicates.ClosedBetweenPredicate;
import com.demandcube.githubflow.utils.predicates.CreatedBetweenPredicate;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

//TODO Externalize repo name, start & end date, log4j configurator
//TODO log intelligently
public class Cron {
	private static final String repositoryName = "DemandCube";
	private static final DateFormat dateFormat = new SimpleDateFormat(
			"dd_MM_yyyy");
	private static Map<String, GHRepository> repos;

	private static final Logger logger = Logger.getLogger(Cron.class);
	static Properties props;

	public static void main(String[] args) throws IOException, EmailException {
		PropertyConfigurator.configure(PropertyUtils
				.getPropertyFile("log4j.properties"));
		props = PropertyUtils.getPropertyFile("viper.properties");
		Stopwatch stopwatch = createUnstarted();

		Date startDate = getStartOfPreviousWeek(1);
		logger.debug("Start date: " + startDate);
		Date endDate = Utils.getEndOfPreviousWeek(1);
		logger.debug("End date: " + endDate);
		// TODO create temp file
		File file = new File(dateFormat.format(startDate) + ".xlsx");
		file.deleteOnExit();
		logger.debug("filename " + file);
		stopwatch.start();
		repos = GitHub
				.connectUsingPassword(System.getProperty("githubUser"),
						System.getProperty("githubPassword"))
				.getOrganization(repositoryName).getRepositories();
		logger.debug("Got repos in " + stopwatch.stop());
		logger.debug(repos.values());
		List<GHIssue> openedIssues = Lists.newArrayList();
		List<GHIssue> closedIssues = Lists.newArrayList();
		List<GHPullRequest> openedPullRequests = Lists.newArrayList();
		List<GHPullRequest> closedPullRequests = Lists.newArrayList();
		Set<GHUser> collaborators = Sets.newHashSet();
		stopwatch.reset().start();
		for (GHRepository repo : repos.values()) {
			logger.debug(repo);

			if (repo.hasIssues()) {
				openedIssues.addAll(getIssues(repo, startDate, endDate,
						GHIssueState.OPEN));
				closedIssues.addAll(getIssues(repo, startDate, endDate,
						GHIssueState.CLOSED));
				openedPullRequests.addAll(getPullRequests(repo, startDate,
						endDate, GHIssueState.OPEN));
				closedPullRequests.addAll(getPullRequests(repo, startDate,
						endDate, GHIssueState.CLOSED));
				collaborators.addAll(repo.getCollaborators());
			}
		}
		logger.debug("Calculating -->" + stopwatch.stop());
		logger.debug("Opened issues ---> " + openedIssues.size());
		logger.debug("Closed issues ---> " + closedIssues.size());
		logger.debug("Opened pull requests ---> " + openedPullRequests.size());
		logger.debug("Closed pull requests ---> " + closedPullRequests.size());
		// A few issues might have been opened and closed within the same range.
		// We thus want to add them to the list of opened lists.
		List<GHIssue> openedAndClosed = Lists.newArrayList(Iterables.filter(
				closedIssues, new CreatedBetweenPredicate<GHIssue>(startDate,
						endDate)));
		openedIssues.addAll(openedAndClosed);
		logger.debug("Opened and closed -->" + openedAndClosed.size());

		Collections.sort(openedIssues, CREATIONDATECOMPARATOR);
		Collections.sort(closedIssues, CLOSUREDATECOMPARATOR);

		// get email addresses of collaborators
		Collection<String> allUsers = Collections2.transform(collaborators,
				NameFunction);

		// remove nulls and blanks
		List<String> userNames = Lists.newArrayList(Collections2.filter(
				allUsers, notNull()));

		Collections.sort(userNames);

		logger.debug("Users " + userNames);
		WorkbookCreator creator = new WorkbookCreator(openedIssues,
				closedIssues, openedPullRequests, closedPullRequests,
				startDate, endDate, userNames);
		logger.debug("Do we get here ");
		XSSFWorkbook workbook = creator.createWorkBook();
		logger.debug("Or even here?");
		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);
		fileOut.close();
		logger.debug("workbook written to file.");
		sendMail(file, collaborators, startDate);
	}

	/**
	 * @param fileName
	 * @param collaborators
	 * @param startDate
	 * @throws EmailException
	 */
	private static void sendMail(File file, Set<GHUser> collaborators,
			Date startDate) throws EmailException {
		// get email addresses of collaborators
		Collection<String> all = Collections2.transform(collaborators,
				UserFunctions.EmailFunction);

		// remove nulls and blanks
		List<String> emailAddresses = Lists.newArrayList(Collections2.filter(
				all, notNull()));
		Collections.sort(emailAddresses);
		logger.debug("emails " + emailAddresses);

		Emailer emailer = new Emailer().setAttachment(file)
				.sendFrom(System.getProperty("gmailUsername"))
				.setPassword(System.getProperty("gmailPassword"))
				.setSubject(props.getProperty("subject"))
				.setHostName(props.getProperty("host"))
				.setBody(props.getProperty("body") + " " + startDate);
		if (Strings.isNullOrEmpty(props.getProperty("to"))) {
			logger.info("Sending mail to " + emailAddresses);
			emailer.sendTo(emailAddresses);
		} else {
			logger.info("Sending mail to " + props.getProperty("to"));
			emailer.sendTo(props.getProperty("to"));
		}
		emailer.sendMail();
	}

	private static List<GHIssue> getIssues(GHRepository repo, Date startDate,
			Date endDate, GHIssueState state) {
		List<GHIssue> issues = null;
		Predicate<GHIssue> predicate;

		if (state.equals(GHIssueState.OPEN))
			predicate = new CreatedBetweenPredicate<GHIssue>(startDate, endDate);

		else
			predicate = new ClosedBetweenPredicate<>(startDate, endDate);
		try {
			List<GHIssue> allIssues = repo.getIssues(state);
			issues = Lists.newArrayList(Iterables.filter(allIssues, predicate));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return issues;
	}

	private static List<GHPullRequest> getPullRequests(GHRepository repo,
			Date startDate, Date endDate, GHIssueState state) {

		List<GHPullRequest> issues = null;
		Predicate<GHPullRequest> predicate;

		if (state.equals(GHIssueState.OPEN))
			predicate = new CreatedBetweenPredicate<GHPullRequest>(startDate,
					endDate);
		else
			// TODO whats happening here?
			predicate = new ClosedBetweenPredicate<GHPullRequest>(startDate,
					endDate);
		try {

			List<GHPullRequest> allIssues = repo.getPullRequests(state);
			issues = Lists.newArrayList(Iterables.filter(allIssues, predicate));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return issues;
	}
}

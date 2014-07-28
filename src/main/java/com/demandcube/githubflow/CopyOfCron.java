package com.demandcube.githubflow;

import static com.demandcube.githubflow.utils.UserFunctions2.NameFunction;
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
import java.util.Properties;
import java.util.Set;

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;

import com.demandcube.githubflow.utils.DateComparator2;
import com.demandcube.githubflow.utils.Emailer;
import com.demandcube.githubflow.utils.PropertyUtils;
import com.demandcube.githubflow.utils.UserFunctions2;
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
public class CopyOfCron {
	private static final String repositoryName = "DemandCube";
	private static final DateFormat dateFormat = new SimpleDateFormat(
			"dd_MM_yyyy");
	private static List<Repository> repos;

	private static final Logger logger = Logger.getLogger(CopyOfCron.class);
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
		GitHubClient gitHubClient = new GitHubClient();
		gitHubClient.setCredentials(System.getProperty("githubUser"),
				System.getProperty("githubPassword"));

		RepositoryService repositoryService = new RepositoryService(
				gitHubClient);
		IssueService issueService = new IssueService(gitHubClient);

		CollaboratorService collaboratorService = new CollaboratorService(
				gitHubClient);
		PullRequestService pullRequestService = new PullRequestService(
				gitHubClient);

		repos = repositoryService.getOrgRepositories(repositoryName);

		logger.debug("Got repos in " + stopwatch.stop());
		logger.debug(repos);
		List<Issue> openedIssues = Lists.newArrayList();
		List<Issue> closedIssues = Lists.newArrayList();
		List<PullRequest> openedPullRequests = Lists.newArrayList();
		List<PullRequest> closedPullRequests = Lists.newArrayList();
		Set<User> collaborators = Sets.newHashSet();
		stopwatch.reset().start();
		for (Repository repo : repos) {
			logger.debug(repo);

			if (repo.isHasIssues()) {

				openedIssues.addAll(getIssues(issueService, repo, startDate,
						endDate, IssueService.STATE_OPEN));
				closedIssues.addAll(getIssues(issueService, repo, startDate,
						endDate, IssueService.STATE_CLOSED));
				openedPullRequests.addAll(getPullRequests(pullRequestService,
						repo, startDate, endDate, IssueService.STATE_OPEN));
				closedPullRequests.addAll(getPullRequests(pullRequestService,
						repo, startDate, endDate, IssueService.STATE_CLOSED));
				// collaborators.addAll(repo.getCollaborators());
				collaborators
						.addAll(collaboratorService.getCollaborators(repo));
			}
		}
		logger.debug("Calculating -->" + stopwatch.stop());
		logger.debug("Opened issues ---> " + openedIssues.size());
		logger.debug("Closed issues ---> " + closedIssues.size());
		logger.debug("Opened pull requests ---> " + openedPullRequests.size());
		logger.debug("Closed pull requests ---> " + closedPullRequests.size());
		// A few issues might have been opened and closed within the same range.
		// We thus want to add them to the list of opened lists.
		List<Issue> openedAndClosed = Lists.newArrayList(Iterables.filter(
				closedIssues, new CreatedBetweenPredicate<Issue>(startDate,
						endDate)));
		openedIssues.addAll(openedAndClosed);
		logger.debug("Opened and closed -->" + openedAndClosed.size());

		Collections.sort(openedIssues, DateComparator2.CREATIONDATECOMPARATOR);
		Collections.sort(closedIssues, DateComparator2.CLOSUREDATECOMPARATOR);

		// get email addresses of collaborators
		Collection<String> allUsers = Collections2.transform(collaborators,
				NameFunction);

		// remove nulls and blanks
		List<String> userNames = Lists.newArrayList(Collections2.filter(
				allUsers, notNull()));

		Collections.sort(userNames);

		logger.debug("Users " + userNames);
		WorkbookCreator2 creator = new WorkbookCreator2(openedIssues,
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
	private static void sendMail(File file, Set<User> collaborators,
			Date startDate) throws EmailException {
		// get email addresses of collaborators
		Collection<String> all = Collections2.transform(collaborators,
				UserFunctions2.EmailFunction);

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

	private static List<Issue> getIssues(IssueService issueService,
			Repository repo, Date startDate, Date endDate, String state) {
		List<Issue> issues = null;
		List<Issue> allIssues;
		Predicate<Issue> predicate;
		try {
			if (state.equals(IssueService.STATE_OPEN)) {
				predicate = new CreatedBetweenPredicate<Issue>(startDate,
						endDate);
			} else {
				predicate = new ClosedBetweenPredicate<Issue>(startDate,
						endDate);
			}
			allIssues = issueService.getIssues(repo,
					Collections.singletonMap(IssueService.FILTER_STATE, state));
			// repo.getIssues(state);
			issues = Lists.newArrayList(Iterables.filter(allIssues, predicate));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return issues;
	}

	private static List<PullRequest> getPullRequests(
			PullRequestService pullRequestService, Repository repo,
			Date startDate, Date endDate, String state) {
		List<PullRequest> issues = null;
		List<PullRequest> allIssues;
		Predicate<PullRequest> predicate;
		try {
			if (state.equals(IssueService.STATE_OPEN)) {
				predicate = new CreatedBetweenPredicate<PullRequest>(startDate,
						endDate);
			} else {
				predicate = new ClosedBetweenPredicate<PullRequest>(startDate,
						endDate);
			}
			allIssues = pullRequestService.getPullRequests(repo, state);
			// repo.getIssues(state);
			issues = Lists.newArrayList(Iterables.filter(allIssues, predicate));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return issues;
	}

}

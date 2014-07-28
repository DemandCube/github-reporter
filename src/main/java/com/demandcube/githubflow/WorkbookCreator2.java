package com.demandcube.githubflow;


//TODO remove tautology
public class WorkbookCreator2 {/*

	private static final Logger logger = Logger
			.getLogger(WorkbookCreator2.class);
	private List<Issue> closedIssues;
	private List<Issue> openedIssues;
	private List<PullRequest> openedPullRequests;
	private List<PullRequest> closedPullRequests;
	private Collection<String> users;
	private Date startDate;
	private Date endDate;

	private CellStyle dateStyle;
	private CellStyle boldStyle;

	public WorkbookCreator2(List<Issue> openedIssues, List<Issue> closedIssues,
			Date startDate, Date endDate) {
		this.openedIssues = openedIssues;
		this.closedIssues = closedIssues;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public WorkbookCreator2(List<Issue> openedIssues, List<Issue> closedIssues,
			List<PullRequest> openedPullRequests,
			List<PullRequest> closedPullRequests, Date startDate, Date endDate,
			Collection<String> users) {
		this.openedIssues = openedIssues;
		this.closedIssues = closedIssues;
		this.openedPullRequests = openedPullRequests;
		this.closedPullRequests = closedPullRequests;
		this.users = users;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public XSSFWorkbook createWorkBook() throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		Stopwatch stopwatch = Stopwatch.createStarted();
		CreationHelper createHelper = workbook.getCreationHelper();

		dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(createHelper.createDataFormat().getFormat(
				"m/d/yy h:mm"));

		boldStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		boldStyle.setFont(font);

		XSSFSheet sheet0 = workbook.createSheet("Summary");
		XSSFSheet sheet1 = workbook.createSheet("Open Issues");
		XSSFSheet sheet2 = workbook.createSheet("Closed Issues");
		XSSFSheet sheet3 = workbook.createSheet("Open Pull Requests");
		XSSFSheet sheet4 = workbook.createSheet("Closed Pull Requests");

		logger.debug("before open issue -->" + stopwatch);
		sheet0 = createSummarySheet(createHelper, sheet0);
		sheet1 = createOpenedIssueSheet(createHelper, sheet1);
		logger.debug("after open issue " + stopwatch);
		sheet2 = createClosedIssueSheet(createHelper, sheet2);
		logger.debug("before open pR's" + stopwatch);
		sheet3 = createOpenedPullRequestSheet(createHelper, sheet3);
		sheet4 = createClosedPullRequestSheet(createHelper, sheet4);

		logger.debug("at the end of it all" + stopwatch.stop());
		return workbook;
	}

	private XSSFSheet createSummarySheet(CreationHelper createHelper,
			XSSFSheet sheet) {
		XSSFRow row = sheet.createRow((short) 0);
		row.createCell(0).setCellValue("DemandCube Summary:");
		row.getCell(0).setCellStyle(boldStyle);

		row = sheet.createRow((short) 1);
		row.createCell(0).setCellValue("Issues Opened:");
		row.createCell(1).setCellValue(openedIssues.size());

		row = sheet.createRow((short) 2);
		row.createCell(0).setCellValue("Issues Closed:");
		row.createCell(1).setCellValue(closedIssues.size());

		row = sheet.createRow((short) 3);
		row.createCell(0).setCellValue("Pull Requests Opened:");
		row.createCell(1).setCellValue(openedPullRequests.size());

		row = sheet.createRow((short) 4);
		row.createCell(0).setCellValue("Pull requests Closed:");
		row.createCell(1).setCellValue(closedPullRequests.size());

		logger.debug("just before the exception? --------------->");
		logger.debug("Opened issues size" + openedIssues.size());
		ListMultimap<String, Issue> openedIssuesByName = Multimaps.index(
				openedIssues, new OpenedByNameFunction<Issue>());
		ListMultimap<String, Issue> closedIssuesByName = Multimaps.index(
				closedIssues, new ClosedByNameFunction());

		ListMultimap<String, PullRequest> openedPullRequestsByName = Multimaps
				.index(openedPullRequests, new OpenedByNameFunction<PullRequest>());
		ListMultimap<String, PullRequest> closedRequestsByName = Multimaps
				.index(closedPullRequests, new ClosedByNameFunction());

		ListMultimap<String, Issue> openedIssuesByRepo = Multimaps.index(
				openedIssues, new RepoFunction<Issue>());
		ListMultimap<String, Issue> closedIssuesByRepo = Multimaps.index(
				closedIssues, new RepoFunction<Issue>());
		ListMultimap<String, PullRequest> openedPullRequestsByRepo = Multimaps
				.index(openedPullRequests, new RepoFunction<PullRequest>());
		ListMultimap<String, PullRequest> closedPullRequestsByRepo = Multimaps
				.index(closedPullRequests, new RepoFunction<PullRequest>());

		Set<String> repos = Sets.newHashSet(Iterables.transform(
				Iterables.concat(openedIssues, closedIssues),
				new RepoFunction<Issue>()));
		Set<String> repos2 = Sets.newHashSet(Iterables.transform(
				Iterables.concat(openedPullRequests, closedPullRequests),
				new RepoFunction<PullRequest>()));

		row = sheet.createRow((short) 6);
		row.createCell(0).setCellValue("Team Summary:");
		row.getCell(0).setCellStyle(boldStyle);

		int i = 7;
		for (String teamMember : users) {
			row = sheet.createRow(i++);
			row.createCell(0).setCellValue(teamMember);
			row.getCell(0).setCellStyle(boldStyle);

			row = sheet.createRow(i++);
			row.createCell(0).setCellValue("Issues Opened:");
			row.createCell(1).setCellValue(
					openedIssuesByName.get(teamMember).size());

			row = sheet.createRow(i++);
			row.createCell(0).setCellValue("Issues Closed:");
			row.createCell(1).setCellValue(
					closedIssuesByName.get(teamMember).size());

			row = sheet.createRow(i++);
			row.createCell(0).setCellValue("PR's Opened:");
			row.createCell(1).setCellValue(
					openedPullRequestsByName.get(teamMember).size());

			row = sheet.createRow(i++);
			row.createCell(0).setCellValue("PR's Closed:");
			row.createCell(1).setCellValue(
					closedRequestsByName.get(teamMember).size());

		}

		row = sheet.createRow(i += 2);
		row.createCell(0).setCellValue("Repo Summary");
		row.getCell(0).setCellStyle(boldStyle);

		// get repos

		for (String repo : Sets.newTreeSet(Iterables.concat(repos, repos2))) {
			row = sheet.createRow(i++);
			row.createCell(0).setCellValue(repo);
			logger.debug("repo " + repo);
			row.getCell(0).setCellStyle(boldStyle);

			row = sheet.createRow(i++);
			row.createCell(0).setCellValue("Open Issues");
			row.createCell(1).setCellValue(openedIssuesByRepo.get(repo).size());

			row = sheet.createRow(i++);
			row.createCell(0).setCellValue("Closed Issues");
			row.createCell(1).setCellValue(closedIssuesByRepo.get(repo).size());

			row = sheet.createRow(i++);
			row.createCell(0).setCellValue("Open Pull requests");
			row.createCell(1).setCellValue(
					openedPullRequestsByRepo.get(repo).size());

			row = sheet.createRow(i++);
			row.createCell(0).setCellValue("Closed pull requests");
			row.createCell(1).setCellValue(
					closedPullRequestsByRepo.get(repo).size());

		}

		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);
		sheet.autoSizeColumn(5);
		sheet.autoSizeColumn(6);
		sheet.autoSizeColumn(7);
		return sheet;
	}

	private XSSFSheet createOpenedPullRequestSheet(CreationHelper createHelper,
			XSSFSheet sheet) throws IOException {
		XSSFRow row;
		PullRequest pullRequest;
		row = sheet.createRow((short) 0);
		row.createCell(0).setCellValue(
				createHelper.createRichTextString("Pull Request No."));
		row.createCell(1).setCellValue(
				createHelper.createRichTextString("Repository"));
		row.createCell(2).setCellValue(
				createHelper.createRichTextString("Description"));
		row.createCell(3).setCellValue(
				createHelper.createRichTextString("Date Created"));
		row.createCell(4).setCellValue(
				createHelper.createRichTextString("Created by"));
		row.createCell(5)
				.setCellValue(createHelper.createRichTextString("URL"));
		row.createCell(6).setCellValue(
				createHelper.createRichTextString("Referenced Issue"));
		row.createCell(7).setCellValue(
				createHelper.createRichTextString("Assigned To"));
		for (Cell cell : row) {
			cell.setCellStyle(boldStyle);
		}
		for (int i = 0; i < openedPullRequests.size(); i++) {
			pullRequest = openedPullRequests.get(i);
			row = sheet.createRow((short) i + 1);
			row.createCell(0).setCellValue(pullRequest.getNumber());
			row.createCell(1).setCellValue(
					createHelper.createRichTextString(pullRequest
							.getRepository().getName()));
			row.createCell(2).setCellValue(
					createHelper.createRichTextString(pullRequest.getTitle()));
			row.createCell(3).setCellValue(pullRequest.getCreatedAt());
			row.getCell(3).setCellStyle(dateStyle);
			row.createCell(4).setCellValue(
					createHelper.createRichTextString(pullRequest.getUser()
							.getName()));
			row.createCell(5).setCellValue(
					createHelper.createRichTextString(pullRequest.getUrl()
							.toString()));
			row.createCell(6).setCellValue(
					createHelper.createRichTextString(pullRequest.getIssueUrl()
							.toExternalForm()));
			if (pullRequest.getAssignee() != null) {
				row.createCell(7).setCellValue(
						createHelper.createRichTextString(pullRequest
								.getAssignee().getName()));
			}
		}

		row = sheet.createRow(openedPullRequests.size() + 3);

		row.createCell(1).setCellValue("Start Date:");
		row.getCell(1).setCellStyle(boldStyle);

		row.createCell(2).setCellValue(startDate);
		row.getCell(2).setCellStyle(dateStyle);

		row = sheet.createRow(openedPullRequests.size() + 4);

		row.createCell(1).setCellValue("End Date:");
		row.getCell(1).setCellStyle(boldStyle);

		row.createCell(2).setCellValue(endDate);
		row.getCell(2).setCellStyle(dateStyle);

		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);
		sheet.autoSizeColumn(5);
		sheet.autoSizeColumn(6);
		sheet.autoSizeColumn(7);

		return sheet;
	}

	private XSSFSheet createClosedPullRequestSheet(CreationHelper createHelper,
			XSSFSheet sheet) throws IOException {
		XSSFRow row;
		PullRequest pullRequest;
		row = sheet.createRow((short) 0);
		row.createCell(0).setCellValue(
				createHelper.createRichTextString("Pull Request No."));
		row.createCell(1).setCellValue(
				createHelper.createRichTextString("Repository"));
		row.createCell(2).setCellValue(
				createHelper.createRichTextString("Description"));
		row.createCell(3).setCellValue(
				createHelper.createRichTextString("Date Created"));
		row.createCell(4).setCellValue(
				createHelper.createRichTextString("Created by"));
		row.createCell(5)
				.setCellValue(createHelper.createRichTextString("URL"));
		row.createCell(6).setCellValue(
				createHelper.createRichTextString("Referenced Issue"));
		row.createCell(7).setCellValue(
				createHelper.createRichTextString("Assigned To"));
		row.createCell(7).setCellValue(
				createHelper.createRichTextString("Closed by"));
		for (Cell cell : row) {
			cell.setCellStyle(boldStyle);
		}
		for (int i = 0; i < closedPullRequests.size(); i++) {
			pullRequest = closedPullRequests.get(i);
			row = sheet.createRow((short) i + 1);
			row.createCell(0).setCellValue(pullRequest.getNumber());
			row.createCell(1).setCellValue(
					createHelper.createRichTextString(pullRequest
							.getRepository().getName()));
			row.createCell(2).setCellValue(
					createHelper.createRichTextString(pullRequest.getTitle()));
			row.createCell(3).setCellValue(pullRequest.getCreatedAt());
			row.getCell(3).setCellStyle(dateStyle);
			row.createCell(4).setCellValue(
					createHelper.createRichTextString(pullRequest.getUser()
							.getName()));
			row.createCell(5).setCellValue(
					createHelper.createRichTextString(pullRequest.getUrl()
							.toString()));
			if (pullRequest.getIssueUrl() != null)
				row.createCell(6).setCellValue(
						createHelper.createRichTextString(pullRequest
								.getIssueUrl()));
			if (pullRequest.getAssignee() != null) {
				row.createCell(7).setCellValue(
						createHelper.createRichTextString(pullRequest
								.getAssignee().getName()));
			}
			if (pullRequest.getClosedBy() != null)
				row.createCell(8).setCellValue(
						createHelper.createRichTextString(pullRequest
								.getClosedBy().toString()));
		}

		row = sheet.createRow(closedPullRequests.size() + 3);

		row.createCell(1).setCellValue("Start Date:");
		row.getCell(1).setCellStyle(boldStyle);

		row.createCell(2).setCellValue(startDate);
		row.getCell(2).setCellStyle(dateStyle);

		row = sheet.createRow(closedPullRequests.size() + 4);

		row.createCell(1).setCellValue("End Date:");
		row.getCell(1).setCellStyle(boldStyle);

		row.createCell(2).setCellValue(endDate);
		row.getCell(2).setCellStyle(dateStyle);

		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);
		sheet.autoSizeColumn(5);
		sheet.autoSizeColumn(6);
		sheet.autoSizeColumn(7);

		return sheet;
	}

	*//**
	 * @param createHelper
	 * @param sheet
	 * @return
	 * @throws IOException
	 *//*
	private XSSFSheet createOpenedIssueSheet(CreationHelper createHelper,
			XSSFSheet sheet) throws IOException {
		XSSFRow row;
		Issue issue;
		row = sheet.createRow((short) 0);
		row.createCell(0).setCellValue(
				createHelper.createRichTextString("Issue No."));
		row.createCell(1).setCellValue(
				createHelper.createRichTextString("Repository"));
		row.createCell(2).setCellValue(
				createHelper.createRichTextString("Description"));
		row.createCell(3).setCellValue(
				createHelper.createRichTextString("Date Created"));
		row.createCell(4).setCellValue(
				createHelper.createRichTextString("Opened by"));
		row.createCell(5).setCellValue(
				createHelper.createRichTextString("Assignee"));
		for (Cell cell : row) {
			cell.setCellStyle(boldStyle);
		}
		for (int i = 0; i < openedIssues.size(); i++) {
			issue = openedIssues.get(i);
			row = sheet.createRow((short) i + 1);
			row.createCell(0).setCellValue(issue.getNumber());
			row.createCell(1).setCellValue(
					createHelper.createRichTextString(issue.getRepository()
							.getName()));
			row.createCell(2).setCellValue(
					createHelper.createRichTextString(issue.getTitle()));
			row.createCell(3).setCellValue(issue.getCreatedAt());
			row.getCell(3).setCellStyle(dateStyle);
			row.createCell(4).setCellValue(
					createHelper
							.createRichTextString(issue.getUser().getName()));
			if (issue.getAssignee() != null) {
				row.createCell(5).setCellValue(
						createHelper.createRichTextString(issue.getAssignee()
								.getName()));
			}
		}

		row = sheet.createRow(openedIssues.size() + 3);

		row.createCell(1).setCellValue("Start Date:");
		row.getCell(1).setCellStyle(boldStyle);

		row.createCell(2).setCellValue(startDate);
		row.getCell(2).setCellStyle(dateStyle);

		row = sheet.createRow(openedIssues.size() + 4);

		row.createCell(1).setCellValue("End Date:");
		row.getCell(1).setCellStyle(boldStyle);

		row.createCell(2).setCellValue(endDate);
		row.getCell(2).setCellStyle(dateStyle);

		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);

		return sheet;
	}

	*//**
	 * @param createHelper
	 * @param sheet
	 * @return
	 * @throws IOException
	 *//*
	private XSSFSheet createClosedIssueSheet(CreationHelper createHelper,
			XSSFSheet sheet) throws IOException {
		XSSFRow row;
		Issue issue;
		row = sheet.createRow((short) 0);
		row.createCell(0).setCellValue(
				createHelper.createRichTextString("Issue No."));
		row.createCell(1).setCellValue(
				createHelper.createRichTextString("Repository"));
		row.createCell(1).setCellValue(
				createHelper.createRichTextString("Description"));
		row.createCell(2).setCellValue(
				createHelper.createRichTextString("Date Created"));
		row.createCell(3).setCellValue(
				createHelper.createRichTextString("Date Closed"));
		row.createCell(4).setCellValue(
				createHelper.createRichTextString("User"));
		row.createCell(5).setCellValue(
				createHelper.createRichTextString("Assignee"));
		for (Cell cell : row) {
			cell.setCellStyle(boldStyle);
		}
		for (int i = 0; i < closedIssues.size(); i++) {
			issue = closedIssues.get(i);
			row = sheet.createRow((short) i + 1);
			row.createCell(0).setCellValue(issue.getNumber());
			row.createCell(1).setCellValue(
					createHelper.createRichTextString(issue.getRepository()
							.getName()));
			row.createCell(2).setCellValue(
					createHelper.createRichTextString(issue.getTitle()));
			row.createCell(3).setCellValue(issue.getCreatedAt());
			row.getCell(3).setCellStyle(dateStyle);
			row.createCell(4).setCellValue(issue.getClosedAt());
			row.getCell(4).setCellStyle(dateStyle);
			row.createCell(5).setCellValue(
					createHelper
							.createRichTextString(issue.getUser().getName()));
			if (issue.getAssignee() != null) {
				row.createCell(6).setCellValue(
						createHelper.createRichTextString(issue.getAssignee()
								.getName()));
			}
		}

		row = sheet.createRow(closedIssues.size() + 3);

		row.createCell(1).setCellValue("Start Date:");
		row.getCell(1).setCellStyle(boldStyle);

		row.createCell(2).setCellValue(startDate);
		row.getCell(2).setCellStyle(dateStyle);

		row = sheet.createRow(closedIssues.size() + 4);

		row.createCell(1).setCellValue("End Date:");
		row.getCell(1).setCellStyle(boldStyle);

		row.createCell(2).setCellValue(endDate);
		row.getCell(2).setCellStyle(dateStyle);

		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);
		sheet.autoSizeColumn(5);

		return sheet;
	}

	private static final class ClosedByNameFunction implements
			Function<Issue, String> {
		public String apply(Issue issue) {

			try {
				if (issue.getClosedBy() != null)
					return issue.getClosedBy().getName();
				else {
					logger.debug("Issue is closed but has no closer. "
							+ issue.getTitle() + "status " + issue.getState());
					return issue.getUser().getName();
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			logger.debug("Issue is closed but has no closer "
					+ issue.getTitle());
			return "";
		}
	}

	private static final class RepoFunction<T> implements Function<T, String> {
		public String apply(T t) {
			String name = "";
			if (t instanceof Issue) {
				Issue issue = (Issue) t;
				name = issue.getRepository().getName();
			}
			if (t instanceof PullRequest) {
				PullRequest pullRequest = (PullRequest) t;
				name = pullRequest.getRepository().getName();
			}

			return name;

		}
	}

	private static final class OpenedByNameFunction<T> implements
			Function<T, String> {
		public String apply(T t) {
			if (t instanceof Issue) {
				Issue issue = (Issue) t;
				if (issue.getUser().getName() != null) {
					return issue.getUser().getName();
				}
			}if (t instanceof PullRequest) {
				PullRequest issue = (PullRequest) t;
				if (issue.getUser().getName() != null) {
					return issue.getUser().getName();
				}
			}
			else {
				return "unkown";
			}
			return "";
		}
	}
*/}

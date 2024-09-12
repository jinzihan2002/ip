import java.util.Objects;
import java.util.Scanner;

public class Bosco {
    private static final String DIVIDER =
            "\t________________________________________________________________________________";
    private static final String INDENT_START = "\t ";
    private static final String INDENT_EXTRA = "  ";
    private static final String MESSAGE_MARK_DONE = "Nice! I've marked this task as done:";
    private static final String MESSAGE_MARK_UNDONE = "OK, I've marked this task as not done yet:";
    private static final String MESSAGE_ADDED_TASK = "Got it. I've added this task:";
    private static final String DEADLINE_PREFIX_BY = "/by";
    private static final String EVENT_PREFIX_FROM = "/from";
    private static final String EVENT_PREFIX_TO = "/to";

    private static final int MAX_TASKS = 100;
    private static final Scanner SCANNER = new Scanner(System.in);

    private static final Task[] taskList = new Task[MAX_TASKS];
    private static int taskCount = 0;

    private static void printWelcomeMessage() {
        printMessages("Hello! I'm Bosco APD.", "What can I do for you?");
    }

    private static void printExitMessage() {
        printMessages("Bye! Hope to see you again soon!");
    }

    private static void printMessages(String... messages) {
        System.out.println(DIVIDER);
        for (String message : messages) {
            System.out.println(INDENT_START + message);
        }
        System.out.println(DIVIDER);
    }

    private static String getUserInput() {
        String userInputString = SCANNER.nextLine();
        // Ignore blank lines
        while (userInputString.trim().isEmpty()) {
            userInputString = SCANNER.nextLine();
        }
        return userInputString;
    }

    private static void executeCommand(String userInputString)
            throws IllegalCommandException, EmptyDescriptionException, MissingPrefixException {
        String[] commandTypeAndArgs = splitCommandTypeAndArgs(userInputString);
        String commandType = commandTypeAndArgs[0];
        String commandArgs = commandTypeAndArgs[1];
        switch (commandType) {
        case "list":
            executeListTasks();
            break;
        case "mark":
        case "unmark":
            executeMarkUnmarkTask(commandType, commandArgs);
            break;
        case "todo":
            executeAddTodo(commandArgs);
            break;
        case "deadline":
            executeAddDeadline(commandArgs);
            break;
        case "event":
            executeAddEvent(commandArgs);
            break;
        case "bye":
        case "exit":
            executeExitProgram();
        default:
            throw new IllegalCommandException();
        }
    }

    private static String[] splitCommandTypeAndArgs(String userInputString) {
        String[] stringParts = userInputString.split("\\s+", 2);
        return stringParts.length == 2 ? stringParts : new String[] {stringParts[0], ""};
    }

    private static String getTaskCountMessage() {
        return String.format("Now you have %1$d tasks in the list.", taskCount);
    }

    private static void executeListTasks() {
        if (taskCount == 0) {
            printMessages("No tasks in list. You're all caught up!");
            return;
        }
        System.out.println(DIVIDER);
        for (int i = 0; i < taskCount; i++) {
            System.out.println(INDENT_START + (i + 1) + "." + taskList[i]);
        }
        System.out.println(DIVIDER);
    }

    private static void executeMarkUnmarkTask(String commandType, String commandArgs) {
        int taskNumber = Integer.parseInt(commandArgs);
        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new IndexOutOfBoundsException();
        }
        Task selectedTask = taskList[taskNumber - 1];
        switch (commandType) {
        case "mark":
            selectedTask.markAsDone();
            printMessages(MESSAGE_MARK_DONE, INDENT_EXTRA + selectedTask);
            break;
        case "unmark":
            selectedTask.markAsNotDone();
            printMessages(MESSAGE_MARK_UNDONE, INDENT_EXTRA + selectedTask);
            break;
        }
    }

    private static void executeAddTodo(String commandArgs) throws EmptyDescriptionException {
        String description = commandArgs.strip();
        if (description.isEmpty()) {
            throw new EmptyDescriptionException();
        }
        addToTaskList(new Todo(description));
    }

    private static void executeAddDeadline(String commandArgs)
            throws EmptyDescriptionException, MissingPrefixException {
        int indexOfByPrefix = commandArgs.indexOf(DEADLINE_PREFIX_BY);
        if (indexOfByPrefix == -1) {
            throw new MissingPrefixException(DEADLINE_PREFIX_BY);
        }
        String description = commandArgs.substring(0, indexOfByPrefix).strip();
        if (description.isEmpty()) {
            throw new EmptyDescriptionException();
        }
        String by = removePrefix(commandArgs.substring(indexOfByPrefix), DEADLINE_PREFIX_BY).strip();
        addToTaskList(new Deadline(description, by));
    }

    private static void executeAddEvent(String commandArgs)
            throws EmptyDescriptionException, MissingPrefixException {
        int indexOfFromPrefix = commandArgs.indexOf(EVENT_PREFIX_FROM);
        if (indexOfFromPrefix == -1) {
            throw new MissingPrefixException(EVENT_PREFIX_FROM);
        }
        int indexOfToPrefix = commandArgs.indexOf(EVENT_PREFIX_TO);
        if (indexOfToPrefix == -1) {
            throw new MissingPrefixException(EVENT_PREFIX_TO);
        }
        String description = commandArgs.substring(0, indexOfFromPrefix).strip();
        if (description.isEmpty()) {
            throw new EmptyDescriptionException();
        }
        String from = removePrefix(commandArgs.substring(indexOfFromPrefix, indexOfToPrefix),
                EVENT_PREFIX_FROM).strip();
        String to = removePrefix(commandArgs.substring(indexOfToPrefix), EVENT_PREFIX_TO).strip();
        addToTaskList(new Event(description, from, to));
    }

    private static String removePrefix(String inputStr, String prefix) {
        return inputStr.replace(prefix, "");
    }

    private static void addToTaskList(Task newTask) {
        taskList[taskCount] = newTask;
        taskCount++;
        printMessages(MESSAGE_ADDED_TASK, INDENT_EXTRA + newTask, getTaskCountMessage());
    }

    private static void executeExitProgram() {
        printExitMessage();
        System.exit(0);
    }

    public static void main(String[] args) {
        printWelcomeMessage();
        while (true) {
            String userInputString = getUserInput();
            try {
                executeCommand(userInputString);
            } catch (IllegalCommandException e) {
                printMessages("Error: invalid command. Please try again!");
            } catch (NumberFormatException e) {
                printMessages("Error: invalid mark/unmark input. Please provide a number!");
            } catch (IndexOutOfBoundsException e) {
                printMessages("Error: input out of bounds. List has " + taskCount + " tasks.");
            } catch (EmptyDescriptionException e) {
                printMessages("Error: task description is empty. Please provide a description!");
            } catch (MissingPrefixException e) {
                printMessages("Error: missing " + e.missingPrefix + " prefix.");
            }
        }
    }
}

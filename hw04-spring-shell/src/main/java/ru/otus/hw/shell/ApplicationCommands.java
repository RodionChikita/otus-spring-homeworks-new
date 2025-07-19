package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import ru.otus.hw.domain.Student;
import ru.otus.hw.service.ResultService;
import ru.otus.hw.service.TestService;

@ShellComponent(value = "Application Events Commands")
@RequiredArgsConstructor
public class ApplicationCommands {

    private final TestService testService;

    private final ResultService resultService;

    private boolean isUserLoggedIn;

    private Student student;

    @ShellMethod(value = "Run test", key = {"r", "run"})
    @ShellMethodAvailability(value = "isRunTestCommandAvailable")
    public void runTest() {
        var testResult = testService.executeTestFor(this.student);
        resultService.showResult(testResult);
    }

    @ShellMethod(value = "Login command", key = {"l", "login"})
    public String login(@ShellOption String firstName, @ShellOption String lastName) {
        this.student = new Student(firstName, lastName);
        this.isUserLoggedIn = true;
        return String.format("Добро пожаловать: %s", firstName + " " + lastName);
    }

    private Availability isRunTestCommandAvailable() {
        return isUserLoggedIn
                ? Availability.available()
                : Availability.unavailable("Сначала залогиньтесь");
    }
}
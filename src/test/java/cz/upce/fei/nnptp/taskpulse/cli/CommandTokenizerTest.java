package cz.upce.fei.nnptp.taskpulse.cli;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommandTokenizerTest {

    @Test
    void tokenizePlainWords() {
        assertThat(CommandTokenizer.tokenize("project list"))
                .containsExactly("project", "list");
    }

    @Test
    void tokenizeDoubleQuotedTokens() {
        assertThat(CommandTokenizer.tokenize("project add \"My, Project\""))
                .containsExactly("project", "add", "My, Project");
    }

    @Test
    void tokenizeEscapedNewlineInsideQuotes() {
        // Regression: \n inside quoted strings previously stayed a literal
        // backslash + 'n' pair instead of becoming a newline character.
        List<String> tokens = CommandTokenizer.tokenize("task add \"line one\\nline two\"");

        assertThat(tokens).containsExactly("task", "add", "line one\nline two");
    }

    @Test
    void tokenizeEscapedTabInsideQuotes() {
        List<String> tokens = CommandTokenizer.tokenize("say \"a\\tb\"");

        assertThat(tokens).containsExactly("say", "a\tb");
    }

    @Test
    void tokenizeEscapedBackslashInsideQuotes() {
        List<String> tokens = CommandTokenizer.tokenize("note \"C:\\\\Users\\\\test\"");

        assertThat(tokens).containsExactly("note", "C:\\Users\\test");
    }

    @Test
    void tokenizeEscapedQuoteInsideQuotesStillWorks() {
        List<String> tokens = CommandTokenizer.tokenize("task add \"say \\\"hi\\\"\"");

        assertThat(tokens).containsExactly("task", "add", "say \"hi\"");
    }

    @Test
    void unknownEscapesAreKeptVerbatim() {
        List<String> tokens = CommandTokenizer.tokenize("say \"a\\xb\"");

        assertThat(tokens).containsExactly("say", "a\\xb");
    }
}

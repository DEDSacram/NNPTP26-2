package cz.upce.fei.nnptp.taskpulse.storage.csv;

import cz.upce.fei.nnptp.taskpulse.storage.StorageException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvUtilsTest {

    @Test
    void parseUnclosedQuoteAtEndOfInputThrowsStorageException() {
        // Regression: previously the parser silently finished and returned
        // rows built from the truncated quoted field instead of failing.
        StringReader reader = new StringReader("id,name\n1,\"unclosed value");

        assertThatThrownBy(() -> CsvUtils.parse(reader))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("unclosed double quote");
    }

    @Test
    void parseUnclosedQuoteInsideFieldThrowsStorageException() {
        StringReader reader = new StringReader("value with \"quote still open");

        assertThatThrownBy(() -> CsvUtils.parse(reader))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("unclosed double quote");
    }

    @Test
    void parseProperlyClosedQuotesStillSucceeds() throws IOException {
        StringReader reader = new StringReader("id,name\n1,\"Hello, \"\"world\"\"\"\n");

        List<List<String>> rows = CsvUtils.parse(reader);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(1)).containsExactly("1", "Hello, \"world\"");
    }
}

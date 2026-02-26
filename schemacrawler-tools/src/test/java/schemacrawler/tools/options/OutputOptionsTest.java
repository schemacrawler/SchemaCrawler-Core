/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.tools.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import us.fatehi.utility.ioresource.ConsoleOutputResource;

public class OutputOptionsTest {

  @Test
  public void testOutputOptionsBuilderDefault() {
    final OutputOptions options = OutputOptionsBuilder.newOutputOptions();

    assertAll(
        () -> assertThat(options.getInputCharset(), is(StandardCharsets.UTF_8)),
        () -> assertThat(options.getOutputCharset(), is(StandardCharsets.UTF_8)),
        () -> assertThat(options.getOutputFormatValue(), is("text")),
        () -> assertThat(options.hasTitle(), is(false)),
        () -> assertThat(options.getOutputResource(), is(notNullValue())));
  }

  @Test
  public void testOutputOptionsBuilder() {
    final OutputOptions options =
        OutputOptionsBuilder.builder()
            .title("Test Title")
            .withOutputFormatValue("json")
            .withInputEncoding(StandardCharsets.UTF_16)
            .withOutputEncoding(StandardCharsets.UTF_16LE)
            .toOptions();

    assertAll(
        () -> assertThat(options.getTitle(), is("Test Title")),
        () -> assertThat(options.hasTitle(), is(true)),
        () -> assertThat(options.getOutputFormatValue(), is("json")),
        () -> assertThat(options.getInputCharset(), is(StandardCharsets.UTF_16)),
        () -> assertThat(options.getOutputCharset(), is(StandardCharsets.UTF_16)));
  }

  @Test
  public void testOutputOptionsBuilderWithStrings() {
    final OutputOptions options =
        OutputOptionsBuilder.builder()
            .withInputEncoding("UTF-8")
            .withOutputEncoding("UTF-16")
            .toOptions();

    assertAll(
        () -> assertThat(options.getInputCharset(), is(StandardCharsets.UTF_8)),
        () -> assertThat(options.getOutputCharset(), is(StandardCharsets.UTF_8)));
  }

  @Test
  public void testOutputOptionsBuilderWithInvalidStrings() {
    final OutputOptions options =
        OutputOptionsBuilder.builder()
            .withInputEncoding("invalid")
            .withOutputEncoding("invalid")
            .toOptions();

    assertAll(
        () -> assertThat(options.getInputCharset(), is(StandardCharsets.UTF_8)),
        () -> assertThat(options.getOutputCharset(), is(StandardCharsets.UTF_8)));
  }

  @Test
  public void testOutputOptionsBuilderWithOutputFile() {
    final Path path = Path.of("output.json");
    final OutputOptions options = OutputOptionsBuilder.builder().withOutputFile(path).toOptions();

    assertAll(
        () -> assertThat(options.getOutputFormatValue(), is("json")),
        () -> assertThat(options.getOutputFile("json").getFileName(), is(path.getFileName())));
  }

  @Test
  public void testOutputOptionsBuilderWithOutputFormat() {
    final OutputFormat format = new OutputFormatState("xml", "XML");
    final OutputOptions options =
        OutputOptionsBuilder.builder().withOutputFormat(format).toOptions();

    assertThat(options.getOutputFormatValue(), is("xml"));
  }

  @Test
  public void testOutputOptionsBuilderFromOptions() {
    final OutputOptions original =
        OutputOptionsBuilder.builder()
            .title("Original")
            .withOutputFormatValue("csv")
            .withInputEncoding(StandardCharsets.UTF_16)
            .toOptions();

    final OutputOptions options = OutputOptionsBuilder.builder(original).toOptions();

    assertAll(
        () -> assertThat(options.getTitle(), is("Original")),
        () -> assertThat(options.getOutputFormatValue(), is("csv")),
        () -> assertThat(options.getInputCharset(), is(StandardCharsets.UTF_16)));
  }

  @Test
  public void testOutputOptionsBuilderWithOutputWriter() {
    final StringWriter stringWriter = new StringWriter();
    final OutputOptions options =
        OutputOptionsBuilder.builder().withOutputWriter(stringWriter).toOptions();

    try (PrintWriter writer = options.openNewOutputWriter()) {
      writer.print("hello");
    }
    assertThat(stringWriter.toString(), is("hello"));
  }

  @Test
  public void testOutputOptionsBuilderNulls() {
    final OutputOptions options =
        OutputOptionsBuilder.builder()
            .withInputEncoding((String) null)
            .withOutputEncoding((String) null)
            .toOptions();

    assertAll(
        () -> assertThat(options.getInputCharset(), is(StandardCharsets.UTF_8)),
        () -> assertThat(options.getOutputCharset(), is(StandardCharsets.UTF_8)));
  }

  @Test
  public void testOutputOptionsBuilderWithConsoleOutput() {
    final OutputOptions options = OutputOptionsBuilder.builder().withConsoleOutput().toOptions();
    assertThat(options.getOutputResource() instanceof ConsoleOutputResource, is(true));

    final Path outputFile = options.getOutputFile("txt");
    assertThat(outputFile.getFileName().toString(), containsString("schemacrawler-"));
    assertThat(outputFile.getFileName().toString(), containsString(".txt"));
  }

  @Test
  public void testOutputOptionsBuilderNewOutputOptions() {
    final OutputFormat format = new OutputFormatState("xml", "XML");
    final Path path = Path.of("output.xml");
    final OutputOptions options = OutputOptionsBuilder.newOutputOptions(format, path);

    assertAll(
        () -> assertThat(options.getOutputFormatValue(), is("xml")),
        () -> assertThat(options.getOutputFile("xml").getFileName(), is(path.getFileName())));
  }

  @Test
  public void testOutputOptionsBuilderInvalid() {
    assertAll(
        () ->
            assertThrows(
                NullPointerException.class,
                () -> OutputOptionsBuilder.builder().withOutputFile(null)),
        () ->
            assertThrows(
                NullPointerException.class,
                () -> OutputOptionsBuilder.builder().withOutputFormat(null)),
        () ->
            assertThrows(
                NullPointerException.class,
                () -> OutputOptionsBuilder.builder().withOutputWriter(null)));
  }
}

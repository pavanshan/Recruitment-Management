package com.springbooters.recruitment.service;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Small SMTP-over-SSL client implemented with standard Java classes.
 *
 * It supports providers such as Gmail SMTP on port 465. Credentials are read
 * from environment variables by EmailNotificationSender.
 */
public class SmtpEmailClient {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String fromAddress;

    public SmtpEmailClient(String host, int port, String username,
                           String password, String fromAddress) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromAddress = fromAddress;
    }

    public void send(String toAddress, String subject, String body) throws IOException {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            expect(reader, 220);
            command(writer, "EHLO localhost");
            expectMultiline(reader, 250);

            command(writer, "AUTH LOGIN");
            expect(reader, 334);
            command(writer, encode(username));
            expect(reader, 334);
            command(writer, encode(password));
            expect(reader, 235);

            command(writer, "MAIL FROM:<" + fromAddress + ">");
            expect(reader, 250);
            command(writer, "RCPT TO:<" + toAddress + ">");
            expect(reader, 250);
            command(writer, "DATA");
            expect(reader, 354);

            writer.write("From: " + fromAddress + "\r\n");
            writer.write("To: " + toAddress + "\r\n");
            writer.write("Subject: " + subject + "\r\n");
            writer.write("Content-Type: text/plain; charset=UTF-8\r\n");
            writer.write("\r\n");
            writer.write(body + "\r\n");
            writer.write(".\r\n");
            writer.flush();
            expect(reader, 250);

            command(writer, "QUIT");
        }
    }

    private void command(BufferedWriter writer, String command) throws IOException {
        writer.write(command + "\r\n");
        writer.flush();
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private void expect(BufferedReader reader, int expectedCode) throws IOException {
        String line = reader.readLine();
        if (line == null || !line.startsWith(String.valueOf(expectedCode))) {
            throw new IOException("SMTP expected " + expectedCode + " but received: " + line);
        }
    }

    private void expectMultiline(BufferedReader reader, int expectedCode) throws IOException {
        String expected = String.valueOf(expectedCode);
        String line;
        do {
            line = reader.readLine();
            if (line == null || !line.startsWith(expected)) {
                throw new IOException("SMTP expected " + expectedCode + " but received: " + line);
            }
        } while (line.length() > 3 && line.charAt(3) == '-');
    }
}

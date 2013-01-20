package de.meldanor.melchatclient;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Core {

    public static void main(String[] args) {
        System.out.println("Starting client...");
        String[] connInfo = parseArguments(args);
        NetworkHandler nHandler = null;
        try {
            nHandler = new NetworkHandler(connInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Client connected!");

    }

    public static String[] parseArguments(String[] args) {
        Options options = new Options();
        options.addOption("p", true, "Port");
        options.addOption("h", true, "Host");
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;
        String host = null;
        String port = null;

        try {
            cmd = parser.parse(options, args);
            host = cmd.getOptionValue("h");
            port = cmd.getOptionValue('p');
        } catch (ParseException e) {
            System.out.println("Usage: -h HOST -p PORT");
            return null;
        }
        return new String[]{host, port};
    }
}

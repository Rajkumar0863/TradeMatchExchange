package utils;

import model.Trade;
import repository.TradeRepository;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TradeExporter {

    private static final String EXPORT_DIRECTORY = "exports";

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private TradeExporter() {

    }

    /**
     * Exports all executed trades into a CSV file.
     *
     * @param repository Trade Repository
     */
    public static void exportTrades(
            TradeRepository repository) {

        if (repository == null || repository.isEmpty()) {

            System.out.println("No trades available for export.");

            return;
        }

        try {

            Path exportDirectory =
                    Paths.get(EXPORT_DIRECTORY);

            Files.createDirectories(exportDirectory);

            String fileName =
                    "trades_"
                            + LocalDateTime.now()
                            .format(FORMATTER)
                            + ".csv";

            Path filePath =
                    exportDirectory.resolve(fileName);

            try (BufferedWriter writer =
                         Files.newBufferedWriter(filePath)) {

                writer.write(
                        "TradeId,BuyOrderId,SellOrderId,Quantity,ExecutionPrice"
                );

                writer.newLine();

                for (Trade trade : repository.getTrades()) {

                    writer.write(trade.toCsvRow());

                    writer.newLine();
                }
            }

            System.out.println();
            System.out.println("======================================");
            System.out.println(" Trades Exported Successfully");
            System.out.println("======================================");
            System.out.println("Location : "
                    + filePath.toAbsolutePath());
            System.out.println("Trades   : "
                    + repository.getTradeCount());
            System.out.println("======================================");

        } catch (IOException exception) {

            System.err.println(
                    "Failed to export trades.");

            exception.printStackTrace();
        }
    }
}
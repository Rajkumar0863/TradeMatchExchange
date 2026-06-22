package utils;

import model.Trade;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TradeExporter {

    public static void exportTrades(
            List<Trade> trades) {

        try {

            File exportFolder =
                    new File("exports");

            if (!exportFolder.exists()) {
                exportFolder.mkdir();
            }

            String timestamp =
                    LocalDateTime.now()
                            .format(
                                    DateTimeFormatter.ofPattern(
                                            "yyyyMMdd_HHmmss"
                                    )
                            );

            String fileName =
                    "exports/trades_"
                            + timestamp
                            + ".csv";

            FileWriter writer =
                    new FileWriter(fileName);

            writer.write(
                    "TradeId,BuyOrderId,SellOrderId,Quantity,Price\n"
            );

            for (Trade trade : trades) {

                writer.write(
                        trade.toCsvRow()
                                + "\n"
                );
            }

            writer.close();

            System.out.println(
                    "\nTrades exported successfully:"
            );

            System.out.println(fileName);

        } catch (IOException e) {

            System.out.println(
                    "Error exporting trades."
            );
        }
    }
}
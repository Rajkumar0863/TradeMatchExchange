import model.Order;
import model.OrderExecutionType;
import model.OrderType;

public class OrderTest {

    public static void main(String[] args) {

        Order order = new Order(
                "ORD001",
                "AAPL",
                100,
                250.0,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        if (order.getOrderId().equals("ORD001")) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }
}
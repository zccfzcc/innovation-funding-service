package com.worth.ifs.application.finance.view.item;

import com.worth.ifs.application.finance.view.jes.AcademicFinanceHandler;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertTrue;

public class CostHandlerTest {
    @Test
    public void getBigDecimalValue() throws Exception {
        AcademicFinanceHandler costHandler = new AcademicFinanceHandler();
        BigDecimal expecting = new BigDecimal(1000500.95).setScale(2, BigDecimal.ROUND_HALF_UP);
        assertTrue(expecting.compareTo(costHandler.getBigDecimalValue("1,000,500.95", new Double(0.0))) == 0);
        assertTrue(expecting.compareTo(costHandler.getBigDecimalValue("1000500.95", new Double(0.0))) == 0);

        expecting = new BigDecimal(1000500).setScale(2, BigDecimal.ROUND_HALF_UP);
        assertTrue(expecting.compareTo(costHandler.getBigDecimalValue("1000500", new Double(0.0))) == 0);
    }

    @Test
    public void getIntegerValue() throws Exception {
        AcademicFinanceHandler costHandler = new AcademicFinanceHandler();
        Integer expecting = new Integer("500000");
        assertTrue(expecting.compareTo(costHandler.getIntegerValue("500,000", new Integer("0"))) == 0);
        assertTrue(expecting.compareTo(costHandler.getIntegerValue("500,000.000", new Integer("0"))) == 0);
        assertTrue(expecting.compareTo(costHandler.getIntegerValue("500,000.500", new Integer("0"))) == 0);

    }

}
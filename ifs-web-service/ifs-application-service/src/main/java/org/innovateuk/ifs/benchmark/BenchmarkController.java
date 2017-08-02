package org.innovateuk.ifs.benchmark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.BaseRestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * A controller that can be used for load testing purposes
 */
@Controller
@RequestMapping("/benchmark")
public class BenchmarkController extends BaseRestService {

    private static final Log LOG = LogFactory.getLog(BenchmarkController.class);

    @GetMapping("/isolated")
    public @ResponseBody String process(@RequestParam("process_time_millis") long processTimeMillis) {
        return processForMillis(processTimeMillis, "isolated web layer benchmarking test");
    }

    @GetMapping("/to-data-layer")
    public @ResponseBody String toDataLayer(@RequestParam("process_time_millis") long processTimeMillis,
                                            @RequestParam("data_layer_process_time_millis") long dataLayerProcessTimeMillis) {

        long start = System.currentTimeMillis();

        RestResult<String> dataLayerResponse = getWithRestResultAnonymous("/benchmark/isolated?process_time_millis=" + dataLayerProcessTimeMillis, String.class);

        if (dataLayerResponse.isFailure()) {
            return "Error response from data layer whilst processing \"to-data-layer\" benchmarking test - " + dataLayerResponse.getFailure().getErrors();
        }

        String thisLayerProcessingMessage = processForMillis(processTimeMillis, "web layer portion");

        String totalTimeMessage = "Overall took " + (System.currentTimeMillis() - start) + " milliseconds to process " +
                "the web layer plus data layer benchmarking test";

        return dataLayerResponse.getSuccessObject() + "\n" + thisLayerProcessingMessage + "\n" + totalTimeMessage;
    }

    private String processForMillis(long processTimeMillis, String processTimeMessage) {
        long start = System.currentTimeMillis();

        double i = 0;
        do {
            Math.tanh(i++);
        } while ((System.currentTimeMillis() - start) < processTimeMillis);

        String response = "Took " + (System.currentTimeMillis() - start) + " milliseconds to process " + processTimeMessage;
        LOG.debug(response);
        return response;
    }
}
